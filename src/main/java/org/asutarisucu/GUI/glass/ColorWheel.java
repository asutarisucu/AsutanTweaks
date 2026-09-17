package org.asutarisucu.GUI.glass;

import org.asutarisucu.lib.config.ColorConfig;
import org.lwjgl.glfw.GLFW;

/**
 * Circular colour picker: hue around the disc, saturation from the centre out,
 * with sliders for brightness and opacity and a hex field.
 *
 * Changes apply to the config as they are made, so the effect can be seen in
 * the world; Cancel and Esc put the old colour back.
 */
public final class ColorWheel {

    private static final int W = 300, H = 224;
    private static final int R = 70;

    private final ColorConfig target;
    private final String title;
    private final int original;
    private float hue, sat, val, alpha;
    private final TextField hexField = TextField.hex();
    private boolean hexFocused;
    /** 0 none, 1 disc, 2 brightness, 3 opacity */
    private int dragging;

    // layout of the last draw
    private int px, py;

    public ColorWheel(ColorConfig target, String title) {
        this.target = target;
        this.title = title;
        this.original = target.getValue();
        setFromArgb(original);
        syncHex();
    }

    public void draw(Gfx g, int screenW, int screenH, int mx, int my) {
        px = (screenW - W) / 2;
        py = (screenH - H) / 2;
        Widgets.popupFrame(g, px, py, W, H, title, mx, my);

        int cx = discX(), cy = discY();
        g.shadow(cx - R, cy - R, R * 2, R * 2, R, 10, 0x60000000);
        g.disc(cx, cy, R, val);
        // Knob at the current hue and saturation.
        double ang = hue * Math.PI * 2;
        int kx = cx + (int) Math.round(Math.cos(ang) * sat * R);
        int ky = cy + (int) Math.round(Math.sin(ang) * sat * R);
        int rgb = hsvToRgb(hue, sat, val);
        g.shadow(kx - 7, ky - 7, 14, 14, 7, 4, 0x80000000);
        g.rounded(kx - 7, ky - 7, 14, 14, 7, 0xFF000000 | rgb);
        g.ring(kx, ky, 7, 1, 0xFFFFFFFF);

        int rx = px + 188, rw = W - 188 - 16;
        // Old colour on the left half, new on the right.
        int sy = py + 40, half = (rw - 4) / 2;
        g.swatch(rx, sy, half, 34, 8, original);
        g.swatch(rx + rw - half, sy, half, 34, 8, argb());
        g.outline(rx, sy, half, 34, 8, 1, 0x40FFFFFF);
        g.outline(rx + rw - half, sy, half, 34, 8, 1, 0x60FFFFFF);

        int hy = py + 84;
        Widgets.field(g, rx, hy, rw, 18, hexFocused);
        hexField.draw(g, rx + 6, hy + 5, rw - 12, 0xFFFFFFFF, "AARRGGBB", 0x80FFFFFF, hexFocused);

        int by = py + 116;
        g.text(Lang.get("ui.color.brightness"), rx, by, 0xD0FFFFFF, false, false);
        g.bar(rx, by + 11, rw, 10, 5, 0xFF000000 | hsvToRgb(hue, sat, 1), false);
        knob(g, rx + Math.round(val * rw), by + 16);

        int ay = py + 146;
        g.text(Lang.get("ui.color.alpha"), rx, ay, 0xD0FFFFFF, false, false);
        g.bar(rx, ay + 11, rw, 10, 5, 0xFF000000 | rgb, true);
        knob(g, rx + Math.round(alpha * rw), ay + 16);

        int btnY = py + H - 30;
        Widgets.button(g, px + W - 16 - 72 - 6 - 72, btnY, 72, 18, Lang.get("ui.cancel"), mx, my, false);
        Widgets.button(g, px + W - 16 - 72, btnY, 72, 18, Lang.get("ui.done"), mx, my, true);
    }

    private void knob(Gfx g, int x, int cy) {
        g.shadow(x - 5, cy - 7, 10, 14, 5, 3, 0x80000000);
        g.roundedSheen(x - 4, cy - 7, 8, 14, 4, 0xFFF4F6FA);
    }

