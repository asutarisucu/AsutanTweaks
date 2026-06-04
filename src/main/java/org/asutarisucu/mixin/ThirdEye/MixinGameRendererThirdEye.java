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
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
//#if MC < 12006
import net.minecraft.client.util.math.MatrixStack;
//#elseif MC >= 12101
//$$ import net.minecraft.client.render.RenderTickCounter;
//#endif

@Mixin(GameRenderer.class)
public abstract class MixinGameRendererThirdEye {

    @Shadow private MinecraftClient client;

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

//#if MC < 12006
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorldHead(float tickDelta, long startTime, MatrixStack matrices, CallbackInfo ci) {
//#elseif MC < 12101
//$$     @Inject(method = "renderWorld", at = @At("HEAD"))
//$$     private void onRenderWorldHead(float tickDelta, long startTime, CallbackInfo ci) {
//#else
//$$     @Inject(method = "renderWorld", at = @At("HEAD"))
//$$     private void onRenderWorldHead(RenderTickCounter counter, CallbackInfo ci) {
//#endif
        if (!Feature.THIRD_EYE.isEnabled())   return;
        if (ThirdEye.isRenderingThirdEye)      return;   // prevent infinite recursion
        if (!ThirdEyeWindow.isOpen())          return;
        if (ThirdEye.thirdEyeFbo == null)      return;
        if (!ThirdEyeCamera.initialized)       return;
        if (client.world == null)              return;

        // --- Redirect rendering to the ThirdEye framebuffer ---
        MixinMinecraftClientThirdEye accessor = (MixinMinecraftClientThirdEye)(Object) client;
        Framebuffer savedFb = accessor.thirdeye$getFramebuffer();

        accessor.thirdeye$setFramebuffer(ThirdEye.thirdEyeFbo);
        ThirdEye.thirdEyeFbo.clear(MinecraftClient.IS_SYSTEM_MAC);

        ThirdEye.isRenderingThirdEye = true;

        // Recursive shadow call: renders with Camera mixin overriding position/rotation.
//#if MC < 12006
        renderWorld(tickDelta, startTime, matrices);
//#elseif MC < 12101
//$$         renderWorld(tickDelta, startTime);
//#else
//$$         renderWorld(counter);
//#endif

        ThirdEye.isRenderingThirdEye = false;

        // Restore main framebuffer and re-bind it at the GL level.
        accessor.thirdeye$setFramebuffer(savedFb);
        savedFb.beginWrite(false);

        // --- Blit result to secondary window ---
        ThirdEyeWindow.blit(
                ThirdEye.thirdEyeFbo.getColorTexId(),
                ThirdEye.thirdEyeFbo.textureWidth,
                ThirdEye.thirdEyeFbo.textureHeight,
                client.getWindow().getHandle()
        );
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
