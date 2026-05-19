package org.asutarisucu.mixin.AutoFillInventory;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
//#if MC <=11904
import net.minecraft.client.util.math.MatrixStack;
//#endif
//#if MC>=260100
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#elseif MC>=12001
//$$ import net.minecraft.client.gui.DrawContext;
//#endif
import org.asutarisucu.Configs.FeatureToggle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen {
    @Shadow
    @Final
    protected T handler;
    protected MixinHandledScreen(Text title) {
        super(title);
    }
//#if MC>=260100
//$$ @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",at=@At("HEAD"))
//$$ private void onOpenScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci){
//#elseif MC>=12001
//$$ @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",at=@At("HEAD"))
//$$ private void onOpenScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci){
//#else
    @Inject(method = "render",at=@At("HEAD"))
    private void onOpenScreen(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
//#endif
        if(FeatureToggle.AUTO_FILL_INVENTORY.getBooleanValue()){
            org.asutarisucu.Utiles.Inventory.Screen.AutoFillInventory(this.handler);
        }
    }
}
