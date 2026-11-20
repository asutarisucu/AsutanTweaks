package org.asutarisucu.tweak.DesktopDrop;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.PointerByReference;

import java.util.ArrayList;
import java.util.List;

/**
 * The blocks on the desktop. Each one is a topmost layered window of its own.
 *
 * The item held in the game follows the cursor outside the game window, and a
 * click drops it there with the speed the cursor had, so a quick flick throws
 * it. Dropped blocks stay on the desktop until the game exits; one can be
 * picked up by pressing on it, dragged, and thrown by letting go.
 *
 * Runs on its own thread with its own message loop, so the windows keep
 * answering Windows while the game thread is busy. The game thread only writes
 * the volatile fields below.
 */
final class DesktopOverlay implements Runnable {

    /** Last time the game drew the item held on the cursor. */
    static volatile long lastHeldNanos;
    /** The game window's client area in screen pixels. */
    static volatile int winX, winY, winW, winH;
    /** The game window's HWND. */
    static volatile long gameHwnd;
    /** Picture of the item held in the game; null until one is taken. */
    static volatile Sprite sprite;

    /** Premultiplied BGRA pixels, top row first. */
    record Sprite(int size, int[] pixels) {
        boolean opaqueAt(int x, int y) {
            return x >= 0 && y >= 0 && x < size && y < size && (pixels[y * size + x] >>> 24) > 16;
        }
    }

    private static final long HELD_TIMEOUT = 250_000_000L;
    private static final float GRAVITY = 3000f;
    private static final float MAX_THROW = 6000f;
    /** How far back the cursor's path is looked at for the throw speed. */
    private static final long THROW_WINDOW = 80_000_000L;

    private static final int WS_EX_LAYERED = 0x00080000;
    private static final int WS_EX_TOPMOST = 0x00000008;
    private static final int WS_EX_TOOLWINDOW = 0x00000080;
    private static final int WS_EX_NOACTIVATE = 0x08000000;
    private static final int WS_POPUP = 0x80000000;
    /** Lets the STATIC window take the clicks on its visible pixels instead of passing them on. */
    private static final int SS_NOTIFY = 0x00000100;
    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOACTIVATE = 0x0010;
    private static final int SWP_SHOWWINDOW = 0x0040;
    private static final int SW_HIDE = 0;
    private static final int PM_REMOVE = 0x0001;
    private static final int ULW_ALPHA = 0x00000002;
    private static final int MONITOR_DEFAULTTONEAREST = 2;
    // createConstant(int) zero-extends -1 to 0xFFFFFFFF, which SetWindowPos rejects as an invalid handle.
    private static final WinDef.HWND HWND_TOPMOST = new WinDef.HWND(Pointer.createConstant(-1L));

    private static Thread thread;

    static synchronized void ensureStarted() {
        if (thread != null) return;
        thread = new Thread(new DesktopOverlay(), "AsutanTweaks DesktopDrop");
        thread.setDaemon(true);
        thread.start();
    }

    private static final class Block {
        Sprite sprite;
        Sprite painted;
        WinDef.HWND hwnd;
        float x, y, vx, vy;
        boolean resting;
    }

    /** Blocks lying on or flying over the desktop, bottom of the stack first. */
    private final List<Block> blocks = new ArrayList<>();
    private Block carried;
    /** Whether the carried block is the item still held in the game. */
    private boolean carriedFromGame;
    /** Cleared when the game's item is dropped, set again once the cursor is back in the game window. */
    private boolean gameItemArmed = true;
    private boolean prevDown = true;

    private final long[] histT = new long[16];
    private final int[] histX = new int[16], histY = new int[16];
    private int histN;

