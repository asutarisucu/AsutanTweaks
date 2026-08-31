package org.asutarisucu.tweak.ClearBlockRender;

//#if MC < 260100
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;

/**
 * The world as seen from inside the capture region: everything outside the
 * selection reads as air.
 *
 * This is what isolates the render — the block models cull against this view,
 * so faces on the region boundary are emitted (they now border air) and no
 * surrounding terrain contributes any geometry.
 */
public final class RegionBlockView implements BlockRenderView {

    private final ClientWorld world;
    private final int minX, minY, minZ, maxX, maxY, maxZ;

    public RegionBlockView(ClientWorld world, int[] bounds) {
        this.world = world;
        this.minX = bounds[0]; this.minY = bounds[1]; this.minZ = bounds[2];
        this.maxX = bounds[3]; this.maxY = bounds[4]; this.maxZ = bounds[5];
    }

    private boolean inside(BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return inside(pos) ? world.getBlockState(pos) : Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return inside(pos) ? world.getFluidState(pos) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return inside(pos) ? world.getBlockEntity(pos) : null;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return world.getBrightness(direction, shaded);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return world.getLightingProvider();
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return world.getColor(pos, colorResolver);
    }

    @Override
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    public int getBottomY() {
        return world.getBottomY();
    }
}
//#else
//$$ import net.minecraft.client.multiplayer.ClientLevel;
//$$ import net.minecraft.client.renderer.block.BlockAndTintGetter;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.CardinalLighting;
//$$ import net.minecraft.world.level.ColorResolver;
//$$ import net.minecraft.world.level.block.Blocks;
//$$ import net.minecraft.world.level.block.entity.BlockEntity;
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.world.level.lighting.LevelLightEngine;
//$$ import net.minecraft.world.level.material.FluidState;
//$$ import net.minecraft.world.level.material.Fluids;
//$$
//$$ /**
//$$  * The world as seen from inside the capture region: everything outside the
//$$  * selection reads as air.
//$$  *
//$$  * This is what isolates the render — the block models cull against this view,
//$$  * so faces on the region boundary are emitted (they now border air) and no
//$$  * surrounding terrain contributes any geometry.
//$$  */
//$$ public final class RegionBlockView implements BlockAndTintGetter {
//$$
//$$     private final ClientLevel level;
//$$     private final int minX, minY, minZ, maxX, maxY, maxZ;
//$$
//$$     public RegionBlockView(ClientLevel level, int[] bounds) {
//$$         this.level = level;
//$$         this.minX = bounds[0]; this.minY = bounds[1]; this.minZ = bounds[2];
//$$         this.maxX = bounds[3]; this.maxY = bounds[4]; this.maxZ = bounds[5];
//$$     }
//$$
//$$     private boolean inside(BlockPos pos) {
//$$         int x = pos.getX(), y = pos.getY(), z = pos.getZ();
//$$         return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
//$$     }
//$$
//$$     @Override
//$$     public BlockState getBlockState(BlockPos pos) {
//$$         return inside(pos) ? level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
//$$     }
//$$
//$$     @Override
//$$     public FluidState getFluidState(BlockPos pos) {
//$$         return inside(pos) ? level.getFluidState(pos) : Fluids.EMPTY.defaultFluidState();
//$$     }
//$$
//$$     @Override
//$$     public BlockEntity getBlockEntity(BlockPos pos) {
//$$         return inside(pos) ? level.getBlockEntity(pos) : null;
//$$     }
//$$
//$$     @Override
//$$     public CardinalLighting cardinalLighting() {
//$$         return level.cardinalLighting();
//$$     }
//$$
//$$     @Override
//$$     public LevelLightEngine getLightEngine() {
//$$         return level.getLightEngine();
//$$     }
//$$
//$$     @Override
//$$     public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
//$$         return level.getBlockTint(pos, colorResolver);
//$$     }
//$$
//$$     @Override
//$$     public int getHeight() {
//$$         return level.getHeight();
//$$     }
//$$
//$$     @Override
//$$     public int getMinY() {
//$$         return level.getMinY();
//$$     }
//$$ }
//#endif
