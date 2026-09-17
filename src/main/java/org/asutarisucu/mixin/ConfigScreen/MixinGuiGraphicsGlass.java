package org.asutarisucu.mixin.ConfigScreen;

//#if MC < 260100
/**
 * Only needed on MC 26.x. MC 1.21.11 uses MixinDrawContextPreview for the same
 * purpose, and earlier versions draw the glass quads directly.
 */
public final class MixinGuiGraphicsGlass {
    private MixinGuiGraphicsGlass() {}
}
//#else
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import com.mojang.blaze3d.textures.GpuSampler;
//$$ import com.mojang.blaze3d.textures.GpuTextureView;
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//$$
//$$ /**
//$$  * Opens up the blit that takes a pipeline and a texture view, so the glass
//$$  * shader can be used from the config screen. The public blits either fix the
//$$  * pipeline to GUI_TEXTURED or want a registered texture.
//$$  */
//$$ @Mixin(GuiGraphicsExtractor.class)
//$$ public interface MixinGuiGraphicsGlass {
//$$
//$$     @Invoker("innerBlit")
//$$     void asutantweaks$innerBlit(RenderPipeline pipeline, GpuTextureView textureView, GpuSampler sampler,
//$$                                 int x0, int y0, int x1, int y1,
//$$                                 float u0, float u1, float v0, float v1, int color);
//$$ }
//#endif
