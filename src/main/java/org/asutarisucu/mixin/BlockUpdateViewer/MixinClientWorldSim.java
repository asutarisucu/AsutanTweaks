package org.asutarisucu.mixin.BlockUpdateViewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.TickPriority;
import org.asutarisucu.tweak.BlockUpdateViewer.SimulationCapture;
//#else
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.ticks.TickPriority;
//$$ import org.asutarisucu.tweak.BlockUpdateViewer.SimulationCapture;
//#endif

/**
 * ClientWorld overrides setBlockState and scheduleBlockTick independently of
 * World/WorldAccess. Without intercepting them here, simulation writes on the
 * ClientWorld would execute (visual glitches) and ticks would be lost.
 */
//#if MC < 260100
@Mixin(ClientWorld.class)
//#else
//$$ @Mixin(net.minecraft.client.multiplayer.ClientLevel.class)
//#endif
public abstract class MixinClientWorldSim {

//#if MC < 260100

    @Inject(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
        at = @At("HEAD"), cancellable = true, require = 0
    )
    private void simCWSetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth,
                                     CallbackInfoReturnable<Boolean> cir) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureSetBlockState(pos, state, flags);
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V",
        at = @At("HEAD"), cancellable = true, require = 0
    )
    private void simCWScheduleBlockTick(BlockPos pos, Block block, int delay, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureScheduledTick(pos, block);
            ci.cancel();
        }
    }

    @Inject(
        method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;ILnet/minecraft/world/tick/TickPriority;)V",
        at = @At("HEAD"), cancellable = true, require = 0
    )
    private void simCWScheduleBlockTickPriority(BlockPos pos, Block block, int delay,
                                                  TickPriority priority, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureScheduledTick(pos, block);
            ci.cancel();
        }
    }

    // Pistons use addSyncedBlockEvent (immediate in ClientWorld) instead of scheduleBlockTick.
    // Cancel it and mark the position as schedulable so the instant-only filter excludes it.
    @Inject(
        method = "addSyncedBlockEvent",
        at = @At("HEAD"), cancellable = true, require = 0
    )
    private void simCWAddSyncedBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureBlockEvent(pos);
            ci.cancel();
        }
    }

//#else

    //$$ @Inject(
    //$$     method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
    //$$     at = @At("HEAD"), cancellable = true, require = 0
    //$$ )
    //$$ private void simCWSetBlock(BlockPos pos, BlockState state, int flags, int maxUpdateDepth,
    //$$                             CallbackInfoReturnable<Boolean> cir) {
    //$$     SimulationCapture cap = SimulationCapture.current();
    //$$     if (cap != null) {
    //$$         cap.captureSetBlockState(pos, state, flags);
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(
    //$$     method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V",
    //$$     at = @At("HEAD"), cancellable = true, require = 0
    //$$ )
    //$$ private void simCWScheduleBlockTick(BlockPos pos, Block block, int delay, CallbackInfo ci) {
    //$$     SimulationCapture cap = SimulationCapture.current();
    //$$     if (cap != null) {
    //$$         cap.captureScheduledTick(pos, block);
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(
    //$$     method = "scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;ILnet/minecraft/world/ticks/TickPriority;)V",
    //$$     at = @At("HEAD"), cancellable = true, require = 0
    //$$ )
    //$$ private void simCWScheduleBlockTickPriority(BlockPos pos, Block block, int delay,
    //$$                                              TickPriority priority, CallbackInfo ci) {
    //$$     SimulationCapture cap = SimulationCapture.current();
    //$$     if (cap != null) {
    //$$         cap.captureScheduledTick(pos, block);
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(method = "blockEvent", at = @At("HEAD"), cancellable = true, require = 0)
    //$$ private void simCWBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
    //$$     SimulationCapture cap = SimulationCapture.current();
    //$$     if (cap != null) {
    //$$         cap.captureBlockEvent(pos);
    //$$         ci.cancel();
    //$$     }
    //$$ }

//#endif
}
