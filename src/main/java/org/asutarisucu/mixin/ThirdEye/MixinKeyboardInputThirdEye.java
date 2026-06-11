package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.Configs.Feature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 12111
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInputThirdEye {

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickReturn(CallbackInfo ci) {
        if (!Feature.THIRD_EYE_MOVEMENT.isEnabled()) return;
        // pressingForward etc. live in the parent Input class; @Shadow can't cross
        // class boundaries in Mixin, so cast to the concrete type instead.
        Input self = (Input)(Object)this;
        self.pressingForward  = false;
        self.pressingBack     = false;
        self.pressingLeft     = false;
        self.pressingRight    = false;
        self.movementForward  = 0.0f;
        self.movementSideways = 0.0f;
        self.jumping          = false;
        self.sneaking         = false;
    }
}
//#elseif MC < 260100
//$$ import net.minecraft.client.input.Input;
//$$ import net.minecraft.client.input.KeyboardInput;
//$$ import net.minecraft.util.PlayerInput;
//$$ import net.minecraft.util.math.Vec2f;
//$$
//$$ /**
//$$  * MC 1.21.11: movement is driven by Input.playerInput (a PlayerInput record)
//$$  * and the derived Input.movementVector. KeyboardInput.tick() computes
//$$  * movementVector from playerInput within the same call, so clearing only
//$$  * playerInput at RETURN leaves the horizontal movement intact. We clear both
//$$  * so the player stays still while ThirdEye movement is active.
//$$  */
//$$ @Mixin(KeyboardInput.class)
//$$ public abstract class MixinKeyboardInputThirdEye {
//$$
//$$     @Inject(method = "tick", at = @At("RETURN"))
//$$     private void onTickReturn(CallbackInfo ci) {
//$$         if (!Feature.THIRD_EYE_MOVEMENT.isEnabled()) return;
//$$         ((Input)(Object)this).playerInput = PlayerInput.DEFAULT;
//$$         // movementVector is protected on Input; set it via the accessor mixin.
//$$         ((MixinInputThirdEye)(Object)this).thirdeye$setMovementVector(Vec2f.ZERO);
//$$     }
//$$ }
//#else
//$$ import net.minecraft.client.Minecraft;
//$$
//$$ @Mixin(Minecraft.class)
//$$ public abstract class MixinKeyboardInputThirdEye {}
//#endif
