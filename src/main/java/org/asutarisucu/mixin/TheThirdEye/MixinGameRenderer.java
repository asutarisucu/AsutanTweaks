package org.asutarisucu.mixin.TheThirdEye;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import org.asutarisucu.tweak.TheThirdEye.WindowManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow public abstract void render(float tickDelta, long startTime, boolean tick);

    @Unique private boolean isThirdEye=false;

    @Inject(method = "render",at = @At("TAIL"))
    private void setWindowReturn(float tickDelta, long startTime, boolean tick, CallbackInfo ci){
        if(WindowManager.window!=null){
            isThirdEye=!isThirdEye;
            if(isThirdEye){
                Window window = MinecraftClient.getInstance().getWindow();
                ((IMixinMinecraftClient)MinecraftClient.getInstance()).setWindow(WindowManager.window);
                MinecraftClient.getInstance().getFramebuffer().clear(true);
                this.render(tickDelta,startTime,false);
                ((IMixinMinecraftClient)MinecraftClient.getInstance()).setWindow(window);
            }

        }
    }
}
