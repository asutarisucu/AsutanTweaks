package org.asutarisucu.GUI.glass;

import java.util.ArrayList;
import java.util.List;

//#if MC < 12001
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
//#elseif MC < 260100
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gui.DrawContext;
//$$ import net.minecraft.item.ItemStack;
//$$ import net.minecraft.text.MutableText;
//$$ import net.minecraft.text.Style;
//$$ import net.minecraft.text.Text;
//$$ import net.minecraft.util.Identifier;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//$$ import net.minecraft.network.chat.Component;
//$$ import net.minecraft.network.chat.MutableComponent;
//$$ import net.minecraft.network.chat.Style;
//$$ import net.minecraft.resources.Identifier;
//$$ import net.minecraft.world.item.ItemStack;
//#endif

/**
 * Drawing for the config screen, the same on every supported version.
 *
 * Plain fills and text go to the game's GUI drawing. Rounded and glass shapes go
 * through the glass shader; when it is not available (the core shader failed to
 * load) they fall back to plain fills.
 */
public final class Gfx {

    /** Multiplies the alpha of everything drawn; used by the open and close animations. */
    public float alpha = 1.0f;

    public static final int GLASS_TINT = 0xFF161A24;

    // Shader modes, see glass_shade.glsl
    private static final int M_BACKDROP = 0, M_GLASS = 1, M_SOLID = 2, M_DISC = 3,
            M_SHADOW = 4, M_BAR = 5, M_RING = 6, M_SWATCH = 7;

    // Our own record of the transform, for the versions whose scissor ignores it.
    private float tx, ty, sx = 1.0f, sy = 1.0f;
    private final List<float[]> stack = new ArrayList<>();

//#if MC < 12001
    private static final Identifier FONT_ID = new Identifier("asutantweaks", "config_screen");
//#elseif MC < 260100
//$$ private static final Identifier FONT_ID = Identifier.of("asutantweaks", "config_screen");
//#else
//$$ private static final Identifier FONT_ID = Identifier.fromNamespaceAndPath("asutantweaks", "config_screen");
//#endif

//#if MC < 12001
    private final MatrixStack ms;

    public Gfx(MatrixStack ms) {
        this.ms = ms;
    }
//#elseif MC < 260100
//$$ private final DrawContext ctx;
//$$
//$$ public Gfx(DrawContext ctx) {
//$$     this.ctx = ctx;
//$$ }
//#else
//$$ private final GuiGraphicsExtractor g;
//$$
//$$ public Gfx(GuiGraphicsExtractor g) {
//$$     this.g = g;
//$$ }
//#endif

    // ── text ─────────────────────────────────────────────────────

    /**
     * The bundled font covers Latin, Greek and Cyrillic only. Anything else, such
     * as Japanese, is drawn whole in the game's default font, so a line does not
     * mix two typefaces.
     */
    private static boolean needsDefaultFont(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x0530 && (c < 0x1E00 || c >= 0x2200)) return true;
        }
        return false;
    }

//#if MC < 260100
    private static Text styled(String s, boolean bold) {
        MutableText t = Text.literal(s);
        Style style = Style.EMPTY.withBold(bold);
        if (!needsDefaultFont(s)) {
//#if MC < 12111
            style = style.withFont(FONT_ID);
//#else
//$$         style = style.withFont(new net.minecraft.text.StyleSpriteSource.Font(FONT_ID));
//#endif
        }
        return t.setStyle(style);
    }
