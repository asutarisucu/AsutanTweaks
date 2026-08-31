package org.asutarisucu.mixin.ClearBlockRender;

//#if MC < 260200
/**
 * Only needed from MC 26.2 on, where a RenderType resolves its output target
 * through GameRenderer.mainRenderTarget() instead of the caller binding a
 * framebuffer. Left out of the mixin config for earlier versions.
 */
public final class MixinGameRendererTargetCbr {
    private MixinGameRendererTargetCbr() {}
}
//#else
//$$ import com.mojang.blaze3d.pipeline.RenderTarget;
//$$ import net.minecraft.client.renderer.GameRenderer;
//$$ import org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$
//$$ /**
//$$  * Sends the capture pass to its own off-screen target.
//$$  *
//$$  * OutputTarget.MAIN_TARGET resolves through gameRenderer.mainRenderTarget(), so
//$$  * every RenderType drawn while the capture is running lands in the capture
//$$  * buffer rather than on screen.
//$$  */
//$$ @Mixin(GameRenderer.class)
//$$ public abstract class MixinGameRendererTargetCbr {
//$$
//$$     @Inject(method = "mainRenderTarget", at = @At("HEAD"), cancellable = true)
//$$     private void asutantweaks$captureTarget(CallbackInfoReturnable<RenderTarget> cir) {
//$$         RenderTarget target = ClearBlockRender.activeCaptureTarget();
//$$         if (target != null) cir.setReturnValue(target);
//$$     }
//$$ }
//#endif
