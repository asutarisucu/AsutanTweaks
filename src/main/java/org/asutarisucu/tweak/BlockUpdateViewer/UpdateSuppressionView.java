package org.asutarisucu.tweak.BlockUpdateViewer;

//#if MC < 260100
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
//#if MC < 12001
import net.minecraft.client.util.math.MatrixStack;
//#else
//$$ import net.minecraft.client.gui.DrawContext;
//#endif
//#if MC >= 12101
//$$ import net.minecraft.client.render.RenderTickCounter;
//#endif
import net.minecraft.item.BlockItem;
import org.asutarisucu.Configs.FeatureToggle;
//#else
//$$ import fi.dy.masa.malilib.render.GuiContext;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.world.item.BlockItem;
//$$ import org.asutarisucu.Configs.FeatureToggle;
//#endif

public class UpdateSuppressionView {

    public static void register() {
//#if MC < 260100
        HudRenderCallback.EVENT.register(UpdateSuppressionView::onHudRender);
//#endif
    }

//#if MC >= 260100
//$$ public static void renderHud26(GuiContext ctx) {
//$$     if (!FeatureToggle.UPDATE_SUPPRESSION_VIEW.getBooleanValue()) return;
//$$     var mc = Minecraft.getInstance();
//$$     if (mc.player == null) return;
//$$     boolean holdingBlockItem = mc.player.getMainHandItem().getItem() instanceof BlockItem;
//$$     boolean hasSuppression = holdingBlockItem
//$$             ? !BlockUpdateCalculator.getPlacementSuppressionPositions().isEmpty()
//$$             : !BlockUpdateCalculator.getSuppressionPositions().isEmpty();
//$$     if (!hasSuppression) return;
//$$     int sw = mc.getWindow().getGuiScaledWidth();
//$$     int sh = mc.getWindow().getGuiScaledHeight();
//$$     String label = "CCE suppress Ready";
//$$     int x = (sw - mc.font.width(label)) / 2;
//$$     int y = sh - 60;
//$$     ctx.drawString(mc.font, label, x, y, 0xFF5555);
//$$ }
//#endif

//#if MC < 260100
    private static boolean hasSuppressions() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean holdingBlockItem = client.player != null
                && client.player.getMainHandStack().getItem() instanceof BlockItem;
        return holdingBlockItem
                ? !BlockUpdateCalculator.getPlacementSuppressionPositions().isEmpty()
                : !BlockUpdateCalculator.getSuppressionPositions().isEmpty();
    }

//#if MC < 12001
    private static void onHudRender(MatrixStack matrices, float tickDelta) {
        if (!FeatureToggle.UPDATE_SUPPRESSION_VIEW.getBooleanValue()) return;
        if (!hasSuppressions()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        String label = "CCE suppress Ready";
        int x = (sw - client.textRenderer.getWidth(label)) / 2;
        int y = sh - 60;
        client.textRenderer.drawWithShadow(matrices, label, (float) x, (float) y, 0xFF5555);
    }
//#else
//#if MC < 12101
    //$$ private static void onHudRender(DrawContext context, float tickDelta) { render(context); }
//#else
    //$$ private static void onHudRender(DrawContext context, RenderTickCounter tickCounter) { render(context); }
//#endif
    //$$ private static void render(DrawContext context) {
    //$$     if (!FeatureToggle.UPDATE_SUPPRESSION_VIEW.getBooleanValue()) return;
    //$$     if (!hasSuppressions()) return;
    //$$     MinecraftClient client = MinecraftClient.getInstance();
    //$$     if (client.player == null) return;
    //$$     int sw = client.getWindow().getScaledWidth();
    //$$     int sh = client.getWindow().getScaledHeight();
    //$$     String label = "CCE suppress Ready";
    //$$     int x = (sw - client.textRenderer.getWidth(label)) / 2;
    //$$     int y = sh - 60;
    //$$     context.drawTextWithShadow(client.textRenderer, label, x, y, 0xFF5555);
    //$$ }
//#endif
//#endif
}
