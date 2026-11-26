package org.asutarisucu.mixin.SearchItems;

//#if MC < 260100
/**
 * Only needed from MC 26.1, to build a lines pipeline drawn through walls.
 * Left out of the mixin config before that.
 */
public final class MixinRenderPipelinesAccessor {
    private MixinRenderPipelinesAccessor() {}
}
//#else
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import net.minecraft.client.renderer.RenderPipelines;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$
//$$ @Mixin(RenderPipelines.class)
//$$ public interface MixinRenderPipelinesAccessor {
//$$     @Accessor("LINES_SNIPPET")
//$$     static RenderPipeline.Snippet getLinesSnippet() {
//$$         throw new AssertionError();
//$$     }
//$$ }
//#endif
