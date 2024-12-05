package org.asutarisucu.mixin.AutoFillInventory;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
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
    private void onOpenScreen(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        if(FeatureToggle.AUTO_FILL_INVENTORY.getBooleanValue()){
            org.asutarisucu.Utiles.Screen.AutoFillInventory(handler);
        }
    }
}
