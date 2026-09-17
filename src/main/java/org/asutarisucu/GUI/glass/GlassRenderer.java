package org.asutarisucu.GUI.glass;

import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeFbo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

//#if MC < 12111
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
//#elseif MC < 260100
//$$ import com.mojang.blaze3d.pipeline.BlendFunction;
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import com.mojang.blaze3d.platform.DepthTestFunction;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import com.mojang.blaze3d.textures.AddressMode;
//$$ import com.mojang.blaze3d.textures.FilterMode;
//$$ import com.mojang.blaze3d.textures.GpuTextureView;
//$$ import com.mojang.blaze3d.vertex.VertexFormat;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gl.Framebuffer;
//$$ import net.minecraft.client.gl.GpuSampler;
//$$ import net.minecraft.client.gl.UniformType;
//$$ import net.minecraft.client.render.VertexFormats;
//$$ import net.minecraft.client.texture.NativeImage;
//$$ import net.minecraft.client.texture.NativeImageBackedTexture;
//$$ import net.minecraft.util.Identifier;
//#else
//$$ import com.mojang.blaze3d.pipeline.BlendFunction;
//$$ import com.mojang.blaze3d.pipeline.ColorTargetState;
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import com.mojang.blaze3d.pipeline.RenderTarget;
//$$ import com.mojang.blaze3d.platform.NativeImage;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import com.mojang.blaze3d.textures.AddressMode;
//$$ import com.mojang.blaze3d.textures.FilterMode;
//$$ import com.mojang.blaze3d.textures.GpuSampler;
//$$ import com.mojang.blaze3d.textures.GpuTextureView;
//$$ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.renderer.texture.DynamicTexture;
//$$ import net.minecraft.resources.Identifier;
//#if MC < 260200
//$$ import com.mojang.blaze3d.shaders.UniformType;
//$$ import com.mojang.blaze3d.vertex.VertexFormat;
//#else
//$$ import com.mojang.blaze3d.PrimitiveTopology;
//$$ import net.minecraft.client.renderer.BindGroupLayouts;
//#endif
//#endif

/**
 * Owns what the glass shader samples and how its quads reach the GPU.
 *
 * The shader bends and blurs "what is behind the screen". That is the background
 * image when one is set, or else a copy of the main render target taken before
 * any GUI is drawn: the world, or the title panorama when there is no world.
 *
 * Before MC 1.21.11 the GUI draws immediately, so the copy is taken at the start
 * of the screen's render and each quad is drawn on the spot with a core shader.
 * From 1.21.11 the screen only records GUI state and everything is drawn later,
 * so the copy is taken by a mixin right before the GUI pass, and quads go through
 * a RenderPipeline.
 */
public final class GlassRenderer {

    private GlassRenderer() {}

    private static ThirdEyeFbo backdrop;
    private static boolean captureWanted;
    private static boolean useCopy;

    /** The user's image, already fitted to the window. */
    private static Object imageTexture;
    private static int imageW, imageH;
    /** Path {@link #imageTexture} was made from. */
    private static String imageKey;
    private static BufferedImage userImage;
    private static String userImagePath;

    // ── setup ────────────────────────────────────────────────────

//#if MC < 12111
    private static ShaderProgram program;

    public static void init() {
        CoreShaderRegistrationCallback.EVENT.register(ctx -> ctx.register(
//#if MC < 12001
                new Identifier("asutantweaks", "glass_legacy"),
//#else
//$$                 Identifier.of("asutantweaks", "glass_legacy"),
//#endif
                VertexFormats.POSITION_TEXTURE_COLOR, p -> program = p));
    }

    public static boolean ready() {
        return program != null;
    }
//#elseif MC < 260100
//$$ public static final RenderPipeline PIPELINE = RenderPipeline.builder()
//$$         .withLocation(Identifier.of("asutantweaks", "pipeline/glass"))
//$$         .withVertexShader(Identifier.of("asutantweaks", "core/glass"))
//$$         .withFragmentShader(Identifier.of("asutantweaks", "core/glass"))
//$$         .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
//$$         .withUniform("Projection", UniformType.UNIFORM_BUFFER)
//$$         .withSampler("Sampler0")
//$$         .withBlend(BlendFunction.TRANSLUCENT)
//$$         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
//$$         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//$$         .build();
//$$
//$$ public static void init() {}
//$$
//$$ public static boolean ready() {
//$$     return true;
//$$ }
//#else
//#if MC < 260200
//$$ public static final RenderPipeline PIPELINE = RenderPipeline.builder()
//$$         .withLocation(Identifier.fromNamespaceAndPath("asutantweaks", "pipeline/glass"))
//$$         .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
//$$         .withUniform("Projection", UniformType.UNIFORM_BUFFER)
//$$         .withVertexShader(Identifier.fromNamespaceAndPath("asutantweaks", "core/glass"))
//$$         .withFragmentShader(Identifier.fromNamespaceAndPath("asutantweaks", "core/glass"))
//$$         .withSampler("Sampler0")
//$$         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
//$$         .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
//$$         .build();
//#else
//$$ // Same layout as the vanilla GUI_TEXTURED pipeline, with the glass shaders.
//$$ public static final RenderPipeline PIPELINE = RenderPipeline.builder()
//$$         .withBindGroupLayout(BindGroupLayouts.GLOBALS)
//$$         .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
//$$         .withVertexShader(Identifier.fromNamespaceAndPath("asutantweaks", "core/glass"))
//$$         .withFragmentShader(Identifier.fromNamespaceAndPath("asutantweaks", "core/glass"))
//$$         .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
//$$         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
//$$         .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
//$$         .withPrimitiveTopology(PrimitiveTopology.QUADS)
//$$         .withLocation(Identifier.fromNamespaceAndPath("asutantweaks", "pipeline/glass"))
//$$         .build();
//#endif
//$$
//$$ public static void init() {}
//$$
//$$ public static boolean ready() {
//$$     return true;
//$$ }
//#endif

