package org.asutarisucu.GUI.glass;

import org.asutarisucu.Configs.Configs;

/** Controls drawn on glass, shared by the config screen and its popups. */
public final class Widgets {

    private Widgets() {}

    public static final int TEXT = 0xFFF5F7FB;
    public static final int TEXT_DIM = 0xC0E3E8F2;
    public static final int TEXT_FAINT = 0x80E3E8F2;
    public static final int ACCENT = 0xFF5AA9FF;
    public static final int ON = 0xFF34C759;
    public static final int CONTROL = 0x40000000;
    public static final int STROKE = 0x30FFFFFF;

    public static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public static float frost() {
        return 1.0f - Configs.Ui.GLASS_TRANSPARENCY.getIntegerValue() / 100.0f;
    }

    public static float refraction() {
        return Configs.Ui.REFRACTION.getIntegerValue() / 100.0f;
    }

    /** A glass panel with its shadow. */
    public static void panel(Gfx g, int x, int y, int w, int h, int radius, boolean dissolve) {
        g.shadow(x, y + 2, w, h, radius, 14, 0x55000000);
        g.glass(x, y, w, h, radius, Gfx.GLASS_TINT, frost(), refraction(), dissolve);
    }

    /** Popup body: shadow, frostier glass, title and a close button. */
    public static void popupFrame(Gfx g, int x, int y, int w, int h, String title, int mx, int my) {
        g.shadow(x, y + 4, w, h, 14, 24, 0x80000000);
        g.glass(x, y, w, h, 14, Gfx.GLASS_TINT, Math.min(1.0f, frost() + 0.25f), refraction(), false);
        g.text(title, x + 14, y + 12, TEXT, true, true);
        closeButton(g, x + w - 22, y + 8, mx, my);
    }

    public static boolean hitClose(int px, int py, int w, int mx, int my) {
        return inside(mx, my, px + w - 22, py + 8, 14, 14);
    }

    public static void closeButton(Gfx g, int x, int y, int mx, int my) {
        boolean hov = inside(mx, my, x, y, 14, 14);
        g.rounded(x, y, 14, 14, 7, hov ? 0xC0FF5F57 : 0x40FFFFFF);
        g.text("×", x + 4, y + 3, hov ? 0xFFFFFFFF : TEXT_DIM, false, false);
    }

    /** Recessed box behind a text field. */
    public static void field(Gfx g, int x, int y, int w, int h, boolean focused) {
        g.rounded(x, y, w, h, Math.min(8, h / 2), 0x50000000);
        g.outline(x, y, w, h, Math.min(8, h / 2), 1, focused ? 0xC05AA9FF : STROKE);
    }

    public static void button(Gfx g, int x, int y, int w, int h, String label, int mx, int my, boolean primary) {
        boolean hov = inside(mx, my, x, y, w, h);
        int bg = primary ? (hov ? 0xFF6DB5FF : 0xE04A96F0) : (hov ? 0x40FFFFFF : 0x26FFFFFF);
        g.roundedSheen(x, y, w, h, h / 2, bg);
        g.outline(x, y, w, h, h / 2, 1, primary ? 0x40FFFFFF : STROKE);
        String s = g.ellipsize(label, w - 8);
        g.text(s, x + (w - g.width(s)) / 2, y + (h - 8) / 2, TEXT, primary, false);
    }

    /** iOS-style switch; {@code knob} runs from 0 (off) to 1 (on) so it can be animated. */
    public static void toggle(Gfx g, int x, int y, float knob, boolean hover) {
        int w = 28, h = 16;
        int track = Gfx.lerpColor(hover ? 0x50FFFFFF : 0x38FFFFFF, ON, knob);
        g.roundedSheen(x, y, w, h, 8, track);
        int kx = x + 2 + Math.round((w - 16) * knob);
        g.shadow(kx, y + 2, 12, 12, 6, 3, 0x70000000);
        g.roundedSheen(kx, y + 2, 12, 12, 6, 0xFFFDFDFE);
    }

    public static void chip(Gfx g, int x, int y, int w, int h, String label, int textColor, boolean hover, boolean active) {
        int bg = active ? 0x604A96F0 : (hover ? 0x40FFFFFF : CONTROL);
        g.rounded(x, y, w, h, h / 2, bg);
        g.outline(x, y, w, h, h / 2, 1, active ? 0xC05AA9FF : STROKE);
        String s = g.ellipsize(label, w - 10);
        g.text(s, x + (w - g.width(s)) / 2, y + (h - 8) / 2, textColor, false, false);
    }

    /** Horizontal slider; {@code t} is the filled share, 0-1. */
    public static void slider(Gfx g, int x, int y, int w, float t, boolean hover) {
        t = Gfx.clamp01(t);
        int cy = y + 5;
        g.rounded(x, cy - 2, w, 4, 2, 0x50000000);
        int fw = Math.round(w * t);
        if (fw > 0) g.rounded(x, cy - 2, fw, 4, 2, hover ? 0xFF7CBBFF : ACCENT);
        int kx = x + fw;
        g.shadow(kx - 5, cy - 5, 10, 10, 5, 3, 0x80000000);
        g.roundedSheen(kx - 5, cy - 5, 10, 10, 5, 0xFFFDFDFE);
    }

    /** Six dots that show a panel can be dragged. */
    public static void grip(Gfx g, int x, int y, int color) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int px = x + col * 3, py = y + row * 3;
                g.fill(px, py, px + 2, py + 2, color);
            }
        }
    }

    public static void magnifier(Gfx g, int x, int y, int color) {
        g.ring(x + 4, y + 4, 4, 1, color);
        g.fill(x + 7, y + 7, x + 9, y + 9, color);
        g.fill(x + 8, y + 8, x + 10, y + 10, color);
    }
}
