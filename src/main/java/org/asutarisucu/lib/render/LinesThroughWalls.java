package org.asutarisucu.lib.render;

//#if MC < 260100
/** Only used from MC 26.1; older versions turn the depth test off with RenderSystem. */
public final class LinesThroughWalls {
    private LinesThroughWalls() {}
}
//#else
//$$ import com.mojang.blaze3d.pipeline.DepthStencilState;
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import com.mojang.blaze3d.platform.CompareOp;
//$$ import net.minecraft.client.renderer.rendertype.LayeringTransform;
//$$ import net.minecraft.client.renderer.rendertype.OutputTarget;
//$$ import net.minecraft.client.renderer.rendertype.RenderSetup;
//$$ import net.minecraft.client.renderer.rendertype.RenderType;
//$$ import net.minecraft.resources.Identifier;
//$$ import org.asutarisucu.mixin.SearchItems.MixinRenderPipelinesAccessor;
//$$ import org.asutarisucu.mixin.SearchItems.MixinRenderTypeInvoker;
//$$
//$$ /**
//$$  * RenderTypes.lines() with the depth test turned off, so the lines show through
//$$  * blocks. Same pipeline snippet and render setup as vanilla's LINES.
//$$  */
//$$ public final class LinesThroughWalls {
//$$     private static RenderType type;
//$$
//$$     private LinesThroughWalls() {}
//$$
//$$     /** Built on first use. */
//$$     public static RenderType get() {
//$$         if (type == null) {
//$$             RenderPipeline pipeline = RenderPipeline.builder(MixinRenderPipelinesAccessor.getLinesSnippet())
//$$                     .withLocation(Identifier.fromNamespaceAndPath("asutantweaks", "pipeline/lines_through_walls"))
//$$                     .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
//$$                     .build();
//$$             type = MixinRenderTypeInvoker.invokeCreate("asutantweaks_lines_through_walls",
//$$                     RenderSetup.builder(pipeline)
//$$                             .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
//$$                             .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
//$$                             .createRenderSetup());
//$$         }
//$$         return type;
//$$     }
//$$ }
//#endif
