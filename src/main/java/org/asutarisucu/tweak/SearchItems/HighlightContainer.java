package org.asutarisucu.tweak.SearchItems;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.render.Color;
import org.asutarisucu.lib.render.WorldRenderer;

//#if MC < 12111
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import org.joml.Matrix4f;
//#else
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.core.BlockPos;
//$$ import org.joml.Matrix4f;
//#endif

import java.util.ArrayList;
import java.util.List;

public class HighlightContainer {

    public static List<BlockPos> posList = new ArrayList<>();

//#if MC < 12111
    public static void render(WorldRenderContext context) {
        Color color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
        Vec3d cam = context.camera().getPos();
        Matrix4f viewRot = context.matrixStack().peek().getPositionMatrix();
        WorldRenderer.renderBlockOutlines(new java.util.HashSet<>(posList), color, 0.0025,
                viewRot, cam.x, cam.y, cam.z);
    }
//#elseif MC < 260100
//$$ public static void render(WorldRenderContext context) {
//$$     Color color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     Matrix4f viewRot = context.matrices().peek().getPositionMatrix();
//$$     WorldRenderer.renderBlockOutlines(new java.util.HashSet<>(posList), color, 0.0025,
//$$             viewRot, cam.x, cam.y, cam.z);
//$$ }
//#else
//$$ public static void render(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
//$$     Color color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
//$$     var cam = context.gameRenderer().getMainCamera().position();
//$$     Matrix4f viewRot = new Matrix4f();
//$$     WorldRenderer.renderBlockOutlines(new java.util.HashSet<>(posList), color, 0.0025,
//$$             viewRot, cam.x, cam.y, cam.z);
//$$ }
//#endif

    /** Legacy entry point used by scan logic before 12111. */
    public static void renderHighlightContainer() {
//#if MC < 260100
        Color color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
        WorldRenderer.renderBlockOutlines(new java.util.HashSet<>(posList), color, 0.0025,
                new Matrix4f(), 0, 0, 0);
//#endif
    }
}
