package org.asutarisucu.mixin.ComparatorSignalView;

import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ComparatorBlock.class)
public interface MixinComparatorBlockInvoker {

    @Invoker("calculateOutputSignal")
    int invokeCalculateOutputSignal(World world, BlockPos pos, BlockState state);
}
