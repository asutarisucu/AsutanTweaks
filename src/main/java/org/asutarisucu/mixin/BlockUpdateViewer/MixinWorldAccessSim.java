package org.asutarisucu.mixin.BlockUpdateViewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
//#if MC < 12111
import net.minecraft.world.WorldAccess;
//#else
//$$ import net.minecraft.world.tick.ScheduledTickView;
//#endif
import net.minecraft.world.tick.TickPriority;
import org.asutarisucu.tweak.BlockUpdateViewer.SimulationCapture;

/**
 * Intercepts WorldAccess/ScheduledTickView interface defaults during simulation.
 * scheduleBlockTick is CAPTURED (not cancelled) so the BFS can process ticks
 * and propagate observer/gate/piston update chains.
 * scheduleFluidTick is cancelled (fluid propagation out of scope).
 */
//#if MC < 12111
@Mixin(WorldAccess.class)
//#else
//$$ @Mixin(ScheduledTickView.class)
//#endif
public interface MixinWorldAccessSim {

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

}
//#else
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.world.level.material.Fluid;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.ScheduledTickAccess;
//$$ import net.minecraft.world.ticks.TickPriority;
//$$ import org.asutarisucu.tweak.BlockUpdateViewer.SimulationCapture;
//$$
//$$ @Mixin(ScheduledTickAccess.class)
//$$ public interface MixinWorldAccessSim {
//$$
//$$     @Inject(
//$$         method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     default void simScheduleBlockTick(BlockPos pos, Block block, int delay, CallbackInfo ci) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureScheduledTick(pos, block);
//$$             ci.cancel();
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;ILnet/minecraft/world/ticks/TickPriority;)V",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     default void simScheduleBlockTickPriority(BlockPos pos, Block block, int delay,
//$$                                                TickPriority priority, CallbackInfo ci) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureScheduledTick(pos, block);
//$$             ci.cancel();
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     default void simScheduleFluidTick(BlockPos pos, Fluid fluid, int delay, CallbackInfo ci) {
//$$         if (SimulationCapture.current() != null) ci.cancel();
//$$     }
//$$
//$$     @Inject(
//$$         method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;ILnet/minecraft/world/ticks/TickPriority;)V",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     default void simScheduleFluidTickPriority(BlockPos pos, Fluid fluid, int delay,
//$$                                                TickPriority priority, CallbackInfo ci) {
//$$         if (SimulationCapture.current() != null) ci.cancel();
//$$     }
//$$
//$$ }
//#endif
