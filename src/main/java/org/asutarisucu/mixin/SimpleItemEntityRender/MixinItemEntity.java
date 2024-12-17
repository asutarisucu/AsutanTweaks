package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class MixinItemEntity {
    @Inject(method = "merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;)V",at = @At("HEAD"))
    private static void onMerge(ItemEntity targetEntity, ItemStack targetStack, ItemEntity sourceEntity, ItemStack sourceStack, CallbackInfo ci){
        //ItemEntityがマージする際にマージ元とマージ先のEntityを抑制リストから削除
        SimpleEntityRender.EntityUUID.remove(targetEntity.getUuid());
        SimpleEntityRender.EntityUUID.remove(sourceEntity.getUuid());
    }
}
