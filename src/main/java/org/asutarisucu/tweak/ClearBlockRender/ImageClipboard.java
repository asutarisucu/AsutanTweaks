package org.asutarisucu.tweak.ClearBlockRender;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.win32.StdCallLibrary;
import org.lwjgl.glfw.GLFWNativeWin32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Puts a captured frame on the system clipboard with its transparency.
 *
 * The game runs with java.awt.headless set, so AWT's clipboard cannot be used;
 * the Win32 clipboard is called through JNA, which the game ships. The image is
 * offered as "PNG", the format image editors, browsers and Discord read with
 * alpha, and as CF_DIBV5, from which Windows derives the plain bitmap formats.
 *
 * A recorded video goes up as a file instead, the way Explorer copies one, so it
 * pastes into Explorer or Discord as that file.
 */
public final class ImageClipboard {

    private ImageClipboard() {}

    private static final int CF_HDROP = 15;
    private static final int CF_DIBV5 = 17;
    private static final int DROPEFFECT_COPY = 1;
    private static final int GMEM_MOVEABLE = 0x0002;

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        boolean OpenClipboard(Pointer owner);

        boolean CloseClipboard();

        boolean EmptyClipboard();

        Pointer SetClipboardData(int format, Pointer data);

        int RegisterClipboardFormatW(WString name);
    }

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        Pointer GlobalAlloc(int flags, BaseTSD.SIZE_T bytes);

        Pointer GlobalLock(Pointer mem);

        boolean GlobalUnlock(Pointer mem);

        Pointer GlobalFree(Pointer mem);
    }

    public static boolean supported() {
        return Platform.isWindows();
    }

    /**
     * @param glfwWindow the game window, which becomes the clipboard owner
     * @throws IOException when not on Windows or the clipboard cannot be written
     */
    public static void copy(BufferedImage image, long glfwWindow) throws IOException {
        if (!Platform.isWindows()) throw new IOException("Copying images is only supported on Windows");
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        byte[] dib = dibV5(image);

        Pointer owner = new Pointer(GLFWNativeWin32.glfwGetWin32Window(glfwWindow));
        if (!User32.INSTANCE.OpenClipboard(owner)) {
            throw new IOException("OpenClipboard failed, error " + Native.getLastError());
        }
        try {
            if (!User32.INSTANCE.EmptyClipboard()) {
                throw new IOException("EmptyClipboard failed, error " + Native.getLastError());
            }
            put(User32.INSTANCE.RegisterClipboardFormatW(new WString("PNG")), png.toByteArray());
            put(CF_DIBV5, dib);
        } finally {
            User32.INSTANCE.CloseClipboard();
        }
    }

    /**
     * Puts {@code file} on the clipboard as CF_HDROP, with "Preferred DropEffect"
     * set to copy so Explorer copies it rather than moving it.
     *
     * @param glfwWindow the game window, which becomes the clipboard owner
     * @throws IOException when not on Windows or the clipboard cannot be written
     */
    public static void copyFile(Path file, long glfwWindow) throws IOException {
        if (!Platform.isWindows()) throw new IOException("Copying files is only supported on Windows");
        // DROPFILES: offset of the list, drop point, non-client flag, wide flag;
        // then the paths in UTF-16, each ending in a NUL, and one more NUL.
        byte[] path = (file.toAbsolutePath() + "\0\0").getBytes(StandardCharsets.UTF_16LE);
        ByteBuffer drop = ByteBuffer.allocate(20 + path.length).order(ByteOrder.LITTLE_ENDIAN);
        drop.putInt(20).putInt(0).putInt(0).putInt(0).putInt(1).put(path);
        byte[] effect = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(DROPEFFECT_COPY).array();

        Pointer owner = new Pointer(GLFWNativeWin32.glfwGetWin32Window(glfwWindow));
        if (!User32.INSTANCE.OpenClipboard(owner)) {
            throw new IOException("OpenClipboard failed, error " + Native.getLastError());
        }
        try {
            if (!User32.INSTANCE.EmptyClipboard()) {
                throw new IOException("EmptyClipboard failed, error " + Native.getLastError());
            }
            put(CF_HDROP, drop.array());
            put(User32.INSTANCE.RegisterClipboardFormatW(new WString("Preferred DropEffect")), effect);
        } finally {
            User32.INSTANCE.CloseClipboard();
        }
    }

    private static void put(int format, byte[] bytes) throws IOException {
        Pointer mem = Kernel32.INSTANCE.GlobalAlloc(GMEM_MOVEABLE, new BaseTSD.SIZE_T(bytes.length));
        if (mem == null) throw new IOException("GlobalAlloc failed, error " + Native.getLastError());
        Pointer p = Kernel32.INSTANCE.GlobalLock(mem);
        if (p == null) {
            Kernel32.INSTANCE.GlobalFree(mem);
            throw new IOException("GlobalLock failed, error " + Native.getLastError());
        }
        p.write(0, bytes, 0, bytes.length);
        Kernel32.INSTANCE.GlobalUnlock(mem);
        // Once SetClipboardData succeeds the memory belongs to the clipboard.
        if (User32.INSTANCE.SetClipboardData(format, mem) == null) {
            int error = Native.getLastError();
            Kernel32.INSTANCE.GlobalFree(mem);
            throw new IOException("SetClipboardData failed, error " + error);
        }
    }

    /** A BITMAPV5HEADER with an alpha mask, then 32-bit BGRA rows, bottom row first. */
    private static byte[] dibV5(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        ByteBuffer b = ByteBuffer.allocate(124 + w * h * 4).order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(124).putInt(w).putInt(h)
                .putShort((short) 1).putShort((short) 32)
                .putInt(3)                  // BI_BITFIELDS
                .putInt(w * h * 4)
                .putInt(0).putInt(0).putInt(0).putInt(0)
                .putInt(0x00FF0000).putInt(0x0000FF00).putInt(0x000000FF).putInt(0xFF000000)
                .putInt(0x73524742);        // LCS_sRGB
        b.position(b.position() + 36 + 12); // endpoints and gamma, unused with sRGB
        b.putInt(4)                         // LCS_GM_IMAGES
                .putInt(0).putInt(0).putInt(0);
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) b.putInt(image.getRGB(x, y));
        }
        return b.array();
    }
}
