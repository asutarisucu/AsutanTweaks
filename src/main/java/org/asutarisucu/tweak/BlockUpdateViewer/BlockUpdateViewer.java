package org.asutarisucu.tweak.BlockUpdateViewer;

import org.asutarisucu.Configs.Feature;
import org.asutarisucu.lib.render.Color;
import org.asutarisucu.lib.render.WorldRenderer;

//#if MC < 12111
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Set;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//$$ import net.minecraft.block.Block;
//$$ import net.minecraft.block.BlockState;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.item.BlockItem;
//$$ import net.minecraft.item.ItemStack;
//$$ import net.minecraft.state.property.Properties;
//$$ import net.minecraft.util.hit.BlockHitResult;
//$$ import net.minecraft.util.hit.HitResult;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import org.joml.Matrix4f;
//$$
//$$ import java.util.Set;
//#else
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.item.BlockItem;
//$$ import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//$$ import net.minecraft.world.phys.BlockHitResult;
//$$ import net.minecraft.world.phys.HitResult;
//$$ import org.joml.Matrix4f;
//$$
//$$ import java.util.Set;
//#endif

public class BlockUpdateViewer {

    private static final Color RED  = Color.fromRgba(1f, 0f, 0f, 1f);
    private static final Color BLUE = Color.fromRgba(0f, 0.3f, 1f, 1f);

    public static void register() {
        // Registration is done via Reference.registerWorldRendering()
    }

//#if MC < 12111
    public static void renderOverlay(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        boolean placementEnabled = Feature.PLACEMENT_UPDATE_VIEWER.isEnabled();
        boolean breakingEnabled  = Feature.BREAKING_UPDATE_VIEWER.isEnabled();
        boolean suppressionNeeds = Feature.UPDATE_SUPPRESSION_VIEW.isEnabled();
        if (!placementEnabled && !breakingEnabled && !suppressionNeeds) return;

        HitResult hitResult = mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit)) return;
        if (blockHit.getType() == HitResult.Type.MISS) return;

        BlockPos hitPos = blockHit.getBlockPos();
        ItemStack heldItem = mc.player.getMainHandStack();
        Vec3d cam = context.camera().getPos();
        Matrix4f viewRot = context.matrixStack().peek().getPositionMatrix();

        boolean holdingBlockItem = heldItem.getItem() instanceof BlockItem;

        if ((placementEnabled || suppressionNeeds) && holdingBlockItem) {
            BlockItem blockItem = (BlockItem) heldItem.getItem();
            BlockPos placedPos = hitPos.offset(blockHit.getSide());
            Block placedBlock = blockItem.getBlock();
            BlockState approxState = placedBlock.getDefaultState();
            if (approxState.contains(Properties.HORIZONTAL_FACING)) {
                approxState = approxState.with(Properties.HORIZONTAL_FACING,
                        mc.player.getHorizontalFacing().getOpposite());
            }
            Set<BlockPos> updateSet = BlockUpdateCalculator.compute(mc.world, placedPos, approxState);
            if (placementEnabled) WorldRenderer.renderBlockGroup(updateSet, RED, viewRot, cam.x, cam.y, cam.z);
        }

        if ((breakingEnabled && !holdingBlockItem) || (suppressionNeeds && !holdingBlockItem)) {
            Set<BlockPos> updateSet = BlockUpdateCalculator.compute(mc.world, hitPos);
            if (breakingEnabled) WorldRenderer.renderBlockGroup(updateSet, BLUE, viewRot, cam.x, cam.y, cam.z);
        }
    }
