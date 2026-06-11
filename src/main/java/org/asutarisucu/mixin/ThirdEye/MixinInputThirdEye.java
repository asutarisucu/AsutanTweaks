package org.asutarisucu.mixin.ThirdEye;

import org.spongepowered.asm.mixin.Mixin;

//#if MC < 12111
import net.minecraft.client.input.Input;

/** Not used before 1.21.11 (legacy Input exposes movement fields publicly). */
@Mixin(Input.class)
public interface MixinInputThirdEye {}
//#elseif MC < 260100
//$$ import net.minecraft.client.input.Input;
//$$ import net.minecraft.util.math.Vec2f;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$
//$$ /**
//$$  * MC 1.21.11: Input.movementVector (the derived horizontal movement) is protected
//$$  * and declared on Input, so it cannot be @Shadow-ed from a KeyboardInput mixin.
//$$  * This accessor exposes a setter used by MixinKeyboardInputThirdEye to zero it
//$$  * while ThirdEye movement is active.
//$$  */
//$$ @Mixin(Input.class)
//$$ public interface MixinInputThirdEye {
//$$     @Accessor("movementVector")
//$$     void thirdeye$setMovementVector(Vec2f v);
//$$ }
//#else
//$$ import net.minecraft.client.player.ClientInput;
//$$ import net.minecraft.world.phys.Vec2;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$
//$$ /**
//$$  * MC 26.1: ClientInput.moveVector is protected; this accessor lets
//$$  * MixinKeyboardInputThirdEye zero it while ThirdEye movement is active.
//$$  */
//$$ @Mixin(ClientInput.class)
//$$ public interface MixinInputThirdEye {
//$$     @Accessor("moveVector")
//$$     void thirdeye$setMoveVector(Vec2 v);
//$$ }
//#endif