    private int discX() { return px + 20 + R; }
    private int discY() { return py + 36 + R; }

    /** @return false when the click was outside the popup and it should close */
    public boolean mouseDown(int mx, int my) {
        if (mx < px || mx >= px + W || my < py || my >= py + H) {
            cancel();
            return false;
        }
        if (Widgets.hitClose(px, py, W, mx, my)) {
            cancel();
            return false;
        }
        int btnY = py + H - 30;
        if (my >= btnY && my < btnY + 18) {
            if (mx >= px + W - 16 - 72 - 6 - 72 && mx < px + W - 16 - 72 - 6) {
                cancel();
                return false;
            }
            if (mx >= px + W - 16 - 72 && mx < px + W - 16) {
                return false;
            }
        }
        int rx = px + 188, rw = W - 188 - 16;
        hexFocused = mx >= rx && mx < rx + rw && my >= py + 84 && my < py + 102;
        int dx = mx - discX(), dy = my - discY();
        if (dx * dx + dy * dy <= (R + 4) * (R + 4)) dragging = 1;
        else if (mx >= rx - 4 && mx < rx + rw + 4 && my >= py + 124 && my < py + 140) dragging = 2;
        else if (mx >= rx - 4 && mx < rx + rw + 4 && my >= py + 154 && my < py + 170) dragging = 3;
        mouseDrag(mx, my);
        return true;
    }

    public void mouseDrag(int mx, int my) {
        int rx = px + 188, rw = W - 188 - 16;
        switch (dragging) {
            case 1 -> {
                double dx = mx - discX(), dy = my - discY();
                double a = Math.atan2(dy, dx) / (Math.PI * 2);
                hue = (float) (a - Math.floor(a));
                sat = (float) Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / R);
            }
            case 2 -> val = Anim.clamp01((mx - rx) / (float) rw);
            case 3 -> alpha = Anim.clamp01((mx - rx) / (float) rw);
            default -> {
                return;
            }
        }
        apply();
        syncHex();
    }

    public void mouseUp() {
        dragging = 0;
    }

    /** @return false when the popup should close */
    public boolean key(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            cancel();
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            return false;
        }
        if (hexFocused && hexField.key(keyCode)) readHex();
        return true;
    }

    public void type(String s) {
        if (!hexFocused) return;
        hexField.type(s.toUpperCase(java.util.Locale.ROOT));
        readHex();
    }

    private void readHex() {
        String h = hexField.text().replace("#", "");
        if (h.length() == 6) h = "FF" + h;
        if (h.length() != 8) return;
        try {
            int v = (int) Long.parseLong(h, 16);
            setFromArgb(v);
            apply();
        } catch (NumberFormatException ignored) {
        }
    }

    private void syncHex() {
        hexField.set(String.format("%08X", argb()));
    }

    private void cancel() {
        target.setValue(original);
    }

    private void apply() {
        target.setValue(argb());
    }

    private int argb() {
        return (Math.round(alpha * 255) << 24) | hsvToRgb(hue, sat, val);
    }

    private void setFromArgb(int argb) {
        alpha = ((argb >>> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f, g = ((argb >> 8) & 0xFF) / 255.0f, b = (argb & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        float d = max - min;
        val = max;
        sat = max == 0 ? 0 : d / max;
        float h;
        if (d == 0) h = hue;
        else if (max == r) h = ((g - b) / d) / 6.0f;
        else if (max == g) h = ((b - r) / d + 2) / 6.0f;
        else h = ((r - g) / d + 4) / 6.0f;
        hue = h - (float) Math.floor(h);
    }

    /** Same conversion as glassHsv in the shader, so the knob matches the disc. */
    static int hsvToRgb(float h, float s, float v) {
        float[] k = new float[3];
        float[] off = { 0, 4, 2 };
        for (int i = 0; i < 3; i++) {
            float t = (h * 6 + off[i]) % 6;
            float c = Math.abs(t - 3) - 1;
            c = c < 0 ? 0 : (c > 1 ? 1 : c);
            k[i] = v * (1 + (c - 1) * s);
        }
        return (Math.round(k[0] * 255) << 16) | (Math.round(k[1] * 255) << 8) | Math.round(k[2] * 255);
    }
}