//#else
//$$ private static Component styled(String s, boolean bold) {
//$$     MutableComponent t = Component.literal(s);
//$$     Style style = Style.EMPTY.withBold(bold);
//$$     if (!needsDefaultFont(s)) {
//$$         style = style.withFont(new net.minecraft.network.chat.FontDescription.Resource(FONT_ID));
//$$     }
//$$     return t.withStyle(style);
//$$ }
//#endif

    public int width(String s) {
        return width(s, false);
    }

    public int width(String s, boolean bold) {
        return textWidth(s, bold);
    }

    /** Text width without a drawing context, for layout done outside a frame. */
    public static int textWidth(String s, boolean bold) {
        if (s == null || s.isEmpty()) return 0;
//#if MC < 260100
        return MinecraftClient.getInstance().textRenderer.getWidth(styled(s, bold));
//#else
//$$     return Minecraft.getInstance().font.width(styled(s, bold));
//#endif
    }

    public void text(String s, int x, int y, int argb) {
        text(s, x, y, argb, true, false);
    }

    public void text(String s, int x, int y, int argb, boolean shadow, boolean bold) {
        if (s == null || s.isEmpty()) return;
        argb = mul(argb);
        // The game draws text with an alpha under 4 fully opaque.
        if ((argb >>> 24) < 4) return;
//#if MC < 12005
        // Before MC 1.20.5 (STB instead of FreeType) TTF glyphs sit higher on the line.
        if (!needsDefaultFont(s)) y += 3;
//#endif
//#if MC < 12001
        var tr = MinecraftClient.getInstance().textRenderer;
        if (shadow) tr.drawWithShadow(ms, styled(s, bold), x, y, argb);
        else tr.draw(ms, styled(s, bold), x, y, argb);
//#elseif MC < 260100
//$$     ctx.drawText(MinecraftClient.getInstance().textRenderer, styled(s, bold), x, y, argb, shadow);
//#else
//$$     g.text(Minecraft.getInstance().font, styled(s, bold), x, y, argb, shadow);
//#endif
    }

    /** Cuts {@code s} to fit, marking the cut with an ellipsis. */
    public String ellipsize(String s, int maxW) {
        if (width(s) <= maxW) return s;
        String dots = "...";
        int end = s.length();
        while (end > 0 && width(s.substring(0, end) + dots) > maxW) end--;
        return s.substring(0, end) + dots;
    }

    /**
     * Wraps at spaces, and between any two characters of text written without
     * spaces such as Japanese. Explicit line breaks are kept.
     */
    public List<String> wrap(String text, int maxW) {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        for (String para : text.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            int lastSpace = -1;
            for (int i = 0; i < para.length(); i++) {
                char c = para.charAt(i);
                line.append(c);
                if (c == ' ') lastSpace = line.length() - 1;
                if (width(line.toString()) > maxW && line.length() > 1) {
                    if (lastSpace > 0 && !needsDefaultFont(line.toString())) {
                        out.add(line.substring(0, lastSpace));
                        String rest = line.substring(lastSpace + 1);
                        line.setLength(0);
                        line.append(rest);
                    } else {
                        char last = line.charAt(line.length() - 1);
                        line.setLength(line.length() - 1);
                        // Keep closing punctuation on the line it closes.
                        if ("、。，．）」』】ー".indexOf(last) >= 0) {
                            line.append(last);
                            out.add(line.toString());
                            line.setLength(0);
                        } else {
                            out.add(line.toString());
                            line.setLength(0);
                            line.append(last);
                        }
                    }
                    lastSpace = -1;
                }
            }
            out.add(line.toString());
        }
        return out;
    }

    // ── plain fills ──────────────────────────────────────────────

    public void fill(int x0, int y0, int x1, int y1, int argb) {
        argb = mul(argb);
        if ((argb >>> 24) == 0) return;
//#if MC < 12001
        DrawableHelper.fill(ms, x0, y0, x1, y1, argb);
//#elseif MC < 260100
//$$     ctx.fill(x0, y0, x1, y1, argb);
//#else
//$$     g.fill(x0, y0, x1, y1, argb);
//#endif
    }

    // ── transform and clipping ───────────────────────────────────

    public void push() {
        stack.add(new float[] { tx, ty, sx, sy });
//#if MC < 12001
        ms.push();
//#elseif MC < 12111
//$$     ctx.getMatrices().push();
//#elseif MC < 260100
//$$     ctx.getMatrices().pushMatrix();
//#else
//$$     g.pose().pushMatrix();
//#endif
    }

    public void pop() {
        float[] s = stack.remove(stack.size() - 1);
        tx = s[0];
        ty = s[1];
        sx = s[2];
        sy = s[3];
//#if MC < 12001
        ms.pop();
//#elseif MC < 12111
//$$     ctx.getMatrices().pop();
//#elseif MC < 260100
//$$     ctx.getMatrices().popMatrix();
//#else
//$$     g.pose().popMatrix();
//#endif
    }

    public void translate(float x, float y) {
        tx += x * sx;
        ty += y * sy;
//#if MC < 12001
        ms.translate(x, y, 0.0f);
//#elseif MC < 12111
//$$     ctx.getMatrices().translate(x, y, 0.0f);
//#elseif MC < 260100
//$$     ctx.getMatrices().translate(x, y);
//#else
//$$     g.pose().translate(x, y);
//#endif
    }

    public void scale(float x, float y) {
        sx *= x;
        sy *= y;
//#if MC < 12001
        ms.scale(x, y, 1.0f);
//#elseif MC < 12111
//$$     ctx.getMatrices().scale(x, y, 1.0f);
//#elseif MC < 260100
//$$     ctx.getMatrices().scale(x, y);
//#else
//$$     g.pose().scale(x, y);
//#endif
    }

    /** Scales by {@code s} around ({@code cx}, {@code cy}). */
    public void scaleAround(float s, float cx, float cy) {
        translate(cx, cy);
        scale(s, s);
        translate(-cx, -cy);
    }

    /**
     * Puts what follows above item icons. Before MC 1.21.11 items are drawn with
     * depth, so a popup would otherwise sit under the icons of the list.
     */
    public void raise() {
//#if MC < 12001
        ms.translate(0.0f, 0.0f, 400.0f);
//#elseif MC < 12111
//$$     ctx.getMatrices().translate(0.0f, 0.0f, 400.0f);
//#endif
    }

    public void scissor(int x0, int y0, int x1, int y1) {
//#if MC < 12001
        DrawableHelper.enableScissor(tfx(x0), tfy(y0), tfx(x1), tfy(y1));
//#elseif MC < 12111
//$$     ctx.enableScissor(tfx(x0), tfy(y0), tfx(x1), tfy(y1));
//#elseif MC < 260100
//$$     ctx.enableScissor(x0, y0, x1, y1);
//#else
//$$     g.enableScissor(x0, y0, x1, y1);
//#endif
    }

    public void unscissor() {
//#if MC < 12001
        DrawableHelper.disableScissor();
//#elseif MC < 260100
//$$     ctx.disableScissor();
//#else
//$$     g.disableScissor();
//#endif
    }

    private int tfx(int x) {
        return Math.round(x * sx + tx);
    }

    private int tfy(int y) {
        return Math.round(y * sy + ty);
    }

    // ── items ────────────────────────────────────────────────────

    public void item(ItemStack stack, int x, int y) {
        if (stack == null || alpha < 0.5f) return;
//#if MC < 12001
        MinecraftClient.getInstance().getItemRenderer().renderInGuiWithOverrides(ms, stack, x, y);
//#elseif MC < 260100
//$$     ctx.drawItem(stack, x, y);
//#else
//$$     g.item(stack, x, y);
//#endif
    }

    // ── Clear Block Render preview ───────────────────────────────

    /** @return false when no preview frame has been rendered yet */
    public boolean previewPane(int x0, int y0, int x1, int y1) {
//#if MC < 12001
        int texture = org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.previewTexture();
        if (texture == 0) return false;
        org.asutarisucu.tweak.ClearBlockRender.PreviewBlit.draw(ms.peek().getPositionMatrix(), texture, x0, y0, x1, y1);
        return true;
//#elseif MC < 12111
//$$     int texture = org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.previewTexture();
//$$     if (texture == 0) return false;
//$$     // Flush the batched GUI elements first; the quad below is drawn immediately.
//$$     ctx.draw();
//$$     org.asutarisucu.tweak.ClearBlockRender.PreviewBlit.draw(
//$$             ctx.getMatrices().peek().getPositionMatrix(), texture, x0, y0, x1, y1);
//$$     return true;
//#elseif MC < 260100
//$$     var texture = org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.previewTexture();
//$$     if (texture == null) return false;
//$$     var sampler = com.mojang.blaze3d.systems.RenderSystem.getSamplerCache().get(
//$$             com.mojang.blaze3d.textures.AddressMode.CLAMP_TO_EDGE,
//$$             com.mojang.blaze3d.textures.AddressMode.CLAMP_TO_EDGE,
//$$             com.mojang.blaze3d.textures.FilterMode.LINEAR,
//$$             com.mojang.blaze3d.textures.FilterMode.LINEAR, false);
//$$     // v flipped: the render target's first row is the bottom of the image.
//$$     ((org.asutarisucu.mixin.ClearBlockRender.MixinDrawContextPreview) ctx)
//$$             .asutantweaks$drawTexturedQuad(
//$$                     net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, texture, sampler,
//$$                     x0, y0, x1, y1, 0.0f, 1.0f, 1.0f, 0.0f, -1);
//$$     return true;
//#else
//$$     var texture = org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender.previewTexture();
//$$     if (texture == null) return false;
//$$     var sampler = com.mojang.blaze3d.systems.RenderSystem.getSamplerCache().getSampler(
//$$             com.mojang.blaze3d.textures.AddressMode.CLAMP_TO_EDGE,
//$$             com.mojang.blaze3d.textures.AddressMode.CLAMP_TO_EDGE,
//$$             com.mojang.blaze3d.textures.FilterMode.LINEAR,
//$$             com.mojang.blaze3d.textures.FilterMode.LINEAR, false);
//$$     // v flipped: the render target's first row is the bottom of the image.
//$$     g.blit(texture, sampler, x0, y0, x1, y1, 0.0f, 1.0f, 1.0f, 0.0f);
//$$     return true;
//#endif
    }

    // ── glass shader shapes ──────────────────────────────────────

    /** Full-screen copy of what is behind the screen. {@code blur} and {@code dim} run 0-1. */
    public void backdrop(int x0, int y0, int x1, int y1, float blur, float dim) {
        if (!GlassRenderer.ready()) {
            fill(x0, y0, x1, y1, ((int) (dim * 200) << 24) | 0x05060A);
            return;
        }
        shape(x0, y0, x1 - x0, y1 - y0, 0, M_BACKDROP, 0, level(blur), level(dim), 0xFFFFFFFF, 0);
    }

    /**
     * A refracting glass panel.
     *
     * @param frost     0 clear to 1 milky
     * @param refract   0 flat to 1 strongly bent edges
     * @param dissolve  fade by melting away instead of turning transparent
     */
    public void glass(int x, int y, int w, int h, int radius, int tint, float frost, float refract, boolean dissolve) {
        if (!GlassRenderer.ready()) {
            fill(x, y, x + w, y + h, (0xB0 << 24) | (tint & 0xFFFFFF));
            return;
        }
        // The fade rides in the colour alpha, so it is not applied by mul().
        int a = Math.round(clamp01(alpha) * 255);
        int argb = (a << 24) | (tint & 0xFFFFFF);
        shape(x, y, w, h, radius, M_GLASS, dissolve ? 1 : 0, level(frost), level(refract), argb, 0, false);
    }

    public void rounded(int x, int y, int w, int h, int radius, int argb) {
        roundedShape(x, y, w, h, radius, argb, 0);
    }

    /** Rounded rect lit slightly from the top. */
    public void roundedSheen(int x, int y, int w, int h, int radius, int argb) {
        roundedShape(x, y, w, h, radius, argb, 1);
    }

    private void roundedShape(int x, int y, int w, int h, int radius, int argb, int flag) {
        if (!GlassRenderer.ready()) {
            fill(x, y, x + w, y + h, argb);
            return;
        }
        shape(x, y, w, h, radius, M_SOLID, flag, 0, 0, argb, 0);
    }

    /** Rounded outline drawn inside the rect. */
    public void outline(int x, int y, int w, int h, int radius, int thickness, int argb) {
        if (!GlassRenderer.ready()) {
            fill(x, y, x + w, y + 1, argb);
            fill(x, y + h - 1, x + w, y + h, argb);
            fill(x, y, x + 1, y + h, argb);
            fill(x + w - 1, y, x + w, y + h, argb);
            return;
        }
        shape(x, y, w, h, radius, M_RING, 0, Math.max(1, Math.min(15, thickness)), 0, argb, 0);
    }

    /** Soft shadow around a rect, reaching {@code spread} pixels out. */
    public void shadow(int x, int y, int w, int h, int radius, int spread, int argb) {
        if (!GlassRenderer.ready()) return;
        // The padding has to stay under three half-sides for the UV packing.
        int pad = Math.min(spread, (int) (Math.min(w, h) * 1.4f));
        shape(x, y, w, h, radius, M_SHADOW, 0, 0, 0, argb, pad);
    }

    /** Hue/saturation disc; {@code value} is the brightness 0-1. */
    public void disc(int cx, int cy, int r, float value) {
        if (!GlassRenderer.ready()) return;
        int v = Math.round(clamp01(value) * 255);
        shape(cx - r, cy - r, r * 2, r * 2, 255, M_DISC, 0, 0, 0, (0xFF << 24) | (v << 16) | (v << 8) | v, 0);
    }

    /** Horizontal gradient bar, from black ({@code checker} false) or a checkerboard to {@code argb}. */
    public void bar(int x, int y, int w, int h, int radius, int argb, boolean checker) {
        if (!GlassRenderer.ready()) {
            fill(x, y, x + w, y + h, argb);
            return;
        }
        shape(x, y, w, h, radius, M_BAR, checker ? 1 : 0, 0, 0, argb | 0xFF000000, 0);
    }

    public void ring(int cx, int cy, int r, int thickness, int argb) {
        if (!GlassRenderer.ready()) {
            fill(cx - r, cy - r, cx + r, cy + r, argb);
            return;
        }
        shape(cx - r, cy - r, r * 2, r * 2, 255, M_RING, 0, Math.max(1, Math.min(15, thickness)), 0, argb, 0);
    }

    /** A colour sample over a checkerboard, so transparency shows. */
    public void swatch(int x, int y, int w, int h, int radius, int argb) {
        if (!GlassRenderer.ready()) {
            fill(x, y, x + w, y + h, argb);
            return;
        }
        int fade = Math.round((1.0f - clamp01(alpha)) * 15);
        shape(x, y, w, h, radius, M_SWATCH, 0, 0, fade, argb, 0, false);
    }

    private void shape(int x, int y, int w, int h, int radius, int mode, int flag, int pa, int pb, int argb, int pad) {
        shape(x, y, w, h, radius, mode, flag, pa, pb, argb, pad, true);
    }

    private void shape(int x, int y, int w, int h, int radius, int mode, int flag, int pa, int pb,
                       int argb, int pad, boolean applyAlpha) {
        if (w <= 0 || h <= 0) return;
        if (applyAlpha) {
            argb = mul(argb);
            if ((argb >>> 24) == 0) return;
        }
        float hw = w / 2.0f, hh = h / 2.0f;
        float shortSide = Math.min(hw, hh);
        int r = radius >= shortSide ? 255 : Math.max(0, Math.round(255.0f * radius / shortSide));
        r += 256 * guiScale();
        int p = mode * 512 + flag * 256 + pa * 16 + pb;
        float ex = pad / hw, ey = pad / hh;
        float u0 = -1 - ex + 8 * r, u1 = 1 + ex + 8 * r;
        float v0 = -1 - ey + 8 * p, v1 = 1 + ey + 8 * p;
        quad(x - pad, y - pad, x + w + pad, y + h + pad, u0, u1, v0, v1, argb);
    }

