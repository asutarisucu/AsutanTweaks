package org.asutarisucu.mixin.EnderChestMaterialList;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.tweak.EnderChestMaterialList.EnderChestInteraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {
    @Inject(method = "close", at = @At("HEAD"))
    private void onClosed(CallbackInfo ci){
        if(FeatureToggle.ENDERCHEST_MATERIALLIST.getBooleanValue()){
            HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
            EnderChestInteraction.close(screen);
        }
    }
}
