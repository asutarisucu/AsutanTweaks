package org.asutarisucu.mixin.BlockUpdateViewer;

import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.TickPriority;
import org.asutarisucu.tweak.BlockUpdateViewer.SimulationCapture;
//#endif

/**
 * Intercepts WorldAccess interface defaults during simulation.
 * scheduleBlockTick is CAPTURED (not cancelled) so the BFS can process ticks
 * and propagate observer/gate/piston update chains.
 * scheduleFluidTick is cancelled (fluid propagation out of scope).
 */
@Mixin(WorldAccess.class)
public interface MixinWorldAccessSim {

//#if MC < 260100

    @Inject(
        method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V",
        at = @At("HEAD"), cancellable = true
    )
    default void simScheduleBlockTick(BlockPos pos, Block block, int delay, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureScheduledTick(pos, block);
            ci.cancel();
        }
    }

    @Inject(
        method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;ILnet/minecraft/world/tick/TickPriority;)V",
        at = @At("HEAD"), cancellable = true
    )
    default void simScheduleBlockTickPriority(BlockPos pos, Block block, int delay,
                                               TickPriority priority, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureScheduledTick(pos, block);
            ci.cancel();
        }
    }

    @Inject(
        method = "scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;I)V",
        at = @At("HEAD"), cancellable = true, require = 0
    )
    default void simScheduleFluidTick(BlockPos pos, Fluid fluid, int delay, CallbackInfo ci) {
        if (SimulationCapture.current() != null) ci.cancel();
    }

    @Inject(
        method = "scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;ILnet/minecraft/world/tick/TickPriority;)V",
        at = @At("HEAD"), cancellable = true, require = 0
    )
    default void simScheduleFluidTickPriority(BlockPos pos, Fluid fluid, int delay,
                                               TickPriority priority, CallbackInfo ci) {
        if (SimulationCapture.current() != null) ci.cancel();
    }

//#endif
}
