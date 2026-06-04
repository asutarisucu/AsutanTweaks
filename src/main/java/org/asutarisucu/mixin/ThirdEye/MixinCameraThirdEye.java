package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.tweak.ThirdEye.ThirdEye;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 260100
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;

/**
 * When a ThirdEye render pass is active (isRenderingThirdEye == true),
 * override the camera's final position and rotation with the ThirdEye values
 * after the normal Camera.update() has already set them from the player entity.
 */
@Mixin(Camera.class)
public abstract class MixinCameraThirdEye {

    // Access protected setters in Camera to override position and rotation.
    @Shadow protected abstract void setPos(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdateReturn(BlockView area, Entity focusedEntity,
                                boolean thirdPerson, boolean inverseView,
                                float tickDelta, CallbackInfo ci) {
        if (!ThirdEye.isRenderingThirdEye) return;
        setPos(ThirdEyeCamera.x, ThirdEyeCamera.y, ThirdEyeCamera.z);
        setRotation(ThirdEyeCamera.yaw, ThirdEyeCamera.pitch);
    }
}
//#else
//$$ import net.minecraft.client.Camera;
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.level.BlockGetter;
//$$
//$$ @Mixin(Camera.class)
//$$ public abstract class MixinCameraThirdEye {
//$$
//$$     @Shadow protected abstract void setRotation(float yaw, float pitch);
//$$     @Shadow protected abstract void setPosition(double x, double y, double z);
//$$
//$$     @Inject(method = "setup", at = @At("RETURN"))
//$$     private void onSetupReturn(BlockGetter area, Entity focusedEntity,
//$$                                boolean detached, boolean mirrorView,
//$$                                float partialTick, CallbackInfo ci) {
//$$         if (!ThirdEye.isRenderingThirdEye) return;
//$$         setPosition(ThirdEyeCamera.x, ThirdEyeCamera.y, ThirdEyeCamera.z);
//$$         setRotation(ThirdEyeCamera.yaw, ThirdEyeCamera.pitch);
//$$     }
//$$ }
//#endif
