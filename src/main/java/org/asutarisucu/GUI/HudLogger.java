package org.asutarisucu.GUI;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.config.AlignMode;
import org.asutarisucu.lib.render.HudRenderer;

import java.util.ArrayList;
import java.util.List;

//#if MC < 12001
import net.minecraft.client.util.math.MatrixStack;
//#elseif MC < 260100
//$$ import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#endif

public class HudLogger {
    public static final HudLogger INSTANCE = new HudLogger();

    private static final int MAX_MESSAGES = 8;
    private static final int LINE_H  = 12;
    private static final int PAD_X   = 4;
    private static final int PAD_Y   = 2;
    private static final int FADE_MS = 500;

    private static class Entry {
        final String text;
        final long   expireAt;
        Entry(String t, long e) { text = t; expireAt = e; }
    }

    private final List<Entry> entries = new ArrayList<>();

    public synchronized void log(String message) {
        long ms = (long) Configs.Generic.HUD_LOG_TIMEOUT.getIntegerValue() * 1000L;
        entries.add(new Entry(message, System.currentTimeMillis() + ms));
        while (entries.size() > MAX_MESSAGES) entries.remove(0);
    }

//#if MC < 12001
    public void render(MatrixStack matrices) {
        long now = System.currentTimeMillis();
        List<Entry> active;
        synchronized (this) {
            entries.removeIf(e -> e.expireAt <= now);
            active = new ArrayList<>(entries);
        }
        if (active.isEmpty()) return;
        renderEntries(active, now,
            (t, x, y, c) -> HudRenderer.drawText(matrices, t, x, y, c),
            (x1, y1, x2, y2, c) -> HudRenderer.fillRect(matrices, x1, y1, x2, y2, c));
    }
//#elseif MC < 260100
//$$ public void render(DrawContext context) {
//$$     long now = System.currentTimeMillis();
//$$     List<Entry> active;
//$$     synchronized (this) {
//$$         entries.removeIf(e -> e.expireAt <= now);
//$$         active = new ArrayList<>(entries);
//$$     }
//$$     if (active.isEmpty()) return;
//$$     renderEntries(active, now,
//$$         (t, x, y, c) -> HudRenderer.drawText(context, t, x, y, c),
//$$         (x1, y1, x2, y2, c) -> HudRenderer.fillRect(context, x1, y1, x2, y2, c));
//$$ }
//#else
//$$ public void render(GuiGraphicsExtractor extractor) {
//$$     long now = System.currentTimeMillis();
//$$     List<Entry> active;
//$$     synchronized (this) {
//$$         entries.removeIf(e -> e.expireAt <= now);
//$$         active = new ArrayList<>(entries);
//$$     }
//$$     if (active.isEmpty()) return;
//$$     renderEntries(active, now,
//$$         (t, x, y, c) -> HudRenderer.drawText(extractor, t, x, y, c),
//$$         (x1, y1, x2, y2, c) -> HudRenderer.fillRect(extractor, x1, y1, x2, y2, c));
//$$ }
//#endif

    @FunctionalInterface
    private interface DrawFn { void draw(String t, int x, int y, int color); }
    @FunctionalInterface
    private interface FillFn { void fill(int x1, int y1, int x2, int y2, int color); }

    private void renderEntries(List<Entry> active, long now, DrawFn drawFn, FillFn fillFn) {
        int sw = HudRenderer.getScaledWidth();
        int sh = HudRenderer.getScaledHeight();
        double xr    = Configs.Generic.HUD_LOG_X.getDoubleValue();
        double yr    = Configs.Generic.HUD_LOG_Y.getDoubleValue();
        int logWidth = Configs.Generic.HUD_LOG_WIDTH.getIntegerValue();
        AlignMode align = Configs.Generic.HUD_LOG_ALIGN.getValue();

        int anchorX = (int)(xr * sw);
        int boxLeft = switch (align) {
            case LEFT   -> anchorX;
            case CENTER -> anchorX - logWidth / 2;
            case RIGHT  -> anchorX - logWidth;
        };
        boxLeft = Math.max(0, Math.min(boxLeft, sw - logWidth));
        int baseY = Math.max(0, Math.min((int)(yr * sh), sh - active.size() * LINE_H));

        for (int i = 0; i < active.size(); i++) {
            Entry e = active.get(i);
            long rem = e.expireAt - now;
            int alpha = rem < FADE_MS ? Math.max(0, (int)(255L * rem / FADE_MS)) : 255;
            if (alpha <= 0) continue;
            int bgAlpha = alpha / 2;
            int textColor = (alpha << 24) | 0xFFFFFF;
            int bgColor   = (bgAlpha << 24);
            int tw = HudRenderer.getTextWidth(e.text);
            int y  = baseY + i * LINE_H;
            fillFn.fill(boxLeft, y - PAD_Y, boxLeft + logWidth, y + LINE_H - PAD_Y, bgColor);
            int textX = switch (align) {
                case LEFT   -> boxLeft + PAD_X;
                case CENTER -> boxLeft + (logWidth - tw) / 2;
                case RIGHT  -> boxLeft + logWidth - tw - PAD_X;
            };
            drawFn.draw(e.text, textX, y, textColor);
        }
    }
}
