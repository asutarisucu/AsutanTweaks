package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.tweak.ThirdEye.ThirdEye;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC < 12111
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Intercepts MinecraftClient.getFramebuffer() during a ThirdEye render pass
 * to redirect the rendering pipeline to ThirdEye's FBO without needing to
 * mutate the final framebuffer field.
 */
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClientThirdEye {

    @Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
    private void thirdeye$interceptGetFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        if (ThirdEye.isRenderingThirdEye && ThirdEye.thirdEyeFbo != null) {
            cir.setReturnValue(ThirdEye.thirdEyeFbo);
        }
    }
}
//#elseif MC < 260100
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gl.Framebuffer;
//$$
//$$ /**
//$$  * Intercepts MinecraftClient.getFramebuffer() during a ThirdEye render pass to
//$$  * redirect world rendering into ThirdEye's FBO (MC 1.21.11; return type Framebuffer).
//$$  */
//$$ @Mixin(MinecraftClient.class)
//$$ public abstract class MixinMinecraftClientThirdEye {
//$$
//$$     @Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
//$$     private void thirdeye$interceptGetFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
//$$         if (ThirdEye.isRenderingThirdEye && ThirdEye.thirdEyeFbo != null) {
//$$             cir.setReturnValue(ThirdEye.thirdEyeFbo);
//$$         }
//$$     }
//$$ }
//#else
//$$ import com.mojang.blaze3d.pipeline.RenderTarget;
//$$ import net.minecraft.client.Minecraft;
//$$
//$$ /**
//$$  * MC 26.1: intercepts Minecraft.getMainRenderTarget() during a ThirdEye render
//$$  * pass. GameRenderer.renderLevel and LevelRenderer both resolve their output
//$$  * target through this getter, so returning the ThirdEye target here redirects
//$$  * the whole recursive level render.
//$$  */
//$$ @Mixin(Minecraft.class)
//$$ public abstract class MixinMinecraftClientThirdEye {
//$$
//$$     @Inject(method = "getMainRenderTarget", at = @At("HEAD"), cancellable = true)
//$$     private void thirdeye$interceptGetMainRenderTarget(CallbackInfoReturnable<RenderTarget> cir) {
//$$         if (ThirdEye.isRenderingThirdEye && ThirdEye.thirdEyeFbo != null) {
//$$             cir.setReturnValue(ThirdEye.thirdEyeFbo);
//$$         }
//$$     }
//$$ }
//#endif
