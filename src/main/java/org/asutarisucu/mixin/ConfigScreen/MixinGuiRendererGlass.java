package org.asutarisucu.mixin.ConfigScreen;

//#if MC < 12111
/**
 * Only needed from MC 1.21.11, where the GUI is drawn after the screen has
 * finished recording it. Left out of the mixin config before that.
 */
public final class MixinGuiRendererGlass {
    private MixinGuiRendererGlass() {}
}
//#else
//$$ import org.asutarisucu.GUI.glass.GlassRenderer;
//$$ import net.minecraft.client.gui.render.GuiRenderer;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ /**
//$$  * Copies what is behind the GUI for the glass config screen, right before the
//$$  * GUI is drawn over it.
//$$  *
//$$  * The screen cannot take the copy itself: on MC 26.x its render call runs before
//$$  * the world is drawn for the frame, and the main target still holds the previous
//$$  * frame, GUI included. On 26.x the title panorama is also drawn here, at the
//$$  * start of the GUI pass, so the copy is taken after it.
//$$  */
//$$ @Mixin(GuiRenderer.class)
//$$ public class MixinGuiRendererGlass {
//#if MC < 260200
//$$     @Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
//$$             at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;prepare()V"))
//#else
//$$     @Inject(method = "render()V",
//$$             at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;prepare()V"))
//#endif
//$$     private void asutantweaks$captureBackdrop(CallbackInfo ci) {
//$$         GlassRenderer.captureBeforeGui();
//$$     }
//$$ }
//#endif
