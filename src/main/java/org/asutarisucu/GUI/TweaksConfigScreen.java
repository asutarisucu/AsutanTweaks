package org.asutarisucu.GUI;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.Configs.Hotkeys;
import org.asutarisucu.lib.config.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

//#if MC < 12001
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
//#elseif MC < 260100
//$$ import net.minecraft.client.gui.DrawContext;
//$$ import net.minecraft.client.gui.screen.Screen;
//$$ import net.minecraft.text.Text;
//$$ import net.minecraft.text.Style;
//$$ import net.minecraft.util.Identifier;
//#else
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//$$ import net.minecraft.client.gui.screens.Screen;
//$$ import net.minecraft.network.chat.Component;
//$$ import net.minecraft.network.chat.Style;
//$$ import net.minecraft.resources.Identifier;
//#endif

public class TweaksConfigScreen extends Screen {

    private static final int TAB_H    = 22;
    private static final int HEADER_H = 52;
    private static final int ROW_H_F  = 50;
    private static final int ROW_H_O  = 30;

    // ── palette ──────────────────────────────────────────────────
    private static final int C_BG         = 0xD8060610;
    private static final int C_PANEL      = 0xFF0A0A18;
    private static final int C_PANEL_EDGE = 0xFF101024;
    private static final int C_ROW        = 0xFF111120;
    private static final int C_ROW_ALT    = 0xFF0E0E1C;
    private static final int C_ROW_HOV    = 0xFF1A1A2E;
    private static final int C_BORDER     = 0xFF222240;
    private static final int C_BORDER_LT  = 0xFF303060;
    private static final int C_ACCENT     = 0xFF3A58B8;
    private static final int C_ACCENT_LT  = 0xFF5070D0;
    private static final int C_HDR_LINE   = 0xFF1A1A38;
    private static final int C_TAB_ON     = 0xFF152258;
    private static final int C_TAB_OFF    = 0xFF09091A;
    private static final int C_ON         = 0xFF102A14;
    private static final int C_ON_TXT     = 0xFF28B856;
    private static final int C_OFF        = 0xFF28100C;
    private static final int C_OFF_TXT    = 0xFF885050;
    private static final int C_KEY        = 0xFF0A1220;
    private static final int C_KEY_REC    = 0xFF0C1E3A;
    private static final int C_BTN        = 0xFF142040;
    private static final int C_BTN_HOV    = 0xFF2238A0;
    private static final int C_WHITE      = 0xFFEAEAF6;
    private static final int C_GRAY       = 0xFF6868A0;
    private static final int C_DIM        = 0xFF404070;
    private static final int C_CYAN       = 0xFF44CCFF;
    private static final int C_CYAN_DIM   = 0xFF2272A0;
    private static final int C_GOLD       = 0xFFCCAA44;
    private static final int C_SBAR_BG    = 0xFF060610;
    private static final int C_SBAR_FG    = 0xFF28285A;

    // ── font ─────────────────────────────────────────────────────
//#if MC < 12001
    private static final Identifier FONT_ID = new Identifier("asutantweaks", "config_screen");
//#elseif MC < 260100
//$$ private static final Identifier FONT_ID = Identifier.of("asutantweaks", "config_screen");
//#else
//$$ private static final Identifier FONT_ID = Identifier.fromNamespaceAndPath("asutantweaks", "config_screen");
//#endif

//#if MC < 12111
    private net.minecraft.text.Text withFont(String s) {
        return net.minecraft.text.Text.literal(s).setStyle(Style.EMPTY.withFont(FONT_ID));
    }
    private int fontWidth(String s) { return textRenderer.getWidth(withFont(s)); }
//#elseif MC < 260100
//$$ private net.minecraft.text.Text withFont(String s) {
//$$     return net.minecraft.text.Text.literal(s).setStyle(
//$$         Style.EMPTY.withFont(new net.minecraft.text.StyleSpriteSource.Font(FONT_ID)));
//$$ }
//$$ private int fontWidth(String s) { return textRenderer.getWidth(withFont(s)); }
//#else
//$$ private net.minecraft.network.chat.Component withFont(String s) {
//$$     return net.minecraft.network.chat.Component.literal(s).withStyle(
//$$         Style.EMPTY.withFont(new net.minecraft.network.chat.FontDescription.Resource(FONT_ID)));
//$$ }
//$$ private int fontWidth(String s) { return minecraft.font.width(withFont(s)); }
//#endif

    // ── layout (set in renderScene, shared with mouse handlers) ──
    private int listX, listY, listW, listH;

    private final Screen parent;
    private int   activeTab    = 0;
    private int   scrollTarget = 0;
    private float scrollOffsetF = 0f;
    private int   scrollOffset  = 0;
    private int   recTab = -1, recRow = -1;

    private final LinkedHashSet<Integer> heldKeys    = new LinkedHashSet<>();
    private String                       pendingCombo = null;

    // popup list editor state
    private int    popupListIdx  = -1;
    private int    popupScrTgt   = 0;
    private float  popupScrF     = 0f;
    private String popupInput    = "";
    private int    popupCursor   = 0;

    // search bar state
    private String searchQuery  = "";
    private int    searchCursor = 0;

    // hud log position drag state
    private boolean dragMode         = false;
    private boolean progressDragMode = false;

    // scrollbar drag state
    private boolean scrollbarDragging   = false;
    private int     scrollbarGrabOffset = 0;

    // color picker state
    private int    colorPickerIdx = -1;
    private int    cpR, cpG, cpB, cpA;
    private String cpHexInput    = "";
    private int    cpHexCursor   = 0;

    // numeric inline edit state
    private IConfig<?> numEditCfg   = null;
    private String     numEditStr    = "";
    private int        numEditCursor = 0;

    // sentinels to distinguish drag-mode buttons from real configs
    private static final BooleanConfig HUD_DRAG_SENTINEL      = new BooleanConfig("__hud_drag__",      false);
    private static final BooleanConfig PROGRESS_DRAG_SENTINEL = new BooleanConfig("__progress_drag__", false);

    private final List<OptionEntry> optionEntries;

    private record OptionEntry(String label, IConfig<?> cfg) {}

    private static final String[]        HK_NAMES = { "Open Config GUI", "Clear Item Count", "Add Highlight Item" };
    private static final org.asutarisucu.lib.config.FeatureConfig[] HK_CFGS = {
        Hotkeys.OPEN_CONFIG_GUI, Hotkeys.CLEAR_ITEM_COUNT, Hotkeys.ADD_HIGHLIGHT_ITEM
    };

    // ── constructors ─────────────────────────────────────────────
//#if MC < 260100
    public TweaksConfigScreen(Screen parent) {
        super(Text.literal("AsutanTweaks Config"));
        this.parent = parent;
        this.optionEntries = buildOptionEntries();
    }
//#else
//$$ public TweaksConfigScreen(Screen parent) {
//$$     super(Component.literal("AsutanTweaks Config"));
//$$     this.parent = parent;
//$$     this.optionEntries = buildOptionEntries();
//$$ }
//#endif

    // ── DrawCtx abstraction ──────────────────────────────────────
    private interface DrawCtx {
        void fill(int x1, int y1, int x2, int y2, int argb);
        void text(String s, int x, int y, int argb);
        int tw(String s);
        void scissor(int x1, int y1, int x2, int y2);
        void unscissor();
    }

//#if MC < 12001
    private DrawCtx mkCtx(final MatrixStack ms) {
        return new DrawCtx() {
            public void fill(int x1, int y1, int x2, int y2, int c) {
                DrawableHelper.fill(ms, x1, y1, x2, y2, c);
            }
            public void text(String s, int x, int y, int c) {
                textRenderer.drawWithShadow(ms, withFont(s), (float) x, (float) y, c);
            }
            public int tw(String s) { return fontWidth(s); }
            public void scissor(int x1, int y1, int x2, int y2) { DrawableHelper.enableScissor(x1, y1, x2, y2); }
            public void unscissor() { DrawableHelper.disableScissor(); }
        };
    }
//#elseif MC < 260100
//$$ private DrawCtx mkCtx(final DrawContext ctx) {
//$$     return new DrawCtx() {
//$$         public void fill(int x1, int y1, int x2, int y2, int c) { ctx.fill(x1, y1, x2, y2, c); }
//$$         public void text(String s, int x, int y, int c) {
//$$             ctx.drawText(textRenderer, withFont(s), x, y, c, true);
//$$         }
//$$         public int tw(String s) { return fontWidth(s); }
//$$         public void scissor(int x1, int y1, int x2, int y2) { ctx.enableScissor(x1, y1, x2, y2); }
//$$         public void unscissor() { ctx.disableScissor(); }
//$$     };
//$$ }
//#else
//$$ private DrawCtx mkCtx(final net.minecraft.client.gui.GuiGraphicsExtractor g) {
//$$     return new DrawCtx() {
//$$         public void fill(int x1, int y1, int x2, int y2, int c) { g.fill(x1, y1, x2, y2, c); }
//$$         public void text(String s, int x, int y, int c) {
//$$             g.text(minecraft.font, withFont(s), x, y, c, true);
//$$         }
//$$         public int tw(String s) { return fontWidth(s); }
//$$         public void scissor(int x1, int y1, int x2, int y2) { g.enableScissor(x1, y1, x2, y2); }
//$$         public void unscissor() { g.disableScissor(); }
//$$     };
//$$ }
//#endif

    // ── render ───────────────────────────────────────────────────
//#if MC < 12001
    @Override
    public void render(MatrixStack ms, int mx, int my, float delta) {
        renderScene(mkCtx(ms), mx, my);
        super.render(ms, mx, my, delta);
    }
//#elseif MC < 260100
//$$ @Override
//$$ public void render(DrawContext c, int mx, int my, float delta) {
//$$     renderScene(mkCtx(c), mx, my);
//$$     super.render(c, mx, my, delta);
//$$ }
//#else
//$$ @Override
//$$ public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float delta) {
//$$     renderScene(mkCtx(g), mx, my);
//$$     super.extractRenderState(g, mx, my, delta);
//$$ }
//#endif

