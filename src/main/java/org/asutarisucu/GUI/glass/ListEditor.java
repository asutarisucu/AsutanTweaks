package org.asutarisucu.GUI.glass;

import org.asutarisucu.lib.config.StringListConfig;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Popup for editing a string list. Typing searches the matching registry and
 * shows suggestions; Enter or a click adds one. Free-text lists take whatever
 * is typed.
 */
public final class ListEditor {

    private static final int ROW_H = 22, SUGGEST_H = 20, MAX_SUGGEST = 7;

    private final StringListConfig target;
    private final Suggestions.Kind kind;
    private final String title;
    private final TextField input = TextField.plain();
    private List<Suggestions.Entry> results = List.of();
    private String lastQuery = "";
    private int selected;
    private float scroll, scrollTarget;

    private int px, py, pw, ph;

    public ListEditor(StringListConfig target, Suggestions.Kind kind, String title) {
        this.target = target;
        this.kind = kind;
        this.title = title;
    }

    private void refresh() {
        String q = input.text();
        if (q.equals(lastQuery)) return;
        lastQuery = q;
        results = kind == null ? List.of() : Suggestions.search(kind, q, 40);
        selected = 0;
    }

    public void draw(Gfx g, int screenW, int screenH, int mx, int my, float dt) {
        refresh();
        pw = Math.min(340, screenW - 40);
        ph = Math.min(300, screenH - 30);
        px = (screenW - pw) / 2;
        py = (screenH - ph) / 2;
        Widgets.popupFrame(g, px, py, pw, ph, title, mx, my);

        int fx = px + 14, fy = py + 30, fw = pw - 28;
        Widgets.field(g, fx, fy, fw, 20, true);
        Widgets.magnifier(g, fx + 7, fy + 5, Widgets.TEXT_DIM);
        input.draw(g, fx + 22, fy + 6, fw - 28, Widgets.TEXT, Lang.get("ui.list.hint"), Widgets.TEXT_FAINT, true);

        // current entries
        List<String> items = target.getStrings();
        int ly = fy + 28, lh = Math.max(0, py + ph - 10 - ly);
        int maxScroll = Math.max(0, items.size() * ROW_H - lh);
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
        scroll = Anim.approach(scroll, scrollTarget, dt, 18);
        int sc = Math.round(scroll);

        if (items.isEmpty()) {
            String s = Lang.get("ui.list.empty");
            g.text(s, px + (pw - g.width(s)) / 2, ly + 12, Widgets.TEXT_FAINT, false, false);
        }
        g.scissor(px + 6, ly, px + pw - 6, ly + lh);
        for (int i = 0; i < items.size(); i++) {
            int ry = ly + i * ROW_H - sc;
            if (ry + ROW_H <= ly || ry >= ly + lh) continue;
            Suggestions.Entry e = Suggestions.describe(kind, items.get(i));
            boolean hov = Widgets.inside(mx, my, fx, ry, fw, ROW_H - 2) && my >= ly && my < ly + lh;
            g.rounded(fx, ry, fw, ROW_H - 2, 6, hov ? 0x30FFFFFF : 0x18FFFFFF);
            drawEntry(g, e, fx + 4, ry + 2, fw - 30);
            int dx = fx + fw - 20, dy = ry + 3;
            boolean dh = Widgets.inside(mx, my, dx, dy, 14, 14);
            g.rounded(dx, dy, 14, 14, 7, dh ? 0xC0FF5F57 : 0x30FFFFFF);
            g.text("×", dx + 4, dy + 3, dh ? 0xFFFFFFFF : Widgets.TEXT_DIM, false, false);
        }
        g.unscissor();
        if (maxScroll > 0) {
            int trackH = lh;
            int thumbH = Math.max(12, trackH * lh / (items.size() * ROW_H));
            int thumbY = ly + Math.round((trackH - thumbH) * (scroll / maxScroll));
            g.rounded(px + pw - 8, thumbY, 3, thumbH, 1, 0x60FFFFFF);
        }

        // suggestions on top of the list
        if (!results.isEmpty()) {
            int n = Math.min(MAX_SUGGEST, results.size());
            int sy = fy + 22, sh = n * SUGGEST_H + 6;
            g.push();
            g.raise();
            g.shadow(fx, sy, fw, sh, 8, 12, 0x90000000);
            g.glass(fx, sy, fw, sh, 8, 0xFF0E1118, 1.0f, Widgets.refraction() * 0.5f, false);
            int first = Math.max(0, Math.min(selected - n + 1, results.size() - n));
            for (int i = 0; i < n; i++) {
                int idx = first + i;
                int ry = sy + 3 + i * SUGGEST_H;
                boolean hov = Widgets.inside(mx, my, fx, ry, fw, SUGGEST_H);
                if (idx == selected || hov) g.rounded(fx + 3, ry, fw - 6, SUGGEST_H, 6, idx == selected ? 0x504A96F0 : 0x28FFFFFF);
                drawEntry(g, results.get(idx), fx + 6, ry + 2, fw - 12);
            }
            g.pop();
        }
    }

