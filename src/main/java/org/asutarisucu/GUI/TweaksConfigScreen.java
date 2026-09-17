package org.asutarisucu.GUI;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.Configs.Hotkeys;
import org.asutarisucu.GUI.ConfigEntries.Action;
import org.asutarisucu.GUI.ConfigEntries.Entry;
import org.asutarisucu.GUI.ConfigEntries.Kind;
import org.asutarisucu.GUI.glass.Anim;
import org.asutarisucu.GUI.glass.BackgroundImage;
import org.asutarisucu.GUI.glass.ColorWheel;
import org.asutarisucu.GUI.glass.Gfx;
import org.asutarisucu.GUI.glass.GlassRenderer;
import org.asutarisucu.GUI.glass.Lang;
import org.asutarisucu.GUI.glass.ListEditor;
import org.asutarisucu.GUI.glass.Suggestions;
import org.asutarisucu.GUI.glass.TextField;
import org.asutarisucu.GUI.glass.Widgets;
import org.asutarisucu.lib.config.*;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//#if MC < 12001
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
//#elseif MC < 260100
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gui.DrawContext;
//$$ import net.minecraft.client.gui.screen.Screen;
//$$ import net.minecraft.text.Text;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//$$ import net.minecraft.client.gui.screens.Screen;
//$$ import net.minecraft.network.chat.Component;
//#endif
//#if MC < 12005
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.asutarisucu.mixin.ConfigScreen.MixinTitleScreenPanorama;
//#endif

/**
 * The config screen: category tabs along the top, the panels of the chosen tab
 * on the right, details of the selected panel and the screen's own settings on
 * the left, all drawn as liquid glass over what is behind the screen.
 *
 * Opened with {@code cbrMode} it becomes the Clear Block Render screen: that
 * feature's settings on the left and a live preview on the right.
 */
public class TweaksConfigScreen extends Screen {

    private static final int M = 10, TOP_H = 22, GAP = 8, CARD_GAP = 4;
    private static final int FEATURE_H = 38, ROW_H = 28, SECTION_H = 18;
    private static final int RADIUS = 10;
    private static final float OPEN_MS = 480, CLOSE_MS = 460;

    private static final String VERSION = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getModContainer("asutantweaks")
            .map(c -> "v" + c.getMetadata().getVersion().getFriendlyString())
            .orElse("");

    private static final String[] TAB_KEYS = { "ui.tab.all", "ui.tab.features", "ui.tab.options", "ui.tab.hotkeys" };
    private static int lastTab = 0;
    /** null follows the space available; set once the player clicks the header. */
    private static Boolean appearanceCollapsed = null;
    private boolean appearCollapsedNow;

    private final Screen parent;
    private final boolean cbrMode;

    private int tab = lastTab;
    private float lensX = -1, lensW;

    private List<Entry> features, options, hotkeys, cbr;

    private final TextField search = TextField.plain();
    private Entry editEntry;
    private TextField editField = TextField.plain();

    private Entry selected;
    private float detailScroll, detailScrollTarget;
    private int detailMaxScroll;

    // list viewport and scrolling
    private int listX, listY, listW, listH, contentH;
    private float scroll, scrollTarget;
    private boolean scrollbarDrag;
    private int scrollbarGrab;

    // per-panel animation state
    private final Map<String, Float> rowY = new HashMap<>();
    private final Map<String, Float> knobs = new HashMap<>();
    private final Map<String, Float> hovers = new HashMap<>();
    private final Map<String, Integer> openOrder = new HashMap<>();

    // pressing and dragging
    private Entry pressEntry;
    private int pressX, pressY, grabDY, dragMouseY;
    private boolean dragging;
    private Entry sliderEntry;
    private int uiSlider = -1;

    // hotkey recording
    private Entry recEntry;
    private final LinkedHashSet<Integer> heldKeys = new LinkedHashSet<>();
    private String pendingCombo;

    // popups and placement
    private ColorWheel colorWheel;
    private ListEditor listEditor;
    private long popupAt;
    private Action placing = Action.NONE;

    // preview navigation (Clear Block Render mode)
    private int[] previewRect;
    private int previewDrag;
    private int previewLastX, previewLastY;

    // timing
    private long openedAt = -1, closingAt = -1, lastFrame;
    private float dt;
    private boolean finishQueued;

    private String toast;
    private boolean toastError;
    private long toastAt;

    private record Row(Entry entry, String header, int y, int h) {}
    private final List<Row> rows = new ArrayList<>();
    private List<Entry> dragSection;
    private int dropIndex = -1;

    // layout of the left column
    private int detailX, detailY, detailW, detailH;
    private int appearX, appearY, appearW, appearH;

    // ── constructors ─────────────────────────────────────────────

    public TweaksConfigScreen(Screen parent) {
        this(parent, false);
    }

    public TweaksConfigScreen(Screen parent, boolean cbrMode) {
//#if MC < 260100
        super(Text.literal(cbrMode ? "Clear Block Render" : "AsutanTweaks Config"));
//#else
//$$     super(Component.literal(cbrMode ? "Clear Block Render" : "AsutanTweaks Config"));
//#endif
        this.parent = parent;
        this.cbrMode = cbrMode;
        reloadEntries();
    }

    private void reloadEntries() {
        features = ConfigEntries.features();
        options = ConfigEntries.options();
        hotkeys = ConfigEntries.hotkeys();
        cbr = ConfigEntries.cbrOptions();
    }

    // ── version glue: rendering ──────────────────────────────────

    // Without a world the title panorama is drawn under the screen, and the
    // backdrop copies it the same way it copies the world.

//#if MC < 12005
    private static final Identifier PANORAMA_OVERLAY = new Identifier("textures/gui/title/background/panorama_overlay.png");
    private static RotatingCubeMapRenderer ownPanorama;

    /** The title screen's panorama when opened from it, so the view keeps turning from where it was. */
    private RotatingCubeMapRenderer titlePanorama() {
        if (parent instanceof TitleScreen title) {
            return ((MixinTitleScreenPanorama) title).asutantweaks$getBackgroundRenderer();
        }
        if (ownPanorama == null) ownPanorama = new RotatingCubeMapRenderer(TitleScreen.PANORAMA_CUBE_MAP);
        return ownPanorama;
    }
//#endif

//#if MC < 12001
    @Override
    public void render(MatrixStack ms, int mx, int my, float delta) {
        if (GlassRenderer.wantsPanorama(hasWorld())) {
            titlePanorama().render(delta, 1.0f);
            RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
            RenderSystem.enableBlend();
            drawTexture(ms, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128);
        }
        renderFrame(new Gfx(ms), mx, my);
    }
//#elseif MC < 12005
//$$ @Override
//$$ public void render(DrawContext c, int mx, int my, float delta) {
//$$     // No super.render: from 1.20.2 it draws the vanilla background over the screen.
//$$     if (GlassRenderer.wantsPanorama(hasWorld())) {
//$$         titlePanorama().render(delta, 1.0f);
//$$         RenderSystem.enableBlend();
//$$         c.drawTexture(PANORAMA_OVERLAY, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128);
//$$     }
//$$     renderFrame(new Gfx(c), mx, my);
//$$ }
//#elseif MC < 12111
//$$ @Override
//$$ public void render(DrawContext c, int mx, int my, float delta) {
//$$     // No super.render: it draws the vanilla background over the screen.
//$$     if (GlassRenderer.wantsPanorama(hasWorld())) renderPanoramaBackground(c, delta);
//$$     renderFrame(new Gfx(c), mx, my);
//$$ }
//#elseif MC < 260100
//$$ @Override
//$$ public void render(DrawContext c, int mx, int my, float delta) {
//$$     renderFrame(new Gfx(c), mx, my);
//$$ }
//$$
//$$ @Override
//$$ public void renderBackground(DrawContext c, int mx, int my, float delta) {
//$$     // The glass backdrop replaces the vanilla blur and darkening.
//$$     if (GlassRenderer.wantsPanorama(hasWorld())) renderPanoramaBackground(c, delta);
//$$ }
//#else
//$$ @Override
//$$ public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
//$$     renderFrame(new Gfx(g), mx, my);
//$$ }
//$$
//$$ @Override
//$$ public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
//$$     // The glass backdrop replaces the vanilla blur and darkening.
//$$     if (GlassRenderer.wantsPanorama(hasWorld())) extractPanorama(g, delta);
//$$ }
//#endif

    private static int framebufferWidth() {
//#if MC < 260100
        return MinecraftClient.getInstance().getWindow().getFramebufferWidth();
//#else
//$$ return Minecraft.getInstance().getWindow().getWidth();
//#endif
    }

    private static int framebufferHeight() {
//#if MC < 260100
        return MinecraftClient.getInstance().getWindow().getFramebufferHeight();
//#else
//$$ return Minecraft.getInstance().getWindow().getHeight();
//#endif
    }

    private static boolean hasWorld() {
//#if MC < 260100
        return MinecraftClient.getInstance().world != null;
//#else
//$$ return Minecraft.getInstance().level != null;
//#endif
    }

    /** Runs {@code r} on the client thread after the current frame, never on the spot. */
    private static void runOnClient(Runnable r) {
//#if MC < 260100
        MinecraftClient.getInstance().send(r);
//#else
//$$ Minecraft.getInstance().schedule(r);
//#endif
    }

    private void showScreen(Screen screen) {
//#if MC < 260100
        MinecraftClient.getInstance().setScreen(screen);
//#elseif MC < 260200
//$$ Minecraft.getInstance().setScreen(screen);
//#else
//$$ Minecraft.getInstance().gui.setScreen(screen);
//#endif
    }

    // ── frame ────────────────────────────────────────────────────

