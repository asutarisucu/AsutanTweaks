package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.Utiles.Render.Renderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render",at=@At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=weather"))
    private void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci){
        if(FeatureToggle.SIMPLE_ENTITY_RENDER_COUNT.getBooleanValue())Renderer.renderCount(camera);
    }
}
