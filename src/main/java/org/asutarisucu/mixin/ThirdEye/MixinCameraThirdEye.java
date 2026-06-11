package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.tweak.ThirdEye.ThirdEye;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC < 12111
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

    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void onIsThirdPersonHead(CallbackInfoReturnable<Boolean> cir) {
        if (ThirdEye.isRenderingThirdEye) cir.setReturnValue(true);
    }
}
//#elseif MC < 260100
//$$ import net.minecraft.client.render.Camera;
//$$
//$$ /**
//$$  * MC 1.21.11: Camera.update() is not called inside renderWorld(), so the
//$$  * ThirdEye position override is applied via the invoker in
//$$  * MixinGameRendererThirdEye instead. Here we only force third-person so the
//$$  * player model is visible in the ThirdEye view.
//$$  */
//$$ @Mixin(Camera.class)
//$$ public abstract class MixinCameraThirdEye {
//$$
//$$     @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
//$$     private void onIsThirdPersonHead(CallbackInfoReturnable<Boolean> cir) {
//$$         if (ThirdEye.isRenderingThirdEye) cir.setReturnValue(true);
//$$     }
//$$ }
//#else
//$$ import net.minecraft.client.Camera;
//$$
//$$ /**
//$$  * MC 26.1: the position override happens in MixinGameRendererThirdEye (the
//$$  * extracted state is rebuilt there). Here we only force detached (third-person)
//$$  * mode during the ThirdEye extraction so the player model is included.
//$$  */
//$$ @Mixin(Camera.class)
//$$ public abstract class MixinCameraThirdEye {
//$$
//$$     @Inject(method = "isDetached", at = @At("HEAD"), cancellable = true)
//$$     private void onIsDetachedHead(CallbackInfoReturnable<Boolean> cir) {
//$$         if (ThirdEye.isRenderingThirdEye) cir.setReturnValue(true);
//$$     }
//$$ }
//#endif