    private void renderFrame(Gfx g, int mx, int my) {
        long now = System.nanoTime();
        if (openedAt < 0) openedAt = now;
        dt = lastFrame == 0 ? 0 : Math.min(0.1f, (now - lastFrame) / 1e9f);
        lastFrame = now;

        float closeT = closingAt < 0 ? 0 : (now - closingAt) / 1e6f / CLOSE_MS;
        if (closingAt >= 0 && closeT >= 1 && !finishQueued) {
            finishQueued = true;
            runOnClient(this::finishClose);
        }

        GlassRenderer.beginFrame(framebufferWidth(), framebufferHeight());

        float openT = (now - openedAt) / 1e6f / OPEN_MS;
        float bgAlpha = closingAt >= 0
                ? 1 - Anim.easeInCubic((closeT - 0.3f) / 0.7f)
                : Anim.easeOutCubic(openT * 1.4f);
        g.alpha = bgAlpha;
        g.backdrop(0, 0, width, height, Configs.Ui.BACKGROUND_BLUR.getIntegerValue() / 100.0f,
                Configs.Ui.BACKGROUND_DIM.getIntegerValue() / 100.0f);
        g.alpha = 1;

        if (placing != Action.NONE) {
            drawPlacement(g, mx, my);
            return;
        }
        boolean popup = colorWheel != null || listEditor != null;
        int imx = popup || closingAt >= 0 ? -10000 : mx, imy = popup || closingAt >= 0 ? -10000 : my;

        layout();
        drawTopBar(g, imx, imy);
        if (cbrMode) {
            drawCbrSide(g, imx, imy);
        } else {
            drawDetails(g);
            drawAppearance(g, imx, imy);
            drawSearch(g);
        }
        drawList(g, imx, imy);

        if (popup) drawPopups(g, mx, my, now);
        drawToast(g, now);
    }

    private void layout() {
        int bodyY = M + TOP_H + GAP, bodyBottom = height - M;
        if (cbrMode) {
            int leftW = Math.max(170, Math.round((width - M * 2) * 0.46f));
            listX = M;
            listW = leftW;
            listY = bodyY;
            listH = bodyBottom - bodyY;
            return;
        }
        int leftW = Math.max(150, Math.min(280, Math.round(width * 0.32f)));
        int expandedH = 24 + APPEAR_ROWS * APPEAR_PITCH + 4;
        int bodyH = bodyBottom - bodyY;
        appearCollapsedNow = appearanceCollapsed != null ? appearanceCollapsed : bodyH < expandedH + GAP + 110;
        appearH = appearCollapsedNow ? 22 : expandedH;
        appearX = M;
        appearW = leftW;
        appearY = bodyBottom - appearH;
        detailX = M;
        detailW = leftW;
        detailY = bodyY;
        detailH = appearY - GAP - bodyY;
        if (detailH < 22) {
            // Not even the details header fits above the open settings: shrink the settings instead.
            detailH = 22;
            appearY = bodyY + detailH + GAP;
            appearH = bodyBottom - appearY;
        }

        listX = M + leftW + GAP;
        listW = width - M - listX - 6;
        listY = bodyY + 20 + 6;
        listH = bodyBottom - listY;
    }

    // ── animation helpers ────────────────────────────────────────

    /**
     * Applies a panel's share of the open or close animation.
     *
     * @return false when the panel is fully hidden and need not be drawn
     */
    private boolean beginPanel(Gfx g, float delayMs, float cx, float cy) {
        long now = System.nanoTime();
        float a, dy, s;
        if (closingAt >= 0) {
            float t = Anim.clamp01(((now - closingAt) / 1e6f - delayMs * 0.35f) / (CLOSE_MS * 0.8f));
            float e = Anim.easeInCubic(t);
            // Linear alpha: the glass melts away at an even pace.
            a = 1 - t;
            dy = e * 14;
            s = 1 + e * 0.015f;
        } else {
            float t = Anim.clamp01(((now - openedAt) / 1e6f - delayMs) / 420f);
            float e = Anim.easeOutBack(t);
            a = Anim.easeOutCubic(t);
            dy = (1 - e) * 18;
            s = 0.94f + 0.06f * e;
        }
        if (a <= 0.002f) return false;
        g.push();
        g.translate(0, dy);
        g.scaleAround(s, cx, cy);
        g.alpha = a;
        return true;
    }

    private void endPanel(Gfx g) {
        g.alpha = 1;
        g.pop();
    }

    private boolean closing() {
        return closingAt >= 0;
    }

    private float approachMap(Map<String, Float> map, String key, float target, float rate) {
        float cur = map.getOrDefault(key, target);
        float next = Anim.approach(cur, target, dt, rate);
        map.put(key, next);
        return next;
    }

    // ── top bar ──────────────────────────────────────────────────

    private void drawTopBar(Gfx g, int mx, int my) {
        if (!beginPanel(g, 0, width / 2f, M + TOP_H / 2f)) return;
        String title = cbrMode ? Lang.get("ui.cbr.title") : "AsutanTweaks";
        int[] tabs = cbrMode ? null : tabRects();
        int titleRoom = (tabs != null ? tabs[0] : width - M - TOP_H) - 8 - (M + 2);
        int titleW = g.width(title, true);
        // Drop the version first, then the title, when the tabs need the room.
        if (titleW <= titleRoom) {
            g.text(title, M + 2, M + 7, Widgets.TEXT, true, true);
            if (titleW + 6 + g.width(VERSION) <= titleRoom) {
                g.text(VERSION, M + 8 + titleW, M + 7, Widgets.TEXT_FAINT, false, false);
            }
        }

        if (!cbrMode) {
            int tx = tabs[0], tw = tabs[tabs.length - 1] - tx;
            Widgets.panel(g, tx - 3, M, tw + 6, TOP_H, TOP_H / 2, closing());
            int selX = tabs[tab] , selW = tabs[tab + 1] - tabs[tab];
            if (lensX < 0) {
                lensX = selX;
                lensW = selW;
            }
            lensX = Anim.approach(lensX, selX, dt, 16);
            lensW = Anim.approach(lensW, selW, dt, 16);
            int lx = Math.round(lensX), lw = Math.round(lensW);
            g.roundedSheen(lx, M + 3, lw, TOP_H - 6, (TOP_H - 6) / 2, 0x38FFFFFF);
            g.outline(lx, M + 3, lw, TOP_H - 6, (TOP_H - 6) / 2, 1, 0x50FFFFFF);
            for (int i = 0; i < TAB_KEYS.length; i++) {
                String s = Lang.get(TAB_KEYS[i]);
                boolean hov = Widgets.inside(mx, my, tabs[i], M, tabs[i + 1] - tabs[i], TOP_H);
                int c = i == tab ? Widgets.TEXT : (hov ? 0xE0FFFFFF : Widgets.TEXT_DIM);
                g.text(s, (tabs[i] + tabs[i + 1] - g.width(s)) / 2, M + 7, c, i == tab, false);
            }
        }

        int cx = width - M - TOP_H;
        boolean hov = Widgets.inside(mx, my, cx, M, TOP_H, TOP_H);
        Widgets.panel(g, cx, M, TOP_H, TOP_H, TOP_H / 2, closing());
        if (hov) g.rounded(cx, M, TOP_H, TOP_H, TOP_H / 2, 0x60FF5F57);
        g.text("×", cx + (TOP_H - g.width("×")) / 2, M + 7, Widgets.TEXT, false, false);
        endPanel(g);
    }

    /** x of each tab's left edge, plus the right edge of the last one. */
    private int[] tabRects() {
        int[] w = new int[TAB_KEYS.length];
        int total = 0;
        for (int i = 0; i < w.length; i++) {
            w[i] = Math.max(52, Gfx.textWidth(Lang.get(TAB_KEYS[i]), false) + 22);
            total += w[i];
        }
        int x = (width - total) / 2;
        // Keep clear of the title when there is room to the right.
        int titleEnd = M + 2 + Gfx.textWidth("AsutanTweaks", true) + 12;
        if (x < titleEnd && titleEnd + total <= width - M - TOP_H - 8) x = titleEnd;
        int[] out = new int[w.length + 1];
        for (int i = 0; i < w.length; i++) {
            out[i] = x;
            x += w[i];
        }
        out[w.length] = x;
        return out;
    }

    // ── search ───────────────────────────────────────────────────

    private void drawSearch(Gfx g) {
        int x = listX, y = M + TOP_H + GAP, w = listW;
        if (!beginPanel(g, 40, x + w / 2f, y + 10)) return;
        Widgets.panel(g, x, y, w, 20, 10, closing());
        Widgets.magnifier(g, x + 8, y + 5, Widgets.TEXT_DIM);
        boolean focused = editEntry == null && recEntry == null;
        search.draw(g, x + 24, y + 6, w - 34, Widgets.TEXT, Lang.get("ui.search"), Widgets.TEXT_FAINT, focused);
        endPanel(g);
    }

    // ── panel list ───────────────────────────────────────────────

    private List<List<Entry>> sections() {
        if (cbrMode) return List.of(cbr);
        return switch (tab) {
            case 1 -> List.of(features);
            case 2 -> List.of(options);
            case 3 -> List.of(hotkeys);
            default -> List.of(features, options, hotkeys);
        };
    }

    private static int heightOf(Entry e) {
        return e.kind == Kind.FEATURE ? FEATURE_H : ROW_H;
    }

