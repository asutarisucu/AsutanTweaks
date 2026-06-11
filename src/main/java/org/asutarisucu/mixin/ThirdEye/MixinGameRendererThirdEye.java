package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.Configs.Feature;
import org.asutarisucu.tweak.ThirdEye.ThirdEye;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeCamera;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 12111
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Vec3d;
//#if MC < 12006
import net.minecraft.client.util.math.MatrixStack;
//#elseif MC >= 12101
//$$ import net.minecraft.client.render.RenderTickCounter;
//#endif

@Mixin(GameRenderer.class)
public abstract class MixinGameRendererThirdEye {

    @Shadow private MinecraftClient client;
    @Shadow private Camera camera;

//#if MC < 12006
    @Shadow
    private void renderWorld(float tickDelta, long startTime, MatrixStack matrices) {}
//#elseif MC < 12101
//$$     @Shadow
//$$     private void renderWorld(float tickDelta, long startTime) {}
//#else
//$$     @Shadow
//$$     private void renderWorld(RenderTickCounter counter) {}
//#endif

    // Hooked at RETURN — after the main world render is fully complete.
    // Doing the ThirdEye pass here (instead of HEAD) guarantees that whatever
    // state the recursive call leaves behind cannot affect the main view of
    // the current frame, which has already finished rendering.
    //
    // Additionally, we explicitly save/restore the shared Camera's pos and
    // rotation around the recursive call so any code that reads camera state
    // AFTER renderWorld() returns (HUD, post-FX, next-frame setup, etc.) sees
    // the player's values rather than the ThirdEye values written by the
    // RETURN inject in MixinCameraThirdEye.
//#if MC < 12006
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void onRenderWorldReturn(float tickDelta, long startTime, MatrixStack matrices, CallbackInfo ci) {
//#elseif MC < 12101
//$$     @Inject(method = "renderWorld", at = @At("RETURN"))
//$$     private void onRenderWorldReturn(float tickDelta, long startTime, CallbackInfo ci) {
//#else
//$$     @Inject(method = "renderWorld", at = @At("RETURN"))
//$$     private void onRenderWorldReturn(RenderTickCounter counter, CallbackInfo ci) {
//#endif
        if (!Feature.THIRD_EYE.isEnabled())   return;
        if (ThirdEye.isRenderingThirdEye)      return;   // prevent recursion via RETURN
        if (!ThirdEyeWindow.isOpen())          return;
        if (ThirdEye.thirdEyeFbo == null)      return;
        if (!ThirdEyeCamera.initialized)       return;
        if (client.world == null)              return;

        // Snapshot the shared Camera state BEFORE we modify it for the
        // ThirdEye pass. The main world render is already complete; this
        // snapshot is what we restore afterwards so the player's camera
        // state is exactly what any post-renderWorld code expects.
        Vec3d savedPos = camera.getPos();
        float savedYaw = camera.getYaw();
        float savedPitch = camera.getPitch();

        // Clear ThirdEye FBO. getFramebuffer() is intercepted by
        // MixinMinecraftClientThirdEye to return thirdEyeFbo while
        // isRenderingThirdEye is true.
        ThirdEye.thirdEyeFbo.clear(MinecraftClient.IS_SYSTEM_MAC);

        ThirdEye.isRenderingThirdEye = true;

        try {
            // Recursive shadow call. MixinCameraThirdEye's RETURN inject on
            // Camera.update() overrides position/rotation to ThirdEye values
            // while isRenderingThirdEye is true. The HEAD/RETURN guards above
            // prevent infinite recursion.
//#if MC < 12006
            renderWorld(tickDelta, startTime, matrices);
//#elseif MC < 12101
//$$             renderWorld(tickDelta, startTime);
//#else
//$$             renderWorld(counter);
//#endif
        } finally {
            ThirdEye.isRenderingThirdEye = false;
            // Restore the original camera state directly via the protected
            // setters (exposed by MixinCameraInvokerThirdEye). Going through
            // Camera.update() instead would re-derive pos/rotation from the
            // focused entity and apply third-person clipping, which is
            // exactly the "horizontal-north offset" we are trying to avoid.
            MixinCameraInvokerThirdEye inv = (MixinCameraInvokerThirdEye)(Object) camera;
            inv.thirdeye$setPos(savedPos.x, savedPos.y, savedPos.z);
            inv.thirdeye$setRotation(savedYaw, savedPitch);
        }

        // Rebind the main framebuffer at the GL level so any subsequent
        // rendering (HUD, screen overlays) writes to the screen, not the
        // ThirdEye FBO.
        client.getFramebuffer().beginWrite(false);

        // Blit ThirdEye FBO result to the secondary OS window.
        ThirdEyeWindow.blit(
                ThirdEye.thirdEyeFbo.getColorTexId(),
                ThirdEye.thirdEyeFbo.textureWidth,
                ThirdEye.thirdEyeFbo.textureHeight,
                client.getWindow().getHandle()
        );
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void onRenderHandHead(CallbackInfo ci) {
        if (ThirdEye.isRenderingThirdEye) ci.cancel();
    }
}
//#elseif MC < 260100
//$$ import net.minecraft.client.render.GameRenderer;
//$$
//$$ // TODO: implement ThirdEye for MC 1.21.11 (GpuTexture API changed).
//$$ @Mixin(GameRenderer.class)
//$$ public abstract class MixinGameRendererThirdEye {
//$$ }
//#else
//$$ import net.minecraft.client.renderer.GameRenderer;
//$$
//$$ // TODO: implement for MC 260100+ (Mojang mappings).
//$$ @Mixin(GameRenderer.class)
//$$ public abstract class MixinGameRendererThirdEye {
//$$ }
//#endif