    @Override
    public void run() {
        User32 u = User32.INSTANCE;
        WinUser.MSG msg = new WinUser.MSG();
        WinDef.POINT cursor = new WinDef.POINT();
        long last = System.nanoTime();
        while (true) {
            while (u.PeekMessage(msg, null, 0, 0, PM_REMOVE)) {
                u.TranslateMessage(msg);
                u.DispatchMessage(msg);
            }
            long now = System.nanoTime();
            float dt = Math.min((now - last) / 1e9f, 0.05f);
            last = now;

            boolean down = (u.GetAsyncKeyState(0x01) & 0x8000) != 0
                    || (u.GetAsyncKeyState(0x02) & 0x8000) != 0
                    || (u.GetAsyncKeyState(0x04) & 0x8000) != 0;
            boolean pressed = down && !prevDown;
            boolean released = !down && prevDown;
            prevDown = down;

            // Only while the game is the active window, so the item does not follow
            // the cursor while the game sits behind other windows.
            WinDef.HWND fg = u.GetForegroundWindow();
            boolean foreground = fg != null && Pointer.nativeValue(fg.getPointer()) == gameHwnd;
            Sprite held = sprite;
            boolean holding = held != null && now - lastHeldNanos < HELD_TIMEOUT && foreground;
            u.GetCursorPos(cursor);
            boolean inside = cursor.x >= winX && cursor.x < winX + winW
                    && cursor.y >= winY && cursor.y < winY + winH;

            if (inside || !holding) gameItemArmed = true;

            // The game's item goes back into the game when the cursor returns.
            // The click that drops it also makes another window active; the drop
            // is handled first.
            if (carried != null && carriedFromGame && (inside || !holding) && !pressed) {
                destroy(carried);
                carried = null;
            }
            if (carried == null && holding && !inside && gameItemArmed) {
                carried = new Block();
                carriedFromGame = true;
                histN = 0;
            }
            if (carried != null && carriedFromGame && held != null) carried.sprite = held;

            // The game's item drops on the first click; a block picked up from
            // the desktop is dragged and thrown when the button is let go.
            if (pressed) {
                if (carried == null) {
                    pickUp(cursor);
                } else if (carriedFromGame && !inside) {
                    throwCarried(now);
                }
            } else if (released && carried != null && !carriedFromGame) {
                throwCarried(now);
            }

            if (carried != null) {
                record(now, cursor);
                carried.x = cursor.x - carried.sprite.size() / 2f;
                carried.y = cursor.y - carried.sprite.size() / 2f;
                place(carried);
            }
            boolean moving = carried != null;
            for (Block b : blocks) {
                if (b.resting) continue;
                step(b, dt);
                place(b);
                moving = true;
            }

            try {
                Thread.sleep(moving ? 8 : 30);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void throwCarried(long now) {
        Block b = carried;
        carried = null;
        if (carriedFromGame) gameItemArmed = false;
        float vx = 0, vy = 0;
        for (int i = 0; i < histN; i++) {
            if (now - histT[i] <= THROW_WINDOW && histT[i] < histT[histN - 1]) {
                float span = (histT[histN - 1] - histT[i]) / 1e9f;
                vx = (histX[histN - 1] - histX[i]) / span;
                vy = (histY[histN - 1] - histY[i]) / span;
                break;
            }
        }
        b.vx = Math.max(-MAX_THROW, Math.min(MAX_THROW, vx));
        b.vy = Math.max(-MAX_THROW, Math.min(MAX_THROW, vy));
        b.resting = false;
        blocks.add(b);
    }

    private void pickUp(WinDef.POINT cursor) {
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block b = blocks.get(i);
            int lx = cursor.x - Math.round(b.x), ly = cursor.y - Math.round(b.y);
            if (!b.sprite.opaqueAt(lx, ly)) continue;
            blocks.remove(i);
            carried = b;
            carriedFromGame = false;
            histN = 0;
            return;
        }
    }

    private void record(long now, WinDef.POINT p) {
        if (histN == histT.length) {
            System.arraycopy(histT, 1, histT, 0, histN - 1);
            System.arraycopy(histX, 1, histX, 0, histN - 1);
            System.arraycopy(histY, 1, histY, 0, histN - 1);
            histN--;
        }
        histT[histN] = now;
        histX[histN] = p.x;
        histY[histN] = p.y;
        histN++;
    }

    /** Gravity, then the edges of the monitor's work area as walls and floor. */
    private static void step(Block b, float dt) {
        int size = b.sprite.size();
        b.vy += GRAVITY * dt;
        b.x += b.vx * dt;
        b.y += b.vy * dt;

        WinDef.RECT wa = workArea(Math.round(b.x + size / 2f), Math.round(b.y + size / 2f));
        if (b.x < wa.left) {
            b.x = wa.left;
            b.vx = -b.vx * 0.5f;
        } else if (b.x + size > wa.right) {
            b.x = wa.right - size;
            b.vx = -b.vx * 0.5f;
        }
        if (b.y + size >= wa.bottom) {
            b.y = wa.bottom - size;
            b.vy = b.vy > 400 ? -b.vy * 0.3f : 0;
            b.vx *= Math.max(0, 1 - 8 * dt);
            if (b.vy == 0 && Math.abs(b.vx) < 10) {
                b.vx = 0;
                b.resting = true;
            }
        }
    }

    private static WinDef.RECT workArea(int x, int y) {
        WinUser.HMONITOR mon = User32.INSTANCE.MonitorFromPoint(new WinDef.POINT.ByValue(x, y), MONITOR_DEFAULTTONEAREST);
        WinUser.MONITORINFO info = new WinUser.MONITORINFO();
        User32.INSTANCE.GetMonitorInfo(mon, info);
        return info.rcWork;
    }

    private static void place(Block b) {
        int size = b.sprite.size();
        if (b.hwnd == null) {
            b.hwnd = User32.INSTANCE.CreateWindowEx(
                    WS_EX_LAYERED | WS_EX_TOPMOST | WS_EX_TOOLWINDOW | WS_EX_NOACTIVATE,
                    "STATIC", "", WS_POPUP | SS_NOTIFY, 0, 0, size, size, null, null, null, null);
            if (b.hwnd == null) return;
        }
        int px = Math.round(b.x), py = Math.round(b.y);
        if (b.sprite != b.painted) {
            paint(b.hwnd, b.sprite, px, py);
            b.painted = b.sprite;
        }
        User32.INSTANCE.SetWindowPos(b.hwnd, HWND_TOPMOST, px, py, 0, 0,
                SWP_NOSIZE | SWP_NOACTIVATE | SWP_SHOWWINDOW);
    }

    private static void destroy(Block b) {
        if (b.hwnd != null) {
            User32.INSTANCE.ShowWindow(b.hwnd, SW_HIDE);
            User32.INSTANCE.DestroyWindow(b.hwnd);
            b.hwnd = null;
        }
    }

    private static void paint(WinDef.HWND hwnd, Sprite s, int px, int py) {
        int size = s.size();
        WinDef.HDC screen = User32.INSTANCE.GetDC(null);
        WinDef.HDC mem = GDI32.INSTANCE.CreateCompatibleDC(screen);
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = size;
        bmi.bmiHeader.biHeight = -size;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
        PointerByReference bits = new PointerByReference();
        WinDef.HBITMAP bmp = GDI32.INSTANCE.CreateDIBSection(mem, bmi, WinGDI.DIB_RGB_COLORS, bits, null, 0);
        if (bmp != null) {
            bits.getValue().write(0, s.pixels(), 0, s.pixels().length);
            WinNT.HANDLE old = GDI32.INSTANCE.SelectObject(mem, bmp);
            WinUser.BLENDFUNCTION blend = new WinUser.BLENDFUNCTION();
            blend.BlendOp = 0;                // AC_SRC_OVER
            blend.SourceConstantAlpha = (byte) 255;
            blend.AlphaFormat = 1;            // AC_SRC_ALPHA
            User32.INSTANCE.UpdateLayeredWindow(hwnd, screen, new WinDef.POINT(px, py),
                    new WinUser.SIZE(size, size), mem, new WinDef.POINT(0, 0), 0, blend, ULW_ALPHA);
            GDI32.INSTANCE.SelectObject(mem, old);
            GDI32.INSTANCE.DeleteObject(bmp);
        }
        GDI32.INSTANCE.DeleteDC(mem);
        User32.INSTANCE.ReleaseDC(null, screen);
    }
}