    // ── scene ────────────────────────────────────────────────────
    private void renderScene(DrawCtx dc, int mx, int my) {
        int px = 8, py = TAB_H + 6;
        int pw = width - 16, ph = height - TAB_H - 6 - 34;

        // list area (must be set before clampScroll)
        listX = px + 4;
        listY = py + HEADER_H;
        listW = pw - 14;
        listH = ph - HEADER_H - 4;

        // smooth scroll advance then clamp
        scrollOffsetF += (scrollTarget - scrollOffsetF) * 0.25f;
        if (Math.abs(scrollOffsetF - scrollTarget) < 0.5f) scrollOffsetF = scrollTarget;
        clampScroll();
        scrollOffset = Math.round(scrollOffsetF);

        // ── background ────────────────────────────────────────
        dc.fill(0, 0, width, height, C_BG);

        // vignette: darker corners
        dc.fill(0,         0,          width / 4,     height / 4,     0x18000000);
        dc.fill(width * 3 / 4, 0,      width,         height / 4,     0x18000000);
        dc.fill(0,         height * 3 / 4, width / 4, height,         0x18000000);
        dc.fill(width * 3 / 4, height * 3 / 4, width, height,         0x18000000);

        // ── panel ────────────────────────────────────────────
        dc.fill(px, py, px + pw, py + ph, C_PANEL);
        // subtle inner highlight at top edge
        dc.fill(px + 1, py + 1, px + pw - 1, py + 2, C_PANEL_EDGE);
        // left/right gradient strips
        for (int i = 0; i < 3; i++) {
            int alpha = 0x08 - i * 0x02;
            dc.fill(px + 1 + i, py + 1, px + 2 + i, py + ph - 1, (alpha << 24) | 0xFFFFFF);
            dc.fill(px + pw - 2 - i, py + 1, px + pw - 1 - i, py + ph - 1, (alpha << 24) | 0xFFFFFF);
        }

        // ── list content (scissored to list area) ──
        dc.scissor(listX, listY, listX + listW, listY + listH);
        switch (activeTab) {
            case 0 -> drawFeatures(dc, mx, my);
            case 1 -> drawOptions(dc, mx, my);
            case 2 -> drawHotkeys(dc, mx, my);
        }
        dc.unscissor();

        // ── header overdraw (panel color over header zone for visual layering) ──
        dc.fill(px, py, px + pw, listY, C_PANEL);

        // header accent line at top
        dc.fill(px + 1, py, px + pw - 1, py + 1, C_ACCENT);
        // header bottom separator
        dc.fill(listX, listY - 1, listX + listW, listY, C_HDR_LINE);

        // title (vertically centered in the top 28px of the header)
        String title = "AsutanTweaks";
        String version = "v" + org.asutarisucu.Reference.VERSION;
        int titleW = dc.tw(title), versionW = dc.tw(version);
        int totalW = titleW + 6 + versionW;
        int tx = px + (pw - totalW) / 2;
        int ty = py + (28 - 8) / 2;
        dc.text(title,   tx,              ty, C_WHITE);
        dc.text(version, tx + titleW + 6, ty, C_CYAN);

        // decorative corner marks inside header
        cornerMark(dc, px + 2,      py + 2,  true,  true);
        cornerMark(dc, px + pw - 3, py + 2,  false, true);

        // separator between title row and search row
        dc.fill(px + 1, py + 28, px + pw - 1, py + 29, C_HDR_LINE);

        // search bar
        boolean searchActive = recTab < 0 && popupListIdx < 0 && colorPickerIdx < 0
                            && numEditCfg == null && !dragMode && !progressDragMode;
        int sbX = px + 8, sbY = py + 31, sbW = pw - 16, sbH = 18;
        dc.fill(sbX, sbY, sbX + sbW, sbY + sbH, searchActive ? C_KEY_REC : C_KEY);
        border(dc, sbX, sbY, sbW, sbH);
        // magnifier prefix
        dc.text("/ ", sbX + 4, sbY + (sbH - 8) / 2, C_DIM);
        int prefixW = dc.tw("/ ");
        if (searchQuery.isEmpty()) {
            dc.text("Search...", sbX + 4 + prefixW, sbY + (sbH - 8) / 2, C_DIM);
        } else {
            dc.text(searchQuery, sbX + 4 + prefixW, sbY + (sbH - 8) / 2, C_WHITE);
        }
        if (searchActive && !searchQuery.isEmpty() && (System.currentTimeMillis() / 530) % 2 == 0) {
            int curX = sbX + 4 + prefixW + dc.tw(searchQuery.substring(0, searchCursor));
            dc.fill(curX, sbY + 2, curX + 1, sbY + sbH - 2, C_WHITE);
        }

        // ── panel bottom overdraw (covers the thin gap between list end and panel bottom) ──
        dc.fill(px, listY + listH, px + pw, py + ph, C_PANEL);
        // bottom accent line
        dc.fill(px + 1, py + ph - 1, px + pw - 1, py + ph, C_ACCENT);

        // ── panel border ────────────────────────────────────
        border(dc, px, py, pw, ph);

        // ── scrollbar ───────────────────────────────────────
        drawScrollbar(dc, px + pw - 6, listY, 4, listH);

        // ── tabs ────────────────────────────────────────────
        drawTabs(dc, mx, my);

        // ── done button ─────────────────────────────────────
        int bw = 84, bh = 20, bx = px + (pw - bw) / 2, by = height - 28;
        boolean bhov = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
        dc.fill(bx, by, bx + bw, by + bh, bhov ? C_BTN_HOV : C_BTN);
        // button top highlight
        dc.fill(bx + 1, by, bx + bw - 1, by + 1, bhov ? C_ACCENT_LT : C_BORDER_LT);
        border(dc, bx, by, bw, bh);
        String done = "Done";
        dc.text(done, bx + (bw - dc.tw(done)) / 2, by + 6, C_WHITE);

        // overlays on top of everything
        drawListEditorPopup(dc, mx, my);
        drawColorPickerPopup(dc, mx, my);
        renderDragMode(dc, mx, my);
    }

    private void cornerMark(DrawCtx dc, int x, int y, boolean leftAnchor, boolean topAnchor) {
        int dx = leftAnchor ? 1 : -1, dy = topAnchor ? 1 : -1;
        dc.fill(x, y, x + dx * 4, y + dy, C_BORDER_LT);
        dc.fill(x, y, x + dx,     y + dy * 4, C_BORDER_LT);
    }

    private void drawTabs(DrawCtx dc, int mx, int my) {
        String[] names = { "Features", "Options", "Hotkeys" };
        int tw0 = 82, gap = 2;
        int tx0 = (width - (tw0 * 3 + gap * 2)) / 2, ty = 2, th = TAB_H - 2;
        for (int i = 0; i < 3; i++) {
            int x = tx0 + i * (tw0 + gap);
            boolean on  = activeTab == i;
            boolean hov = !on && mx >= x && mx < x + tw0 && my >= ty && my < ty + th;
            dc.fill(x, ty, x + tw0, ty + th, on ? C_TAB_ON : (hov ? 0xFF141430 : C_TAB_OFF));
            // top edge highlight for active tab
            if (on) {
                dc.fill(x + 1, ty, x + tw0 - 1, ty + 1, C_ACCENT_LT);
                dc.fill(x + 3, ty + th - 2, x + tw0 - 3, ty + th, C_CYAN);
            }
            border(dc, x, ty, tw0, th);
            String n = names[i];
            dc.text(n, x + (tw0 - dc.tw(n)) / 2, ty + (th - 8) / 2, on ? C_WHITE : (hov ? C_GRAY : C_DIM));
        }
    }

    // ── feature list ─────────────────────────────────────────────
    private void drawFeatures(DrawCtx dc, int mx, int my) {
        int[] indices = filteredFeatureIndices();
        Feature[] fs = Feature.values();
        for (int row = 0; row < indices.length; row++) {
            int ry = listY + row * ROW_H_F - scrollOffset;
            if (ry + ROW_H_F <= listY || ry >= listY + listH) continue;
            drawFeatureRow(dc, fs[indices[row]], indices[row], ry, mx, my, row % 2 == 1);
        }
    }

    private void drawFeatureRow(DrawCtx dc, Feature f, int origIdx, int ry, int mx, int my, boolean alt) {
        boolean hov = mx >= listX && mx < listX + listW && my >= ry && my < ry + ROW_H_F;
        boolean on  = f.isEnabled();
        int bg = hov ? C_ROW_HOV : (alt ? C_ROW_ALT : C_ROW);
        dc.fill(listX, ry, listX + listW, ry + ROW_H_F - 1, bg);
        dc.fill(listX, ry + ROW_H_F - 1, listX + listW, ry + ROW_H_F, C_BORDER);
        // left accent bar
        if (on || hov) {
            int barColor = on ? C_ON_TXT : C_ACCENT;
            dc.fill(listX, ry, listX + 2, ry + ROW_H_F - 1, barColor);
        }
        // top separator highlight on hover
        if (hov) dc.fill(listX + 2, ry, listX + listW, ry + 1, C_BORDER_LT);

        int mid = ry + ROW_H_F / 2;

        // name + wrapped description
        dc.text(f.displayName, listX + 8, ry + 8, on ? C_WHITE : C_GRAY);
        List<String> descLines = wrapText(f.description, listW - 126, dc);
        for (int li = 0; li < Math.min(descLines.size(), 2); li++) {
            dc.text(descLines.get(li), listX + 8, ry + 20 + li * 11, C_DIM);
        }

        // toggle pill
        int tW = 36, tH = 14, tX = listX + listW - tW - 6 - 72, tY = mid - tH / 2;
        dc.fill(tX, tY, tX + tW, tY + tH, on ? C_ON : C_OFF);
        // pill top shimmer
        dc.fill(tX + 1, tY + 1, tX + tW - 1, tY + 2, on ? 0x1A44FF44 : 0x1AFF4444);
        border(dc, tX, tY, tW, tH);
        String tl = on ? "ON" : "OFF";
        dc.text(tl, tX + (tW - dc.tw(tl)) / 2, tY + 3, on ? C_ON_TXT : C_OFF_TXT);

        // hotkey chip
        boolean rec = recTab == 0 && recRow == origIdx;
        String ks = rec ? liveKeyDisplay() : fmtKey(f.config.getHotkey().getStorageString());
        int kw = 72, kh = 14, kx = listX + listW - kw - 4, ky = mid - kh / 2;
        dc.fill(kx, ky, kx + kw, ky + kh, rec ? C_KEY_REC : C_KEY);
        border(dc, kx, ky, kw, kh);
        dc.text(ks, kx + 4, ky + 3, rec ? C_CYAN : C_DIM);
    }

