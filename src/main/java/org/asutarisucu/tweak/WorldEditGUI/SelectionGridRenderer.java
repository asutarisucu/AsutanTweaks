package org.asutarisucu.tweak.WorldEditGUI;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.render.Color;
import org.asutarisucu.lib.render.WorldRenderer;

//#if MC < 12111
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import org.joml.Matrix4f;
//#else
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//$$ import org.joml.Matrix4f;
//#endif

import java.util.Arrays;

/**
 * Draws the WorldEdit cuboid selection as a lattice on its six faces, plus the
 * box edges in a second colour.
 *
 * The segment list is rebuilt only when the selection or the spacing changes;
 * frames reuse the cached array.
 */
public final class SelectionGridRenderer {

    private static double[] gridSegs = new double[0];
    private static int gridCount;
    private static double[] edgeSegs = new double[0];
    private static int[] cachedBounds;
    private static int cachedSpacing = -1;

    private SelectionGridRenderer() {}

    private static void rebuildIfNeeded(int[] b) {
        int spacing = effectiveSpacing(b);
        if (cachedBounds != null && Arrays.equals(cachedBounds, b) && cachedSpacing == spacing) return;
        cachedBounds = b.clone();
        cachedSpacing = spacing;

        double x0 = b[0], y0 = b[1], z0 = b[2];
        double x1 = b[3] + 1.0, y1 = b[4] + 1.0, z1 = b[5] + 1.0;

        SegmentBuffer out = new SegmentBuffer();
        // faces perpendicular to Y
        for (double z : ticks(z0, z1, spacing)) {
            out.add(x0, y0, z, x1, y0, z);
            out.add(x0, y1, z, x1, y1, z);
        }
        for (double x : ticks(x0, x1, spacing)) {
            out.add(x, y0, z0, x, y0, z1);
            out.add(x, y1, z0, x, y1, z1);
        }
        // faces perpendicular to X
        for (double z : ticks(z0, z1, spacing)) {
            out.add(x0, y0, z, x0, y1, z);
            out.add(x1, y0, z, x1, y1, z);
        }
        for (double y : ticks(y0, y1, spacing)) {
            out.add(x0, y, z0, x0, y, z1);
            out.add(x1, y, z0, x1, y, z1);
        }
        // faces perpendicular to Z
        for (double x : ticks(x0, x1, spacing)) {
            out.add(x, y0, z0, x, y1, z0);
            out.add(x, y0, z1, x, y1, z1);
        }
        for (double y : ticks(y0, y1, spacing)) {
            out.add(x0, y, z0, x1, y, z0);
            out.add(x0, y, z1, x1, y, z1);
        }
        gridSegs = out.data;
        gridCount = out.count;

        // box edges, nudged outwards so they are not z-fighting with the grid
        double e = 0.003;
        double ex0 = x0 - e, ey0 = y0 - e, ez0 = z0 - e;
        double ex1 = x1 + e, ey1 = y1 + e, ez1 = z1 + e;
        SegmentBuffer edges = new SegmentBuffer();
        edges.add(ex0, ey0, ez0, ex1, ey0, ez0); edges.add(ex1, ey0, ez0, ex1, ey0, ez1);
        edges.add(ex1, ey0, ez1, ex0, ey0, ez1); edges.add(ex0, ey0, ez1, ex0, ey0, ez0);
        edges.add(ex0, ey1, ez0, ex1, ey1, ez0); edges.add(ex1, ey1, ez0, ex1, ey1, ez1);
        edges.add(ex1, ey1, ez1, ex0, ey1, ez1); edges.add(ex0, ey1, ez1, ex0, ey1, ez0);
        edges.add(ex0, ey0, ez0, ex0, ey1, ez0); edges.add(ex1, ey0, ez0, ex1, ey1, ez0);
        edges.add(ex1, ey0, ez1, ex1, ey1, ez1); edges.add(ex0, ey0, ez1, ex0, ey1, ez1);
        edgeSegs = edges.data;
    }

    /** Doubles the configured spacing until the segment count fits the configured cap. */
    private static int effectiveSpacing(int[] b) {
        int spacing = Configs.Generic.WORLDEDIT_GRID_SPACING.getIntegerValue();
        int max = Configs.Generic.WORLDEDIT_GRID_MAX_LINES.getIntegerValue();
        int dx = b[3] - b[0] + 1, dy = b[4] - b[1] + 1, dz = b[5] - b[2] + 1;
        while (spacing < 4096) {
            long lines = 4L * ((dx / spacing + 1) + (dy / spacing + 1) + (dz / spacing + 1));
            if (lines <= max) break;
            spacing *= 2;
        }
        return spacing;
    }