    private void drawEntry(Gfx g, Suggestions.Entry e, int x, int y, int w) {
        int tx = x;
        if (e.icon() != null && !e.icon().isEmpty()) {
            g.item(e.icon(), x, y);
            tx += 20;
        } else if (e.swatch() >= 0) {
            g.rounded(x + 2, y + 2, 12, 12, 6, 0xFF000000 | e.swatch());
            tx += 20;
        }
        boolean sameAsLabel = e.sub().isEmpty() || e.sub().equalsIgnoreCase(e.label());
        int labelW = g.width(e.label());
        int room = x + w - tx;
        g.text(g.ellipsize(e.label(), room), tx, y + 4, Widgets.TEXT, false, false);
        if (!sameAsLabel && labelW + 12 < room) {
            String sub = g.ellipsize(e.sub(), room - labelW - 12);
            g.text(sub, x + w - g.width(sub), y + 4, Widgets.TEXT_FAINT, false, false);
        }
    }

    /** @return false when the popup should close */
    public boolean mouseDown(int mx, int my) {
        if (!Widgets.inside(mx, my, px, py, pw, ph) || Widgets.hitClose(px, py, pw, mx, my)) return false;
        int fx = px + 14, fy = py + 30, fw = pw - 28;
        if (!results.isEmpty()) {
            int n = Math.min(MAX_SUGGEST, results.size());
            int sy = fy + 22;
            int first = Math.max(0, Math.min(selected - n + 1, results.size() - n));
            if (Widgets.inside(mx, my, fx, sy, fw, n * SUGGEST_H + 6)) {
                int i = (my - sy - 3) / SUGGEST_H;
                if (i >= 0 && i < n) add(results.get(first + i).value());
                return true;
            }
        }
        List<String> items = target.getStrings();
        int ly = fy + 28, lh = py + ph - 10 - ly;
        if (my >= ly && my < ly + lh) {
            int i = (my - ly + Math.round(scroll)) / ROW_H;
            int ry = ly + i * ROW_H - Math.round(scroll);
            if (i >= 0 && i < items.size() && Widgets.inside(mx, my, fx + fw - 20, ry + 3, 14, 14)) {
                List<String> next = new ArrayList<>(items);
                next.remove(i);
                target.setValue(next);
            }
        }
        return true;
    }

    public void scroll(double amount) {
        scrollTarget -= (float) amount * ROW_H * 2;
    }

    /** @return false when the popup should close */
    public boolean key(int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (!input.isEmpty()) {
                    input.set("");
                    return true;
                }
                return false;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                refresh();
                if (!results.isEmpty()) add(results.get(selected).value());
                else if (kind == null && !input.text().isBlank()) add(input.text().trim());
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (!results.isEmpty()) selected = Math.min(results.size() - 1, selected + 1);
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                selected = Math.max(0, selected - 1);
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                refresh();
                if (!results.isEmpty()) input.set(results.get(selected).value());
                return true;
            }
            default -> {
                input.key(keyCode);
                return true;
            }
        }
    }

    public void type(String s) {
        input.type(s);
    }

    private void add(String value) {
        List<String> items = new ArrayList<>(target.getStrings());
        for (String s : items) {
            if (s.toLowerCase(Locale.ROOT).equals(value.toLowerCase(Locale.ROOT))) {
                input.set("");
                return;
            }
        }
        items.add(value);
        target.setValue(items);
        input.set("");
        scrollTarget = Float.MAX_VALUE;
    }
}
