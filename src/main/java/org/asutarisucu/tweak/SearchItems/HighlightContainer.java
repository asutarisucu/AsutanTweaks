package org.asutarisucu.tweak.SearchItems;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.render.Color;
import org.asutarisucu.lib.render.WorldRenderer;

//#if MC < 12111
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import org.joml.Matrix4f;
//#else
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//$$ import net.minecraft.core.BlockPos;
//$$ import org.joml.Matrix4f;
//#endif

import java.util.Collections;
import java.util.Set;

public class HighlightContainer {

    /**
     * Highlighted container positions, rebuilt by RenderCash once per second.
     * The scanner publishes a fresh concurrent set and keeps filling it from
     * async sync callbacks, so rendering iterates without copying or locking.
     */
    public static volatile Set<BlockPos> positions = Collections.emptySet();

//#if MC < 12111
    public static void render(WorldRenderContext context) {
        Set<BlockPos> posSet = positions;
        if (posSet.isEmpty()) return;
        Color color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
        Vec3d cam = context.camera().getPos();
        Matrix4f viewRot = context.matrixStack().peek().getPositionMatrix();
        WorldRenderer.renderBlockOutlines(posSet, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
    }
//#elseif MC < 260100
//$$ public static void render(WorldRenderContext context) {
//$$     Set<BlockPos> posSet = positions;
//$$     if (posSet.isEmpty()) return;
//$$     Color color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     Matrix4f viewRot = context.matrices().peek().getPositionMatrix();
//$$     WorldRenderer.renderBlockOutlines(posSet, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
//$$ }
//#else
//$$ public static void render(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
//$$     Set<BlockPos> posSet = positions;
//$$     if (posSet.isEmpty()) return;
//$$     Color color = Configs.Generic.HIGHLIGHT_CONTAINER_COLOR.getColor();
//$$     var cam = context.gameRenderer().getMainCamera().position();
//$$     Matrix4f viewRot = new Matrix4f();
//$$     WorldRenderer.renderBlockOutlines(posSet, color, 0.0025, viewRot, cam.x, cam.y, cam.z);
//$$ }
//#endif
}
