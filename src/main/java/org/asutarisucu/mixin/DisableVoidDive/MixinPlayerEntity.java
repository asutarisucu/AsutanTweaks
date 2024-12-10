package org.asutarisucu.mixin.DisableVoidDive;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.tweak.DisableVoidDive.DisableVoidDive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @Inject(method = "tick",at=@At("HEAD"))
    private void onTick(CallbackInfo ci){
        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player!=null){
            if(FeatureToggle.DISABLE_VOID_DIVE.getBooleanValue()){
                if(!player.isOnGround()){
                    String dimension = player.getWorld().getRegistryKey().getValue().toString();
                    DisableVoidDive.safeVoidDive(dimension);
                }
            }
        }
    }
}
