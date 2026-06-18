package org.asutarisucu.mixin.VisualiseLazyEntity;

//#if MC >= 260100
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.entity.item.FallingBlockEntity;
//#else
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
//#endif

import org.asutarisucu.tweak.VisualiseLazyEntity.VisualiseLazyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the client-side tick of a falling block while it is pinned as lazy, so it neither falls,
 * lands, nor despawns — it simply holds the authoritative server position.
 */
@Mixin(FallingBlockEntity.class)
public class MixinFallingBlockLazy {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void atweaks$cancelLazyTick(CallbackInfo ci) {
        if (VisualiseLazyEntity.shouldFreezeTick((Entity) (Object) this)) ci.cancel();
    }
}
