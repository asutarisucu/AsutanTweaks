package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.Configs.Feature;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeCamera;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeWindow;
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
        if (!Feature.THIRD_EYE_MOVEMENT.isEnabled()) return;

        if (ThirdEyeCamera.initialized && ThirdEyeWindow.isOpen()
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
//$$         if (!Feature.THIRD_EYE_MOVEMENT.isEnabled()) return;
//$$
//$$         if (ThirdEyeCamera.initialized && ThirdEyeWindow.isOpen()
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
//$$
//$$ @Mixin(Minecraft.class)
//$$ public abstract class MixinMouseThirdEye {}
//#endif