    // ── option list ──────────────────────────────────────────────
    private void drawOptions(DrawCtx dc, int mx, int my) {
        int[] indices = filteredOptionIndices();
        for (int row = 0; row < indices.length; row++) {
            int ry = listY + row * ROW_H_O - scrollOffset;
            if (ry + ROW_H_O <= listY || ry >= listY + listH) continue;
            drawOptionRow(dc, optionEntries.get(indices[row]), ry, mx, my, row % 2 == 1);
        }
    }

    private void drawOptionRow(DrawCtx dc, OptionEntry e, int ry, int mx, int my, boolean alt) {
        boolean hov = mx >= listX && mx < listX + listW && my >= ry && my < ry + ROW_H_O;
        int bg = hov ? C_ROW_HOV : (alt ? C_ROW_ALT : C_ROW);
        dc.fill(listX, ry, listX + listW, ry + ROW_H_O - 1, bg);
        dc.fill(listX, ry + ROW_H_O - 1, listX + listW, ry + ROW_H_O, C_BORDER);
        if (hov) {
            dc.fill(listX, ry, listX + 2, ry + ROW_H_O - 1, C_ACCENT);
            dc.fill(listX + 2, ry, listX + listW, ry + 1, C_BORDER_LT);
        }
        dc.text(e.label(), listX + 8, ry + (ROW_H_O - 8) / 2, C_WHITE);
        drawOptionValue(dc, e.cfg(), listX + listW - 120, ry + (ROW_H_O - 14) / 2, 116, 14);
    }

    private void drawOptionValue(DrawCtx dc, IConfig<?> cfg, int x, int y, int w, int h) {
        if (cfg == HUD_DRAG_SENTINEL || cfg == PROGRESS_DRAG_SENTINEL) {
            dc.fill(x, y, x + w, y + h, C_BTN);
            border(dc, x, y, w, h);
            String lab = "Set Position";
            dc.text(lab, x + (w - dc.tw(lab)) / 2, y + 3, C_CYAN);
        } else if (cfg instanceof BooleanConfig bc) {
            boolean on = bc.getBooleanValue();
            dc.fill(x, y, x + w, y + h, on ? C_ON : C_OFF);
            dc.fill(x + 1, y + 1, x + w - 1, y + 2, on ? 0x1A44FF44 : 0x1AFF4444);
            border(dc, x, y, w, h);
            String l = on ? "ON" : "OFF";
            dc.text(l, x + (w - dc.tw(l)) / 2, y + 3, on ? C_ON_TXT : C_OFF_TXT);
        } else if (cfg instanceof IntegerConfig ic) {
            if (numEditCfg == cfg) {
                dc.fill(x, y, x + w, y + h, C_KEY_REC);
                border(dc, x, y, w, h);
                dc.text(numEditStr, x + 4, y + 3, C_WHITE);
                int cx = x + 4 + dc.tw(numEditStr.substring(0, numEditCursor));
                if ((System.currentTimeMillis() / 530) % 2 == 0)
                    dc.fill(cx, y + 2, cx + 1, y + h - 2, C_WHITE);
            } else {
                dc.fill(x, y, x + w, y + h, C_KEY);
                border(dc, x, y, w, h);
                String v = String.valueOf(ic.getIntegerValue());
                dc.text("<", x + 4, y + 3, C_GRAY);
                dc.text(v, x + (w - dc.tw(v)) / 2, y + 3, C_WHITE);
                dc.text(">", x + w - dc.tw(">") - 4, y + 3, C_GRAY);
            }
        } else if (cfg instanceof DoubleConfig dbl) {
            if (numEditCfg == cfg) {
                dc.fill(x, y, x + w, y + h, C_KEY_REC);
                border(dc, x, y, w, h);
                dc.text(numEditStr, x + 4, y + 3, C_WHITE);
                int cx = x + 4 + dc.tw(numEditStr.substring(0, numEditCursor));
                if ((System.currentTimeMillis() / 530) % 2 == 0)
                    dc.fill(cx, y + 2, cx + 1, y + h - 2, C_WHITE);
            } else {
                dc.fill(x, y, x + w, y + h, C_KEY);
                border(dc, x, y, w, h);
                String v = String.format("%.2f", dbl.getDoubleValue());
                dc.text("<", x + 4, y + 3, C_GRAY);
                dc.text(v, x + (w - dc.tw(v)) / 2, y + 3, C_WHITE);
                dc.text(">", x + w - dc.tw(">") - 4, y + 3, C_GRAY);
            }
        } else if (cfg instanceof ColorConfig cc) {
            int argb = cc.getValue();
            boolean active = colorPickerIdx >= 0 && optionEntries.get(colorPickerIdx).cfg() == cc;
            dc.fill(x, y, x + 16, y + h, 0xFF000000);
            dc.fill(x, y, x + 16, y + h, argb);
            dc.fill(x + 17, y, x + w, y + h, active ? C_KEY_REC : C_KEY);
            border(dc, x, y, w, h);
            dc.text(String.format("#%08X", argb), x + 21, y + 3, C_WHITE);
        } else if (cfg instanceof OptionListConfig<?> olc) {
            dc.fill(x, y, x + w, y + h, C_KEY);
            border(dc, x, y, w, h);
            String v = "< " + olc.getValue() + " >";
            dc.text(v, x + (w - dc.tw(v)) / 2, y + 3, C_WHITE);
        } else if (cfg instanceof StringListConfig slc) {
            dc.fill(x, y, x + w, y + h, C_KEY);
            border(dc, x, y, w, h);
            dc.text(slc.getStrings().size() + " items", x + 6, y + 3, C_CYAN_DIM);
            int eW = 36, eH = 10, eX = x + w - eW - 3, eY = y + (h - eH) / 2;
            dc.fill(eX, eY, eX + eW, eY + eH, C_BTN);
            border(dc, eX, eY, eW, eH);
            dc.text("Edit", eX + (eW - dc.tw("Edit")) / 2, eY + 1, C_CYAN);
        }
    }

    // ── hotkey list ──────────────────────────────────────────────
    private void drawHotkeys(DrawCtx dc, int mx, int my) {
        int[] indices = filteredHotkeyIndices();
        for (int row = 0; row < indices.length; row++) {
            int ry = listY + row * ROW_H_O - scrollOffset;
            if (ry + ROW_H_O <= listY || ry >= listY + listH) continue;
            drawHotkeyRow(dc, indices[row], ry, mx, my, row % 2 == 1);
        }
    }

    private void drawHotkeyRow(DrawCtx dc, int origIdx, int ry, int mx, int my, boolean alt) {
        boolean hov = mx >= listX && mx < listX + listW && my >= ry && my < ry + ROW_H_O;
        int bg = hov ? C_ROW_HOV : (alt ? C_ROW_ALT : C_ROW);
        dc.fill(listX, ry, listX + listW, ry + ROW_H_O - 1, bg);
        dc.fill(listX, ry + ROW_H_O - 1, listX + listW, ry + ROW_H_O, C_BORDER);
        if (hov) {
            dc.fill(listX, ry, listX + 2, ry + ROW_H_O - 1, C_ACCENT);
            dc.fill(listX + 2, ry, listX + listW, ry + 1, C_BORDER_LT);
        }
        dc.text(HK_NAMES[origIdx], listX + 8, ry + (ROW_H_O - 8) / 2, C_WHITE);

        boolean rec = recTab == 2 && recRow == origIdx;
        String ks = rec ? liveKeyDisplay() : fmtKey(HK_CFGS[origIdx].getHotkey().getStorageString());
        int kw = 100, kh = 14, kx = listX + listW - kw - 4, ky = ry + (ROW_H_O - kh) / 2;
        dc.fill(kx, ky, kx + kw, ky + kh, rec ? C_KEY_REC : C_KEY);
        border(dc, kx, ky, kw, kh);
        dc.text(ks, kx + 4, ky + 3, rec ? C_CYAN : C_DIM);
    }

    // ── shared draw helpers ──────────────────────────────────────
    private void border(DrawCtx dc, int x, int y, int w, int h) {
        dc.fill(x,         y,         x + w,     y + 1,     C_BORDER);
        dc.fill(x,         y + h - 1, x + w,     y + h,     C_BORDER);
        dc.fill(x,         y,         x + 1,     y + h,     C_BORDER);
        dc.fill(x + w - 1, y,         x + w,     y + h,     C_BORDER);
    }

    private void drawScrollbar(DrawCtx dc, int sbX, int sbY, int sbW, int sbH) {
        int rowH   = activeTab == 0 ? ROW_H_F : ROW_H_O;
        int totalH = rowCount() * rowH;
        dc.fill(sbX, sbY, sbX + sbW, sbY + sbH, C_SBAR_BG);
        if (totalH <= sbH) return;
        int tH = Math.max(16, sbH * sbH / totalH);
        int tY = sbY + (int) ((long) (sbH - tH) * scrollTarget / Math.max(1, totalH - sbH));
        dc.fill(sbX, tY, sbX + sbW, tY + tH, scrollbarDragging ? C_ACCENT : C_SBAR_FG);
        // scrollbar highlight
        dc.fill(sbX, tY, sbX + 1, tY + tH, scrollbarDragging ? C_ACCENT_LT : C_BORDER_LT);
    }

    // scrollbar geometry: { sbX, sbY, sbW, sbH, thumbH, thumbY, totalH } or null when no scroll needed
    private int[] scrollbarMetrics() {
        int px = 8, pw = width - 16;
        int sbX = px + pw - 6, sbW = 4;
        int sbY = listY, sbH = listH;
        int rowH = activeTab == 0 ? ROW_H_F : ROW_H_O;
        int totalH = rowCount() * rowH;
        if (totalH <= sbH) return null;
        int tH = Math.max(16, sbH * sbH / totalH);
        int tY = sbY + (int) ((long) (sbH - tH) * scrollTarget / Math.max(1, totalH - sbH));
        return new int[] { sbX, sbY, sbW, sbH, tH, tY, totalH };
    }

    private void dragScrollbarTo(int imy, int[] sb) {
        int sbY = sb[1], sbH = sb[3], tH = sb[4], totalH = sb[6];
        int denom = sbH - tH;
        if (denom <= 0) return;
        int topY = imy - scrollbarGrabOffset;
        scrollTarget = (int) ((long) (topY - sbY) * (totalH - sbH) / denom);
        clampScroll();
        scrollOffsetF = scrollTarget; // follow the cursor instantly while dragging
    }

