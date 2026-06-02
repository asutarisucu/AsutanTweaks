package org.asutarisucu.mixin.SimpleItemEntityRender;

//#if MC < 12111
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
//#elseif MC < 260100
//$$ import net.minecraft.client.render.entity.EntityRenderManager;
//$$ import net.minecraft.client.render.entity.state.EntityRenderState;
//$$ import net.minecraft.client.render.entity.state.ItemEntityRenderState;
//$$ import net.minecraft.client.render.entity.state.LivingEntityRenderState;
//$$ import net.minecraft.client.render.state.CameraRenderState;
//$$ import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.entity.Entity;
//#else
//$$ import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
//$$ import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
//$$ import net.minecraft.client.renderer.state.level.CameraRenderState;
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.minecraft.world.entity.Entity;
//#endif
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC < 12111
@Mixin(EntityRenderDispatcher.class)
//#elseif MC < 260100
//$$ @Mixin(EntityRenderManager.class)
//#else
//$$ @Mixin(EntityRenderDispatcher.class)
//#endif
public class MixinEntityRenderDispatcher {

//#if MC < 12111
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
                          MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof ItemEntity) {
            if (FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue()
                    && SimpleEntityRender.isSuppressed(entity.getId())) {
                ci.cancel();
            }
        } else if (entity instanceof MobEntity) {
            if (FeatureToggle.SIMPLE_MOB_ENTITY_RENDER.getBooleanValue()
                    && SimpleEntityRender.isSuppressed(entity.getId())) {
                ci.cancel();
            }
        }
    }
//#elseif MC < 260100
//$$ @Inject(method = "getAndUpdateRenderState", at = @At("RETURN"))
//$$ private void trackState(Entity entity, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
//$$     SimpleEntityRender.stateEntityIds.put(cir.getReturnValue(), entity.getId());
//$$ }
//$$
//$$ @Inject(method = "render", at = @At("HEAD"), cancellable = true)
//$$ private void onRender(EntityRenderState state, CameraRenderState camera,
//$$                        double x, double y, double z,
//$$                        MatrixStack matrices, OrderedRenderCommandQueue queue, CallbackInfo ci) {
//$$     Integer entityId = SimpleEntityRender.stateEntityIds.get(state);
//$$     if (entityId == null) return;
//$$     if (state instanceof ItemEntityRenderState) {
//$$         if (FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue() && SimpleEntityRender.isSuppressed(entityId))
//$$             ci.cancel();
//$$     } else if (state instanceof LivingEntityRenderState) {
//$$         if (FeatureToggle.SIMPLE_MOB_ENTITY_RENDER.getBooleanValue() && SimpleEntityRender.isSuppressed(entityId))
//$$             ci.cancel();
//$$     }
//$$ }
//#else
//$$ @Inject(method = "extractEntity", at = @At("RETURN"))
//$$ private void trackState(Entity entity, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
//$$     SimpleEntityRender.stateEntityIds.put(cir.getReturnValue(), entity.getId());
//$$ }
//$$
//$$ @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
//$$ private void onSubmit(EntityRenderState state, CameraRenderState camera,
//$$                        double x, double y, double z,
//$$                        PoseStack matrices, SubmitNodeCollector queue, CallbackInfo ci) {
//$$     Integer entityId = SimpleEntityRender.stateEntityIds.get(state);
//$$     if (entityId == null) return;
//$$     if (state instanceof ItemEntityRenderState) {
//$$         if (FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue() && SimpleEntityRender.isSuppressed(entityId))
//$$             ci.cancel();
//$$     } else if (state instanceof LivingEntityRenderState) {
//$$         if (FeatureToggle.SIMPLE_MOB_ENTITY_RENDER.getBooleanValue() && SimpleEntityRender.isSuppressed(entityId))
//$$             ci.cancel();
//$$     }
//$$ }
//#endif
}