    // ── per frame ────────────────────────────────────────────────

    /**
     * Whether the screen should draw the title panorama under itself, for the
     * backdrop to copy.
     */
    public static boolean wantsPanorama(boolean hasWorld) {
        return !hasWorld && !imageUsable();
    }

    private static boolean imageUsable() {
        String path = org.asutarisucu.Configs.Configs.Ui.BACKGROUND_IMAGE.getValue();
        return !path.isEmpty() && loadUserImage(path);
    }

    /**
     * Called at the start of the screen's render with the window's framebuffer
     * size. Picks the backdrop source and, before MC 1.21.11, takes the copy.
     */
    public static void beginFrame(int fbWidth, int fbHeight) {
        fbWidth = Math.max(1, fbWidth);
        fbHeight = Math.max(1, fbHeight);
        useCopy = !imageUsable();

        if (useCopy) {
            ensureBackdrop(fbWidth, fbHeight);
//#if MC < 12111
            copyMainTarget();
//#else
//$$         captureWanted = true;
//#endif
        } else {
            ensureImage(org.asutarisucu.Configs.Configs.Ui.BACKGROUND_IMAGE.getValue(), fbWidth, fbHeight);
        }
//#if MC < 12111
        // HUD item icons leave depth behind, which would show them through the
        // backdrop and the panels drawn over it.
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
//#endif
    }

    /** From MC 1.21.11: called right before the GUI pass draws. */
    public static void captureBeforeGui() {
        if (!captureWanted) return;
        captureWanted = false;
//#if MC >= 12111
//$$     copyMainTarget();
//#endif
    }

    /** Frees the copy of the screen; the fitted image is kept for the next opening. */
    public static void release() {
        captureWanted = false;
        if (backdrop != null) {
            backdrop.delete();
            backdrop = null;
        }
    }

    // ── backdrop copy ────────────────────────────────────────────

    private static void ensureBackdrop(int w, int h) {
//#if MC < 12111
        if (backdrop != null && backdrop.textureWidth == w && backdrop.textureHeight == h) return;
        if (backdrop != null) backdrop.delete();
        backdrop = new ThirdEyeFbo(w, h, false, MinecraftClient.IS_SYSTEM_MAC);
        backdrop.setTexFilter(GL11.GL_LINEAR);
        // Creating a framebuffer leaves framebuffer 0 bound, not the game's.
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
//#else
//$$     if (backdrop != null && backdrop.textureWidth == w && backdrop.textureHeight == h) return;
//$$     if (backdrop != null) backdrop.delete();
//$$     backdrop = new ThirdEyeFbo(w, h, false);
//#endif
    }

//#if MC < 12111
    private static void copyMainTarget() {
        Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
        if (backdrop == null || backdrop.textureWidth != main.textureWidth
                || backdrop.textureHeight != main.textureHeight) return;
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.fbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, backdrop.fbo);
        GlStateManager._glBlitFrameBuffer(0, 0, main.textureWidth, main.textureHeight,
                0, 0, main.textureWidth, main.textureHeight, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        main.beginWrite(true);
    }
//#elseif MC < 260100
//$$ private static void copyMainTarget() {
//$$     Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
//$$     if (backdrop == null || main.getColorAttachment() == null
//$$             || backdrop.textureWidth != main.textureWidth || backdrop.textureHeight != main.textureHeight) return;
//$$     RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(main.getColorAttachment(),
//$$             backdrop.getColorAttachment(), 0, 0, 0, 0, 0, main.textureWidth, main.textureHeight);
//$$ }
//#else
//$$ private static void copyMainTarget() {
//#if MC < 260200
//$$     RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
//#else
//$$     RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
//#endif
//$$     if (backdrop == null || main.getColorTexture() == null
//$$             || backdrop.width != main.width || backdrop.height != main.height) return;
//$$     RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(main.getColorTexture(),
//$$             backdrop.getColorTexture(), 0, 0, 0, 0, 0, main.width, main.height);
//$$ }
//#endif

