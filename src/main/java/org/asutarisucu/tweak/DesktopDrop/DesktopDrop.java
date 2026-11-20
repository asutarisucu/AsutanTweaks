package org.asutarisucu.tweak.DesktopDrop;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.asutarisucu.Configs.Feature;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

/**
 * Desktop Drop: while an item is held on the cursor in an inventory, it keeps
 * following the cursor outside the game window, and the first click out there
 * drops it onto the desktop. Nothing changes inside the game; the item stays
 * on the cursor at the edge of the game window.
 *
 * The game side lives here: it reports that an item is held, where the game
 * window is, and a picture of the item. {@link DesktopOverlay} draws it.
 */
public final class DesktopDrop {

    private DesktopDrop() {}

    private static final long CAPTURE_INTERVAL = 300_000_000L;

    private static long lastCapture;
    private static boolean outside;

    /** Floating item position of the frame being built, for matching the GUI item atlas. */
    private static int floatX = Integer.MIN_VALUE, floatY = Integer.MIN_VALUE;
    /** An atlas region found in the last frame, read back at the start of the next one. */
    private static int pendingTex, pendingX, pendingY, pendingSize;

    /** Reused every frame; only the render thread touches them. */
    private static final int[] wx = new int[1], wy = new int[1], fw = new int[1], fh = new int[1];
    private static final WinDef.POINT cursor = new WinDef.POINT();

    public static boolean active() {
        return Feature.DESKTOP_DROP.isEnabled() && Platform.isWindows();
    }

    /**
     * Called each frame the screen draws the item held on the cursor, at the
     * position it draws it.
     */
    public static void onFloatingItem(long glfwWindow, int x, int y) {
        floatX = Integer.MIN_VALUE;
        if (!active()) return;
        DesktopOverlay.ensureStarted();

        GLFW.glfwGetWindowPos(glfwWindow, wx, wy);
        GLFW.glfwGetFramebufferSize(glfwWindow, fw, fh);
        DesktopOverlay.gameHwnd = GLFWNativeWin32.glfwGetWin32Window(glfwWindow);
        DesktopOverlay.lastHeldNanos = System.nanoTime();

        User32.INSTANCE.GetCursorPos(cursor);
        outside = cursor.x < wx[0] || cursor.x >= wx[0] + fw[0]
                || cursor.y < wy[0] || cursor.y >= wy[0] + fh[0];
        if (!outside) {
            // A picture of the last item must not show up for the next one.
            DesktopOverlay.sprite = null;
            pendingTex = 0;
            return;
        }

        if (pendingTex != 0) {
            captureTexture(pendingTex, pendingX, pendingY, pendingSize, true);
            pendingTex = 0;
        }
        floatX = x;
        floatY = y;
    }

    /** Whether the item should be drawn again for a fresh picture this frame. */
    public static boolean wantsCapture() {
        if (!outside || floatX == Integer.MIN_VALUE) return false;
        long now = System.nanoTime();
        if (DesktopOverlay.sprite != null && now - lastCapture < CAPTURE_INTERVAL) return false;
        lastCapture = now;
        return true;
    }

    /**
     * MC 1.21.11 and later draw GUI items into an atlas texture and then copy
     * them to the screen. Called for each item copied from the atlas; picks the
     * one at the floating item's position.
     *
     * @param u0 left edge in UV
     * @param v0 top edge in UV (the larger v)
     */
    public static void onAtlasItem(int x, int y, int glTexture, int textureSize, float u0, float v0, int slotSize) {
        // floatX is left over once the screen stops drawing a held item.
        if (System.nanoTime() - DesktopOverlay.lastHeldNanos >= DesktopOverlay.HELD_TIMEOUT) return;
        if (x != floatX || y != floatY || glTexture == 0 || !wantsCapture()) return;
        // The atlas is filled later in this frame, so it is read at the next one.
        pendingTex = glTexture;
        pendingX = Math.round(u0 * textureSize);
        pendingY = Math.round(v0 * textureSize) - slotSize;
        pendingSize = slotSize;
    }

    /**
     * Reads a {@code size}-square region of a texture as the picture of the item.
     * x and y are in GL texture coordinates, with the origin at the bottom left.
     */
    public static void captureTexture(int glTexture, int x, int y, int size, boolean premultiplied) {
        if (size <= 0 || size > 1024) return;
        ByteBuffer buf = BufferUtils.createByteBuffer(size * size * 4);

        int prevRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int prevPack = GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING);
        // The game leaves GL_PACK_ROW_LENGTH at the width of the last texture it
        // copied to a buffer (GlCommandEncoder.copyTextureToBuffer on 26.2). With
        // that left in place glReadPixels writes past the end of buf.
        int prevAlign = GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT);
        int prevRowLength = GL11.glGetInteger(GL11.GL_PACK_ROW_LENGTH);
        int prevSkipRows = GL11.glGetInteger(GL11.GL_PACK_SKIP_ROWS);
        int prevSkipPixels = GL11.glGetInteger(GL11.GL_PACK_SKIP_PIXELS);
        int fbo = GL30.glGenFramebuffers();
        try {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fbo);
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                    GL11.GL_TEXTURE_2D, glTexture, 0);
            // A bound pack buffer would turn the destination into an offset into it.
            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);
            GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, 0);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, 0);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, 0);
            GL11.glReadPixels(x, y, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        } finally {
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, prevAlign);
            GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, prevRowLength);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, prevSkipRows);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, prevSkipPixels);
            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, prevPack);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevRead);
            GL30.glDeleteFramebuffers(fbo);
        }

        int[] px = new int[size * size];
        boolean any = false;
        for (int row = 0; row < size; row++) {
            // GL rows run bottom to top; the window wants the top row first.
            int src = (size - 1 - row) * size * 4;
            for (int col = 0; col < size; col++) {
                int i = src + col * 4;
                int r = buf.get(i) & 0xFF, g = buf.get(i + 1) & 0xFF, b = buf.get(i + 2) & 0xFF;
                int a = buf.get(i + 3) & 0xFF;
                if (!premultiplied) {
                    r = r * a / 255;
                    g = g * a / 255;
                    b = b * a / 255;
                }
                if (a != 0) any = true;
                px[row * size + col] = a << 24 | r << 16 | g << 8 | b;
            }
        }
        if (any) DesktopOverlay.sprite = new DesktopOverlay.Sprite(size, px);
    }
}
