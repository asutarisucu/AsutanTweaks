package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.tweak.ThirdEye.ThirdEye;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 12111
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public abstract class MixinMouseThirdEye {

    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;
    @Shadow private MinecraftClient client;

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onUpdateMouseHead(CallbackInfo ci) {
        if (!ThirdEye.isMovementActive()) return;

        if (ThirdEyeCamera.initialized
                && (cursorDeltaX != 0 || cursorDeltaY != 0)) {
            double s = client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
            double sensitivity = s * s * s * 8.0;
            ThirdEyeCamera.yaw   += (float)(cursorDeltaX * sensitivity * 0.15);
            ThirdEyeCamera.pitch += (float)(cursorDeltaY * sensitivity * 0.15);
            ThirdEyeCamera.pitch  = Math.max(-90f, Math.min(90f, ThirdEyeCamera.pitch));
        }

        cursorDeltaX = 0;
        cursorDeltaY = 0;
        ci.cancel();
    }
}
//#elseif MC < 260100
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.Mouse;
//$$
//$$ /**
//$$  * MC 1.21.11: updateMouse now takes a timeDelta arg. We still consume the
//$$  * cursor delta for the ThirdEye camera and cancel the normal mouse-look while
//$$  * ThirdEye movement is active. Method is matched by name (single overload).
//$$  */
//$$ @Mixin(Mouse.class)
//$$ public abstract class MixinMouseThirdEye {
//$$
//$$     @Shadow private double cursorDeltaX;
//$$     @Shadow private double cursorDeltaY;
//$$     @Shadow private MinecraftClient client;
//$$
//$$     @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
//$$     private void onUpdateMouseHead(CallbackInfo ci) {
//$$         if (!ThirdEye.isMovementActive()) return;
//$$
//$$         if (ThirdEyeCamera.initialized
//$$                 && (cursorDeltaX != 0 || cursorDeltaY != 0)) {
//$$             double s = client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
//$$             double sensitivity = s * s * s * 8.0;
//$$             ThirdEyeCamera.yaw   += (float)(cursorDeltaX * sensitivity * 0.15);
//$$             ThirdEyeCamera.pitch += (float)(cursorDeltaY * sensitivity * 0.15);
//$$             ThirdEyeCamera.pitch  = Math.max(-90f, Math.min(90f, ThirdEyeCamera.pitch));
//$$         }
//$$
//$$         cursorDeltaX = 0;
//$$         cursorDeltaY = 0;
//$$         ci.cancel();
//$$     }
//$$ }
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.MouseHandler;
//$$ import org.spongepowered.asm.mixin.Final;
//$$
//$$ /**
//$$  * MC 26.1: MouseHandler.turnPlayer(double) applies accumulated mouse movement to
//$$  * the player. While ThirdEye movement is active we consume the deltas for the
//$$  * ThirdEye camera and cancel the normal mouse-look.
//$$  */
//$$ @Mixin(MouseHandler.class)
//$$ public abstract class MixinMouseThirdEye {
//$$
//$$     @Shadow private double accumulatedDX;
//$$     @Shadow private double accumulatedDY;
//$$     @Shadow @Final private Minecraft minecraft;
//$$
//$$     @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
//$$     private void onTurnPlayerHead(double movementTime, CallbackInfo ci) {
//$$         if (!ThirdEye.isMovementActive()) return;
//$$
//$$         if (ThirdEyeCamera.initialized
//$$                 && (accumulatedDX != 0 || accumulatedDY != 0)) {
//$$             double s = minecraft.options.sensitivity().get() * 0.6 + 0.2;
//$$             double sensitivity = s * s * s * 8.0;
//$$             ThirdEyeCamera.yaw   += (float)(accumulatedDX * sensitivity * 0.15);
//$$             ThirdEyeCamera.pitch += (float)(accumulatedDY * sensitivity * 0.15);
//$$             ThirdEyeCamera.pitch  = Math.max(-90f, Math.min(90f, ThirdEyeCamera.pitch));
//$$         }
//$$
//$$         accumulatedDX = 0;
//$$         accumulatedDY = 0;
//$$         ci.cancel();
//$$     }
//$$ }
//#endif