    private void buildRows() {
        rows.clear();
        String q = cbrMode ? "" : search.text();
        int y = 0;
        List<List<Entry>> secs = sections();
        dropIndex = -1;
        float dragCenter = dragging ? dragMouseY - grabDY + heightOf(pressEntry) / 2f - listY + scroll : 0;
        for (List<Entry> sec : secs) {
            List<Entry> visible = new ArrayList<>();
            for (Entry e : sec) if (e.matches(q)) visible.add(e);
            if (visible.isEmpty()) continue;
            if (secs.size() > 1) {
                String header = Lang.get(switch (visible.get(0).kind) {
                    case FEATURE -> "ui.tab.features";
                    case OPTION -> "ui.tab.options";
                    case HOTKEY -> "ui.tab.hotkeys";
                });
                rows.add(new Row(null, header, y, SECTION_H));
                y += SECTION_H;
            }
            boolean dragHere = dragging && sec == dragSection;
            int index = 0;
            for (Entry e : visible) {
                if (dragHere && e == pressEntry) continue;
                int h = heightOf(e);
                if (dragHere && dropIndex < 0 && dragCenter < y + h / 2f) {
                    dropIndex = index;
                    y += heightOf(pressEntry) + CARD_GAP;
                }
                rows.add(new Row(e, null, y, h));
                y += h + CARD_GAP;
                index++;
            }
            if (dragHere && dropIndex < 0) {
                dropIndex = index;
                y += heightOf(pressEntry) + CARD_GAP;
            }
            y += 4;
        }
        contentH = y;
    }

    private void drawList(Gfx g, int mx, int my) {
        buildRows();
        int maxScroll = Math.max(0, contentH - listH);
        if (dragging) {
            // Scroll while a panel is held near either edge.
            if (dragMouseY < listY + 24) scrollTarget -= dt * 260;
            if (dragMouseY > listY + listH - 24) scrollTarget += dt * 260;
        }
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
        scroll = scrollbarDrag ? scrollTarget : Anim.approach(scroll, scrollTarget, dt, 16);

        if (rows.isEmpty() && !cbrMode) {
            String s = Lang.get("ui.no_results");
            g.text(s, listX + (listW - g.width(s)) / 2, listY + 20, Widgets.TEXT_FAINT, false, false);
        }

        g.scissor(listX - 8, listY, listX + listW + 8, listY + listH);
        int order = 0;
        for (Row row : rows) {
            String id = row.entry != null ? row.entry.id() : "H:" + row.header;
            float ry = approachMap(rowY, id, row.y, dragging ? 22 : 18);
            if (!openOrder.containsKey(id)) openOrder.put(id, order);
            order++;
            int y = Math.round(listY + ry - scroll);
            if (y + row.h < listY - 16 || y > listY + listH + 16) continue;
            float delay = 60 + Math.min(openOrder.get(id), 16) * 22;
            if (!beginPanel(g, delay, listX + listW / 2f, y + row.h / 2f)) continue;
            if (row.entry == null) {
                g.text(row.header, listX + 6, y + 6, Widgets.TEXT_DIM, true, true);
            } else {
                boolean inView = my >= listY && my < listY + listH;
                drawCard(g, row.entry, listX, y, listW, row.h, inView ? mx : -10000, inView ? my : -10000, false);
            }
            endPanel(g);
        }
        g.unscissor();

        if (maxScroll > 0) {
            int[] sb = scrollbar();
            boolean hov = Widgets.inside(mx, my, sb[0] - 3, listY, sb[2] + 6, listH);
            g.rounded(sb[0], sb[1], sb[2], sb[3], sb[2] / 2, hov || scrollbarDrag ? 0xA0FFFFFF : 0x50FFFFFF);
        }

        if (dragging && pressEntry != null) {
            int h = heightOf(pressEntry);
            int y = Math.max(listY - h / 2, Math.min(listY + listH - h / 2, dragMouseY - grabDY));
            g.push();
            g.raise();
            g.scaleAround(1.03f, listX + listW / 2f, y + h / 2f);
            g.shadow(listX, y + 6, listW, h, RADIUS, 24, 0x90000000);
            drawCard(g, pressEntry, listX, y, listW, h, -10000, -10000, true);
            g.pop();
            rowY.put(pressEntry.id(), (float) (y - listY) + scroll);
        }
    }

    /** {x, y, w, h} of the scrollbar thumb. */
    private int[] scrollbar() {
        int maxScroll = Math.max(1, contentH - listH);
        int thumbH = Math.max(18, listH * listH / Math.max(1, contentH));
        int thumbY = listY + Math.round((listH - thumbH) * (scroll / maxScroll));
        return new int[] { listX + listW + 2, thumbY, 3, thumbH };
    }

    private void drawCard(Gfx g, Entry e, int x, int y, int w, int h, int mx, int my, boolean lifted) {
        boolean hov = Widgets.inside(mx, my, x, y, w, h) && !dragging;
        float hv = approachMap(hovers, e.id(), hov ? 1 : 0, 14);
        Widgets.panel(g, x, y, w, h, RADIUS, closing());
        if (hv > 0.01f) g.rounded(x, y, w, h, RADIUS, Gfx.withAlpha(0xFFFFFF, 0.07f * hv));
        if (lifted) g.rounded(x, y, w, h, RADIUS, 0x14FFFFFF);
        if (e == selected) g.outline(x, y, w, h, RADIUS, 1, 0xC05AA9FF);
        else g.outline(x, y, w, h, RADIUS, 1, 0x18FFFFFF);
        Widgets.grip(g, x + 8, y + h / 2 - 4, hv > 0.5f || lifted ? Widgets.TEXT_DIM : Widgets.TEXT_FAINT);

        int nameX = x + 20;
        switch (e.kind) {
            case FEATURE -> {
                int tx = x + w - 10 - 28, ty = y + (h - 16) / 2;
                int kw = featureKeyWidth(w), kx = tx - 8 - kw;
                int textW = kx - 8 - nameX;
                g.text(g.ellipsize(e.name(), textW), nameX, y + 8, Widgets.TEXT, true, false);
                g.text(g.ellipsize(e.summary(), textW), nameX, y + 21, Widgets.TEXT_DIM, false, false);
                drawHotkeyChip(g, e, kx, ty, kw, 16, mx, my);
                float knob = approachMap(knobs, e.id(), e.feature.isEnabled() ? 1 : 0, 16);
                Widgets.toggle(g, tx, ty, knob, Widgets.inside(mx, my, tx, ty, 28, 16));
            }
            case OPTION -> {
                int[] c = controlRect(e, x, y, w, h);
                g.text(g.ellipsize(e.name(), c[0] - 8 - nameX), nameX, y + (h - 8) / 2, Widgets.TEXT, true, false);
                drawControl(g, e, c, mx, my);
            }
            case HOTKEY -> {
                int kw = Math.min(130, w / 2), kx = x + w - 10 - kw, ky = y + (h - 16) / 2;
                g.text(g.ellipsize(e.name(), kx - 8 - nameX), nameX, y + (h - 8) / 2, Widgets.TEXT, true, false);
                drawHotkeyChip(g, e, kx, ky, kw, 16, mx, my);
            }
        }
    }

    private static int featureKeyWidth(int panelW) {
        return Math.max(44, Math.min(78, Math.round(panelW * 0.2f)));
    }

    private void drawHotkeyChip(Gfx g, Entry e, int x, int y, int w, int h, int mx, int my) {
        boolean rec = recEntry == e;
        String s = rec ? liveKeyDisplay() : fmtKey(e.hotkey.getHotkey().getStorageString());
        Widgets.chip(g, x, y, w, h, s, rec ? 0xFF9FD0FF : Widgets.TEXT_DIM, Widgets.inside(mx, my, x, y, w, h), rec);
    }

    /** {x, y, w, h} of an option's control. */
    private static int[] controlRect(Entry e, int x, int y, int w, int h) {
        int cw = Math.min(170, Math.round((w - 20) * 0.5f));
        if (e.config instanceof BooleanConfig) cw = 28;
        return new int[] { x + w - 10 - cw, y + (h - 16) / 2, cw, 16 };
    }

