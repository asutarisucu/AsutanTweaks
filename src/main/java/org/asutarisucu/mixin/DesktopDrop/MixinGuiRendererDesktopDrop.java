package org.asutarisucu.mixin.DesktopDrop;

//#if MC < 12111
/**
 * Only needed from MC 1.21.11, where GUI items are drawn through an atlas
 * texture. Left out of the mixin config before that.
 */
public final class MixinGuiRendererDesktopDrop {
    private MixinGuiRendererDesktopDrop() {}
}
//#elseif MC < 260100
//$$ import com.mojang.blaze3d.textures.GpuTexture;
//$$ import net.minecraft.client.gui.render.GuiRenderer;
//$$ import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
//$$ import net.minecraft.client.texture.GlTexture;
//$$ import org.asutarisucu.tweak.DesktopDrop.DesktopDrop;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ /** Finds the held item's picture in the GUI item atlas. */
//$$ @Mixin(GuiRenderer.class)
//$$ public class MixinGuiRendererDesktopDrop {
//$$     @Shadow private GpuTexture itemAtlasTexture;
//$$
//$$     @Inject(method = "prepareItem", at = @At("HEAD"))
//$$     private void desktopDrop(ItemGuiElementRenderState state, float u, float v, int pixelsPerItem, int atlasSize,
//$$                              CallbackInfo ci) {
//$$         if (!DesktopDrop.active() || !(itemAtlasTexture instanceof GlTexture gl)) return;
//$$         DesktopDrop.onAtlasItem(state.x(), state.y(), gl.getGlId(), atlasSize, u, v, pixelsPerItem);
//$$     }
//$$ }
//#else
//$$ import com.mojang.blaze3d.opengl.GlTexture;
//$$ import net.minecraft.client.gui.render.GuiItemAtlas;
//$$ import net.minecraft.client.gui.render.GuiRenderer;
//$$ import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
//$$ import org.asutarisucu.tweak.DesktopDrop.DesktopDrop;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ /** Finds the held item's picture in the GUI item atlas. */
//$$ @Mixin(GuiRenderer.class)
//$$ public class MixinGuiRendererDesktopDrop {
//$$     @Inject(method = "submitBlitFromItemAtlas", at = @At("HEAD"))
//$$     private void desktopDrop(GuiItemRenderState state, GuiItemAtlas.SlotView slot, CallbackInfo ci) {
//$$         if (!DesktopDrop.active() || !(slot.textureView().texture() instanceof GlTexture gl)) return;
//$$         int size = slot.textureView().texture().getWidth(0);
//$$         int slotSize = Math.round((slot.u1() - slot.u0()) * size);
//$$         DesktopDrop.onAtlasItem(state.x(), state.y(), gl.glId(), size, slot.u0(), slot.v0(), slotSize);
//$$     }
//$$ }
//#endif
