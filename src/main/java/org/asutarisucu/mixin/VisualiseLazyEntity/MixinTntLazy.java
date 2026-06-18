package org.asutarisucu.mixin.VisualiseLazyEntity;

//#if MC >= 260100
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.entity.item.PrimedTnt;
//#else
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
//#endif

import org.asutarisucu.tweak.VisualiseLazyEntity.VisualiseLazyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the client-side tick of primed TNT while it is pinned as lazy. Without this the client
 * counts the fuse down and {@code discard()}s the entity, making lazy TNT disappear entirely.
 */
//#if MC >= 260100
//$$ @Mixin(PrimedTnt.class)
//#else
@Mixin(TntEntity.class)
//#endif
public class MixinTntLazy {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void atweaks$cancelLazyTick(CallbackInfo ci) {
        if (VisualiseLazyEntity.shouldFreezeTick((Entity) (Object) this)) ci.cancel();
    }
}
