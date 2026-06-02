package org.asutarisucu.mixin.BlockUpdateViewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
//#if MC >= 12111
//$$ import net.minecraft.world.block.WireOrientation;
//#endif
import org.asutarisucu.tweak.BlockUpdateViewer.SimulationCapture;

/**
 * Intercepts World-level methods during simulation to redirect writes into
 * SimulationCapture and reads to return overlay state.
 *
 * ClientWorld overrides setBlockState — see MixinClientWorldSim.
 * ServerWorld overrides updateNeighbors* — not used (simulation uses ClientWorld).
 */
@Mixin(World.class)
public abstract class MixinWorldSim {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void simGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            BlockState overlay = cap.getOverlay(pos);
            if (overlay != null) cir.setReturnValue(overlay);
        }
    }

    @Inject(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
        at = @At("HEAD"), cancellable = true
    )
    private void simSetBlockState3(BlockPos pos, BlockState state, int flags,
                                    CallbackInfoReturnable<Boolean> cir) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureSetBlockState(pos, state, flags);
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
        at = @At("HEAD"), cancellable = true
    )
    private void simSetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth,
                                   CallbackInfoReturnable<Boolean> cir) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureSetBlockState(pos, state, flags);
            cir.setReturnValue(true);
        }
    }

//#if MC >= 12111
//$$ @Inject(method = "updateNeighborsAlways", at = @At("HEAD"), cancellable = true)
//$$ private void simUpdateNeighborsAlways(BlockPos pos, Block block, WireOrientation orientation, CallbackInfo ci) {
//$$     SimulationCapture cap = SimulationCapture.current();
//$$     if (cap != null) {
//$$         cap.captureUpdateAll(pos, block);
//$$         ci.cancel();
//$$     }
//$$ }
//#else
    @Inject(method = "updateNeighborsAlways", at = @At("HEAD"), cancellable = true)
    private void simUpdateNeighborsAlways(BlockPos pos, Block block, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureUpdateAll(pos, block);
            ci.cancel();
        }
    }
//#endif

//#if MC >= 12111
//$$ @Inject(method = "updateNeighborsExcept", at = @At("HEAD"), cancellable = true)
//$$ private void simUpdateNeighborsExcept(BlockPos pos, Block block, Direction direction, WireOrientation orientation, CallbackInfo ci) {
//$$     SimulationCapture cap = SimulationCapture.current();
//$$     if (cap != null) {
//$$         cap.captureUpdateExcept(pos, block, direction);
//$$         ci.cancel();
//$$     }
//$$ }
//#else
    @Inject(method = "updateNeighborsExcept", at = @At("HEAD"), cancellable = true)
    private void simUpdateNeighborsExcept(BlockPos pos, Block block, Direction direction, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureUpdateExcept(pos, block, direction);
            ci.cancel();
        }
    }
//#endif

//#if MC >= 12111
//$$ @Inject(
//$$     method = "updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;)V",
//$$     at = @At("HEAD"), cancellable = true
//$$ )
//$$ private void simUpdateNeighbor(BlockPos target, Block sourceBlock, WireOrientation wireOrientation, CallbackInfo ci) {
//$$     SimulationCapture cap = SimulationCapture.current();
//$$     if (cap != null) {
//$$         cap.captureSingleNeighbor(target, sourceBlock, target);
//$$         ci.cancel();
//$$     }
//$$ }
//$$
//$$ @Inject(
//$$     method = "updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V",
//$$     at = @At("HEAD"), cancellable = true
//$$ )
//$$ private void simUpdateNeighborWithState(BlockState state, BlockPos target, Block sourceBlock,
//$$                                          WireOrientation wireOrientation, boolean movedByPiston, CallbackInfo ci) {
//$$     SimulationCapture cap = SimulationCapture.current();
//$$     if (cap != null) {
//$$         cap.captureSingleNeighbor(target, sourceBlock, target);
//$$         ci.cancel();
//$$     }
//$$ }
//#else
    @Inject(
        method = "updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V",
        at = @At("HEAD"), cancellable = true
    )
    private void simUpdateNeighbor(BlockPos target, Block sourceBlock, BlockPos sourcePos, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureSingleNeighbor(sourcePos, sourceBlock, target);
            ci.cancel();
        }
    }

    @Inject(
        method = "updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V",
        at = @At("HEAD"), cancellable = true
    )
    private void simUpdateNeighborWithState(BlockState state, BlockPos target, Block sourceBlock,
                                             BlockPos sourcePos, boolean movedByPiston, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureSingleNeighbor(sourcePos, sourceBlock, target);
            ci.cancel();
        }
    }
