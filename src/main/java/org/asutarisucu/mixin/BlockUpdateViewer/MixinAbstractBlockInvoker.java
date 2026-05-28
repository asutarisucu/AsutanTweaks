package org.asutarisucu.mixin.BlockUpdateViewer;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//#endif

@Mixin(AbstractBlock.class)
public interface MixinAbstractBlockInvoker {

//#if MC < 260100

    @Invoker("neighborUpdate")
    void invokeNeighborUpdate(BlockState state, World world, BlockPos pos,
                               Block sourceBlock, BlockPos sourcePos, boolean notify);

    @Invoker("onBlockAdded")
    void invokeOnBlockAdded(BlockState state, World world, BlockPos pos,
                             BlockState oldState, boolean notify);

    @Invoker("onStateReplaced")
    void invokeOnStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved);

//#endif
}
