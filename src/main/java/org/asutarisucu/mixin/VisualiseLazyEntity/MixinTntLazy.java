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
 *
 * <p>Additionally, while the feature is enabled the client is never allowed to self-discard a TNT
 * whose fuse is about to expire: the explosion and the actual removal are server-authoritative (the
 * server sends a remove packet). Detection runs on a sampling interval, so a TNT moved into a lazy
 * chunk in its final game tick would otherwise vanish before it can be recognised as lazy.
 */
//#if MC >= 260100
//$$ @Mixin(PrimedTnt.class)
//#else
@Mixin(TntEntity.class)
//#endif
public class MixinTntLazy {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void atweaks$cancelLazyTick(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (VisualiseLazyEntity.shouldFreezeTick(self)) {
            ci.cancel();
            return;
        }
        if (VisualiseLazyEntity.isActiveClientEntity(self)) {
//#if MC >= 260100
//$$         if (((PrimedTnt) (Object) this).getFuse() <= 1) ci.cancel();
//#else
            if (((TntEntity) (Object) this).getFuse() <= 1) ci.cancel();
//#endif
        }
    }
}
