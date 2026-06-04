package org.asutarisucu.GUI;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.render.HudRenderer;

//#if MC < 12001
import net.minecraft.client.util.math.MatrixStack;
//#elseif MC < 260100
//$$ import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#endif

public class ProgressMeter {
    public static final ProgressMeter INSTANCE = new ProgressMeter();

    private static final int HOLD_MS = 4000;
    private static final int FADE_MS = 600;
    private static final int BAR_W   = 140;
    private static final int BAR_H   = 5;
    private static final int PAD_X   = 10;
    private static final int PAD_Y   = 5;

    private static final int C_BG_RGB   = 0x060610;
    private static final int C_ACCENT   = 0xFF3A58B8;
    private static final int C_BORDER   = 0xFF222240;
    private static final int C_TRACK    = 0xFF0C0C22;
    private static final int C_TITLE    = 0xFF44CCFF;
    private static final int C_TEXT     = 0xFFEAEAF6;
    private static final int C_DIM      = 0xFF6868A0;

    private String title    = "";
    private String subtext  = "";
    private float  progress = 0f;
    private int    barColor = C_ACCENT;
    private long   hideAt   = 0L;

    public synchronized void show(String title, float progress, String subtext) {
        this.title    = title;
        this.progress = Math.max(0f, Math.min(1f, progress));
        this.subtext  = subtext != null ? subtext : "";
        this.barColor = C_ACCENT;
        this.hideAt   = System.currentTimeMillis() + HOLD_MS;
    }

    public synchronized void show(String title, float progress, String subtext, int barColor) {
        show(title, progress, subtext);
        this.barColor = barColor;
    }

//#if MC < 12001
    public void render(MatrixStack matrices) {
        render((t, x, y, c) -> HudRenderer.drawText(matrices, t, x, y, c),
               (x1, y1, x2, y2, c) -> HudRenderer.fillRect(matrices, x1, y1, x2, y2, c));
    }
//#elseif MC < 260100
//$$ public void render(DrawContext ctx) {
//$$     render((t, x, y, c) -> HudRenderer.drawText(ctx, t, x, y, c),
//$$            (x1, y1, x2, y2, c) -> HudRenderer.fillRect(ctx, x1, y1, x2, y2, c));
//$$ }
//#else
//$$ public void render(GuiGraphicsExtractor ext) {
//$$     render((t, x, y, c) -> HudRenderer.drawText(ext, t, x, y, c),
//$$            (x1, y1, x2, y2, c) -> HudRenderer.fillRect(ext, x1, y1, x2, y2, c));
//$$ }
//#endif

    @FunctionalInterface private interface DrawFn { void draw(String t, int x, int y, int c); }
    @FunctionalInterface private interface FillFn { void fill(int x1, int y1, int x2, int y2, int c); }

    private void render(DrawFn df, FillFn ff) {
        long now = System.currentTimeMillis();
        String t; String sub; float p; int bc; long hide;
        synchronized (this) {
            if (hideAt == 0 || now >= hideAt + FADE_MS) return;
            t = title; sub = subtext; p = progress; bc = barColor; hide = hideAt;
        }
        long rem = hide - now;
        int alpha = rem < FADE_MS ? Math.max(0, (int)(255L * rem / FADE_MS)) : 255;
        if (alpha <= 0) return;

        int sw  = HudRenderer.getScaledWidth();
        int sh  = HudRenderer.getScaledHeight();
        String pctStr = (int)(p * 100) + "%";
        int tW   = HudRenderer.getTextWidth(t);
        int pctW = HudRenderer.getTextWidth(pctStr);
        int subW = sub.isEmpty() ? 0 : HudRenderer.getTextWidth(sub);

        int rowW   = tW + 8 + BAR_W + 8 + pctW;
        int contentW = Math.max(rowW, subW);
        int totalW = PAD_X + contentW + PAD_X;
        boolean hasSub = !sub.isEmpty();
        int boxH = PAD_Y + 9 + (hasSub ? 10 : 0) + PAD_Y;
        int bx = (int)(Configs.Generic.PROGRESS_METER_X.getDoubleValue() * sw) - totalW / 2;
        int by = (int)(Configs.Generic.PROGRESS_METER_Y.getDoubleValue() * sh);
        bx = Math.max(0, Math.min(bx, sw - totalW));
        by = Math.max(0, Math.min(by, sh - boxH));

        // background
        int bgAlpha = alpha * 0xC0 / 255;
        ff.fill(bx, by, bx + totalW, by + boxH, (bgAlpha << 24) | C_BG_RGB);
        // top accent bar
        ff.fill(bx + 1, by, bx + totalW - 1, by + 1, withAlpha(C_ACCENT, alpha));
        // border
        ff.fill(bx,           by,        bx + totalW,  by + 1,      withAlpha(C_BORDER, alpha));
        ff.fill(bx,           by+boxH-1, bx + totalW,  by + boxH,   withAlpha(C_BORDER, alpha));
        ff.fill(bx,           by,        bx + 1,       by + boxH,   withAlpha(C_BORDER, alpha));
        ff.fill(bx+totalW-1,  by,        bx + totalW,  by + boxH,   withAlpha(C_BORDER, alpha));

        // row content: center rowW inside contentW
        int cx = bx + PAD_X + (contentW - rowW) / 2;
        int rowY = by + PAD_Y;

        // title
        df.draw(t, cx, rowY, withAlpha(C_TITLE, alpha));
        cx += tW + 8;

        // bar track
        int barY = rowY + 1;
        ff.fill(cx, barY, cx + BAR_W, barY + BAR_H, withAlpha(C_TRACK, alpha));
        // bar fill
        int fillW = (int)(p * BAR_W);
        if (fillW > 0) {
            ff.fill(cx, barY, cx + fillW, barY + BAR_H, withAlpha(bc, alpha));
            // inner highlight line
            ff.fill(cx, barY, cx + fillW, barY + 1, withAlpha(0x40FFFFFF, alpha));
        }
        // bar frame
        ff.fill(cx,         barY,          cx + BAR_W,  barY + 1,       withAlpha(C_BORDER, alpha));
        ff.fill(cx,         barY+BAR_H-1,  cx + BAR_W,  barY + BAR_H,   withAlpha(C_BORDER, alpha));
        ff.fill(cx,         barY,          cx + 1,      barY + BAR_H,   withAlpha(C_BORDER, alpha));
        ff.fill(cx+BAR_W-1, barY,          cx + BAR_W,  barY + BAR_H,   withAlpha(C_BORDER, alpha));
        cx += BAR_W + 8;

        // percentage
        df.draw(pctStr, cx, rowY, withAlpha(C_TEXT, alpha));

        // subtext (centered)
        if (hasSub) {
            int sx = bx + (totalW - subW) / 2;
            df.draw(sub, sx, rowY + 10, withAlpha(C_DIM, alpha));
        }
    }

    private static int withAlpha(int argb, int fadeAlpha) {
        int base = (argb >>> 24) & 0xFF;
        return ((base * fadeAlpha / 255) << 24) | (argb & 0x00FFFFFF);
    }
}