    private void drawControl(Gfx g, Entry e, int[] c, int mx, int my) {
        int x = c[0], y = c[1], w = c[2], h = c[3];
        boolean hov = Widgets.inside(mx, my, x, y, w, h);
        IConfig<?> cfg = e.config;
        if (e.action != Action.NONE) {
            Widgets.button(g, x, y, w, h, Lang.get("ui.set_position"), mx, my, false);
        } else if (cfg instanceof BooleanConfig bc) {
            float knob = approachMap(knobs, e.id(), bc.getBooleanValue() ? 1 : 0, 16);
            Widgets.toggle(g, x, y, knob, hov);
        } else if (cfg instanceof IntegerConfig || cfg instanceof DoubleConfig) {
            int vw = 50, sw = w - vw - 8;
            if (sw >= 30) {
                float t = sliderValue(cfg);
                Widgets.slider(g, x, y + 3, sw, t, Widgets.inside(mx, my, x, y, sw, h) || sliderEntry == e);
            }
            int vx = x + w - vw;
            if (editEntry == e) {
                Widgets.field(g, vx, y, vw, h, true);
                editField.draw(g, vx + 5, y + 4, vw - 10, Widgets.TEXT, "", 0, true);
            } else {
                Widgets.chip(g, vx, y, vw, h, formatValue(cfg), Widgets.TEXT, Widgets.inside(mx, my, vx, y, vw, h), false);
            }
        } else if (cfg instanceof ColorConfig cc) {
            Widgets.chip(g, x, y, w, h, "", Widgets.TEXT, hov, colorWheel != null);
            g.swatch(x + 3, y + 3, 22, h - 6, 5, cc.getValue());
            g.text(String.format("#%08X", cc.getValue()), x + 32, y + 4, Widgets.TEXT_DIM, false, false);
        } else if (cfg instanceof OptionListConfig<?> olc) {
            Widgets.chip(g, x, y, w, h, Lang.enumName(olc.getValue()), Widgets.TEXT, hov, false);
            g.text("‹", x + 6, y + 4, Widgets.TEXT_FAINT, false, false);
            g.text("›", x + w - 9, y + 4, Widgets.TEXT_FAINT, false, false);
        } else if (cfg instanceof StringConfig sc) {
            Widgets.field(g, x, y, w, h, editEntry == e);
            if (editEntry == e) {
                editField.draw(g, x + 6, y + 4, w - 12, Widgets.TEXT, "", 0, true);
            } else {
                String v = sc.getValue();
                // Keep the end of a long path in view.
                while (g.width(v) > w - 12 && v.length() > 1) v = v.substring(1);
                g.text(v, x + 6, y + 4, Widgets.TEXT_DIM, false, false);
            }
        } else if (cfg instanceof StringListConfig slc) {
            Widgets.chip(g, x, y, w, h, "", Widgets.TEXT, hov, false);
            List<String> items = slc.getStrings();
            int ix = x + 4;
            int icons = 0;
            for (int i = 0; i < items.size() && icons < 3 && e.listKind != null && e.listKind != Suggestions.Kind.STATE; i++) {
                Suggestions.Entry se = Suggestions.describe(e.listKind, items.get(i));
                if (se.icon() != null && !se.icon().isEmpty()) {
                    g.push();
                    g.translate(ix, y + 1);
                    g.scale(0.875f, 0.875f);
                    g.item(se.icon(), 0, 0);
                    g.pop();
                    ix += 13;
                    icons++;
                }
            }
            String count = Lang.format("ui.items", items.size());
            g.text(count, ix + 3, y + 4, Widgets.TEXT_DIM, false, false);
            String edit = Lang.get("ui.edit");
            g.text(edit, x + w - 8 - g.width(edit), y + 4, Widgets.ACCENT, false, false);
        }
    }

    private static float sliderValue(IConfig<?> cfg) {
        if (cfg instanceof IntegerConfig ic) {
            return (ic.getIntegerValue() - ic.getMin()) / (float) Math.max(1, ic.getMax() - ic.getMin());
        }
        DoubleConfig dc = (DoubleConfig) cfg;
        return (float) ((dc.getDoubleValue() - dc.getMin()) / Math.max(1e-9, dc.getMax() - dc.getMin()));
    }

    private static void setSlider(IConfig<?> cfg, float t) {
        t = Anim.clamp01(t);
        if (cfg instanceof IntegerConfig ic) {
            ic.setValue(Math.round(ic.getMin() + t * (ic.getMax() - ic.getMin())));
        } else if (cfg instanceof DoubleConfig dc) {
            double v = dc.getMin() + t * (dc.getMax() - dc.getMin());
            dc.setValue(Math.round(v * 100) / 100.0);
        }
    }

    private static String formatValue(IConfig<?> cfg) {
        if (cfg instanceof IntegerConfig ic) return String.valueOf(ic.getIntegerValue());
        if (cfg instanceof DoubleConfig dc) return String.format(Locale.ROOT, "%.2f", dc.getDoubleValue());
        return String.valueOf(cfg.getValue());
    }

    // ── left column ──────────────────────────────────────────────

    private void drawDetails(Gfx g) {
        int x = detailX, y = detailY, w = detailW, h = detailH;
        if (!beginPanel(g, 20, x + w / 2f, y + h / 2f)) return;
        Widgets.panel(g, x, y, w, h, 14, closing());
        g.text(Lang.get("ui.details"), x + 12, y + (h < 30 ? 7 : 10), Widgets.TEXT_DIM, true, true);
        if (h < 40) {
            endPanel(g);
            return;
        }

        int tx = x + 12, tw = w - 24;
        int top = y + 26, bottom = y + h - 8;
        g.scissor(x + 4, top, x + w - 4, bottom);
        int cy = top - Math.round(detailScroll);
        if (selected == null) {
            for (String line : g.wrap(Lang.get("ui.details.none"), tw)) {
                g.text(line, tx, cy, Widgets.TEXT_FAINT, false, false);
                cy += 11;
            }
        } else {
            Entry e = selected;
            String kind = Lang.get(switch (e.kind) {
                case FEATURE -> "ui.kind.feature";
                case OPTION -> "ui.kind.option";
                case HOTKEY -> "ui.kind.hotkey";
            });
            int kw = g.width(kind) + 12;
            g.rounded(tx, cy, kw, 12, 6, 0x404A96F0);
            g.text(kind, tx + 6, cy + 2, 0xFFB8DAFF, false, false);
            cy += 17;
            for (String line : g.wrap(e.name(), tw)) {
                g.text(line, tx, cy, Widgets.TEXT, true, true);
                cy += 11;
            }
            cy += 3;
            for (String line : statusLines(e)) {
                for (String l : g.wrap(line, tw)) {
                    g.text(l, tx, cy, 0xFFB8DAFF, false, false);
                    cy += 11;
                }
            }
            cy += 3;
            g.fill(tx, cy, tx + tw, cy + 1, 0x20FFFFFF);
            cy += 6;
            for (String line : g.wrap(e.detail(), tw)) {
                g.text(line, tx, cy, Widgets.TEXT_DIM, false, false);
                cy += 11;
            }
        }
        g.unscissor();
        int contentBottom = cy + Math.round(detailScroll);
        detailMaxScroll = Math.max(0, contentBottom - bottom);
        detailScrollTarget = Math.max(0, Math.min(detailScrollTarget, detailMaxScroll));
        detailScroll = Anim.approach(detailScroll, detailScrollTarget, dt, 16);
        endPanel(g);
    }

