package org.asutarisucu.mixin.SimpleItemEntityRender;

//#if MC < 12111
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
//#elseif MC < 260100
//$$ import net.minecraft.client.render.entity.EntityRenderer;
//$$ import net.minecraft.client.render.entity.state.EntityRenderState;
//$$ import net.minecraft.client.render.state.CameraRenderState;
//$$ import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//$$ import net.minecraft.client.util.math.MatrixStack;
//#elseif MC < 260200
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.gui.Font;
//$$ import net.minecraft.client.renderer.MultiBufferSource;
//$$ import net.minecraft.client.renderer.entity.EntityRenderer;
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import net.minecraft.client.renderer.state.level.CameraRenderState;
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#else
//$$ import net.minecraft.client.renderer.entity.EntityRenderer;
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import net.minecraft.client.renderer.state.level.CameraRenderState;
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import net.minecraft.network.chat.Component;
//$$ import net.minecraft.world.phys.Vec3;
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif

import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.Utiles.Render.Renderer;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRender {
//#if MC < 12111
    @org.spongepowered.asm.mixin.Shadow @org.spongepowered.asm.mixin.Final private TextRenderer textRenderer;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(Entity entity, float yaw, float tickDelta, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (FeatureToggle.SIMPLE_ENTITY_RENDER_COUNT.getBooleanValue()) {
            int count = SimpleEntityRender.getSuppressCount(entity.getId());
            if (count > 0) Renderer.renderCount(entity, matrices, vertexConsumers, textRenderer,
                    String.valueOf(count + 1));
        }
    }
//#elseif MC < 260100
//$$ @Inject(method = "render", at = @At("RETURN"))
//$$ private void onRender(EntityRenderState state, MatrixStack matrices,
//$$                        OrderedRenderCommandQueue queue, CameraRenderState camera, CallbackInfo ci) {
//$$     if (!FeatureToggle.SIMPLE_ENTITY_RENDER_COUNT.getBooleanValue()) return;
//$$     Integer entityId = SimpleEntityRender.stateEntityIds.get(state);
//$$     if (entityId == null) return;
//$$     int count = SimpleEntityRender.getSuppressCount(entityId);
//$$     if (count > 0) {
//$$         queue.submitLabel(matrices,
//$$             new net.minecraft.util.math.Vec3d(0, state.height + 0.25, 0),
//$$             state.light,
//$$             net.minecraft.text.Text.literal(String.valueOf(count + 1)),
//$$             false, 0, state.squaredDistanceToCamera, camera);
//$$     }
//$$ }
//#elseif MC < 260200
//$$ @Inject(method = "submit", at = @At("RETURN"))
//$$ private void onSubmit(EntityRenderState state, PoseStack matrices,
//$$                        SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {
//$$     if (!FeatureToggle.SIMPLE_ENTITY_RENDER_COUNT.getBooleanValue()) return;
//$$     Integer entityId = SimpleEntityRender.stateEntityIds.get(state);
//$$     if (entityId == null) return;
//$$     int count = SimpleEntityRender.getSuppressCount(entityId);
//$$     if (count > 0) {
//$$         MultiBufferSource vertexConsumers =
//$$             Minecraft.getInstance().renderBuffers().bufferSource();
//$$         Renderer.renderCount(state.boundingBoxHeight, matrices, vertexConsumers,
//$$             ((EntityRenderer<?, ?>) (Object) this).getFont(), String.valueOf(count + 1));
//$$     }
//$$ }
//#else
//$$ // MC 26.2 removed MultiBufferSource; text above an entity goes through the
//$$ // submit pipeline's name tag node, which is what vanilla name tags use.
//$$ @Inject(method = "submit", at = @At("RETURN"))
//$$ private void onSubmit(EntityRenderState state, PoseStack matrices,
//$$                        SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {
//$$     if (!FeatureToggle.SIMPLE_ENTITY_RENDER_COUNT.getBooleanValue()) return;
//$$     Integer entityId = SimpleEntityRender.stateEntityIds.get(state);
//$$     if (entityId == null) return;
//$$     int count = SimpleEntityRender.getSuppressCount(entityId);
//$$     if (count > 0) {
//$$         queue.submitNameTag(matrices,
//$$             new Vec3(0.0, state.boundingBoxHeight + 0.25, 0.0), 0,
//$$             Component.literal(String.valueOf(count + 1)),
//$$             false, state.lightCoords, camera);
//$$     }
//$$ }
//#endif
}
