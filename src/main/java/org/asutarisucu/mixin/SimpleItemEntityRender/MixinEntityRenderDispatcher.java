package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Inject(method = "render",at = @At("HEAD"), cancellable = true)
    private void onRender(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        //ItemEntityの場合
        if(entity instanceof ItemEntity){
            if(FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue()){
                if(SimpleEntityRender.checkSuppressed(entity)) {
                    SimpleEntityRender.suppressRenderItem((ItemEntity) entity,ci);
                }else ci.cancel();
            }
            //MobEntityの場合
        } else if (entity instanceof MobEntity) {
            if (FeatureToggle.SIMPLE_MOB_ENTITY_RENDER.getBooleanValue()){
                if(SimpleEntityRender.checkSuppressed(entity)) {
                    SimpleEntityRender.suppressRenderMob((MobEntity) entity,ci);
                }else ci.cancel();
            }
        }
    }
}
