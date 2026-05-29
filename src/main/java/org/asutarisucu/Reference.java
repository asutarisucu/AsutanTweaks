package org.asutarisucu;

import org.asutarisucu.Event.LastUseCancel;
import org.asutarisucu.Event.RenderCash;
import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateViewer;
import org.asutarisucu.tweak.BlockUpdateViewer.UpdateSuppressionView;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
//#if MC >= 260100
//$$ import fi.dy.masa.malilib.event.RenderEventHandler;
//$$ import fi.dy.masa.malilib.interfaces.IRenderer;
//$$ import fi.dy.masa.malilib.render.GuiContext;
//$$ import org.asutarisucu.Configs.FeatureToggle;
//$$ import org.asutarisucu.tweak.SearchItems.HighlightBlock;
//$$ import org.asutarisucu.tweak.SearchItems.HighlightContainer;
//#endif

public class Reference {
    public static final String MOD_ID = "AsutanTweaks";
    public static final String VERSION = "1.1.0";

    public static void LoadEvent(){
        LastUseCancel.UseBlockEvents();
        RenderCash.registerCash();
        BlockUpdateViewer.register();
        UpdateSuppressionView.register();
        SimpleEntityRender.register();
//#if MC >= 260100
//$$         RenderEventHandler.getInstance().registerWorldLastRenderer(new IRenderer() {
//$$             @Override
//$$             public void onRenderWorldLast(
//$$                     com.mojang.blaze3d.pipeline.RenderTarget renderTarget,
//$$                     org.joml.Matrix4fc matrix4fc,
//$$                     net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState,
//$$                     net.minecraft.client.renderer.culling.Frustum frustum,
//$$                     net.minecraft.client.renderer.RenderBuffers renderBuffers,
//$$                     com.mojang.blaze3d.buffers.GpuBufferSlice gpuBufferSlice,
//$$                     org.joml.Vector4f vector4f,
//$$                     net.minecraft.util.profiling.ProfilerFiller profiler) {
//$$                 if (FeatureToggle.SEARCH_BLOCK_HIGHLIGHT.getBooleanValue()) {
//$$                     HighlightBlock.renderHighlightBlock();
//$$                 }
//$$                 if (FeatureToggle.SEARCH_CONTAINER_HIGHLIGHT.getBooleanValue()) {
//$$                     HighlightContainer.renderHighlightContainer();
//$$                 }
//$$                 BlockUpdateViewer.renderOverlay26();
//$$             }
//$$             @Override
//$$             public void onExtractGuiOverlayPost(GuiContext ctx, float partialTicks,
//$$                     net.minecraft.util.profiling.ProfilerFiller profiler) {
//$$                 UpdateSuppressionView.renderHud26(ctx);
//$$             }
//$$         });
//#endif
    }
}
