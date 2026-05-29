package org.asutarisucu.tweak.SearchItems;

//#if MC < 260100
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.asutarisucu.Configs.Configs;
//#else
//$$ import fi.dy.masa.malilib.render.RenderUtils;
//$$ import fi.dy.masa.malilib.util.data.Color4f;
//$$ import net.minecraft.core.registries.BuiltInRegistries;
//$$ import org.asutarisucu.Configs.Configs;
//#endif
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class HighlightBlock {

    public static void renderHighlightBlock() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if (world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();
        int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Color4f color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();
                    Identifier id = Registries.BLOCK.getId(block);
                    String name = id.getPath();
                    if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
                        RenderUtils.renderBlockOutline(pos, 0.0025f, 2.0f, color, mc);
                    }
                }
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
//#else
//$$         var mc = Minecraft.getInstance();
//$$         var world = mc.level;
//$$         if (world == null || mc.player == null) return;
//$$
//$$         var center = mc.player.blockPosition();
//$$         int radius = Configs.Generic.HIGHLIGHT_BLOCK_RANGE.getIntegerValue();
//$$         Color4f color = Configs.Generic.HIGHLIGHT_BLOCK_COLOR.getColor();
//$$
//$$         for (int x = -radius; x <= radius; x++) {
//$$             for (int y = -radius; y <= radius; y++) {
//$$                 for (int z = -radius; z <= radius; z++) {
//$$                     var pos = center.offset(x, y, z);
//$$                     var block = world.getBlockState(pos).getBlock();
//$$                     var id = BuiltInRegistries.BLOCK.getKey(block);
//$$                     String name = id.getPath();
//$$                     if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
//$$                         RenderUtils.renderBlockOutline(pos, 0.0025f, 2.0f, color, false);
//$$                     }
//$$                 }
//$$             }
//$$         }
//#endif
    }
}
