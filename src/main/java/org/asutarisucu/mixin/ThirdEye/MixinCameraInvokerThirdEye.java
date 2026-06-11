package org.asutarisucu.mixin.ThirdEye;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//#if MC < 260100
import net.minecraft.client.render.Camera;

/**
 * Exposes Camera.setPos / Camera.setRotation (both protected) so the
 * GameRenderer mixin can directly restore camera state after the ThirdEye
 * render pass without going through Camera.update() (which would re-derive
 * pos/rotation from the entity and apply third-person clipping).
 */
@Mixin(Camera.class)
public interface MixinCameraInvokerThirdEye {

    @Invoker("setPos")
    void thirdeye$setPos(double x, double y, double z);

    @Invoker("setRotation")
    void thirdeye$setRotation(float yaw, float pitch);
}
//#else
//$$ import net.minecraft.client.Camera;
//$$ import net.minecraft.world.phys.Vec3;
//$$ import org.joml.Matrix4f;
//$$ import org.joml.Matrix4fc;
//$$
//$$ @Mixin(Camera.class)
//$$ public interface MixinCameraInvokerThirdEye {
//$$
//$$     @Invoker("setPosition")
//$$     void thirdeye$setPos(double x, double y, double z);
//$$
//$$     @Invoker("setRotation")
//$$     void thirdeye$setRotation(float yaw, float pitch);
//$$
//$$     // The cull frustum is computed once in Camera.update() and NOT refreshed by
//$$     // setPosition/setRotation. These two invokers let the ThirdEye pass rebuild
//$$     // it after overriding the camera (mirrors the tail of Camera.update()).
//$$     @Invoker("createProjectionMatrixForCulling")
//$$     Matrix4f thirdeye$createProjectionMatrixForCulling();
//$$
//$$     @Invoker("prepareCullFrustum")
//$$     void thirdeye$prepareCullFrustum(Matrix4fc viewRotMatrix, Matrix4f projectionMatrix, Vec3 pos);
//$$ }
//#endif
