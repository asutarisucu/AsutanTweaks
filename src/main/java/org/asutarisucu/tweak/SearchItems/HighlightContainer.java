package org.asutarisucu.tweak.SearchItems;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.asutarisucu.Configs.Configs;

import java.util.ArrayList;
import java.util.List;

public class HighlightContainer {
    public static List<BlockPos> posList=new ArrayList<>();
    public static void renderHighlightContainer(){
        MinecraftClient mc=MinecraftClient.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Color4f color= Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();

        if(posList!=null){
            for(BlockPos pos:posList){
                RenderUtils.renderBlockOutline(pos, 0.0025f, 2.0f, color, mc);
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}