    private List<String> wrapText(String text, int maxWidth, DrawCtx dc) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        if (dc.tw(text) <= maxWidth) { lines.add(text); return lines; }
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            String trial = cur.length() == 0 ? word : cur + " " + word;
            if (dc.tw(trial) <= maxWidth) {
                cur = new StringBuilder(trial);
            } else {
                if (cur.length() > 0) lines.add(cur.toString());
                cur = new StringBuilder(word);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }

    private void clampScroll() {
        int rowH = activeTab == 0 ? ROW_H_F : ROW_H_O;
        int max  = Math.max(0, rowCount() * rowH - listH);
        scrollTarget  = Math.max(0, Math.min(scrollTarget, max));
        scrollOffsetF = Math.max(0, Math.min(scrollOffsetF, max));
    }

    private int rowCount() {
        return switch (activeTab) {
            case 0 -> filteredFeatureIndices().length;
            case 1 -> filteredOptionIndices().length;
            case 2 -> filteredHotkeyIndices().length;
            default -> 0;
        };
    }

    private boolean matchesSearch(String... texts) {
        if (searchQuery.isEmpty()) return true;
        String q = searchQuery.toLowerCase();
        for (String t : texts) if (t != null && t.toLowerCase().contains(q)) return true;
        return false;
    }

    private int[] filteredFeatureIndices() {
        Feature[] fs = Feature.values();
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i < fs.length; i++)
            if (matchesSearch(fs[i].displayName, fs[i].description)) r.add(i);
        return r.stream().mapToInt(x -> x).toArray();
    }

    private int[] filteredOptionIndices() {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i < optionEntries.size(); i++)
            if (matchesSearch(optionEntries.get(i).label())) r.add(i);
        return r.stream().mapToInt(x -> x).toArray();
    }

    private int[] filteredHotkeyIndices() {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i < HK_NAMES.length; i++)
            if (matchesSearch(HK_NAMES[i])) r.add(i);
        return r.stream().mapToInt(x -> x).toArray();
    }

    private String liveKeyDisplay() {
        if (heldKeys.isEmpty()) return "...";
        return buildCombo(heldKeys) + (pendingCombo == null ? "+?" : "");
    }

    private static String fmtKey(String s) {
        if (s == null || s.isBlank()) return "None";
        return s.replace(",", "+");
    }

    private List<OptionEntry> buildOptionEntries() {
        List<OptionEntry> list = new ArrayList<>();
        list.add(new OptionEntry("Restock Count",             Configs.Generic.RESTOCK_COUNT));
        list.add(new OptionEntry("Void Height (Overworld)",   Configs.Generic.VOID_HEIGHT_OW));
        list.add(new OptionEntry("Void Height (Nether)",      Configs.Generic.VOID_HEIGHT_NE));
        list.add(new OptionEntry("Void Height (End)",         Configs.Generic.VOID_HEIGHT_END));
        list.add(new OptionEntry("Void Disconnect",           Configs.Generic.VOID_DISCONNECT));
        list.add(new OptionEntry("Restriction Whitelist",     Configs.Generic.RESTRICTION_STATE_WHITELIST));
        list.add(new OptionEntry("Restriction Whitelist Msg", Configs.Generic.RESTRICTION_WHITELIST_MESSAGE_TYPE));
        list.add(new OptionEntry("Last Use Blacklist",        Configs.Generic.LAST_USE_CANCEL_BLACKLIST));
        list.add(new OptionEntry("EC Materiallist Whitelist", Configs.Generic.ENDERCHEST_MATERIALLIST_WHITELIST));
        list.add(new OptionEntry("EC Materiallist Blacklist", Configs.Generic.ENDERCHEST_MATERIALLIST_BLACKLIST));
        list.add(new OptionEntry("EC Materiallist Filter",    Configs.Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE));
        list.add(new OptionEntry("Highlight Item List",       Configs.Generic.HIGHLIGHT_ITEM_LIST));
        list.add(new OptionEntry("Highlight Block Color",     Configs.Generic.HIGHLIGHT_BLOCK_COLOR));
        list.add(new OptionEntry("Highlight Block Range",     Configs.Generic.HIGHLIGHT_BLOCK_RANGE));
        list.add(new OptionEntry("Highlight Container Color", Configs.Generic.HIGHLIGHT_CONTAINER_COLOR));
        list.add(new OptionEntry("Highlight Container Range", Configs.Generic.HIGHLIGHT_CONTAINER_RANGE));
        list.add(new OptionEntry("HUD Log Timeout (sec)",    Configs.Generic.HUD_LOG_TIMEOUT));
        list.add(new OptionEntry("HUD Log Width (px)",       Configs.Generic.HUD_LOG_WIDTH));
        list.add(new OptionEntry("HUD Log Alignment",        Configs.Generic.HUD_LOG_ALIGN));
        list.add(new OptionEntry("HUD Log Position",         HUD_DRAG_SENTINEL));
        list.add(new OptionEntry("Meter Position",           PROGRESS_DRAG_SENTINEL));
        return list;
    }

    // ── mouse ────────────────────────────────────────────────────
//#if MC < 12111
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        return handleMouseClick((int) mx, (int) my, btn);
    }
//#elseif MC < 260100
//$$ @Override
//$$ public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean isHandled) {
//$$     if (isHandled) return false;
//$$     return handleMouseClick((int) click.x(), (int) click.y(), click.button());
//$$ }
//#else
//$$ @Override
//$$ public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean isHandled) {
//$$     if (isHandled) return false;
//$$     return handleMouseClick((int) event.x(), (int) event.y(), event.button());
//$$ }
//#endif

    private boolean handleMouseClick(int imx, int imy, int btn) {
        if (btn != 0) return false;

        if (numEditCfg != null) commitNumEdit();

        // progress meter drag
        if (progressDragMode) {
            Configs.Generic.PROGRESS_METER_X.setValue(Math.max(0.0, Math.min(1.0, (double) imx / width)));
            Configs.Generic.PROGRESS_METER_Y.setValue(Math.max(0.0, Math.min(1.0, (double) imy / height)));
            progressDragMode = false;
            return true;
        }

        // color picker intercept
        if (colorPickerIdx >= 0) {
            handleColorPickerClick(imx, imy);
            return true;
        }

        // drag mode: click to set HUD log position
        if (dragMode) {
            org.asutarisucu.lib.config.AlignMode align = Configs.Generic.HUD_LOG_ALIGN.getValue();
            int PREV_W = Configs.Generic.HUD_LOG_WIDTH.getIntegerValue();
            int PREV_H = 64;
            int bxd = Math.max(0, Math.min(imx - PREV_W / 2, width  - PREV_W));
            int byd = Math.max(0, Math.min(imy - PREV_H / 2, height - PREV_H));
            double anchorX = switch (align) {
                case LEFT   -> (double) bxd / width;
                case CENTER -> (double)(bxd + PREV_W / 2) / width;
                case RIGHT  -> (double)(bxd + PREV_W)     / width;
            };
            Configs.Generic.HUD_LOG_X.setValue(Math.max(0.0, Math.min(1.0, anchorX)));
            Configs.Generic.HUD_LOG_Y.setValue(Math.max(0.0, Math.min(1.0, (double) byd / height)));
            dragMode = false;
            return true;
        }

        // popup editor intercept
        if (popupListIdx >= 0) {
            handlePopupClick(imx, imy);
            return true;
        }

        int bw = 84, bh = 20, bx = 8 + (width - 16 - bw) / 2, by = height - 28;
        if (imx >= bx && imx < bx + bw && imy >= by && imy < by + bh) {
            doClose(); return true;
        }

        int tw0 = 82, gap = 2;
        int tabX = (width - (tw0 * 3 + gap * 2)) / 2;
        if (imy >= 2 && imy < TAB_H) {
            for (int i = 0; i < 3; i++) {
                int tx = tabX + i * (tw0 + gap);
                if (imx >= tx && imx < tx + tw0) {
                    activeTab = i; scrollTarget = 0; scrollOffsetF = 0f; stopRec(); return true;
                }
            }
        }

        // scrollbar grab
        int[] sb = scrollbarMetrics();
        if (sb != null) {
            int sbX = sb[0], sbY = sb[1], sbW = sb[2], sbH = sb[3], tH = sb[4], tY = sb[5];
            if (imx >= sbX - 2 && imx < sbX + sbW + 2 && imy >= sbY && imy < sbY + sbH) {
                scrollbarGrabOffset = (imy >= tY && imy < tY + tH) ? imy - tY : tH / 2;
                scrollbarDragging = true;
                dragScrollbarTo(imy, sb);
                return true;
            }
        }

        if (imx < listX || imx >= listX + listW) return false;
        switch (activeTab) {
            case 0 -> clickFeature(imx, imy);
            case 1 -> clickOption(imx, imy);
            case 2 -> clickHotkey(imx, imy);
        }
        return true;
    }

    private void clickFeature(int mx, int my) {
        int[] indices = filteredFeatureIndices();
        Feature[] fs = Feature.values();
        for (int row = 0; row < indices.length; row++) {
            int origIdx = indices[row];
            int ry = listY + row * ROW_H_F - scrollOffset;
            if (my < ry || my >= ry + ROW_H_F) continue;

            int mid = ry + ROW_H_F / 2;
            int tW = 36, tH = 14, tX = listX + listW - tW - 6 - 72, tY = mid - tH / 2;
            if (mx >= tX && mx < tX + tW && my >= tY && my < tY + tH) {
                fs[origIdx].toggle(); return;
            }
            int kw = 72, kx = listX + listW - kw - 4, ky = mid - 7;
            if (mx >= kx && mx < kx + kw && my >= ky && my < ky + 14) {
                startRec(0, origIdx); return;
            }
            return;
        }
    }

    private void clickOption(int mx, int my) {
        int[] indices = filteredOptionIndices();
        for (int row = 0; row < indices.length; row++) {
            int origIdx = indices[row];
            int ry = listY + row * ROW_H_O - scrollOffset;
            if (my < ry || my >= ry + ROW_H_O) continue;

            IConfig<?> cfg = optionEntries.get(origIdx).cfg();
            int vx = listX + listW - 120, vy = ry + (ROW_H_O - 14) / 2, vw = 116;
            if (mx < vx || mx >= vx + vw) return;

            if (cfg == HUD_DRAG_SENTINEL) {
                dragMode = true;
            } else if (cfg == PROGRESS_DRAG_SENTINEL) {
                progressDragMode = true;
            } else if (cfg instanceof ColorConfig) {
                openColorPicker(origIdx);
            } else if (cfg instanceof StringListConfig) {
                openPopup(origIdx);
            } else if (cfg instanceof BooleanConfig bc) {
                bc.setValue(!bc.getBooleanValue());
            } else if (cfg instanceof IntegerConfig ic) {
                if (mx < vx + 22) ic.setValue(ic.getIntegerValue() - 1);
                else if (mx >= vx + vw - 22) ic.setValue(ic.getIntegerValue() + 1);
                else openNumEdit(ic);
            } else if (cfg instanceof DoubleConfig dbl) {
                double step = Math.max(0.1, (dbl.getMax() - dbl.getMin()) * 0.05);
                if (mx < vx + 22) dbl.setValue(dbl.getDoubleValue() - step);
                else if (mx >= vx + vw - 22) dbl.setValue(dbl.getDoubleValue() + step);
                else openNumEdit(dbl);
            } else if (cfg instanceof OptionListConfig<?> olc) {
                cycleOption(olc);
            }
            return;
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void cycleOption(OptionListConfig<E> olc) {
        E[] all = olc.getAllValues();
        E cur = olc.getValue();
        for (int i = 0; i < all.length; i++) {
            if (all[i] == cur) { olc.setValue(all[(i + 1) % all.length]); return; }
        }
    }

    private void clickHotkey(int mx, int my) {
        int[] indices = filteredHotkeyIndices();
        for (int row = 0; row < indices.length; row++) {
            int origIdx = indices[row];
            int ry = listY + row * ROW_H_O - scrollOffset;
            if (my < ry || my >= ry + ROW_H_O) continue;
            int kw = 100, kx = listX + listW - kw - 4, ky = ry + (ROW_H_O - 14) / 2;
            if (mx >= kx && mx < kx + kw && my >= ky && my < ky + 14) {
                startRec(2, origIdx);
            }
            return;
        }
    }

    @Override
//#if MC < 12004
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (popupListIdx >= 0) { popupScrTgt -= (int)(amount * 20); return true; }
        scrollTarget -= (int) (amount * ROW_H_F / 2);
//#else
//$$ public boolean mouseScrolled(double mx, double my, double sx, double sy) {
//$$     if (popupListIdx >= 0) { popupScrTgt -= (int)(sy * 20); return true; }
//$$     scrollTarget -= (int) (sy * ROW_H_F / 2);
//#endif
        return true;
    }

    // ── mouse drag / release (scrollbar) ─────────────────────────
//#if MC < 12111
    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        return handleMouseDragged((int) mx, (int) my, btn);
    }
    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        return handleMouseReleased();
    }
