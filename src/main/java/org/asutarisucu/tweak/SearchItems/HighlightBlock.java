package org.asutarisucu.tweak.SearchItems;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.asutarisucu.Configs.Configs;

public class HighlightBlock {

    public static void renderHighlightBlock(){
        MinecraftClient mc = MinecraftClient.getInstance();
        World world=mc.world;
        if (world == null || MinecraftClient.getInstance().player == null) return;

        BlockPos center = MinecraftClient.getInstance().player.getBlockPos();
        int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Color4f color= Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();

        //探索＆描画
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    Identifier id = Registries.BLOCK.getId(block);
                    String name = id.getPath();

                    // Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings() に一致するか確認
                    if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
                        RenderUtils.renderBlockOutline(pos, 0.0025f, 2.0f, color, mc);
                    }
                }
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
