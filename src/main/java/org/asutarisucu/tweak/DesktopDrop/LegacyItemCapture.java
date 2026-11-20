package org.asutarisucu.tweak.DesktopDrop;

//#if MC < 12111
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeFbo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
//#if MC >= 12001
//$$ import net.minecraft.client.gui.DrawContext;
//#else
import net.minecraft.client.util.math.MatrixStack;
//#endif

/**
 * Takes the picture of the held item on MC 1.21.1 and older, where GUI items are
 * drawn straight to the screen: the item is drawn once more into a framebuffer
 * of its own, at the top-left corner so it is never cut off by the window edge.
 */
public final class LegacyItemCapture {

    private LegacyItemCapture() {}

    private static ThirdEyeFbo fbo;

    public static void capture(ItemStack stack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        var main = mc.getFramebuffer();
        int w = main.textureWidth, h = main.textureHeight;
        if (fbo == null) {
            fbo = new ThirdEyeFbo(w, h, true, MinecraftClient.IS_SYSTEM_MAC);
        } else if (fbo.textureWidth != w || fbo.textureHeight != h) {
            fbo.resize(w, h, MinecraftClient.IS_SYSTEM_MAC);
        }
        // The screen may be drawing into a framebuffer other than the main one.
        int prevFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int[] prevViewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);
        fbo.setClearColor(0, 0, 0, 0);
        fbo.clear(MinecraftClient.IS_SYSTEM_MAC);
        fbo.beginWrite(true);
//#if MC >= 12001
//$$         DrawContext ctx = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());
//$$         ctx.drawItem(stack, 0, 0);
//$$         ctx.draw();
//#else
        mc.getItemRenderer().renderInGui(new MatrixStack(), stack, 0, 0);
//#endif
        int size = (int) Math.round(16 * mc.getWindow().getScaleFactor());
        DesktopDrop.captureTexture(fbo.getColorTexId(), 0, h - size, size, false);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GlStateManager._viewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);
    }
}
//#else
//$$ public final class LegacyItemCapture {
//$$     private LegacyItemCapture() {}
//$$ }
//#endif
