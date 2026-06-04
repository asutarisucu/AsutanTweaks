package org.asutarisucu.lib.render;

//#if MC < 12001
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
//#elseif MC < 260100
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#endif

public class HudRenderer {

//#if MC < 12001
    public static void drawText(MatrixStack matrices, String text, int x, int y, int argbColor) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.textRenderer.drawWithShadow(matrices, text, (float) x, (float) y, argbColor);
    }

    public static void fillRect(MatrixStack matrices, int x1, int y1, int x2, int y2, int argbColor) {
        DrawableHelper.fill(matrices, x1, y1, x2, y2, argbColor);
    }

    public static int getTextWidth(String text) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    public static int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }
//#elseif MC < 260100
//$$ public static void drawText(DrawContext context, String text, int x, int y, int argbColor) {
//$$     MinecraftClient mc = MinecraftClient.getInstance();
//$$     context.drawTextWithShadow(mc.textRenderer, text, x, y, argbColor);
//$$ }
//$$
//$$ public static void fillRect(DrawContext context, int x1, int y1, int x2, int y2, int argbColor) {
//$$     context.fill(x1, y1, x2, y2, argbColor);
//$$ }
//$$
//$$ public static int getTextWidth(String text) {
//$$     return MinecraftClient.getInstance().textRenderer.getWidth(text);
//$$ }
//$$
//$$ public static int getScaledWidth() {
//$$     return MinecraftClient.getInstance().getWindow().getScaledWidth();
//$$ }
//$$
//$$ public static int getScaledHeight() {
//$$     return MinecraftClient.getInstance().getWindow().getScaledHeight();
//$$ }
//#else
//$$ public static void drawText(GuiGraphicsExtractor context, String text, int x, int y, int argbColor) {
//$$     Minecraft mc = Minecraft.getInstance();
//$$     context.text(mc.font, text, x, y, argbColor, true);
//$$ }
//$$
//$$ public static void fillRect(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int argbColor) {
//$$     context.fill(x1, y1, x2, y2, argbColor);
//$$ }
//$$
//$$ public static int getTextWidth(String text) {
//$$     return Minecraft.getInstance().font.width(text);
//$$ }
//$$
//$$ public static int getScaledWidth() {
//$$     return Minecraft.getInstance().getWindow().getGuiScaledWidth();
//$$ }
//$$
//$$ public static int getScaledHeight() {
//$$     return Minecraft.getInstance().getWindow().getGuiScaledHeight();
//$$ }
//#endif
}