//#elseif MC < 260100
//$$ public static void renderOverlay(WorldRenderContext context) {
//$$     MinecraftClient mc = MinecraftClient.getInstance();
//$$     if (mc.player == null || mc.world == null) return;
//$$
//$$     boolean placementEnabled = Feature.PLACEMENT_UPDATE_VIEWER.isEnabled();
//$$     boolean breakingEnabled  = Feature.BREAKING_UPDATE_VIEWER.isEnabled();
//$$     boolean suppressionNeeds = Feature.UPDATE_SUPPRESSION_VIEW.isEnabled();
//$$     if (!placementEnabled && !breakingEnabled && !suppressionNeeds) return;
//$$
//$$     HitResult hitResult = mc.crosshairTarget;
//$$     if (!(hitResult instanceof BlockHitResult blockHit)) return;
//$$     if (blockHit.getType() == HitResult.Type.MISS) return;
//$$
//$$     BlockPos hitPos = blockHit.getBlockPos();
//$$     ItemStack heldItem = mc.player.getMainHandStack();
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     Matrix4f viewRot = context.matrices().peek().getPositionMatrix();
//$$
//$$     boolean holdingBlockItem = heldItem.getItem() instanceof BlockItem;
//$$
//$$     if ((placementEnabled || suppressionNeeds) && holdingBlockItem) {
//$$         BlockItem blockItem = (BlockItem) heldItem.getItem();
//$$         BlockPos placedPos = hitPos.offset(blockHit.getSide());
//$$         Block placedBlock = blockItem.getBlock();
//$$         BlockState approxState = placedBlock.getDefaultState();
//$$         if (approxState.contains(Properties.HORIZONTAL_FACING)) {
//$$             approxState = approxState.with(Properties.HORIZONTAL_FACING,
//$$                     mc.player.getHorizontalFacing().getOpposite());
//$$         }
//$$         Set<BlockPos> updateSet = BlockUpdateCalculator.compute(mc.world, placedPos, approxState);
//$$         if (placementEnabled) WorldRenderer.renderBlockGroup(updateSet, RED, viewRot, cam.x, cam.y, cam.z);
//$$     }
//$$
//$$     if ((breakingEnabled && !holdingBlockItem) || (suppressionNeeds && !holdingBlockItem)) {
//$$         Set<BlockPos> updateSet = BlockUpdateCalculator.compute(mc.world, hitPos);
//$$         if (breakingEnabled) WorldRenderer.renderBlockGroup(updateSet, BLUE, viewRot, cam.x, cam.y, cam.z);
//$$     }
//$$ }
//#else
//$$ public static void renderOverlay(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
//$$     Minecraft mc = Minecraft.getInstance();
//$$     if (mc.player == null || mc.level == null) return;
//$$
//$$     boolean placementEnabled = Feature.PLACEMENT_UPDATE_VIEWER.isEnabled();
//$$     boolean breakingEnabled  = Feature.BREAKING_UPDATE_VIEWER.isEnabled();
//$$     boolean suppressionNeeds = Feature.UPDATE_SUPPRESSION_VIEW.isEnabled();
//$$     if (!placementEnabled && !breakingEnabled && !suppressionNeeds) return;
//$$
//$$     if (!(mc.hitResult instanceof BlockHitResult blockHit)) return;
//$$     if (blockHit.getType() == HitResult.Type.MISS) return;
//$$
//$$     BlockPos hitPos = blockHit.getBlockPos();
//$$     var heldItem = mc.player.getMainHandItem();
//#if MC < 260200
//$$     var cam = context.gameRenderer().getMainCamera().position();
//#else
//$$     var cam = context.gameRenderer().mainCamera().position();
//$$     WorldRenderer.beginFrame(context.submitNodeCollector(), context.poseStack());
//#endif
//$$     Matrix4f viewRot = new Matrix4f();
//$$
//$$     boolean holdingBlockItem = heldItem.getItem() instanceof BlockItem;
//$$
//$$     if ((placementEnabled || suppressionNeeds) && holdingBlockItem) {
//$$         var blockItem = (BlockItem) heldItem.getItem();
//$$         var placedPos = hitPos.relative(blockHit.getDirection());
//$$         var placedBlock = blockItem.getBlock();
//$$         var approxState = placedBlock.defaultBlockState();
//$$         if (approxState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
//$$             approxState = approxState.setValue(BlockStateProperties.HORIZONTAL_FACING,
//$$                     mc.player.getDirection().getOpposite());
//$$         }
//$$         Set<BlockPos> updateSet = BlockUpdateCalculator.compute(mc.level, placedPos, approxState);
//$$         if (placementEnabled) WorldRenderer.renderBlockGroup(updateSet, RED, viewRot, cam.x, cam.y, cam.z);
//$$     }
//$$
//$$     if ((breakingEnabled && !holdingBlockItem) || (suppressionNeeds && !holdingBlockItem)) {
//$$         Set<BlockPos> updateSet = BlockUpdateCalculator.compute(mc.level, hitPos);
//$$         if (breakingEnabled) WorldRenderer.renderBlockGroup(updateSet, BLUE, viewRot, cam.x, cam.y, cam.z);
//$$     }
//$$ }
//#endif
}
