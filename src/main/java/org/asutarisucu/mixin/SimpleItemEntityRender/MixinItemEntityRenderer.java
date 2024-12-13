package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleItemEntityRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer {

    @Inject(
            method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRender(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
//        if(FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue())SimpleItemEntityRender.shouldRender(itemEntity,ci);
    }


}