//#elseif MC < 260100
//$$ @Override
//$$ public boolean mouseDragged(net.minecraft.client.gui.Click click, double dx, double dy) {
//$$     return handleMouseDragged((int) click.x(), (int) click.y(), click.button());
//$$ }
//$$ @Override
//$$ public boolean mouseReleased(net.minecraft.client.gui.Click click) {
//$$     return handleMouseReleased();
//$$ }
//#else
//$$ @Override
//$$ public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
//$$     return handleMouseDragged((int) event.x(), (int) event.y(), event.button());
//$$ }
//$$ @Override
//$$ public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
//$$     return handleMouseReleased();
//$$ }
//#endif

    private boolean handleMouseDragged(int imx, int imy, int btn) {
        if (btn != 0 || !scrollbarDragging) return false;
        int[] sb = scrollbarMetrics();
        if (sb != null) dragScrollbarTo(imy, sb);
        return true;
    }

    private boolean handleMouseReleased() {
        if (!scrollbarDragging) return false;
        scrollbarDragging = false;
        return true;
    }

    // ── keyboard ─────────────────────────────────────────────────
//#if MC < 12111
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return handleKeyPress(keyCode);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return handleKeyRelease(keyCode);
    }
//#elseif MC < 260100
//$$ @Override
//$$ public boolean keyPressed(net.minecraft.client.input.KeyInput key) {
//$$     return handleKeyPress(key.key());
//$$ }
//$$ @Override
//$$ public boolean keyReleased(net.minecraft.client.input.KeyInput key) {
//$$     return handleKeyRelease(key.key());
//$$ }
//#else
//$$ @Override
//$$ public boolean keyPressed(net.minecraft.client.input.KeyEvent key) {
//$$     return handleKeyPress(key.key());
//$$ }
//$$ @Override
//$$ public boolean keyReleased(net.minecraft.client.input.KeyEvent key) {
//$$     return handleKeyRelease(key.key());
//$$ }
//#endif

    private boolean handleKeyPress(int keyCode) {
        if (dragMode || progressDragMode) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { dragMode = false; progressDragMode = false; return true; }
            return false;
        }
        if (colorPickerIdx >= 0) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE    -> { closeColorPicker(); return true; }
                case GLFW.GLFW_KEY_ENTER,
                     GLFW.GLFW_KEY_KP_ENTER  -> { commitColorPicker(); return true; }
                case GLFW.GLFW_KEY_LEFT  -> { if (cpHexCursor > 0) cpHexCursor--; return true; }
                case GLFW.GLFW_KEY_RIGHT -> { if (cpHexCursor < cpHexInput.length()) cpHexCursor++; return true; }
                case GLFW.GLFW_KEY_HOME  -> { cpHexCursor = 0; return true; }
                case GLFW.GLFW_KEY_END   -> { cpHexCursor = cpHexInput.length(); return true; }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (cpHexCursor > 0) {
                        cpHexInput = cpHexInput.substring(0, cpHexCursor - 1) + cpHexInput.substring(cpHexCursor);
                        cpHexCursor--;
                        syncRGBAFromHex();
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (cpHexCursor < cpHexInput.length()) {
                        cpHexInput = cpHexInput.substring(0, cpHexCursor) + cpHexInput.substring(cpHexCursor + 1);
                        syncRGBAFromHex();
                    }
                    return true;
                }
                default -> {
//#if MC >= 12111
//$$                     handleColorHexInput(keyCode);
//#endif
                    return true;
                }
            }
        }
        if (numEditCfg != null) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE    -> { cancelNumEdit(); return true; }
                case GLFW.GLFW_KEY_ENTER,
                     GLFW.GLFW_KEY_KP_ENTER  -> { commitNumEdit(); return true; }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (numEditCursor > 0) {
                        numEditStr = numEditStr.substring(0, numEditCursor - 1) + numEditStr.substring(numEditCursor);
                        numEditCursor--;
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_DELETE    -> {
                    if (numEditCursor < numEditStr.length()) {
                        numEditStr = numEditStr.substring(0, numEditCursor) + numEditStr.substring(numEditCursor + 1);
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_LEFT  -> { if (numEditCursor > 0) numEditCursor--; return true; }
                case GLFW.GLFW_KEY_RIGHT -> { if (numEditCursor < numEditStr.length()) numEditCursor++; return true; }
                case GLFW.GLFW_KEY_HOME  -> { numEditCursor = 0; return true; }
                case GLFW.GLFW_KEY_END   -> { numEditCursor = numEditStr.length(); return true; }
                default -> {
//#if MC >= 12111
//$$                     handleNumEditCharInput(keyCode);
//#endif
                    return true;
                }
            }
        }
        if (popupListIdx >= 0) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE    -> { closePopup(); return true; }
                case GLFW.GLFW_KEY_ENTER,
                     GLFW.GLFW_KEY_KP_ENTER  -> { addPopupItem(); return true; }
                case GLFW.GLFW_KEY_BACKSPACE -> { if (popupCursor > 0) { popupInput = popupInput.substring(0, popupCursor - 1) + popupInput.substring(popupCursor); popupCursor--; } return true; }
                case GLFW.GLFW_KEY_DELETE    -> { if (popupCursor < popupInput.length()) { popupInput = popupInput.substring(0, popupCursor) + popupInput.substring(popupCursor + 1); } return true; }
                case GLFW.GLFW_KEY_LEFT      -> { if (popupCursor > 0) popupCursor--; return true; }
                case GLFW.GLFW_KEY_RIGHT     -> { if (popupCursor < popupInput.length()) popupCursor++; return true; }
                case GLFW.GLFW_KEY_HOME      -> { popupCursor = 0; return true; }
                case GLFW.GLFW_KEY_END       -> { popupCursor = popupInput.length(); return true; }
                default -> {
//#if MC >= 12111
//$$                     handlePopupCharInput(keyCode);
//#endif
                    return true;
                }
            }
        }
        if (recTab >= 0) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (recTab == 0) Feature.values()[recRow].config.getHotkey().setFromString("");
                else if (recTab == 2) HK_CFGS[recRow].getHotkey().setFromString("");
                stopRec(); return true;
            }
            heldKeys.add(keyCode);
            if (!isMod(keyCode)) {
                pendingCombo = buildCombo(heldKeys);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = ""; searchCursor = 0;
                scrollTarget = 0; scrollOffsetF = 0f;
                return true;
            }
            doClose(); return true;
        }
        // search bar input
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (searchCursor > 0) {
                    searchQuery = searchQuery.substring(0, searchCursor - 1) + searchQuery.substring(searchCursor);
                    searchCursor--;
                    scrollTarget = 0; scrollOffsetF = 0f;
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (searchCursor < searchQuery.length()) {
                    searchQuery = searchQuery.substring(0, searchCursor) + searchQuery.substring(searchCursor + 1);
                    scrollTarget = 0; scrollOffsetF = 0f;
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT  -> { if (searchCursor > 0) searchCursor--; return true; }
            case GLFW.GLFW_KEY_RIGHT -> { if (searchCursor < searchQuery.length()) searchCursor++; return true; }
            case GLFW.GLFW_KEY_HOME  -> { searchCursor = 0; return true; }
            case GLFW.GLFW_KEY_END   -> { searchCursor = searchQuery.length(); return true; }
            default -> {
//#if MC >= 12111
//$$                 handleSearchCharInput(keyCode);
//#endif
            }
        }
        return false;
    }

    private boolean handleKeyRelease(int keyCode) {
        if (recTab < 0) return false;
        heldKeys.remove(keyCode);
        if (heldKeys.isEmpty() && pendingCombo != null) {
            if (recTab == 0) Feature.values()[recRow].config.getHotkey().setFromString(pendingCombo);
            else if (recTab == 2) HK_CFGS[recRow].getHotkey().setFromString(pendingCombo);
            stopRec();
        }
        return false;
    }

    private String buildCombo(java.util.Set<Integer> keys) {
        List<String> mods = new ArrayList<>(), regs = new ArrayList<>();
        for (int k : keys) {
            String n = keyName(k);
            if (n == null) continue;
            if (isMod(k)) mods.add(n); else regs.add(n);
        }
        mods.addAll(regs);
        return String.join(",", mods);
    }

    private static boolean isMod(int k) {
        return k == GLFW.GLFW_KEY_LEFT_SHIFT  || k == GLFW.GLFW_KEY_RIGHT_SHIFT
            || k == GLFW.GLFW_KEY_LEFT_CONTROL || k == GLFW.GLFW_KEY_RIGHT_CONTROL
            || k == GLFW.GLFW_KEY_LEFT_ALT     || k == GLFW.GLFW_KEY_RIGHT_ALT;
    }

    private static String keyName(int c) {
        if (c >= GLFW.GLFW_KEY_A && c <= GLFW.GLFW_KEY_Z)
            return String.valueOf((char)('A' + c - GLFW.GLFW_KEY_A));
        if (c >= GLFW.GLFW_KEY_0 && c <= GLFW.GLFW_KEY_9)
            return String.valueOf((char)('0' + c - GLFW.GLFW_KEY_0));
        if (c >= GLFW.GLFW_KEY_F1 && c <= GLFW.GLFW_KEY_F12)
            return "F" + (1 + c - GLFW.GLFW_KEY_F1);
        if (c >= GLFW.GLFW_KEY_KP_0 && c <= GLFW.GLFW_KEY_KP_9)
            return "KP_" + (c - GLFW.GLFW_KEY_KP_0);
        if (c == GLFW.GLFW_KEY_LEFT_SHIFT)    return "LSHIFT";
        if (c == GLFW.GLFW_KEY_LEFT_CONTROL)  return "LCTRL";
        if (c == GLFW.GLFW_KEY_LEFT_ALT)      return "LALT";
        if (c == GLFW.GLFW_KEY_RIGHT_SHIFT)   return "RIGHT_SHIFT";
        if (c == GLFW.GLFW_KEY_RIGHT_CONTROL) return "RIGHT_CONTROL";
        if (c == GLFW.GLFW_KEY_RIGHT_ALT)     return "RIGHT_ALT";
        if (c == GLFW.GLFW_KEY_SPACE)         return "SPACE";
        if (c == GLFW.GLFW_KEY_ENTER)         return "ENTER";
        if (c == GLFW.GLFW_KEY_TAB)           return "TAB";
        if (c == GLFW.GLFW_KEY_UP)            return "UP";
        if (c == GLFW.GLFW_KEY_DOWN)          return "DOWN";
        if (c == GLFW.GLFW_KEY_LEFT)          return "LEFT";
        if (c == GLFW.GLFW_KEY_RIGHT)         return "RIGHT";
        if (c == GLFW.GLFW_KEY_HOME)          return "HOME";
        if (c == GLFW.GLFW_KEY_END)           return "END";
        if (c == GLFW.GLFW_KEY_INSERT)        return "INSERT";
        if (c == GLFW.GLFW_KEY_DELETE)        return "DELETE";
        if (c == GLFW.GLFW_KEY_PAGE_UP)       return "PAGE_UP";
        if (c == GLFW.GLFW_KEY_PAGE_DOWN)     return "PAGE_DOWN";
        if (c == GLFW.GLFW_KEY_MINUS)         return "MINUS";
        if (c == GLFW.GLFW_KEY_EQUAL)         return "EQUAL";
        if (c == GLFW.GLFW_KEY_BACKSLASH)     return "BACKSLASH";
        if (c == GLFW.GLFW_KEY_SEMICOLON)     return "SEMICOLON";
        if (c == GLFW.GLFW_KEY_APOSTROPHE)    return "APOSTROPHE";
        if (c == GLFW.GLFW_KEY_COMMA)         return "COMMA";
        if (c == GLFW.GLFW_KEY_PERIOD)        return "PERIOD";
        if (c == GLFW.GLFW_KEY_SLASH)         return "SLASH";
        if (c == GLFW.GLFW_KEY_GRAVE_ACCENT)  return "GRAVE_ACCENT";
        if (c == GLFW.GLFW_KEY_LEFT_BRACKET)  return "LEFT_BRACKET";
        if (c == GLFW.GLFW_KEY_RIGHT_BRACKET) return "RIGHT_BRACKET";
        return null;
    }

    private void startRec(int tab, int row) { recTab = tab; recRow = row; heldKeys.clear(); pendingCombo = null; }
    private void stopRec()                  { recTab = -1; recRow = -1; heldKeys.clear(); pendingCombo = null; }

    // ── char typed (text input for popup, MC < 12111 only) ──────
