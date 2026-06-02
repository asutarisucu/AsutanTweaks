package org.asutarisucu.mixin.BlockUpdateViewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//#if MC < 12111
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//#elseif MC < 260100
//$$ import net.minecraft.block.AbstractBlock;
//$$ import net.minecraft.block.Block;
//$$ import net.minecraft.block.BlockState;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.world.World;
//$$ import net.minecraft.server.world.ServerWorld;
//$$ import net.minecraft.world.block.WireOrientation;
//#else
//$$ import net.minecraft.world.level.block.state.BlockBehaviour;
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.Level;
//$$ import net.minecraft.world.level.redstone.Orientation;
//#endif

//#if MC < 260100
@Mixin(AbstractBlock.class)
//#else
//$$ @Mixin(BlockBehaviour.class)
//#endif
public interface MixinAbstractBlockInvoker {

//#if MC < 12111

    @Invoker("neighborUpdate")
    void invokeNeighborUpdate(BlockState state, World world, BlockPos pos,
                               Block sourceBlock, BlockPos sourcePos, boolean notify);

    @Invoker("onBlockAdded")
    void invokeOnBlockAdded(BlockState state, World world, BlockPos pos,
                             BlockState oldState, boolean notify);

    @Invoker("onStateReplaced")
    void invokeOnStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved);

//#elseif MC < 260100

//$$ @Invoker("neighborUpdate")
//$$ void invokeNeighborUpdate(BlockState state, World world, BlockPos pos,
//$$                            Block sourceBlock, WireOrientation orientation, boolean notify);
//$$
//$$ @Invoker("onBlockAdded")
//$$ void invokeOnBlockAdded(BlockState state, World world, BlockPos pos,
//$$                          BlockState oldState, boolean notify);
//$$
//$$ @Invoker("onStateReplaced")
//$$ void invokeOnStateReplaced(BlockState state, ServerWorld world, BlockPos pos,
//$$                             boolean moved);

//#else

    //$$ @Invoker("neighborChanged")
    //$$ void invokeNeighborChanged(BlockState state, Level level, BlockPos pos,
    //$$                             Block sourceBlock, Orientation orientation, boolean movedByPiston);
    //$$
    //$$ @Invoker("onPlace")
    //$$ void invokeOnPlace(BlockState state, Level level, BlockPos pos,
    //$$                     BlockState oldState, boolean movedByPiston);

//#endif
}
