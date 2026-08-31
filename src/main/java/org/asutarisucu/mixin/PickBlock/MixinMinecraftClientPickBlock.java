package org.asutarisucu.mixin.PickBlock;

import org.asutarisucu.tweak.PickBlock.PickBlockUltimate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClientPickBlock {

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void asutantweaks$pickBlockUltimate(CallbackInfo ci) {
        if (PickBlockUltimate.pick()) ci.cancel();
    }
}
//#else
//$$ import net.minecraft.client.Minecraft;
//$$
//$$ @Mixin(Minecraft.class)
//$$ public class MixinMinecraftClientPickBlock {
//$$
//$$     @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
//$$     private void asutantweaks$pickBlockUltimate(CallbackInfo ci) {
//$$         if (PickBlockUltimate.pick()) ci.cancel();
//$$     }
//$$ }
//#endif