//#endif

    @Inject(
        method = "breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;I)Z",
        at = @At("HEAD"), cancellable = true
    )
    private void simBreakBlock(BlockPos pos, boolean drop, Entity entity, int maxUpdateDepth,
                                CallbackInfoReturnable<Boolean> cir) {
        if (SimulationCapture.current() != null) cir.setReturnValue(false);
    }

    @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void simSpawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SimulationCapture.current() != null) cir.setReturnValue(false);
    }

    @Inject(method = "addBlockEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void simAddBlockEntity(BlockEntity entity, CallbackInfo ci) {
        if (SimulationCapture.current() != null) ci.cancel();
    }

    @Inject(method = "removeBlockEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void simRemoveBlockEntity(BlockPos pos, CallbackInfo ci) {
        if (SimulationCapture.current() != null) ci.cancel();
    }

    @Inject(method = "addSyncedBlockEvent", at = @At("HEAD"), cancellable = true, require = 0)
    private void simAddSyncedBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
        SimulationCapture cap = SimulationCapture.current();
        if (cap != null) {
            cap.captureBlockEvent(pos);
            ci.cancel();
        }
    }

}
//#else
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.world.level.block.entity.BlockEntity;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.core.Direction;
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.level.Level;
//$$ import net.minecraft.world.level.redstone.Orientation;
//$$ import org.asutarisucu.tweak.BlockUpdateViewer.SimulationCapture;
//$$
//$$ @Mixin(Level.class)
//$$ public abstract class MixinWorldSim {
//$$
//$$     @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
//$$     private void simGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             BlockState overlay = cap.getOverlay(pos);
//$$             if (overlay != null) cir.setReturnValue(overlay);
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
//$$         at = @At("HEAD"), cancellable = true
//$$     )
//$$     private void simSetBlock3(BlockPos pos, BlockState state, int flags,
//$$                                CallbackInfoReturnable<Boolean> cir) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureSetBlockState(pos, state, flags);
//$$             cir.setReturnValue(true);
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
//$$         at = @At("HEAD"), cancellable = true
//$$     )
//$$     private void simSetBlock4(BlockPos pos, BlockState state, int flags, int maxUpdateDepth,
//$$                                CallbackInfoReturnable<Boolean> cir) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureSetBlockState(pos, state, flags);
//$$             cir.setReturnValue(true);
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "updateNeighborsAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V",
//$$         at = @At("HEAD"), cancellable = true
//$$     )
//$$     private void simUpdateNeighborsAt(BlockPos pos, Block block, Orientation orientation, CallbackInfo ci) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureUpdateAll(pos, block);
//$$             ci.cancel();
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "updateNeighborsAtExceptFromFacing(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/redstone/Orientation;)V",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     private void simUpdateNeighborsExcept(BlockPos pos, Block block, Direction direction,
//$$                                            Orientation orientation, CallbackInfo ci) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureUpdateExcept(pos, block, direction);
//$$             ci.cancel();
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     private void simNeighborChanged3(BlockPos pos, Block block, Orientation orientation, CallbackInfo ci) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureSingleNeighbor(pos, block, pos);
//$$             ci.cancel();
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;Z)V",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     private void simNeighborChangedWithState(BlockState state, BlockPos pos, Block block,
//$$                                               Orientation orientation, boolean movedByPiston, CallbackInfo ci) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureSingleNeighbor(pos, block, pos);
//$$             ci.cancel();
//$$         }
//$$     }
//$$
//$$     @Inject(
//$$         method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
//$$         at = @At("HEAD"), cancellable = true, require = 0
//$$     )
//$$     private void simDestroyBlock(BlockPos pos, boolean drop, Entity entity, int maxUpdateDepth,
//$$                                   CallbackInfoReturnable<Boolean> cir) {
//$$         if (SimulationCapture.current() != null) cir.setReturnValue(false);
//$$     }
//$$
//$$     @Inject(method = "setBlockEntity", at = @At("HEAD"), cancellable = true, require = 0)
//$$     private void simSetBlockEntity(BlockEntity entity, CallbackInfo ci) {
//$$         if (SimulationCapture.current() != null) ci.cancel();
//$$     }
//$$
//$$     @Inject(method = "removeBlockEntity", at = @At("HEAD"), cancellable = true, require = 0)
//$$     private void simRemoveBlockEntity(BlockPos pos, CallbackInfo ci) {
//$$         if (SimulationCapture.current() != null) ci.cancel();
//$$     }
//$$
//$$     @Inject(method = "blockEvent", at = @At("HEAD"), cancellable = true, require = 0)
//$$     private void simBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
//$$         SimulationCapture cap = SimulationCapture.current();
//$$         if (cap != null) {
//$$             cap.captureBlockEvent(pos);
//$$             ci.cancel();
//$$         }
//$$     }
//$$
//$$ }
//#endif