//#if MC < 12111
    @Override
    public boolean charTyped(char c, int modifiers) {
        if (colorPickerIdx >= 0) {
            char u = Character.toUpperCase(c);
            if ((u >= '0' && u <= '9') || (u >= 'A' && u <= 'F')) {
                cpHexInput = cpHexInput.substring(0, cpHexCursor) + u + cpHexInput.substring(cpHexCursor);
                cpHexCursor++;
                syncRGBAFromHex();
            }
            return true;
        }
        if (numEditCfg != null) {
            if (c >= '0' && c <= '9' || c == '-' || c == '.') {
                numEditStr = numEditStr.substring(0, numEditCursor) + c + numEditStr.substring(numEditCursor);
                numEditCursor++;
            }
            return true;
        }
        if (popupListIdx >= 0 && c >= 32) {
            popupInput = popupInput.substring(0, popupCursor) + c + popupInput.substring(popupCursor);
            popupCursor++;
            return true;
        }
        // search input
        if (c >= 32) {
            searchQuery = searchQuery.substring(0, searchCursor) + c + searchQuery.substring(searchCursor);
            searchCursor++;
            scrollTarget = 0; scrollOffsetF = 0f;
            return true;
        }
        return false;
    }
//#endif

    // ── key → char mapping for MC >= 12111 (no charTyped) ───────
    private void handlePopupCharInput(int keyCode) {
//#if MC < 260100
        long win = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
//#else
//$$ long win = net.minecraft.client.Minecraft.getInstance().getWindow().handle();
//#endif
        boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT)  == GLFW.GLFW_PRESS
                     || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        char c = popupKeyToChar(keyCode, shift);
        if (c != 0) {
            popupInput = popupInput.substring(0, popupCursor) + c + popupInput.substring(popupCursor);
            popupCursor++;
        }
    }

    private static char popupKeyToChar(int keyCode, boolean shift) {
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z)
            return (char)((shift ? 'A' : 'a') + keyCode - GLFW.GLFW_KEY_A);
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9 && !shift)
            return (char)('0' + keyCode - GLFW.GLFW_KEY_0);
        if (keyCode == GLFW.GLFW_KEY_MINUS)     return shift ? '_' : '-';
        if (keyCode == GLFW.GLFW_KEY_SEMICOLON) return shift ? ':' : ';';
        if (keyCode == GLFW.GLFW_KEY_PERIOD)    return '.';
        if (keyCode == GLFW.GLFW_KEY_SLASH)     return '/';
        if (keyCode == GLFW.GLFW_KEY_SPACE)     return ' ';
        return 0;
    }

    // ── numeric inline edit helpers ──────────────────────────────
    private void openNumEdit(IConfig<?> cfg) {
        numEditCfg = cfg;
        if (cfg instanceof IntegerConfig ic) numEditStr = String.valueOf(ic.getIntegerValue());
        else if (cfg instanceof DoubleConfig dbl) numEditStr = String.format("%.2f", dbl.getDoubleValue());
        numEditCursor = numEditStr.length();
    }

    private void commitNumEdit() {
        if (numEditCfg == null) return;
        if (numEditCfg instanceof IntegerConfig ic) {
            try { ic.setValue(Integer.parseInt(numEditStr)); } catch (NumberFormatException ignored) {}
        } else if (numEditCfg instanceof DoubleConfig dbl) {
            try { dbl.setValue(Double.parseDouble(numEditStr)); } catch (NumberFormatException ignored) {}
        }
        numEditCfg = null; numEditStr = ""; numEditCursor = 0;
    }

    private void cancelNumEdit() { numEditCfg = null; numEditStr = ""; numEditCursor = 0; }

    private void handleNumEditCharInput(int keyCode) {
//#if MC < 260100
        long win = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
//#else
//$$ long win = net.minecraft.client.Minecraft.getInstance().getWindow().handle();
//#endif
        boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                     || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        char c = numericKeyToChar(keyCode, shift);
        if (c != 0) {
            numEditStr = numEditStr.substring(0, numEditCursor) + c + numEditStr.substring(numEditCursor);
            numEditCursor++;
        }
    }

    private static char numericKeyToChar(int keyCode, boolean shift) {
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9 && !shift)
            return (char)('0' + keyCode - GLFW.GLFW_KEY_0);
        if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)
            return (char)('0' + keyCode - GLFW.GLFW_KEY_KP_0);
        if ((keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) && !shift) return '-';
        if (keyCode == GLFW.GLFW_KEY_PERIOD || keyCode == GLFW.GLFW_KEY_KP_DECIMAL) return '.';
        return 0;
    }

    // ── color picker helpers ─────────────────────────────────────
    private void openColorPicker(int idx) {
        colorPickerIdx = idx;
        int argb = ((ColorConfig) optionEntries.get(idx).cfg()).getValue();
        cpA = (argb >>> 24) & 0xFF;
        cpR = (argb >> 16)  & 0xFF;
        cpG = (argb >> 8)   & 0xFF;
        cpB =  argb         & 0xFF;
        cpHexInput  = String.format("%02X%02X%02X%02X", cpA, cpR, cpG, cpB);
        cpHexCursor = cpHexInput.length();
    }

    private void closeColorPicker()  { colorPickerIdx = -1; }

    private void commitColorPicker() {
        if (colorPickerIdx < 0) return;
        syncRGBAFromHex();
        ((ColorConfig) optionEntries.get(colorPickerIdx).cfg())
                .setValue((cpA << 24) | (cpR << 16) | (cpG << 8) | cpB);
        colorPickerIdx = -1;
    }

    private void syncRGBAFromHex() {
        String h = cpHexInput.toUpperCase().replaceAll("[^0-9A-F]", "");
        if (h.length() == 6) h = "FF" + h;
        if (h.length() >= 8) {
            try {
                int v = (int) Long.parseLong(h.substring(0, 8), 16);
                cpA = (v >>> 24) & 0xFF;
                cpR = (v >> 16)  & 0xFF;
                cpG = (v >> 8)   & 0xFF;
                cpB =  v         & 0xFF;
            } catch (NumberFormatException ignored) {}
        }
    }

    private void handleColorHexInput(int keyCode) {
//#if MC < 260100
        long win = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
//#else
//$$ long win = net.minecraft.client.Minecraft.getInstance().getWindow().handle();
//#endif
        boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                     || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        char c = colorHexKeyToChar(keyCode, shift);
        if (c != 0) {
            cpHexInput = cpHexInput.substring(0, cpHexCursor) + c + cpHexInput.substring(cpHexCursor);
            cpHexCursor++;
            syncRGBAFromHex();
        }
    }

    private static char colorHexKeyToChar(int keyCode, boolean shift) {
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9 && !shift)
            return (char)('0' + keyCode - GLFW.GLFW_KEY_0);
        if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)
            return (char)('0' + keyCode - GLFW.GLFW_KEY_KP_0);
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_F)
            return (char)('A' + keyCode - GLFW.GLFW_KEY_A);
        return 0;
    }

    private void handleSearchCharInput(int keyCode) {
//#if MC < 260100
        long win = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
//#else
//$$ long win = net.minecraft.client.Minecraft.getInstance().getWindow().handle();
//#endif
        boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                     || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        char c = popupKeyToChar(keyCode, shift);
        if (c != 0) {
            searchQuery = searchQuery.substring(0, searchCursor) + c + searchQuery.substring(searchCursor);
            searchCursor++;
            scrollTarget = 0; scrollOffsetF = 0f;
        }
    }

    // ── popup list editor helpers ────────────────────────────────
    private void openPopup(int idx) {
        popupListIdx = idx;
        popupInput   = "";
        popupCursor  = 0;
        popupScrTgt  = 0;
        popupScrF    = 0f;
    }

    private void closePopup() {
        popupListIdx = -1;
        popupInput   = "";
        popupCursor  = 0;
    }

    private void addPopupItem() {
        if (popupInput.isBlank()) return;
        StringListConfig slc = (StringListConfig) optionEntries.get(popupListIdx).cfg();
        List<String> items = new ArrayList<>(slc.getStrings());
        items.add(popupInput.trim());
        slc.setValue(items);
        popupInput  = "";
        popupCursor = 0;
        // scroll to bottom to reveal new item
        popupScrTgt = Integer.MAX_VALUE;
    }

    private void deletePopupItem(int idx) {
        StringListConfig slc = (StringListConfig) optionEntries.get(popupListIdx).cfg();
        List<String> items = new ArrayList<>(slc.getStrings());
        if (idx >= 0 && idx < items.size()) items.remove(idx);
        slc.setValue(items);
    }

    private void handlePopupClick(int mx, int my) {
        int pw = Math.min(340, width - 40), ph = Math.min(320, height - 60);
        int px = (width - pw) / 2, py = (height - ph) / 2;
        // close button
        int closeBtnX = px + pw - 22, closeBtnY = py + 4;
        if (mx >= closeBtnX && mx < closeBtnX + 18 && my >= closeBtnY && my < closeBtnY + 18) {
            closePopup(); return;
        }
        // list items
        StringListConfig slc = (StringListConfig) optionEntries.get(popupListIdx).cfg();
        List<String> items = slc.getStrings();
        int ITEM_H = 20, titleH = 26, inputH = 24;
        int listY0 = py + titleH + 2;
        int listH0 = ph - titleH - 2 - inputH - 6;
        int scroll = Math.round(popupScrF);
        for (int i = 0; i < items.size(); i++) {
            int iy = listY0 + i * ITEM_H - scroll;
            if (iy + ITEM_H <= listY0 || iy >= listY0 + listH0) continue;
            if (my >= iy && my < iy + ITEM_H) {
                int delX = px + pw - 26;
                if (mx >= delX && mx < delX + 18) deletePopupItem(i);
                return;
            }
        }
        // click outside → close
        if (mx < px || mx >= px + pw || my < py || my >= py + ph) closePopup();
    }

    // ── color picker draw & click ────────────────────────────────
    private void drawColorPickerPopup(DrawCtx dc, int mx, int my) {
        if (colorPickerIdx < 0) return;
        int pw = Math.min(300, width - 40);
        int ph = 220;
        int px = (width - pw) / 2, py = (height - ph) / 2;

        dc.fill(0, 0, width, height, 0xA0000010);
        dc.fill(px, py, px + pw, py + ph, C_PANEL);
        dc.fill(px + 1, py, px + pw - 1, py + 1, C_ACCENT);
        border(dc, px, py, pw, ph);

        int titleH = 26;
        String label = optionEntries.get(colorPickerIdx).label();
        dc.fill(px, py, px + pw, py + titleH, 0xFF0A0A1E);
        dc.fill(px, py + titleH - 1, px + pw, py + titleH, C_HDR_LINE);
        dc.text("Color: " + label, px + 8, py + (titleH - 8) / 2, C_WHITE);
        int closeBtnX = px + pw - 22, closeBtnY = py + 4;
        boolean closeHov = mx >= closeBtnX && mx < closeBtnX + 18 && my >= closeBtnY && my < closeBtnY + 18;
        dc.fill(closeBtnX, closeBtnY, closeBtnX + 18, closeBtnY + 18, closeHov ? 0xFF5A1A1A : 0xFF2A0808);
        border(dc, closeBtnX, closeBtnY, 18, 18);
        dc.text("X", closeBtnX + (18 - dc.tw("X")) / 2, closeBtnY + 5, C_OFF_TXT);

        int bodyY = py + titleH + 8;
        int swW = 46, swH = 46;
        int argbPreview = (cpA << 24) | (cpR << 16) | (cpG << 8) | cpB;
        dc.fill(px + 8, bodyY, px + 8 + swW, bodyY + swH, 0xFF888888);
        for (int ty = 0; ty < swH; ty += 8)
            for (int tx = 0; tx < swW; tx += 8)
                if (((tx / 8 + ty / 8) % 2) == 0)
                    dc.fill(px + 8 + tx, bodyY + ty, px + 8 + Math.min(tx + 8, swW), bodyY + Math.min(ty + 8, swH), 0xFFCCCCCC);
        dc.fill(px + 8, bodyY, px + 8 + swW, bodyY + swH, argbPreview);
        border(dc, px + 8, bodyY, swW, swH);

        int hexX = px + 8 + swW + 8, hexY = bodyY + (swH - 16) / 2;
        int hexW = pw - swW - 32;
        dc.fill(hexX, hexY, hexX + hexW, hexY + 16, C_KEY);
        border(dc, hexX, hexY, hexW, 16);
        String displayHex = cpHexInput.isEmpty() ? "AARRGGBB" : cpHexInput;
        int hexColor = cpHexInput.isEmpty() ? C_DIM : C_WHITE;
        dc.text("#" + displayHex, hexX + 4, hexY + 4, hexColor);
        int cursorX = hexX + 4 + dc.tw("#" + cpHexInput.substring(0, cpHexCursor));
        if ((System.currentTimeMillis() / 530) % 2 == 0)
            dc.fill(cursorX, hexY + 2, cursorX + 1, hexY + 14, C_WHITE);

        int sliderStartY = bodyY + swH + 8;
        String[] chLabels = { "R", "G", "B", "A" };
        int[]    chVals   = { cpR, cpG, cpB, cpA };
        int[]    chColors = { 0xFFFF4444, 0xFF44FF44, 0xFF4488FF, 0xFFAAAAAA };
        int sliderX = px + 16, sliderW = pw - 60, sliderH = 8;
        for (int i = 0; i < 4; i++) {
            int sy = sliderStartY + i * 18;
            dc.text(chLabels[i], px + 6, sy, C_DIM);
            dc.fill(sliderX, sy, sliderX + sliderW, sy + sliderH, C_KEY);
            int fw = chVals[i] * sliderW / 255;
            if (fw > 0) {
                dc.fill(sliderX, sy, sliderX + fw, sy + sliderH, chColors[i]);
                dc.fill(sliderX, sy, sliderX + fw, sy + 1, 0x40FFFFFF);
            }
            dc.fill(sliderX + fw, sy, sliderX + fw + 2, sy + sliderH, C_WHITE);
            border(dc, sliderX, sy, sliderW, sliderH);
            dc.text(String.valueOf(chVals[i]), sliderX + sliderW + 4, sy, C_GRAY);
        }

        int btnY = py + ph - 26;
        int cancelX = px + pw / 2 - 90, okX = px + pw / 2 + 10, bW = 72, bH = 18;
        boolean cancelHov = mx >= cancelX && mx < cancelX + bW && my >= btnY && my < btnY + bH;
        boolean okHov     = mx >= okX     && mx < okX + bW     && my >= btnY && my < btnY + bH;
        dc.fill(cancelX, btnY, cancelX + bW, btnY + bH, cancelHov ? C_BTN_HOV : C_BTN);
        border(dc, cancelX, btnY, bW, bH);
        dc.text("Cancel", cancelX + (bW - dc.tw("Cancel")) / 2, btnY + 5, C_GRAY);
        dc.fill(okX, btnY, okX + bW, btnY + bH, okHov ? C_BTN_HOV : C_BTN);
        dc.fill(okX + 1, btnY, okX + bW - 1, btnY + 1, okHov ? C_ACCENT_LT : C_BORDER_LT);
        border(dc, okX, btnY, bW, bH);
        dc.text("OK", okX + (bW - dc.tw("OK")) / 2, btnY + 5, C_WHITE);
    }

    private void handleColorPickerClick(int mx, int my) {
        int pw = Math.min(300, width - 40), ph = 220;
        int px = (width - pw) / 2, py = (height - ph) / 2;
        int titleH = 26, bodyY = py + titleH + 8, swH = 46;
        int sliderStartY = bodyY + swH + 8;
        int sliderX = px + 16, sliderW = pw - 60, sliderH = 8;

        // close button
        if (mx >= px + pw - 22 && mx < px + pw - 4 && my >= py + 4 && my < py + 22) {
            closeColorPicker(); return;
        }
        // RGBA sliders
        for (int i = 0; i < 4; i++) {
            int sy = sliderStartY + i * 18;
            if (mx >= sliderX && mx < sliderX + sliderW && my >= sy && my < sy + sliderH) {
                int val = Math.max(0, Math.min(255, (mx - sliderX) * 255 / sliderW));
                switch (i) { case 0 -> cpR = val; case 1 -> cpG = val; case 2 -> cpB = val; case 3 -> cpA = val; }
                cpHexInput = String.format("%02X%02X%02X%02X", cpA, cpR, cpG, cpB);
                cpHexCursor = cpHexInput.length();
                return;
            }
        }
        // hex field focus
        int swW = 46, hexX = px + 8 + swW + 8, hexY = bodyY + (swH - 16) / 2;
        int hexW = pw - swW - 32;
        if (mx >= hexX && mx < hexX + hexW && my >= hexY && my < hexY + 16) return;
        // buttons
        int btnY = py + ph - 26, bW = 72, bH = 18;
        int cancelX = px + pw / 2 - 90, okX = px + pw / 2 + 10;
        if (mx >= cancelX && mx < cancelX + bW && my >= btnY && my < btnY + bH) { closeColorPicker(); return; }
        if (mx >= okX     && mx < okX + bW     && my >= btnY && my < btnY + bH) { commitColorPicker(); return; }
        // click outside panel → close
        if (mx < px || mx >= px + pw || my < py || my >= py + ph) closeColorPicker();
    }

    // ── popup list editor draw ───────────────────────────────────
    private void drawListEditorPopup(DrawCtx dc, int mx, int my) {
        if (popupListIdx < 0) return;
        StringListConfig slc = (StringListConfig) optionEntries.get(popupListIdx).cfg();
        List<String> items = slc.getStrings();
        String label = optionEntries.get(popupListIdx).label();

        int pw = Math.min(340, width - 40), ph = Math.min(320, height - 60);
        int px = (width - pw) / 2, py = (height - ph) / 2;

        // dimmed overlay
        dc.fill(0, 0, width, height, 0xA0000010);

        // panel
        dc.fill(px, py, px + pw, py + ph, C_PANEL);
        dc.fill(px + 1, py, px + pw - 1, py + 1, C_ACCENT);
        border(dc, px, py, pw, ph);

        // title bar
        int titleH = 26;
        dc.fill(px, py, px + pw, py + titleH, 0xFF0A0A1E);
        dc.fill(px, py + titleH - 1, px + pw, py + titleH, C_HDR_LINE);
        dc.text("Edit: " + label, px + 8, py + (titleH - 8) / 2, C_WHITE);

        // close button
        int closeBtnX = px + pw - 22, closeBtnY = py + 4, closeS = 18;
        boolean closeHov = mx >= closeBtnX && mx < closeBtnX + closeS && my >= closeBtnY && my < closeBtnY + closeS;
        dc.fill(closeBtnX, closeBtnY, closeBtnX + closeS, closeBtnY + closeS, closeHov ? 0xFF5A1A1A : 0xFF2A0808);
        border(dc, closeBtnX, closeBtnY, closeS, closeS);
        dc.text("X", closeBtnX + (closeS - dc.tw("X")) / 2, closeBtnY + (closeS - 8) / 2, C_OFF_TXT);

        // list area
        int ITEM_H = 20, inputH = 24;
        int listY0 = py + titleH + 2;
        int listH0 = ph - titleH - 2 - inputH - 6;

        // smooth scroll
        popupScrF += (popupScrTgt - popupScrF) * 0.25f;
        if (Math.abs(popupScrF - popupScrTgt) < 0.5f) popupScrF = popupScrTgt;
        int maxScroll = Math.max(0, items.size() * ITEM_H - listH0);
        popupScrTgt = Math.max(0, Math.min(popupScrTgt, maxScroll));
        popupScrF   = Math.max(0, Math.min(popupScrF,   (float) maxScroll));
        int scroll  = Math.round(popupScrF);

        for (int i = 0; i < items.size(); i++) {
            int iy = listY0 + i * ITEM_H - scroll;
            if (iy + ITEM_H <= listY0 || iy >= listY0 + listH0) continue;
            boolean rowHov = mx >= px + 4 && mx < px + pw - 4 && my >= iy && my < iy + ITEM_H;
            dc.fill(px + 4, iy, px + pw - 4, iy + ITEM_H - 1, rowHov ? C_ROW_HOV : (i % 2 == 0 ? C_ROW : C_ROW_ALT));
            dc.fill(px + 4, iy + ITEM_H - 1, px + pw - 4, iy + ITEM_H, C_BORDER);
            // delete button
            int delX = px + pw - 26, delY = iy + 3, delW = 18, delH = 14;
            boolean delHov = mx >= delX && mx < delX + delW && my >= delY && my < delY + delH;
            dc.fill(delX, delY, delX + delW, delY + delH, delHov ? 0xFF5A1A1A : 0xFF2A0808);
            border(dc, delX, delY, delW, delH);
            dc.text("-", delX + (delW - dc.tw("-")) / 2, delY + 3, C_OFF_TXT);
            // item text (clipped by del button)
            dc.text(items.get(i), px + 8, iy + 6, C_WHITE);
        }
        if (items.isEmpty()) {
            dc.text("(empty)", px + (pw - dc.tw("(empty)")) / 2, listY0 + 10, C_DIM);
        }

        // input field at bottom
        int inputY = py + ph - inputH - 2;
        dc.fill(px + 4, inputY, px + pw - 4, inputY + inputH, C_KEY);
        dc.fill(px + 4, inputY, px + pw - 4, inputY + 1, C_ACCENT);
        border(dc, px + 4, inputY, pw - 8, inputH);

        int textX = px + 8, textY = inputY + (inputH - 8) / 2;
        if (popupInput.isEmpty()) {
            dc.text("Type here and press Enter to add...", textX, textY, C_DIM);
        } else {
            dc.text(popupInput, textX, textY, C_WHITE);
        }
        // blinking cursor
        int cursorX = textX + dc.tw(popupInput.substring(0, popupCursor));
        if ((System.currentTimeMillis() / 530) % 2 == 0) {
            dc.fill(cursorX, textY - 1, cursorX + 1, textY + 9, C_WHITE);
        }

        // scrollbar for popup list
        if (items.size() * ITEM_H > listH0) {
            int sbX = px + pw - 6, sbW = 4;
            dc.fill(sbX, listY0, sbX + sbW, listY0 + listH0, C_SBAR_BG);
            int tH = Math.max(12, listH0 * listH0 / (items.size() * ITEM_H));
            int tY = listY0 + (int)((long)(listH0 - tH) * scroll / Math.max(1, maxScroll));
            dc.fill(sbX, tY, sbX + sbW, tY + tH, C_SBAR_FG);
        }
    }

    // ── hud position drag mode draw ──────────────────────────────
    private void renderDragMode(DrawCtx dc, int mx, int my) {
        if (!dragMode && !progressDragMode) return;

        if (progressDragMode) {
            dc.fill(0, 0, width, height, 0x70000010);

            // progress meter preview: fixed-size bar centered on cursor
            int PREV_W = 200, PREV_H = 30;
            int bx = Math.max(0, Math.min(mx - PREV_W / 2, width  - PREV_W));
            int by = Math.max(0, Math.min(my,               height - PREV_H));
            dc.fill(bx, by, bx + PREV_W, by + PREV_H, 0xC0000A1A);
            dc.fill(bx + 1, by, bx + PREV_W - 1, by + 1, C_ACCENT);
            border(dc, bx, by, PREV_W, PREV_H);

            String meterLabel = "AutoFill  [=====>    ] 60%";
            dc.text(meterLabel, bx + (PREV_W - dc.tw(meterLabel)) / 2, by + (PREV_H - 8) / 2, C_CYAN);

            String inst = "Click to place  |  ESC: cancel";
            dc.text(inst, (width - dc.tw(inst)) / 2, 8, C_GOLD);

            dc.fill(mx - 6, my,     mx + 7, my + 1, C_WHITE);
            dc.fill(mx,     my - 6, mx + 1, my + 7, C_WHITE);
            return;
        }

        // HUD log drag
        dc.fill(0, 0, width, height, 0x70000010);

        org.asutarisucu.lib.config.AlignMode align = Configs.Generic.HUD_LOG_ALIGN.getValue();
        int PREV_W = Configs.Generic.HUD_LOG_WIDTH.getIntegerValue();
        int PREV_H = 64;
        int bx = Math.max(0, Math.min(mx - PREV_W / 2, width  - PREV_W));
        int by = Math.max(0, Math.min(my - PREV_H / 2, height - PREV_H));

        dc.fill(bx, by, bx + PREV_W, by + PREV_H, 0xC0000A1A);
        dc.fill(bx + 1, by, bx + PREV_W - 1, by + 1, C_ACCENT);
        border(dc, bx, by, PREV_W, PREV_H);

        // example lines with alignment
        String[] lines = { "[ HUD Log ]", "Feature toggle notification", "Auto Restock / item messages", "...appear here" };
        int[] colors = { C_CYAN, C_ON_TXT, C_GRAY, C_DIM };
        for (int i = 0; i < lines.length; i++) {
            int lw = dc.tw(lines[i]);
            int lx = switch (align) {
                case LEFT   -> bx + 8;
                case CENTER -> bx + (PREV_W - lw) / 2;
                case RIGHT  -> bx + PREV_W - lw - 8;
            };
            dc.text(lines[i], lx, by + 6 + i * 14, colors[i]);
        }

        // alignment label + crosshair at anchor
        String alignLabel = "Align: " + align.name();
        dc.text(alignLabel, (width - dc.tw(alignLabel)) / 2, 20, C_GRAY);
        String inst = "Click to confirm  |  ESC: cancel";
        dc.text(inst, (width - dc.tw(inst)) / 2, 8, C_GOLD);

        // crosshair at cursor
        dc.fill(mx - 6, my,     mx + 7, my + 1,     C_WHITE);
        dc.fill(mx,     my - 6, mx + 1, my + 7,     C_WHITE);
    }

    // ── close ────────────────────────────────────────────────────
//#if MC < 260100
    @Override
    public void close() { doClose(); }
//#else
//$$ @Override
//$$ public void onClose() { doClose(); }
//#endif

    private void doClose() {
        org.asutarisucu.lib.config.ConfigManager.INSTANCE.save();
//#if MC < 260100
        this.client.setScreen(parent);
//#else
//$$ this.minecraft.setScreen(parent);
//#endif
    }
}
