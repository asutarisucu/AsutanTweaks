package org.asutarisucu.GUI.glass;

import org.asutarisucu.AsutanTweaks;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

/**
 * The config screen's background picture: the file picker, and reading and
 * fitting the user's image.
 */
public final class BackgroundImage {

    private BackgroundImage() {}

    private static volatile boolean dialogOpen;

    /**
     * Opens the system file picker on its own thread, so the game keeps drawing
     * while it is up. {@code onChosen} gets the path, or null when cancelled, and
     * runs on the thread that {@code executor} hands it to.
     */
    public static void choose(String title, Consumer<Runnable> executor, Consumer<String> onChosen) {
        if (dialogOpen) return;
        dialogOpen = true;
        Thread t = new Thread(() -> {
            String path = null;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(5);
                filters.put(stack.UTF8("*.png"));
                filters.put(stack.UTF8("*.jpg"));
                filters.put(stack.UTF8("*.jpeg"));
                filters.put(stack.UTF8("*.bmp"));
                filters.put(stack.UTF8("*.gif"));
                filters.flip();
                path = TinyFileDialogs.tinyfd_openFileDialog(title, null, filters, "Images", false);
            } catch (Throwable e) {
                AsutanTweaks.LOGGER.warn("[BackgroundImage] File dialog failed", e);
            } finally {
                dialogOpen = false;
            }
            String chosen = path;
            executor.accept(() -> onChosen.accept(chosen));
        }, "AsutanTweaks background picker");
        t.setDaemon(true);
        t.start();
    }

    /** @return the image, scaled down to at most 2560 px wide, or null when it cannot be read */
    public static BufferedImage read(String path) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(new File(path));
            if (img == null) return null;
            if (img.getWidth() > 2560) {
                int h = Math.max(1, img.getHeight() * 2560 / img.getWidth());
                img = scale(img, 0, 0, img.getWidth(), img.getHeight(), 2560, h);
            }
            return img;
        } catch (Exception e) {
            AsutanTweaks.LOGGER.warn("[BackgroundImage] Failed to read {}", path, e);
            return null;
        }
    }

    /** Crops the middle of {@code src} to the target shape and scales it to fill it. */
    public static BufferedImage cover(BufferedImage src, int w, int h) {
        double target = (double) w / h;
        double have = (double) src.getWidth() / src.getHeight();
        int cw = src.getWidth(), ch = src.getHeight();
        if (have > target) cw = (int) Math.round(ch * target);
        else ch = (int) Math.round(cw / target);
        int cx = (src.getWidth() - cw) / 2, cy = (src.getHeight() - ch) / 2;
        return scale(src, cx, cy, cw, ch, w, h);
    }

    public static BufferedImage flipped(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, h, w, 0, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static BufferedImage scale(BufferedImage src, int sx, int sy, int sw, int sh, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, sx, sy, sx + sw, sy + sh, null);
        g.dispose();
        return out;
    }
}
