package org.asutarisucu;

import org.asutarisucu.Configs.Feature;
import org.asutarisucu.GUI.HudLogger;
import org.asutarisucu.GUI.ProgressMeter;
import org.asutarisucu.Event.LastUseCancel;
import org.asutarisucu.Event.RenderCash;
import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateViewer;
import org.asutarisucu.tweak.BlockUpdateViewer.UpdateSuppressionView;
import org.asutarisucu.tweak.SearchItems.HighlightBlock;
import org.asutarisucu.tweak.SearchItems.HighlightContainer;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
import org.asutarisucu.tweak.ThirdEye.ThirdEye;
import org.asutarisucu.tweak.VisualiseLazyEntity.VisualiseLazyEntity;
import org.asutarisucu.tweak.WorldEditGUI.SelectionGridRenderer;
import org.asutarisucu.tweak.WorldEditGUI.WorldEditCui;

//#if MC >= 12111
//$$ import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
//#if MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
//$$ import net.minecraft.util.Identifier;
//#else
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
//$$ import net.minecraft.resources.Identifier;
//#endif
//#else
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//#endif

public class Reference {
    public static final String MOD_ID = "AsutanTweaks";
    public static final String VERSION = "1.3.3";

    public static void LoadEvent() {
        LastUseCancel.UseBlockEvents();
        RenderCash.registerCash();
        BlockUpdateViewer.register();
        UpdateSuppressionView.register();
        SimpleEntityRender.register();
        ThirdEye.register();
        VisualiseLazyEntity.register();
        WorldEditCui.register();
        registerWorldRendering();
        registerHud();
    }

    private static void registerWorldRendering() {
//#if MC >= 260100
//$$ LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
//$$     if (Feature.SEARCH_BLOCK_HIGHLIGHT.isEnabled())    HighlightBlock.render(context);
//$$     if (Feature.SEARCH_CONTAINER_HIGHLIGHT.isEnabled()) HighlightContainer.render(context);
//$$     if (Feature.WORLDEDIT_GUI.isEnabled())              SelectionGridRenderer.render(context);
//$$     BlockUpdateViewer.renderOverlay(context);
//$$ });
//#else
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (Feature.SEARCH_BLOCK_HIGHLIGHT.isEnabled())    HighlightBlock.render(context);
            if (Feature.SEARCH_CONTAINER_HIGHLIGHT.isEnabled()) HighlightContainer.render(context);
            if (Feature.WORLDEDIT_GUI.isEnabled())              SelectionGridRenderer.render(context);
//#if MC >= 12111
//$$ BlockUpdateViewer.renderOverlay(context);
//#else
            BlockUpdateViewer.renderOverlay(context);
//#endif
        });
//#endif
    }

    private static void registerHud() {
//#if MC < 12001
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            UpdateSuppressionView.renderHud(matrices);
            HudLogger.INSTANCE.render(matrices);
            ProgressMeter.INSTANCE.render(matrices);
        });
//#elseif MC < 12101
        //$$ HudRenderCallback.EVENT.register((context, tickDelta) -> {
        //$$     UpdateSuppressionView.renderHud(context);
        //$$     HudLogger.INSTANCE.render(context);
        //$$     ProgressMeter.INSTANCE.render(context);
        //$$ });
//#elseif MC < 12111
        //$$ HudRenderCallback.EVENT.register((context, tickCounter) -> {
        //$$     UpdateSuppressionView.renderHud(context);
        //$$     HudLogger.INSTANCE.render(context);
        //$$     ProgressMeter.INSTANCE.render(context);
        //$$ });
//#elseif MC < 260100
//$$ HudElementRegistry.addLast(
//$$     Identifier.of("asutantweaks", "update_suppression"),
//$$     (context, tickCounter) -> UpdateSuppressionView.renderHud(context)
//$$ );
//$$ HudElementRegistry.addLast(
//$$     Identifier.of("asutantweaks", "hud_logger"),
//$$     (context, tickCounter) -> HudLogger.INSTANCE.render(context)
//$$ );
//$$ HudElementRegistry.addLast(
//$$     Identifier.of("asutantweaks", "progress_meter"),
//$$     (context, tickCounter) -> ProgressMeter.INSTANCE.render(context)
//$$ );
//#else
//$$ HudElementRegistry.addLast(
//$$     Identifier.fromNamespaceAndPath("asutantweaks", "update_suppression"),
//$$     (extractor, tickDelta) -> UpdateSuppressionView.renderHud(extractor)
//$$ );
//$$ HudElementRegistry.addLast(
//$$     Identifier.fromNamespaceAndPath("asutantweaks", "hud_logger"),
//$$     (extractor, tickDelta) -> HudLogger.INSTANCE.render(extractor)
//$$ );
//$$ HudElementRegistry.addLast(
//$$     Identifier.fromNamespaceAndPath("asutantweaks", "progress_meter"),
//$$     (extractor, tickDelta) -> ProgressMeter.INSTANCE.render(extractor)
//$$ );
//#endif
    }
}