//#if MC < 12001
    private void quad(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int argb) {
        GlassRenderer.drawQuad(ms.peek().getPositionMatrix(), x0, y0, x1, y1, u0, u1, v0, v1, argb);
    }
//#elseif MC < 12111
//$$ private void quad(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int argb) {
//$$     // The quad is drawn immediately, so whatever is batched has to go first.
//$$     ctx.draw();
//$$     GlassRenderer.drawQuad(ctx.getMatrices().peek().getPositionMatrix(), x0, y0, x1, y1, u0, u1, v0, v1, argb);
//$$ }
//#elseif MC < 260100
//$$ private void quad(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int argb) {
//$$     var view = GlassRenderer.view();
//$$     if (view == null) return;
//$$     ((org.asutarisucu.mixin.ClearBlockRender.MixinDrawContextPreview) ctx).asutantweaks$drawTexturedQuad(
//$$             GlassRenderer.PIPELINE, view, GlassRenderer.sampler(), x0, y0, x1, y1, u0, u1, v0, v1, argb);
//$$ }
//#else
//$$ private void quad(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int argb) {
//$$     var view = GlassRenderer.view();
//$$     if (view == null) return;
//$$     ((org.asutarisucu.mixin.ConfigScreen.MixinGuiGraphicsGlass) g).asutantweaks$innerBlit(
//$$             GlassRenderer.PIPELINE, view, GlassRenderer.sampler(), x0, y0, x1, y1, u0, u1, v0, v1, argb);
//$$ }
//#endif

    // ── helpers ──────────────────────────────────────────────────

    /** Screen pixels per GUI pixel; the shader sizes its effects with it. */
    private static int guiScale() {
//#if MC < 260100
        int s = (int) Math.round(MinecraftClient.getInstance().getWindow().getScaleFactor());
//#else
//$$ int s = Minecraft.getInstance().getWindow().getGuiScale();
//#endif
        return Math.max(1, Math.min(15, s));
    }

    private int mul(int argb) {
        if (alpha >= 1.0f) return argb;
        int a = Math.round((argb >>> 24) * clamp01(alpha));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private static int level(float v) {
        return Math.round(clamp01(v) * 15);
    }

    public static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    /** Mixes two ARGB colours. */
    public static int lerpColor(int a, int b, float t) {
        t = clamp01(t);
        int aa = a >>> 24, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = b >>> 24, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return (Math.round(aa + (ba - aa) * t) << 24) | (Math.round(ar + (br - ar) * t) << 16)
                | (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
    }

    public static int withAlpha(int rgb, float a) {
        return (Math.round(clamp01(a) * 255) << 24) | (rgb & 0xFFFFFF);
    }
}
