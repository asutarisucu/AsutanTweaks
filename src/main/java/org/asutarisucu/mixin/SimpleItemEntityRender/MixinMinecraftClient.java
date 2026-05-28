package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.client.MinecraftClient;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
//#if MC >= 260100
//$$ @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
//$$ private void onDisconnect(net.minecraft.client.gui.screens.Screen screen, boolean bl, CallbackInfo ci) {
//#else
    @Inject(method = "disconnect()V",at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci){
//#endif
        //ワールドを退出した際にMapをクリアする
        SimpleEntityRender.clearAll();
    }
}
