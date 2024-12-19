package org.asutarisucu.mixin.TheThirdEye;

import net.minecraft.client.MinecraftClient;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
import org.asutarisucu.tweak.TheThirdEye.WindowManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "disconnect()V",at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci){
        //ワールドを退出した際にwindowをクリアする
        WindowManager.window.removeAll();
    }
}
