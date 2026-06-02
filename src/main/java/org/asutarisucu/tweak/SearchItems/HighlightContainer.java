package org.asutarisucu.tweak.SearchItems;

//#if MC < 12111
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.MinecraftClient;
import org.asutarisucu.Configs.Configs;
//#elseif MC < 260100
//$$ import fi.dy.masa.malilib.render.MaLiLibPipelines;
//$$ import fi.dy.masa.malilib.render.RenderContext;
//$$ import fi.dy.masa.malilib.render.RenderUtils;
//$$ import fi.dy.masa.malilib.util.data.Color4f;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import org.asutarisucu.Configs.Configs;
//#else
//$$ import fi.dy.masa.malilib.render.RenderUtils;
//$$ import fi.dy.masa.malilib.util.data.Color4f;
//$$ import org.asutarisucu.Configs.Configs;
//#endif
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HighlightContainer {

    public static List<BlockPos> posList = new ArrayList<>();

    public static void renderHighlightContainer() {
//#if MC < 12111
        MinecraftClient mc = MinecraftClient.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Color4f color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();

        if (posList != null) {
            for (BlockPos pos : posList) {
                RenderUtils.renderBlockOutline(pos, 0.0025f, 2.0f, color, mc);
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
//#elseif MC < 260100
//$$ Color4f color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
//$$ Vec3d cam = RenderUtils.camPos();
//$$ try (var ctx = new RenderContext(() -> "SearchHL/container", MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_LEQUAL_DEPTH)) {
//$$     var buf = ctx.getBuilder();
//$$     for (var pos : posList) {
//$$         RenderUtils.drawBlockBoundingBoxOutlinesBatchedLines(pos, cam, color, 0.0025, 2.0f, buf);
//$$     }
//$$     var mesh = buf.endNullable();
//$$     if (mesh != null) { ctx.draw(mesh, false, true); mesh.close(); }
//$$ } catch (Exception ignored) {}
//#else
//$$         Color4f color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
//$$         for (var pos : posList) {
//$$             RenderUtils.renderBlockOutline(pos, 0.0025f, 2.0f, color, false);
//$$         }
//#endif
    }
}
