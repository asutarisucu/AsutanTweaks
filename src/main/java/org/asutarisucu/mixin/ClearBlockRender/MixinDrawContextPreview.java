package org.asutarisucu.mixin.ClearBlockRender;

//#if MC < 12111
/**
 * Only needed on MC 1.21.11, whose DrawContext has no public way to draw a
 * GpuTextureView. Left out of the mixin config elsewhere.
 */
public final class MixinDrawContextPreview {
    private MixinDrawContextPreview() {}
}
//#elseif MC < 260100
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import net.minecraft.client.gl.GpuSampler;
//$$ import com.mojang.blaze3d.textures.GpuTextureView;
//$$ import net.minecraft.client.gui.DrawContext;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//$$
//$$ /**
//$$  * Opens up DrawContext's textured-quad draw so the Clear Block Render preview
//$$  * can be blitted.
//$$  *
//$$  * Only the Identifier-keyed overload is public on MC 1.21.11, and the preview is
//$$  * an off-screen render target rather than a registered texture. The public
//$$  * fill(pipeline, textureSetup, ...) would draw it upside down, as a render
//$$  * target's first row is the bottom of the image and it takes no UVs.
//$$  */
//$$ @Mixin(DrawContext.class)
//$$ public interface MixinDrawContextPreview {
//$$
//$$     @Invoker("drawTexturedQuad")
//$$     void asutantweaks$drawTexturedQuad(RenderPipeline pipeline, GpuTextureView texture, GpuSampler sampler,
//$$                                        int x1, int y1, int x2, int y2,
//$$                                        float u1, float v1, float u2, float v2, int color);
//$$ }
//#else
//$$ /**
//$$  * Only needed on MC 1.21.11, whose DrawContext has no public way to draw a
//$$  * GpuTextureView. Left out of the mixin config elsewhere.
//$$  */
//$$ public final class MixinDrawContextPreview {
//$$     private MixinDrawContextPreview() {}
//$$ }
//#endif
