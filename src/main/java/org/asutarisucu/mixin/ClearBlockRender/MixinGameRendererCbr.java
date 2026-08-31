package org.asutarisucu.mixin.ClearBlockRender;

import org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 12111
import net.minecraft.client.render.GameRenderer;
//#if MC < 12006
import net.minecraft.client.util.math.MatrixStack;
//#elseif MC >= 12101
//$$ import net.minecraft.client.render.RenderTickCounter;
//#endif

/**
 * Runs the capture pass right after the player's world render, where the world
 * render state (lightmap, textures, shaders) is still set up.
 */
@Mixin(GameRenderer.class)
public class MixinGameRendererCbr {

//#if MC < 12006
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void asutantweaks$captureFrame(float tickDelta, long startTime, MatrixStack matrices, CallbackInfo ci) {
        ClearBlockRender.onFrame(tickDelta);
    }
//#elseif MC < 12101
    //$$ @Inject(method = "renderWorld", at = @At("RETURN"))
    //$$ private void asutantweaks$captureFrame(float tickDelta, long startTime, CallbackInfo ci) {
    //$$     ClearBlockRender.onFrame(tickDelta);
    //$$ }
//#else
    //$$ @Inject(method = "renderWorld", at = @At("RETURN"))
    //$$ private void asutantweaks$captureFrame(RenderTickCounter counter, CallbackInfo ci) {
    //$$     ClearBlockRender.onFrame(counter.getTickDelta(false));
    //$$ }
//#endif
}
//#elseif MC < 260100
//$$ import net.minecraft.client.render.GameRenderer;
//$$ import net.minecraft.client.render.RenderTickCounter;
//$$
//$$ /**
//$$  * Runs the capture pass right after the player's world render, where the world
//$$  * render state (lightmap, textures, shaders) is still set up.
//$$  */
//$$ @Mixin(GameRenderer.class)
//$$ public class MixinGameRendererCbr {
//$$
//$$     @Inject(method = "renderWorld", at = @At("RETURN"))
//$$     private void asutantweaks$captureFrame(RenderTickCounter counter, CallbackInfo ci) {
//$$         ClearBlockRender.onFrame(counter.getTickProgress(false));
//$$     }
//$$ }
//#else
//$$ import net.minecraft.client.DeltaTracker;
//$$ import net.minecraft.client.renderer.GameRenderer;
//$$
//$$ /**
//$$  * Runs the capture pass right after the player's level render, while the block
//$$  * atlas and lightmap the block models need are still bound.
//$$  */
//$$ @Mixin(GameRenderer.class)
//$$ public class MixinGameRendererCbr {
//$$
//$$     @Inject(method = "renderLevel", at = @At("RETURN"))
//$$     private void asutantweaks$captureFrame(DeltaTracker deltaTracker, CallbackInfo ci) {
//$$         ClearBlockRender.onFrame(deltaTracker.getGameTimeDeltaPartialTick(false));
//$$     }
//$$ }
//#endif
