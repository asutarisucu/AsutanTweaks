package org.asutarisucu.mixin.AutoFillInventory;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
//#if MC <=11904
import net.minecraft.client.util.math.MatrixStack;
//#endif
//#if MC>=12001
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
    @Inject(method = "render",at=@At("HEAD"))
//#if MC <=11904
    private void onOpenScreen(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
//#endif
//#if MC >=12001
//$$ private void onOpenScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci){
//#endif
        if(FeatureToggle.AUTO_FILL_INVENTORY.getBooleanValue()){
            org.asutarisucu.Utiles.Inventory.Screen.AutoFillInventory(handler);
        }
    }
}