    private List<String> statusLines(Entry e) {
        List<String> out = new ArrayList<>();
        switch (e.kind) {
            case FEATURE -> {
                out.add((e.feature.isEnabled() ? Lang.get("ui.on") : Lang.get("ui.off"))
                        + "  /  " + Lang.format("ui.hotkey", fmtKey(e.hotkey.getHotkey().getStorageString())));
            }
            case OPTION -> {
                IConfig<?> c = e.config;
                if (c == null) break;
                out.add(Lang.format("ui.default", displayDefault(c)));
                if (c instanceof IntegerConfig ic) out.add(Lang.format("ui.range", ic.getMin(), ic.getMax()));
                if (c instanceof DoubleConfig dc) {
                    out.add(Lang.format("ui.range", trim(dc.getMin()), trim(dc.getMax())));
                }
            }
            case HOTKEY -> out.add(Lang.format("ui.hotkey", fmtKey(e.hotkey.getHotkey().getStorageString())));
        }
        return out;
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private static String displayDefault(IConfig<?> c) {
        Object d = c.getDefaultValue();
        if (c instanceof BooleanConfig) return (Boolean) d ? Lang.get("ui.on") : Lang.get("ui.off");
        if (c instanceof ColorConfig) return String.format("#%08X", (Integer) d);
        if (c instanceof DoubleConfig) return trim((Double) d);
        if (d instanceof Enum<?> en) return Lang.enumName(en);
        if (d instanceof List<?> l) return l.isEmpty() ? Lang.get("ui.none") : String.join(", ", l.stream().map(String::valueOf).toList());
        String s = String.valueOf(d);
        return s.isEmpty() ? Lang.get("ui.none") : s;
    }

    private static final String[] UI_SLIDER_KEYS = { "ui.transparency", "ui.blur", "ui.refraction", "ui.dim" };
    private static final int APPEAR_ROWS = 7, APPEAR_PITCH = 18;

    private static IntegerConfig uiSliderConfig(int i) {
        return switch (i) {
            case 0 -> Configs.Ui.GLASS_TRANSPARENCY;
            case 1 -> Configs.Ui.BACKGROUND_BLUR;
            case 2 -> Configs.Ui.REFRACTION;
            default -> Configs.Ui.BACKGROUND_DIM;
        };
    }

    /** y of an appearance row. Rows: 0 language, 1 background, 2-5 sliders, 6 reset. */
    private int appearRowY(int row) {
        return appearY + 24 + row * APPEAR_PITCH;
    }

    /** {labelX, labelW, controlX, controlW} of the appearance rows, sized so the labels fit. */
    private int[] appearColumns() {
        int lx = appearX + 12, cw = appearW - 24;
        int labelW = Gfx.textWidth(Lang.get("ui.language"), false);
        labelW = Math.max(labelW, Gfx.textWidth(Lang.get("ui.background"), false));
        for (String k : UI_SLIDER_KEYS) labelW = Math.max(labelW, Gfx.textWidth(Lang.get(k), false));
        labelW = Math.min(labelW + 8, cw * 11 / 20);
        return new int[] { lx, labelW, lx + labelW, cw - labelW };
    }

    private void drawAppearance(Gfx g, int mx, int my) {
        int x = appearX, y = appearY, w = appearW, h = appearH;
        if (!beginPanel(g, 30, x + w / 2f, y + h / 2f)) return;
        Widgets.panel(g, x, y, w, h, 14, closing());
        g.text(Lang.get("ui.appearance"), x + 12, y + 8, Widgets.TEXT_DIM, true, true);
        chevron(g, x + w - 18, y + 9, !appearCollapsedNow, Widgets.inside(mx, my, x, y, w, 22));
        if (appearCollapsedNow) {
            endPanel(g);
            return;
        }
        int[] col = appearColumns();
        int lx = col[0], labelW = col[1], ctrlX = col[2], ctrlW = col[3];

        // language: one chip that steps through the choices
        int ry = appearRowY(0);
        g.scissor(x, y + 22, x + w, y + h);
        g.text(g.ellipsize(Lang.get("ui.language"), labelW - 4), lx, ry + 4, Widgets.TEXT, false, false);
        Widgets.chip(g, ctrlX, ry, ctrlW, 16, Lang.enumName(Configs.Ui.LANGUAGE.getValue()), Widgets.TEXT,
                Widgets.inside(mx, my, ctrlX, ry, ctrlW, 16), false);
        g.text("‹", ctrlX + 6, ry + 4, Widgets.TEXT_FAINT, false, false);
        g.text("›", ctrlX + ctrlW - 9, ry + 4, Widgets.TEXT_FAINT, false, false);

        // background: the picker, and a clear button once an image is set
        ry = appearRowY(1);
        g.text(g.ellipsize(Lang.get("ui.background"), labelW - 4), lx, ry + 4, Widgets.TEXT, false, false);
        String path = Configs.Ui.BACKGROUND_IMAGE.getValue();
        boolean hasImage = !path.isEmpty();
        int pickW = hasImage ? ctrlW - 20 : ctrlW;
        String label = hasImage ? new File(path).getName() : Lang.get("ui.background.none");
        Widgets.button(g, ctrlX, ry, pickW, 16, label, mx, my, false);
        if (hasImage) Widgets.closeButton(g, ctrlX + ctrlW - 16, ry + 1, mx, my);

        // sliders; the value replaces the label while the slider is in use
        for (int i = 0; i < UI_SLIDER_KEYS.length; i++) {
            ry = appearRowY(2 + i);
            IntegerConfig c = uiSliderConfig(i);
            boolean active = uiSlider == i || Widgets.inside(mx, my, lx, ry, appearW - 24, 16);
            String text = active ? c.getIntegerValue() + "%" : g.ellipsize(Lang.get(UI_SLIDER_KEYS[i]), labelW - 4);
            g.text(text, lx, ry + 4, active ? 0xFFB8DAFF : Widgets.TEXT, false, false);
            Widgets.slider(g, ctrlX + 5, ry + 3, ctrlW - 10, c.getIntegerValue() / 100.0f, active);
        }

        ry = appearRowY(6);
        Widgets.button(g, lx, ry, appearW - 24, 16, Lang.get("ui.reset_order"), mx, my, false);
        g.unscissor();
        endPanel(g);
    }

    private static void chevron(Gfx g, int x, int y, boolean open, boolean hover) {
        int c = hover ? Widgets.TEXT : Widgets.TEXT_DIM;
        if (open) {
            for (int i = 0; i < 4; i++) g.fill(x + i, y + i, x + 7 - i, y + i + 1, c);
        } else {
            for (int i = 0; i < 4; i++) g.fill(x + i, y + i, x + i + 1, y + 7 - i, c);
        }
    }

    // ── Clear Block Render side ──────────────────────────────────

    private void drawCbrSide(Gfx g, int mx, int my) {
        int x = listX + listW + GAP + 6, x1 = width - M;
        int top = M + TOP_H + GAP, bottom = height - M;
        int btnY = bottom - 20;
        int infoH = 44;
        int infoY = btnY - 8 - infoH;
        int pvBottom = infoY - GAP;

        if (beginPanel(g, 20, (x + x1) / 2f, (top + pvBottom) / 2f)) {
            Widgets.panel(g, x, top, x1 - x, pvBottom - top, 14, closing());
            drawPreview(g, x + 6, top + 6, x1 - 6, pvBottom - 6);
            endPanel(g);
        }

        if (beginPanel(g, 40, (x + x1) / 2f, infoY + infoH / 2f)) {
            Widgets.panel(g, x, infoY, x1 - x, infoH, 12, closing());
            String info = selected != null ? selected.detail() : cbrStatusLine();
            int ty = infoY + 7;
            for (String line : g.wrap(info, x1 - x - 20)) {
                if (ty > infoY + infoH - 12) break;
                g.text(line, x + 10, ty, Widgets.TEXT_DIM, false, false);
                ty += 11;
            }
            endPanel(g);
        }

        if (beginPanel(g, 60, (x + x1) / 2f, btnY + 10)) {
            String status = cbrStatusLine();
            int[] b = cbrButtons();
            Widgets.button(g, b[2], btnY, b[4], 20, Lang.get("ui.done"), mx, my, true);
            Widgets.button(g, b[1], btnY, b[3], 20, Lang.get("ui.cbr.save_image"), mx, my, false);
            if (b[0] >= 0) Widgets.button(g, b[0], btnY, b[3], 20, Lang.get("ui.cbr.copy_image"), mx, my, false);
            if (selected != null) {
                int left = b[0] >= 0 ? b[0] : b[1];
                g.text(g.ellipsize(status, left - 10 - x), x, btnY + 6, Widgets.TEXT_FAINT, false, false);
            }
            endPanel(g);
        }
    }

    /**
     * The buttons under the preview: {copy x (-1 when copying is unsupported),
     * save x, done x, copy and save width, done width}. They shrink evenly when
     * the column is too narrow for their usual widths.
     */
    private int[] cbrButtons() {
        int x = listX + listW + GAP + 6, x1 = width - M, gap = 6;
        boolean copy = org.asutarisucu.tweak.ClearBlockRender.ImageClipboard.supported();
        int n = copy ? 3 : 2;
        int even = (x1 - x - gap * (n - 1)) / n;
        int doneW = Math.min(84, even), sideW = Math.min(96, even);
        int doneX = x1 - doneW, saveX = doneX - gap - sideW;
        int copyX = copy ? saveX - gap - sideW : -1;
        return new int[] { copyX, saveX, doneX, sideW, doneW };
    }

    /**
     * Draws the live capture preview, letterboxed to the configured output aspect
     * so what is shown matches what a capture would produce.
     */
    private void drawPreview(Gfx g, int x0, int y0, int x1, int y1) {
        previewRect = new int[] { x0, y0, x1, y1 };
        g.rounded(x0, y0, x1 - x0, y1 - y0, 9, 0x40000000);
        int paneW = x1 - x0 - 2, paneH = y1 - y0 - 2;
        if (paneW < 16 || paneH < 16) return;

        double aspect = (double) Configs.Generic.CBR_WIDTH.getIntegerValue()
                / Math.max(1, Configs.Generic.CBR_HEIGHT.getIntegerValue());
        int w = paneW, h = (int) Math.round(paneW / aspect);
        if (h > paneH) {
            h = paneH;
            w = (int) Math.round(paneH * aspect);
        }
        int ix = x0 + 1 + (paneW - w) / 2, iy = y0 + 1 + (paneH - h) / 2;

        String note = previewNote();
        if (note != null) {
            g.text(note, (x0 + x1 - g.width(note)) / 2, (y0 + y1) / 2 - 4, Widgets.TEXT_FAINT, false, false);
            org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.releasePreview();
            return;
        }
        org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.requestPreview(w, h);
        // The glass shader does not draw the preview, so it does not fade; skip it while hidden.
        if (g.alpha < 0.5f || !g.previewPane(ix, iy, ix + w, iy + h)) {
            // The pass runs on the frame hook, so the first frame after the pane
            // is sized has nothing to show yet.
            String msg = Lang.get("ui.cbr.preview_rendering");
            g.text(msg, (x0 + x1 - g.width(msg)) / 2, (y0 + y1) / 2 - 4, Widgets.TEXT_FAINT, false, false);
            return;
        }
        String hint = Lang.get("ui.cbr.preview_hint");
        if (g.width(hint) < x1 - x0 - 12) {
            g.rounded(x0 + 4, y1 - 15, g.width(hint) + 8, 12, 6, 0x60000000);
            g.text(hint, x0 + 8, y1 - 13, Widgets.TEXT_DIM, false, false);
        }
    }

    private boolean inPreview(int mx, int my) {
        return cbrMode && previewRect != null
                && mx >= previewRect[0] && mx < previewRect[2]
                && my >= previewRect[1] && my < previewRect[3];
    }

    /** Blocks per pixel of drag, so panning tracks the cursor at any zoom. */
    private double panScale() {
        int[] b = org.asutarisucu.tweak.WorldEditGUI.WorldEditSelection.getBounds();
        if (b == null || previewRect == null) return 0.05;
        double sizeX = b[3] - b[0] + 1, sizeY = b[4] - b[1] + 1, sizeZ = b[5] - b[2] + 1;
        double radius = Math.sqrt(sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ) / 2.0
                / Math.max(0.01, Configs.Generic.CBR_ZOOM.getDoubleValue());
        int paneH = Math.max(1, previewRect[3] - previewRect[1]);
        return 2.0 * radius / paneH;
    }

    private void dragPreview(int mx, int my) {
        int dx = mx - previewLastX, dy = my - previewLastY;
        previewLastX = mx;
        previewLastY = my;
        if (previewDrag == 1) {
            Configs.Generic.CBR_YAW.setValue(wrapDegrees(Configs.Generic.CBR_YAW.getDoubleValue() - dx * 0.5));
            Configs.Generic.CBR_PITCH.setValue(Configs.Generic.CBR_PITCH.getDoubleValue() - dy * 0.5);
        } else if (previewDrag == 2) {
            double scale = panScale();
            // The picture follows the cursor, so the camera moves the other way.
            Configs.Generic.CBR_PAN_X.setValue(Configs.Generic.CBR_PAN_X.getDoubleValue() + dx * scale);
            Configs.Generic.CBR_PAN_Y.setValue(Configs.Generic.CBR_PAN_Y.getDoubleValue() + dy * scale);
        }
    }

    /** Keeps yaw inside the config's own range so the slider stays usable. */
    private static double wrapDegrees(double deg) {
        while (deg > 180.0) deg -= 360.0;
        while (deg < -180.0) deg += 360.0;
        return deg;
    }

    private void resetPreviewView() {
        Configs.Generic.CBR_PAN_X.setValue(0.0);
        Configs.Generic.CBR_PAN_Y.setValue(0.0);
        Configs.Generic.CBR_ZOOM.setValue((double) Configs.Generic.CBR_ZOOM.getDefaultValue());
    }

    /** @return why the preview cannot be shown, or null when it can */
    private String previewNote() {
        if (!Feature.CLEAR_BLOCK_RENDER.isEnabled()) return Lang.get("ui.cbr.preview_off");
        if (org.asutarisucu.tweak.WorldEditGUI.WorldEditSelection.getBounds() == null) return Lang.get("ui.cbr.no_selection");
        return null;
    }

    /** One line saying whether a capture can start right now. */
    private String cbrStatusLine() {
        if (org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.isRecording()) return Lang.get("ui.cbr.recording");
        if (!Feature.CLEAR_BLOCK_RENDER.isEnabled()) return Lang.get("ui.cbr.disabled");
        int[] b = org.asutarisucu.tweak.WorldEditGUI.WorldEditSelection.getBounds();
        if (b == null) return Lang.get("ui.cbr.no_selection");
        String size = (b[3] - b[0] + 1) + " x " + (b[4] - b[1] + 1) + " x " + (b[5] - b[2] + 1);
        return Lang.format("ui.cbr.ready", size, fmtKey(Hotkeys.CLEAR_BLOCK_RENDER_RECORD.getHotkey().getStorageString()));
    }

    // ── popups, placement, toast ─────────────────────────────────

    private void drawPopups(Gfx g, int mx, int my, long now) {
        float t = Anim.easeOutBack((now - popupAt) / 1e6f / 260f);
        g.push();
        g.raise();
        g.alpha = Anim.easeOutCubic((now - popupAt) / 1e6f / 200f);
        g.fill(0, 0, width, height, 0x60000000);
        g.scaleAround(0.92f + 0.08f * t, width / 2f, height / 2f);
        if (colorWheel != null) colorWheel.draw(g, width, height, mx, my);
        if (listEditor != null) listEditor.draw(g, width, height, mx, my, dt);
        g.alpha = 1;
        g.pop();
    }

    private void openPopup() {
        popupAt = System.nanoTime();
        commitEdit();
        stopRec();
    }

    private void showToast(String s, boolean error) {
        toast = s;
        toastError = error;
        toastAt = System.nanoTime();
    }

    private void drawToast(Gfx g, long now) {
        if (toast == null) return;
        float t = (now - toastAt) / 1e6f;
        if (t > 3000) {
            toast = null;
            return;
        }
        float a = Math.min(Anim.easeOutCubic(t / 200f), 1 - Anim.clamp01((t - 2600) / 400f));
        // The Clear Block Render screen has buttons along the bottom, so its
        // toasts sit at the foot of the preview instead.
        int[] area = cbrMode && previewRect != null ? previewRect : new int[] { M, 0, width - M, height - M };
        String s = g.ellipsize(toast, area[2] - area[0] - 36);
        int w = g.width(s) + 24, x = (area[0] + area[2] - w) / 2, y = area[3] - 26;
        g.push();
        g.raise();
        g.alpha = a;
        Widgets.panel(g, x, y, w, 20, 10, false);
        g.text(s, x + 12, y + 6, toastError ? 0xFFFFC2C2 : 0xFFBDF5CB, false, false);
        g.alpha = 1;
        g.pop();
    }

    private void drawPlacement(Gfx g, int mx, int my) {
        String hint = Lang.get("ui.place_hint");
        int hw = g.width(hint) + 24;
        Widgets.panel(g, (width - hw) / 2, 8, hw, 20, 10, false);
        g.text(hint, (width - hw) / 2 + 12, 14, 0xFFFFE08A, false, false);

        if (placing == Action.METER_POSITION) {
            int pw = 200, ph = 30;
            int bx = Math.max(0, Math.min(mx - pw / 2, width - pw));
            int by = Math.max(0, Math.min(my, height - ph));
            Widgets.panel(g, bx, by, pw, ph, 10, false);
            String label = "AutoFill  [=====>    ] 60%";
            g.text(label, bx + (pw - g.width(label)) / 2, by + (ph - 8) / 2, 0xFF9FD0FF, false, false);
        } else {
            AlignMode align = Configs.Generic.HUD_LOG_ALIGN.getValue();
            int pw = Configs.Generic.HUD_LOG_WIDTH.getIntegerValue(), ph = 64;
            int bx = Math.max(0, Math.min(mx - pw / 2, width - pw));
            int by = Math.max(0, Math.min(my - ph / 2, height - ph));
            Widgets.panel(g, bx, by, pw, ph, 10, false);
            String[] lines = { "[ HUD Log ]", "Feature toggle notification", "Auto Restock / item messages", "...appear here" };
            int[] colors = { 0xFF9FD0FF, 0xFF7EE08C, Widgets.TEXT_DIM, Widgets.TEXT_FAINT };
            for (int i = 0; i < lines.length; i++) {
                int lw = g.width(lines[i]);
                int lx = switch (align) {
                    case LEFT -> bx + 8;
                    case CENTER -> bx + (pw - lw) / 2;
                    case RIGHT -> bx + pw - lw - 8;
                };
                g.text(lines[i], lx, by + 6 + i * 14, colors[i]);
            }
        }
        g.fill(mx - 6, my, mx + 7, my + 1, 0xFFFFFFFF);
        g.fill(mx, my - 6, mx + 1, my + 7, 0xFFFFFFFF);
    }

    private void commitPlacement(int mx, int my) {
        if (placing == Action.METER_POSITION) {
            Configs.Generic.PROGRESS_METER_X.setValue(Math.max(0.0, Math.min(1.0, (double) mx / width)));
            Configs.Generic.PROGRESS_METER_Y.setValue(Math.max(0.0, Math.min(1.0, (double) my / height)));
        } else {
            AlignMode align = Configs.Generic.HUD_LOG_ALIGN.getValue();
            int pw = Configs.Generic.HUD_LOG_WIDTH.getIntegerValue(), ph = 64;
            int bx = Math.max(0, Math.min(mx - pw / 2, width - pw));
            int by = Math.max(0, Math.min(my - ph / 2, height - ph));
            double anchorX = switch (align) {
                case LEFT -> (double) bx / width;
                case CENTER -> (double) (bx + pw / 2) / width;
                case RIGHT -> (double) (bx + pw) / width;
            };
            Configs.Generic.HUD_LOG_X.setValue(Math.max(0.0, Math.min(1.0, anchorX)));
            Configs.Generic.HUD_LOG_Y.setValue(Math.max(0.0, Math.min(1.0, (double) by / height)));
        }
        placing = Action.NONE;
    }

    // ── version glue: input ──────────────────────────────────────

//#if MC < 12111
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        return onMouseDown((int) mx, (int) my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        return onMouseDrag((int) mx, (int) my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        return onMouseUp((int) mx, (int) my, btn);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return onKeyDown(keyCode);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return onKeyUp(keyCode);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return onChar(String.valueOf(c));
    }
//#elseif MC < 260100
//$$ @Override
//$$ public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
//$$     return onMouseDown((int) click.x(), (int) click.y(), click.button());
//$$ }
//$$
//$$ @Override
//$$ public boolean mouseDragged(net.minecraft.client.gui.Click click, double dx, double dy) {
//$$     return onMouseDrag((int) click.x(), (int) click.y(), click.button());
//$$ }
//$$
//$$ @Override
//$$ public boolean mouseReleased(net.minecraft.client.gui.Click click) {
//$$     return onMouseUp((int) click.x(), (int) click.y(), click.button());
//$$ }
//$$
//$$ @Override
//$$ public boolean keyPressed(net.minecraft.client.input.KeyInput key) {
//$$     return onKeyDown(key.key());
//$$ }
//$$
//$$ @Override
//$$ public boolean keyReleased(net.minecraft.client.input.KeyInput key) {
//$$     return onKeyUp(key.key());
//$$ }
//$$
//$$ @Override
//$$ public boolean charTyped(net.minecraft.client.input.CharInput input) {
//$$     return onChar(input.asString());
//$$ }
//#else
//$$ @Override
//$$ public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
//$$     return onMouseDown((int) event.x(), (int) event.y(), event.button());
//$$ }
//$$
//$$ @Override
//$$ public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
//$$     return onMouseDrag((int) event.x(), (int) event.y(), event.button());
//$$ }
//$$
//$$ @Override
//$$ public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
//$$     return onMouseUp((int) event.x(), (int) event.y(), event.button());
//$$ }
//$$
//$$ @Override
//$$ public boolean keyPressed(net.minecraft.client.input.KeyEvent key) {
//$$     return onKeyDown(key.key());
//$$ }
//$$
//$$ @Override
//$$ public boolean keyReleased(net.minecraft.client.input.KeyEvent key) {
//$$     return onKeyUp(key.key());
//$$ }
//$$
//$$ @Override
//$$ public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
//$$     return onChar(Character.toString(event.codepoint()));
//$$ }
//#endif

    @Override
//#if MC < 12004
    public boolean mouseScrolled(double mx, double my, double amount) {
//#else
//$$ public boolean mouseScrolled(double mx, double my, double sx, double amount) {
//#endif
        return onScroll((int) mx, (int) my, amount);
    }

    // ── input handling ───────────────────────────────────────────

    private boolean onMouseDown(int mx, int my, int btn) {
        if (closing()) return true;
        if (placing != Action.NONE) {
            if (btn == 0) commitPlacement(mx, my);
            return true;
        }
        if (colorWheel != null) {
            if (btn == 0 && !colorWheel.mouseDown(mx, my)) colorWheel = null;
            return true;
        }
        if (listEditor != null) {
            if (btn == 0 && !listEditor.mouseDown(mx, my)) listEditor = null;
            return true;
        }

        // Preview navigation takes every button.
        if (inPreview(mx, my)) {
            commitEdit();
            previewLastX = mx;
            previewLastY = my;
            if (btn == 2) {
                resetPreviewView();
                return true;
            }
            previewDrag = (btn == 1 || TextField.shiftDown()) ? 2 : 1;
            return true;
        }

        if (editEntry != null && !editHit(mx, my)) commitEdit();
        if (recEntry != null) {
            stopRec();
            return true;
        }

        // Right click steps option lists backwards.
        if (btn == 1) {
            Row row = rowAt(mx, my);
            if (row != null && row.entry.config instanceof OptionListConfig<?> olc) {
                int[] c = controlRect(row.entry, listX, rowScreenY(row), listW, row.h);
                if (Widgets.inside(mx, my, c[0], c[1], c[2], c[3])) cycle(olc, -1);
            }
            return true;
        }
        if (btn != 0) return false;

        // top bar
        int cx = width - M - TOP_H;
        if (Widgets.inside(mx, my, cx, M, TOP_H, TOP_H)) {
            startClose();
            return true;
        }
        if (!cbrMode && my >= M && my < M + TOP_H) {
            int[] tabs = tabRects();
            for (int i = 0; i < TAB_KEYS.length; i++) {
                if (mx >= tabs[i] && mx < tabs[i + 1]) {
                    if (tab != i) {
                        tab = i;
                        lastTab = i;
                        scrollTarget = 0;
                        scroll = 0;
                        rowY.clear();
                    }
                    return true;
                }
            }
        }

        if (cbrMode && clickCbrButtons(mx, my)) return true;
        if (!cbrMode && clickAppearance(mx, my)) return true;
        if (!cbrMode && Widgets.inside(mx, my, detailX, detailY, detailW, detailH)) return true;

        // scrollbar
        if (contentH > listH && Widgets.inside(mx, my, listX + listW, listY, 8, listH)) {
            int[] sb = scrollbar();
            scrollbarGrab = my >= sb[1] && my < sb[1] + sb[3] ? my - sb[1] : sb[3] / 2;
            scrollbarDrag = true;
            dragScrollbar(my);
            return true;
        }

        Row row = rowAt(mx, my);
        if (row == null) return false;
        Entry e = row.entry;
        int y = rowScreenY(row);
        if (clickControl(e, listX, y, listW, row.h, mx, my)) {
            selected = e;
            detailScrollTarget = 0;
            return true;
        }
        pressEntry = e;
        pressX = mx;
        pressY = my;
        grabDY = my - y;
        dragMouseY = my;
        return true;
    }

    private Row rowAt(int mx, int my) {
        if (mx < listX || mx >= listX + listW || my < listY || my >= listY + listH) return null;
        for (Row r : rows) {
            if (r.entry == null) continue;
            int y = rowScreenY(r);
            if (my >= y && my < y + r.h) return r;
        }
        return null;
    }

    private int rowScreenY(Row r) {
        return Math.round(listY + rowY.getOrDefault(r.entry != null ? r.entry.id() : "H:" + r.header, (float) r.y) - scroll);
    }

    /** @return true when the click landed on a control of the panel */
    private boolean clickControl(Entry e, int x, int y, int w, int h, int mx, int my) {
        switch (e.kind) {
            case FEATURE -> {
                int tx = x + w - 10 - 28, ty = y + (h - 16) / 2;
                int kw = featureKeyWidth(w), kx = tx - 8 - kw;
                if (Widgets.inside(mx, my, tx - 2, ty - 2, 32, 20)) {
                    e.feature.toggle();
                    return true;
                }
                if (Widgets.inside(mx, my, kx, ty, kw, 16)) {
                    startRec(e);
                    return true;
                }
                return false;
            }
            case HOTKEY -> {
                int kw = Math.min(130, w / 2), kx = x + w - 10 - kw, ky = y + (h - 16) / 2;
                if (Widgets.inside(mx, my, kx, ky, kw, 16)) {
                    startRec(e);
                    return true;
                }
                return false;
            }
            default -> {
            }
        }
        int[] c = controlRect(e, x, y, w, h);
        if (!Widgets.inside(mx, my, c[0] - 2, c[1] - 2, c[2] + 4, c[3] + 4)) return false;
        IConfig<?> cfg = e.config;
        if (e.action != Action.NONE) {
            placing = e.action;
        } else if (cfg instanceof BooleanConfig bc) {
            bc.setValue(!bc.getBooleanValue());
        } else if (cfg instanceof IntegerConfig || cfg instanceof DoubleConfig) {
            int vw = 50, sw = c[2] - vw - 8;
            if (mx >= c[0] + c[2] - vw) {
                startEdit(e, TextField.numeric(), formatValue(cfg));
            } else if (sw >= 30 && mx < c[0] + sw) {
                if (editEntry == e) cancelEdit();
                sliderEntry = e;
                setSlider(cfg, (mx - c[0]) / (float) sw);
            }
        } else if (cfg instanceof ColorConfig cc) {
            colorWheel = new ColorWheel(cc, e.name());
            openPopup();
        } else if (cfg instanceof OptionListConfig<?> olc) {
            cycle(olc, mx < c[0] + c[2] / 3 ? -1 : 1);
        } else if (cfg instanceof StringConfig sc) {
            startEdit(e, TextField.plain(), sc.getValue());
        } else if (cfg instanceof StringListConfig slc) {
            listEditor = new ListEditor(slc, e.listKind, e.name());
            openPopup();
        }
        return true;
    }

    private static <E extends Enum<E>> void cycle(OptionListConfig<E> olc, int dir) {
        E[] all = olc.getAllValues();
        int i = olc.getValue().ordinal();
        olc.setValue(all[Math.floorMod(i + dir, all.length)]);
    }

    private boolean clickCbrButtons(int mx, int my) {
        int btnY = height - M - 20;
        int[] b = cbrButtons();
        if (Widgets.inside(mx, my, b[2], btnY, b[4], 20)) {
            startClose();
            return true;
        }
        if (Widgets.inside(mx, my, b[1], btnY, b[3], 20)) {
            saveStill(false);
            return true;
        }
        if (b[0] >= 0 && Widgets.inside(mx, my, b[0], btnY, b[3], 20)) {
            saveStill(true);
            return true;
        }
        return mx >= listX + listW + GAP;
    }

    private void saveStill(boolean toClipboard) {
        org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.saveStill(toClipboard, (result, file) -> {
            switch (result) {
                case SAVED -> showToast(Lang.format("ui.cbr.saved", file), false);
                case COPIED -> showToast(Lang.get("ui.cbr.copied"), false);
                case NOT_READY -> {
                    String why = previewNote();
                    showToast(why != null ? why : Lang.get(toClipboard ? "ui.cbr.copy_failed" : "ui.cbr.save_failed"), true);
                }
                case BUSY -> showToast(Lang.get("ui.cbr.busy"), true);
                case FAILED -> showToast(Lang.get(toClipboard ? "ui.cbr.copy_failed" : "ui.cbr.save_failed"), true);
            }
        });
    }

    private boolean clickAppearance(int mx, int my) {
        if (!Widgets.inside(mx, my, appearX, appearY, appearW, appearH)) return false;
        if (my < appearY + 22) {
            appearanceCollapsed = !appearCollapsedNow;
            return true;
        }
        if (appearCollapsedNow) return true;
        int[] col = appearColumns();
        int ctrlX = col[2], ctrlW = col[3];

        int ry = appearRowY(0);
        if (my >= ry && my < ry + 16 && mx >= ctrlX) {
            UiLanguage[] langs = UiLanguage.values();
            int dir = mx < ctrlX + ctrlW / 3 ? -1 : 1;
            int i = Configs.Ui.LANGUAGE.getValue().ordinal();
            Configs.Ui.LANGUAGE.setValue(langs[Math.floorMod(i + dir, langs.length)]);
            return true;
        }
        ry = appearRowY(1);
        if (my >= ry && my < ry + 16 && mx >= ctrlX) {
            boolean hasImage = !Configs.Ui.BACKGROUND_IMAGE.getValue().isEmpty();
            if (hasImage && mx >= ctrlX + ctrlW - 18) {
                Configs.Ui.BACKGROUND_IMAGE.setValue("");
                GlassRenderer.forgetImage();
            } else {
                chooseBackground();
            }
            return true;
        }
        for (int i = 0; i < UI_SLIDER_KEYS.length; i++) {
            ry = appearRowY(2 + i);
            if (my >= ry && my < ry + 16 && mx >= ctrlX) {
                uiSlider = i;
                dragUiSlider(mx);
                return true;
            }
        }
        ry = appearRowY(6);
        if (my >= ry && my < ry + 16) {
            for (StringListConfig c : List.of(Configs.Ui.ORDER_FEATURES, Configs.Ui.ORDER_OPTIONS,
                    Configs.Ui.ORDER_HOTKEYS, Configs.Ui.ORDER_CBR)) {
                c.setValue(List.of());
            }
            reloadEntries();
            return true;
        }
        return true;
    }

    private void dragUiSlider(int mx) {
        int[] col = appearColumns();
        int sx = col[2] + 5, sw = col[3] - 10;
        float t = (mx - sx) / (float) Math.max(1, sw);
        uiSliderConfig(uiSlider).setValue(Math.round(Anim.clamp01(t) * 100));
    }

    private void chooseBackground() {
        BackgroundImage.choose(Lang.get("ui.background.dialog"), TweaksConfigScreen::runOnClient, path -> {
            if (path == null) return;
            if (BackgroundImage.read(path) == null) {
                showToast(Lang.get("ui.background.failed"), true);
                return;
            }
            Configs.Ui.BACKGROUND_IMAGE.setValue(path);
            GlassRenderer.forgetImage();
        });
    }

    private boolean onMouseDrag(int mx, int my, int btn) {
        if (closing()) return true;
        if (colorWheel != null) {
            colorWheel.mouseDrag(mx, my);
            return true;
        }
        if (previewDrag != 0) {
            dragPreview(mx, my);
            return true;
        }
        if (uiSlider >= 0) {
            dragUiSlider(mx);
            return true;
        }
        if (sliderEntry != null) {
            int[] c = null;
            for (Row r : rows) {
                if (r.entry == sliderEntry) c = controlRect(r.entry, listX, rowScreenY(r), listW, r.h);
            }
            if (c != null) setSlider(sliderEntry.config, (mx - c[0]) / (float) Math.max(1, c[2] - 58));
            return true;
        }
        if (scrollbarDrag) {
            dragScrollbar(my);
            return true;
        }
        if (pressEntry != null) {
            dragMouseY = my;
            boolean canReorder = cbrMode || search.isEmpty();
            if (!dragging && canReorder && Math.abs(my - pressY) + Math.abs(mx - pressX) > 4) {
                dragging = true;
                dragSection = sectionOf(pressEntry);
            }
            return true;
        }
        return false;
    }

    private List<Entry> sectionOf(Entry e) {
        if (cbrMode) return cbr;
        return switch (e.kind) {
            case FEATURE -> features;
            case OPTION -> options;
            case HOTKEY -> hotkeys;
        };
    }

    private void dragScrollbar(int my) {
        int[] sb = scrollbar();
        int travel = listH - sb[3];
        if (travel <= 0) return;
        float t = (my - scrollbarGrab - listY) / (float) travel;
        scrollTarget = Anim.clamp01(t) * Math.max(0, contentH - listH);
        scroll = scrollTarget;
    }

    private boolean onMouseUp(int mx, int my, int btn) {
        if (colorWheel != null) colorWheel.mouseUp();
        if (previewDrag != 0) {
            previewDrag = 0;
            return true;
        }
        uiSlider = -1;
        sliderEntry = null;
        scrollbarDrag = false;
        if (pressEntry != null) {
            if (dragging) {
                dropPanel();
            } else {
                selected = pressEntry;
                detailScrollTarget = 0;
                detailScroll = 0;
            }
            pressEntry = null;
            dragging = false;
            dragSection = null;
            return true;
        }
        return false;
    }

    private void dropPanel() {
        List<Entry> sec = dragSection;
        if (sec == null || dropIndex < 0) return;
        sec.remove(pressEntry);
        sec.add(Math.min(dropIndex, sec.size()), pressEntry);
        StringListConfig order = cbrMode ? Configs.Ui.ORDER_CBR : switch (pressEntry.kind) {
            case FEATURE -> Configs.Ui.ORDER_FEATURES;
            case OPTION -> Configs.Ui.ORDER_OPTIONS;
            case HOTKEY -> Configs.Ui.ORDER_HOTKEYS;
        };
        ConfigEntries.saveOrder(order, sec);
        selected = pressEntry;
    }

    private boolean onScroll(int mx, int my, double amount) {
        if (closing()) return true;
        if (listEditor != null) {
            listEditor.scroll(amount);
            return true;
        }
        if (colorWheel != null || placing != Action.NONE) return true;
        if (inPreview(mx, my)) {
            if (amount != 0) {
                double zoom = Configs.Generic.CBR_ZOOM.getDoubleValue() * Math.pow(1.1, amount);
                Configs.Generic.CBR_ZOOM.setValue(zoom);
            }
            return true;
        }
        if (!cbrMode && Widgets.inside(mx, my, detailX, detailY, detailW, detailH)) {
            detailScrollTarget -= (float) amount * 22;
            return true;
        }
        scrollTarget -= (float) amount * 34;
        return true;
    }

    private boolean onKeyDown(int keyCode) {
        if (closing()) return true;
        if (placing != Action.NONE) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) placing = Action.NONE;
            return true;
        }
        if (colorWheel != null) {
            if (!colorWheel.key(keyCode)) colorWheel = null;
            return true;
        }
        if (listEditor != null) {
            if (!listEditor.key(keyCode)) listEditor = null;
            return true;
        }
        if (recEntry != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                recEntry.hotkey.getHotkey().setFromString("");
                stopRec();
                return true;
            }
            // GLFW reports every key it has no code for as -1, which cannot be told apart or polled.
            if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return true;
            heldKeys.add(keyCode);
            // Rebuild on every press, modifiers included, so a modifier on its own
            // (Alt, Ctrl, Shift) is bindable.
            pendingCombo = buildCombo(heldKeys);
            return true;
        }
        if (editEntry != null) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> cancelEdit();
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> commitEdit();
                default -> editField.key(keyCode);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!search.isEmpty()) {
                search.set("");
                scrollTarget = 0;
                return true;
            }
            startClose();
            return true;
        }
        if (!cbrMode && !dragging && search.key(keyCode)) {
            scrollTarget = 0;
            return true;
        }
        return false;
    }

    private boolean onKeyUp(int keyCode) {
        if (recEntry == null) return false;
        heldKeys.remove(keyCode);
        if (heldKeys.isEmpty() && pendingCombo != null) {
            recEntry.hotkey.getHotkey().setFromString(pendingCombo);
            stopRec();
        }
        return true;
    }

    private boolean onChar(String s) {
        if (closing() || placing != Action.NONE) return true;
        // A key being bound would otherwise also be typed into the search box.
        if (recEntry != null) return true;
        if (colorWheel != null) {
            colorWheel.type(s);
            return true;
        }
        if (listEditor != null) {
            listEditor.type(s);
            return true;
        }
        if (editEntry != null) {
            editField.type(s);
            return true;
        }
        if (cbrMode) return false;
        // The drop position is counted in the unfiltered list.
        if (dragging) return true;
        search.type(s);
        scrollTarget = 0;
        return true;
    }

    // ── inline editing ───────────────────────────────────────────

    private void startEdit(Entry e, TextField field, String value) {
        commitEdit();
        editEntry = e;
        editField = field;
        editField.set(value);
    }

    /** Whether ({@code mx}, {@code my}) is on the field being edited. */
    private boolean editHit(int mx, int my) {
        for (Row r : rows) {
            if (r.entry != editEntry) continue;
            int[] c = controlRect(r.entry, listX, rowScreenY(r), listW, r.h);
            return Widgets.inside(mx, my, c[0], c[1], c[2], c[3]);
        }
        return false;
    }

    private void commitEdit() {
        if (editEntry == null) return;
        String s = editField.text().trim();
        IConfig<?> cfg = editEntry.config;
        try {
            if (cfg instanceof IntegerConfig ic) {
                long v = Math.round(Double.parseDouble(s));
                ic.setValue((int) Math.max(ic.getMin(), Math.min(ic.getMax(), v)));
            } else if (cfg instanceof DoubleConfig dc) {
                dc.setValue(Double.parseDouble(s));
            } else if (cfg instanceof StringConfig sc) {
                sc.setValue(editField.text());
            }
        } catch (NumberFormatException ignored) {
        }
        editEntry = null;
    }

    private void cancelEdit() {
        editEntry = null;
    }

    // ── hotkey recording ─────────────────────────────────────────

    private void startRec(Entry e) {
        commitEdit();
        recEntry = e;
        heldKeys.clear();
        pendingCombo = null;
    }

    private void stopRec() {
        recEntry = null;
        heldKeys.clear();
        pendingCombo = null;
    }

    private String liveKeyDisplay() {
        if (heldKeys.isEmpty()) return Lang.get("ui.press_keys");
        return fmtKey(buildCombo(heldKeys)) + (pendingCombo == null ? " + ?" : "");
    }

    private static String fmtKey(String s) {
        if (s == null || s.isBlank()) return Lang.get("ui.none");
        return s.replace(",", " + ");
    }

    /**
     * Builds the storage string for the keys currently held, modifiers first.
     * Names come from {@link org.asutarisucu.lib.hotkey.ComboKey#nameOf}, the same
     * table the parser uses, so every key GLFW reports round-trips.
     */
    private static String buildCombo(java.util.Set<Integer> keys) {
        List<String> mods = new ArrayList<>(), regs = new ArrayList<>();
        for (int k : keys) {
            String n = org.asutarisucu.lib.hotkey.ComboKey.nameOf(k);
            if (org.asutarisucu.lib.hotkey.ComboKey.isModifier(k)) mods.add(n);
            else regs.add(n);
        }
        mods.addAll(regs);
        return String.join(",", mods);
    }

    // ── closing ──────────────────────────────────────────────────

    private void startClose() {
        if (closing()) return;
        commitEdit();
        stopRec();
        colorWheel = null;
        listEditor = null;
        pressEntry = null;
        dragging = false;
        closingAt = System.nanoTime();
    }

//#if MC < 260100
    @Override
    public void close() {
        startClose();
    }
//#else
//$$ @Override
//$$ public void onClose() {
//$$     startClose();
//$$ }
//#endif

    private void finishClose() {
        cleanup();
        showScreen(parent);
    }

    @Override
    public void removed() {
        cleanup();
        super.removed();
    }

    private void cleanup() {
        commitEdit();
        org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.releasePreview();
        GlassRenderer.release();
        ConfigManager.INSTANCE.save();
    }
}
