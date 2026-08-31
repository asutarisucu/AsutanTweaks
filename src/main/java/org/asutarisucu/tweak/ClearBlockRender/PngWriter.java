package org.asutarisucu.tweak.ClearBlockRender;

import org.asutarisucu.AsutanTweaks;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Writes a captured frame out as a PNG with its alpha channel intact. */
public final class PngWriter {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private PngWriter() {}

    /**
     * @param rgba  pixels as they come off the GPU: RGBA bytes, bottom row first
     * @return the file that was written
     */
    public static Path write(Path outputDir, ByteBuffer rgba, int width, int height) throws IOException {
        Files.createDirectories(outputDir);
        Path out = outputDir.resolve(LocalDateTime.now().format(STAMP) + ".png");

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            // flip: the GPU hands back the bottom row first
            int row = (height - 1 - y) * width * 4;
            for (int x = 0; x < width; x++) {
                int i = row + x * 4;
                int r = rgba.get(i)     & 0xFF;
                int g = rgba.get(i + 1) & 0xFF;
                int b = rgba.get(i + 2) & 0xFF;
                int a = rgba.get(i + 3) & 0xFF;
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        ImageIO.write(image, "png", out.toFile());
        AsutanTweaks.LOGGER.info("[ClearBlockRender] wrote {}", out);
        return out;
    }
}