    /** Grid positions from {@code from} to {@code to} inclusive of both ends. */
    private static double[] ticks(double from, double to, int spacing) {
        int n = (int) Math.floor((to - from) / spacing);
        double[] out = new double[n + 2];
        int i = 0;
        for (double v = from; v < to; v += spacing) out[i++] = v;
        out[i++] = to;
        return Arrays.copyOf(out, i);
    }

    private static final class SegmentBuffer {
        double[] data = new double[256];
        int count;

        void add(double ax, double ay, double az, double bx, double by, double bz) {
            if ((count + 1) * 6 > data.length) data = Arrays.copyOf(data, data.length * 2);
            int o = count * 6;
            data[o] = ax; data[o + 1] = ay; data[o + 2] = az;
            data[o + 3] = bx; data[o + 4] = by; data[o + 5] = bz;
            count++;
        }
    }

    private static void draw(Matrix4f viewRot, double camX, double camY, double camZ) {
        int[] b = WorldEditSelection.getBounds();
        if (b != null) {
            rebuildIfNeeded(b);
            Color grid = Configs.Generic.WORLDEDIT_GRID_COLOR.getColor();
            Color edge = Configs.Generic.WORLDEDIT_EDGE_COLOR.getColor();
            WorldRenderer.renderLineSegments(gridSegs, gridCount, grid, viewRot, camX, camY, camZ);
            WorldRenderer.renderLineSegments(edgeSegs, 12, edge, viewRot, camX, camY, camZ);
        }
        // Drawn whether or not the box is complete, so the first click already shows.
        drawPoint(WorldEditSelection.getPoint(0), Configs.Generic.WORLDEDIT_POS1_COLOR.getColor(),
                  viewRot, camX, camY, camZ);
        drawPoint(WorldEditSelection.getPoint(1), Configs.Generic.WORLDEDIT_POS2_COLOR.getColor(),
                  viewRot, camX, camY, camZ);
    }

    /** Outlines the single block a selection point sits on, slightly inflated so it reads over the grid. */
    private static void drawPoint(int[] point, Color color, Matrix4f viewRot,
                                  double camX, double camY, double camZ) {
        if (point == null) return;
        double e = 0.012;
        double x0 = point[0] - e, y0 = point[1] - e, z0 = point[2] - e;
        double x1 = point[0] + 1 + e, y1 = point[1] + 1 + e, z1 = point[2] + 1 + e;
        SegmentBuffer box = new SegmentBuffer();
        box.add(x0, y0, z0, x1, y0, z0); box.add(x1, y0, z0, x1, y0, z1);
        box.add(x1, y0, z1, x0, y0, z1); box.add(x0, y0, z1, x0, y0, z0);
        box.add(x0, y1, z0, x1, y1, z0); box.add(x1, y1, z0, x1, y1, z1);
        box.add(x1, y1, z1, x0, y1, z1); box.add(x0, y1, z1, x0, y1, z0);
        box.add(x0, y0, z0, x0, y1, z0); box.add(x1, y0, z0, x1, y1, z0);
        box.add(x1, y0, z1, x1, y1, z1); box.add(x0, y0, z1, x0, y1, z1);
        WorldRenderer.renderLineSegments(box.data, box.count, color, viewRot, camX, camY, camZ);
    }

//#if MC < 12111
    public static void render(WorldRenderContext context) {
        Vec3d cam = context.camera().getPos();
        draw(context.matrixStack().peek().getPositionMatrix(), cam.x, cam.y, cam.z);
    }
//#elseif MC < 260100
//$$ public static void render(WorldRenderContext context) {
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     draw(context.matrices().peek().getPositionMatrix(), cam.x, cam.y, cam.z);
//$$ }
//#else
//$$ public static void render(LevelRenderContext context) {
//#if MC < 260200
//$$     var cam = context.gameRenderer().getMainCamera().position();
//#else
//$$     var cam = context.gameRenderer().mainCamera().position();
//$$     WorldRenderer.beginFrame(context.submitNodeCollector(), context.poseStack());
//#endif
//$$     draw(new Matrix4f(), cam.x, cam.y, cam.z);
//$$ }
//#endif
}
