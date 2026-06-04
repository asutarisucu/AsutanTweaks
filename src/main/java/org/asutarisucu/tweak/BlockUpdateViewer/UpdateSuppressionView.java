package org.asutarisucu.tweak.BlockUpdateViewer;

import org.asutarisucu.Configs.Feature;
import org.asutarisucu.lib.render.HudRenderer;

//#if MC < 12001
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
//#elseif MC < 260100
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gui.DrawContext;
//$$ import net.minecraft.item.BlockItem;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//$$ import net.minecraft.world.item.BlockItem;
//#endif

public class UpdateSuppressionView {

    public static void register() {
        // Registration handled by Reference.registerHud()
    }

//#if MC < 12001
    public static void renderHud(MatrixStack matrices) {
        if (!Feature.UPDATE_SUPPRESSION_VIEW.isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!hasSuppression(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;
        int sw = HudRenderer.getScaledWidth();
        int sh = HudRenderer.getScaledHeight();
        String label = "CCE suppress Ready";
        int x = (sw - HudRenderer.getTextWidth(label)) / 2;
        HudRenderer.drawText(matrices, label, x, sh - 60, 0xFF5555);
    }
//#elseif MC < 260100
//$$ public static void renderHud(DrawContext context) {
//$$     if (!Feature.UPDATE_SUPPRESSION_VIEW.isEnabled()) return;
//$$     MinecraftClient mc = MinecraftClient.getInstance();
//$$     if (mc.player == null) return;
//$$     if (!hasSuppression(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;
//$$     int sw = HudRenderer.getScaledWidth();
//$$     int sh = HudRenderer.getScaledHeight();
//$$     String label = "CCE suppress Ready";
//$$     int x = (sw - HudRenderer.getTextWidth(label)) / 2;
//$$     HudRenderer.drawText(context, label, x, sh - 60, 0xFF5555);
//$$ }
//#else
//$$ public static void renderHud(GuiGraphicsExtractor context) {
//$$     if (!Feature.UPDATE_SUPPRESSION_VIEW.isEnabled()) return;
//$$     Minecraft mc = Minecraft.getInstance();
//$$     if (mc.player == null) return;
//$$     if (!hasSuppression(mc.player.getMainHandItem().getItem() instanceof BlockItem)) return;
//$$     int sw = HudRenderer.getScaledWidth();
//$$     int sh = HudRenderer.getScaledHeight();
//$$     String label = "CCE suppress Ready";
//$$     int x = (sw - HudRenderer.getTextWidth(label)) / 2;
//$$     HudRenderer.drawText(context, label, x, sh - 60, 0xFF5555);
//$$ }
//#endif

    private static boolean hasSuppression(boolean holdingBlockItem) {
        return holdingBlockItem
                ? !BlockUpdateCalculator.getPlacementSuppressionPositions().isEmpty()
                : !BlockUpdateCalculator.getSuppressionPositions().isEmpty();
    }
}
