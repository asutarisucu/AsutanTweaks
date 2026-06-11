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
//$$
//$$ @Mixin(Camera.class)
//$$ public interface MixinCameraInvokerThirdEye {
//$$
//$$     @Invoker("setPosition")
//$$     void thirdeye$setPos(double x, double y, double z);
//$$
//$$     @Invoker("setRotation")
//$$     void thirdeye$setRotation(float yaw, float pitch);
//$$ }
//#endif
