package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import org.asutarisucu.Utiles.Render.Renderer;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleItemEntityRender;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRender {
    @Shadow @Final private TextRenderer textRenderer;

    @Inject(method = "render",at = @At("HEAD"))
    private void onRender(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        if(!SimpleItemEntityRender.EntityUUID.containsKey(entity.getUuid())){
            long count=SimpleItemEntityRender.EntityUUID.values().stream()
                    .filter(value->value==entity)
                    .count();
            if (count>0) Renderer.renderCount(entity,matrices,vertexConsumers,textRenderer,String.valueOf(count+1));
        }
    }
}
