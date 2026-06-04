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
//$$ import org.joml.Matrix4f;
//#endif

import java.util.HashSet;
import java.util.Set;

public class HighlightBlock {

//#if MC < 12111
    public static void render(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if (world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();
        int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();
        Color color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();
        Vec3d cam = context.camera().getPos();
        Matrix4f viewRot = context.matrixStack().peek().getPositionMatrix();

        Set<BlockPos> positions = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    String name = Registries.BLOCK.getId(block).getPath();
                    if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
                        positions.add(pos);
                    }
                }
            }
        }
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
//$$     Color color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     Matrix4f viewRot = context.matrices().peek().getPositionMatrix();
//$$
//$$     Set<BlockPos> positions = new HashSet<>();
//$$     for (int x = -radius; x <= radius; x++) {
//$$         for (int y = -radius; y <= radius; y++) {
//$$             for (int z = -radius; z <= radius; z++) {
//$$                 BlockPos pos = center.add(x, y, z);
//$$                 Block block = world.getBlockState(pos).getBlock();
//$$                 String name = Registries.BLOCK.getId(block).getPath();
//$$                 if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
//$$                     positions.add(pos);
//$$                 }
//$$             }
//$$         }
//$$     }
//$$     WorldRenderer.renderBlockOutlines(positions, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
//$$ }
//#else
//$$ public static void render(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
//$$     Minecraft mc = Minecraft.getInstance();
//$$     var world = mc.level;
//$$     if (world == null || mc.player == null) return;
//$$
//$$     BlockPos center = mc.player.blockPosition();
//$$     int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();
//$$     Color color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();
//$$     var cam = context.gameRenderer().getMainCamera().position();
//$$     Matrix4f viewRot = new Matrix4f();
//$$
//$$     Set<BlockPos> positions = new HashSet<>();
//$$     for (int x = -radius; x <= radius; x++) {
//$$         for (int y = -radius; y <= radius; y++) {
//$$             for (int z = -radius; z <= radius; z++) {
//$$                 BlockPos pos = center.offset(x, y, z);
//$$                 var block = world.getBlockState(pos).getBlock();
//$$                 var id = BuiltInRegistries.BLOCK.getKey(block);
//$$                 String name = id.getPath();
//$$                 if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
//$$                     positions.add(pos);
//$$                 }
//$$             }
//$$         }
//$$     }
//$$     WorldRenderer.renderBlockOutlines(positions, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
//$$ }
//#endif

    /** Legacy entry point used by MixinWorldRenderer for MC < 12111. */
    public static void renderHighlightBlock() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if (world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();
        int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();
        Color color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();

        Set<BlockPos> positions = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    String name = Registries.BLOCK.getId(block).getPath();
                    if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
                        positions.add(pos);
                    }
                }
            }
        }
        WorldRenderer.renderBlockOutlines(positions, color, 0.0025, new Matrix4f(), 0, 0, 0);
//#endif
    }
}
