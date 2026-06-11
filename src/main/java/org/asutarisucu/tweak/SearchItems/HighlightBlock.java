package org.asutarisucu.tweak.SearchItems;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.render.Color;
import org.asutarisucu.lib.render.WorldRenderer;

//#if MC < 12111
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//$$ import net.minecraft.block.Block;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.registry.Registries;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import net.minecraft.world.World;
//$$ import org.joml.Matrix4f;
//#else
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.core.registries.BuiltInRegistries;
//$$ import net.minecraft.world.level.Level;
//$$ import net.minecraft.world.level.block.Block;
//$$ import org.joml.Matrix4f;
//#endif

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Highlights configured blocks around the player.
 *
 * The per-position test is a Block identity lookup in a precomputed set
 * (no registry/string work in the scan loop), and the volume scan itself
 * runs at most once per game tick; render frames reuse the cached result.
 */
public class HighlightBlock {

    // Target blocks resolved from HIGHLIGHT_ITEM_LIST; rebuilt only when the list changes.
    private static Set<Block> targetBlocks = Collections.emptySet();
    private static List<String> targetNamesSnapshot = null;

    // Scan result cache, valid for one game tick at a fixed center/radius.
    private static Set<BlockPos> matchedPositions = Collections.emptySet();
    private static long lastScanTime = Long.MIN_VALUE;
    private static BlockPos lastCenter = null;
    private static int lastRadius = -1;
    private static Object lastWorld = null;

    private static Set<Block> resolveTargetBlocks() {
        List<String> names = Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings();
        if (targetNamesSnapshot == null || !targetNamesSnapshot.equals(names)) {
            targetNamesSnapshot = List.copyOf(names);
            Set<Block> resolved = new HashSet<>();
            if (!names.isEmpty()) {
                Set<String> nameSet = new HashSet<>(names);
//#if MC < 260100
                for (Block block : Registries.BLOCK) {
                    if (nameSet.contains(Registries.BLOCK.getId(block).getPath())) {
                        resolved.add(block);
                    }
                }
//#else
//$$             for (Block block : BuiltInRegistries.BLOCK) {
//$$                 if (nameSet.contains(BuiltInRegistries.BLOCK.getKey(block).getPath())) {
//$$                     resolved.add(block);
//$$                 }
//$$             }
//#endif
            }
            targetBlocks = resolved;
            lastScanTime = Long.MIN_VALUE; // force a rescan with the new targets
        }
        return targetBlocks;
    }

//#if MC < 260100
    private static Set<BlockPos> getMatchedPositions(World world, BlockPos center, int radius) {
        Set<Block> targets = resolveTargetBlocks();
        long time = world.getTime();
        if (time == lastScanTime && radius == lastRadius && world == lastWorld
                && center.equals(lastCenter)) {
            return matchedPositions;
        }
        lastScanTime = time;
        lastRadius = radius;
        lastCenter = center.toImmutable();
        lastWorld = world;

        if (targets.isEmpty()) {
            matchedPositions = Collections.emptySet();
            return matchedPositions;
        }
        Set<BlockPos> found = new HashSet<>();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (targets.contains(world.getBlockState(pos).getBlock())) {
                        found.add(pos.toImmutable());
                    }
                }
            }
        }
        matchedPositions = found;
        return matchedPositions;
    }
//#else
//$$ private static Set<BlockPos> getMatchedPositions(Level world, BlockPos center, int radius) {
//$$     Set<Block> targets = resolveTargetBlocks();
//$$     long time = world.getGameTime();
//$$     if (time == lastScanTime && radius == lastRadius && world == lastWorld
//$$             && center.equals(lastCenter)) {
//$$         return matchedPositions;
//$$     }
//$$     lastScanTime = time;
//$$     lastRadius = radius;
//$$     lastCenter = center.immutable();
//$$     lastWorld = world;
//$$
//$$     if (targets.isEmpty()) {
//$$         matchedPositions = Collections.emptySet();
//$$         return matchedPositions;
//$$     }
//$$     Set<BlockPos> found = new HashSet<>();
//$$     BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
//$$     for (int x = -radius; x <= radius; x++) {
//$$         for (int y = -radius; y <= radius; y++) {
//$$             for (int z = -radius; z <= radius; z++) {
//$$                 pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
//$$                 if (targets.contains(world.getBlockState(pos).getBlock())) {
//$$                     found.add(pos.immutable());
//$$                 }
//$$             }
//$$         }
//$$     }
//$$     matchedPositions = found;
//$$     return matchedPositions;
//$$ }
//#endif

//#if MC < 12111
    public static void render(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if (world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();
        int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();
        Set<BlockPos> positions = getMatchedPositions(world, center, radius);
        if (positions.isEmpty()) return;

        Color color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();
        Vec3d cam = context.camera().getPos();
        Matrix4f viewRot = context.matrixStack().peek().getPositionMatrix();
        WorldRenderer.renderBlockOutlines(positions, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
    }
//#elseif MC < 260100
//$$ public static void render(WorldRenderContext context) {
//$$     MinecraftClient mc = MinecraftClient.getInstance();
//$$     World world = mc.world;
//$$     if (world == null || mc.player == null) return;
//$$
//$$     BlockPos center = mc.player.getBlockPos();
//$$     int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();
//$$     Set<BlockPos> positions = getMatchedPositions(world, center, radius);
//$$     if (positions.isEmpty()) return;
//$$
//$$     Color color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     Matrix4f viewRot = context.matrices().peek().getPositionMatrix();
//$$     WorldRenderer.renderBlockOutlines(positions, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
//$$ }
//#else
//$$ public static void render(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
//$$     Minecraft mc = Minecraft.getInstance();
//$$     Level world = mc.level;
//$$     if (world == null || mc.player == null) return;
//$$
//$$     BlockPos center = mc.player.blockPosition();
//$$     int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();
//$$     Set<BlockPos> positions = getMatchedPositions(world, center, radius);
//$$     if (positions.isEmpty()) return;
//$$
//$$     Color color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();
//$$     var cam = context.gameRenderer().getMainCamera().position();
//$$     Matrix4f viewRot = new Matrix4f();
//$$     WorldRenderer.renderBlockOutlines(positions, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
//$$ }
//#endif
}