    // ── images ───────────────────────────────────────────────────

    /** Reads the user's image once per path. @return false when it cannot be read */
    private static boolean loadUserImage(String path) {
        if (path.equals(userImagePath)) return userImage != null;
        userImagePath = path;
        userImage = BackgroundImage.read(path);
        return userImage != null;
    }

    /** Drops the cached image so the next frame reads the file again. */
    public static void forgetImage() {
        userImagePath = null;
        userImage = null;
        imageKey = null;
    }

    private static void ensureImage(String path, int fbW, int fbH) {
        // The texture is fitted to the window shape, so a resize rebuilds it.
        int w = Math.min(fbW, 1920);
        int h = Math.max(1, Math.round((float) fbH * w / fbW));
        if (path.equals(imageKey) && imageTexture != null && imageW == w && imageH == h) return;
        BufferedImage src = BackgroundImage.cover(userImage, w, h);
        closeImage();
        imageKey = path;
        imageW = w;
        imageH = h;
        try {
            imageTexture = upload(BackgroundImage.flipped(src));
        } catch (Exception e) {
            AsutanTweaks.LOGGER.warn("[GlassRenderer] Failed to upload the background", e);
            imageTexture = null;
        }
    }

    private static void closeImage() {
        if (imageTexture == null) return;
//#if MC < 12111
        ((NativeImageBackedTexture) imageTexture).close();
//#elseif MC < 260100
//$$     ((NativeImageBackedTexture) imageTexture).close();
//#else
//$$     ((DynamicTexture) imageTexture).close();
//#endif
        imageTexture = null;
    }

    /**
     * Uploads a picture as a texture. Rows are stored bottom first, the same way
     * a render target holds them, so the shader samples both alike.
     */
    private static Object upload(BufferedImage img) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", bytes);
        NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes.toByteArray()));
//#if MC < 12111
        NativeImageBackedTexture tex = new NativeImageBackedTexture(image);
        tex.setFilter(true, false);
        GlStateManager._bindTexture(tex.getGlId());
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        return tex;
//#elseif MC < 260100
//$$     return new NativeImageBackedTexture(() -> "asutantweaks glass background", image);
//#else
//$$     return new DynamicTexture(() -> "asutantweaks glass background", image);
//#endif
    }

    // ── what the shader samples ──────────────────────────────────

//#if MC < 12111
    private static int textureId() {
        if (useCopy && backdrop != null) return backdrop.getColorTexId();
        return imageTexture != null ? ((NativeImageBackedTexture) imageTexture).getGlId() : 0;
    }

    /**
     * Draws one glass quad right away. {@code u}/{@code v} carry the packed shape
     * parameters, see glass_decode.glsl.
     */
    public static void drawQuad(Matrix4f m, float x0, float y0, float x1, float y1,
                                float u0, float u1, float v0, float v1, int argb) {
        if (program == null) return;
        int a = argb >>> 24, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        RenderSystem.setShader(() -> program);
        RenderSystem.setShaderTexture(0, textureId());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
//#if MC < 12101
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(m, x0, y1, 0.0f).texture(u0, v1).color(r, g, b, a).next();
        buffer.vertex(m, x1, y1, 0.0f).texture(u1, v1).color(r, g, b, a).next();
        buffer.vertex(m, x1, y0, 0.0f).texture(u1, v0).color(r, g, b, a).next();
        buffer.vertex(m, x0, y0, 0.0f).texture(u0, v0).color(r, g, b, a).next();
//#else
//$$     BufferBuilder buffer = Tessellator.getInstance()
//$$             .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
//$$     buffer.vertex(m, x0, y1, 0.0f).texture(u0, v1).color(r, g, b, a);
//$$     buffer.vertex(m, x1, y1, 0.0f).texture(u1, v1).color(r, g, b, a);
//$$     buffer.vertex(m, x1, y0, 0.0f).texture(u1, v0).color(r, g, b, a);
//$$     buffer.vertex(m, x0, y0, 0.0f).texture(u0, v0).color(r, g, b, a);
//#endif
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
//#elseif MC < 260100
//$$ public static GpuTextureView view() {
//$$     if (useCopy && backdrop != null) return backdrop.getColorAttachmentView();
//$$     return imageTexture != null ? ((NativeImageBackedTexture) imageTexture).getGlTextureView() : null;
//$$ }
//$$
//$$ public static GpuSampler sampler() {
//$$     return RenderSystem.getSamplerCache().get(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
//$$             FilterMode.LINEAR, FilterMode.LINEAR, false);
//$$ }
//#else
//$$ public static GpuTextureView view() {
//$$     if (useCopy && backdrop != null) return backdrop.getColorTextureView();
//$$     return imageTexture != null ? ((DynamicTexture) imageTexture).getTextureView() : null;
//$$ }
//$$
//$$ public static GpuSampler sampler() {
//$$     return RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
//$$             FilterMode.LINEAR, FilterMode.LINEAR, false);
//$$ }
//#endif
}
