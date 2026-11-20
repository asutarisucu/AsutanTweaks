package org.asutarisucu.mixin.DesktopDrop;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import org.asutarisucu.tweak.DesktopDrop.DesktopDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC >= 260100
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#else
import net.minecraft.client.MinecraftClient;
//#if MC >= 12001
//$$ import net.minecraft.client.gui.DrawContext;
//#else
import net.minecraft.client.util.math.MatrixStack;
//#endif
//#endif
//#if MC < 12111
import org.asutarisucu.tweak.DesktopDrop.LegacyItemCapture;
//#endif

/** Reports the item held on the cursor each frame it is drawn. */
@Mixin(HandledScreen.class)
public abstract class MixinHandledScreenDesktopDrop {

//#if MC >= 260100
//$$ @Inject(method = "extractFloatingItem", at = @At("HEAD"))
//$$ private void desktopDrop(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, String amount, CallbackInfo ci) {
//$$     DesktopDrop.onFloatingItem(Minecraft.getInstance().getWindow().handle(), x, y);
//$$ }
//#elseif MC >= 12111
//$$ @Inject(method = "drawItem", at = @At("HEAD"))
//$$ private void desktopDrop(DrawContext context, ItemStack stack, int x, int y, String amount, CallbackInfo ci) {
//$$     DesktopDrop.onFloatingItem(MinecraftClient.getInstance().getWindow().getHandle(), x, y);
//$$ }
//#elseif MC >= 12001
//$$ @Inject(method = "drawItem", at = @At("HEAD"))
//$$ private void desktopDrop(DrawContext context, ItemStack stack, int x, int y, String amount, CallbackInfo ci) {
//$$     DesktopDrop.onFloatingItem(MinecraftClient.getInstance().getWindow().getHandle(), x, y);
//$$     if (DesktopDrop.wantsCapture()) {
//$$         // Flush what the screen has queued so it lands on the screen, not in the capture.
//$$         context.draw();
//$$         LegacyItemCapture.capture(stack);
//$$     }
//$$ }
//#else
    @Inject(method = "drawItem", at = @At("HEAD"))
    private void desktopDrop(MatrixStack matrices, ItemStack stack, int x, int y, String amount, CallbackInfo ci) {
        DesktopDrop.onFloatingItem(MinecraftClient.getInstance().getWindow().getHandle(), x, y);
        if (DesktopDrop.wantsCapture()) LegacyItemCapture.capture(stack);
    }
//#endif
}
