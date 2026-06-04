package org.asutarisucu.lib.render;

//#if MC < 12101
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
//#elseif MC < 12111
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import net.minecraft.client.render.BufferRenderer;
//$$ import net.minecraft.client.render.GameRenderer;
//$$ import net.minecraft.client.render.Tessellator;
//$$ import net.minecraft.client.render.VertexConsumer;
//$$ import net.minecraft.client.render.VertexFormat;
//$$ import net.minecraft.client.render.VertexFormats;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import org.joml.Matrix4f;
//#elseif MC < 260100
//$$ import com.mojang.blaze3d.vertex.VertexFormat;
//$$ import net.minecraft.client.render.RenderLayers;
//$$ import net.minecraft.client.render.Tessellator;
//$$ import net.minecraft.client.render.VertexConsumer;
//$$ import net.minecraft.client.render.VertexFormats;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import org.joml.Matrix4f;
//#else
//$$ import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//$$ import com.mojang.blaze3d.vertex.Tesselator;
//$$ import com.mojang.blaze3d.vertex.VertexFormat;
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes;
//$$ import net.minecraft.core.BlockPos;
//$$ import org.joml.Matrix4f;
//#endif

import java.util.Set;

/**
 * Precision-fixed world-space block rendering.
 *
 * All render methods take camera position as doubles and compute camera-relative
 * vertex coordinates before casting to float, avoiding catastrophic cancellation
 * at large world coordinates (e.g. x=50000).
 *
 * For MC < 12111: caller supplies the view-rotation matrix (no translation component);
 * vertices are transformed per-vertex before upload.
 * For MC >= 12111: the pipeline shader handles view transform via uniforms;
 * camera-relative coordinates are passed directly (viewRot is unused).
 */
public class WorldRenderer {

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public static void renderBlockFills(Set<BlockPos> positions, Color color,
                                        boolean depthTest, Matrix4f viewRot,
                                        double camX, double camY, double camZ) {
        if (positions.isEmpty()) return;
        setupBlend();
        if (!depthTest) disableDepth();
        drawFills(positions, color, viewRot, camX, camY, camZ);
        if (!depthTest) enableDepth();
        teardownBlend();
    }

    public static void renderBlockOutlines(Set<BlockPos> positions, Color color,
                                           double expand, Matrix4f viewRot,
                                           double camX, double camY, double camZ) {
        if (positions.isEmpty()) return;
        setupBlend();
        drawOutlines(positions, color, (float) expand, viewRot, camX, camY, camZ);
        teardownBlend();
    }

    /** Draws solid fill (depth-tested) + faint through-wall fill + outline. */
    public static void renderBlockGroup(Set<BlockPos> positions, Color baseColor,
                                        Matrix4f viewRot,
                                        double camX, double camY, double camZ) {
        if (positions.isEmpty()) return;
        Color solid   = baseColor.withAlpha(0.04f);
        Color through = baseColor.withAlpha(0.02f);
        Color outline = baseColor.withAlpha(0.60f);
//#if MC < 12111
        setupBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        drawFills(positions, solid, viewRot, camX, camY, camZ);
        disableDepth();
        drawFills(positions, through, viewRot, camX, camY, camZ);
        enableDepth();
        drawOuterBoundaryLines(positions, outline, 0.0025f, viewRot, camX, camY, camZ);
        RenderSystem.enableCull();
        teardownBlend();
//#else
//$$ // MC 12111+: RenderLayer pipelines handle blend/depth; skip through-wall pass
//$$ drawFills(positions, solid, viewRot, camX, camY, camZ);
//$$ drawOuterBoundaryLines(positions, outline, 0.0025f, viewRot, camX, camY, camZ);
//#endif
    }

    // -------------------------------------------------------------------------
    // Internal draw helpers
    // -------------------------------------------------------------------------

    private static void drawFills(Set<BlockPos> positions, Color c, Matrix4f m,
                                   double camX, double camY, double camZ) {
//#if MC < 12101
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : positions) {
            float x = (float)(pos.getX() - camX);
            float y = (float)(pos.getY() - camY);
            float z = (float)(pos.getZ() - camZ);
            addBox(m, buf, x, y, z, x + 1f, y + 1f, z + 1f, c.r(), c.g(), c.b(), c.a());
        }
        tess.draw();
//#elseif MC < 12111
//$$ RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//$$ for (BlockPos pos : positions) {
//$$     float x = (float)(pos.getX() - camX);
//$$     float y = (float)(pos.getY() - camY);
//$$     float z = (float)(pos.getZ() - camZ);
//$$     addBox(m, buf, x, y, z, x + 1f, y + 1f, z + 1f, c.r(), c.g(), c.b(), c.a());
//$$ }
//$$ BufferRenderer.drawWithGlobalProgram(buf.end());
//#elseif MC < 260100
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//$$ for (BlockPos pos : positions) {
//$$     float x = (float)(pos.getX() - camX);
//$$     float y = (float)(pos.getY() - camY);
//$$     float z = (float)(pos.getZ() - camZ);
//$$     addBox12111(buf, x, y, z, x + 1f, y + 1f, z + 1f, c.r(), c.g(), c.b(), c.a());
//$$ }
//$$ var mesh = buf.endNullable();
//$$ if (mesh != null) { RenderLayers.debugFilledBox().draw(mesh); mesh.close(); }
//#else
//$$ var buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//$$ for (BlockPos pos : positions) {
//$$     float x = (float)(pos.getX() - camX);
//$$     float y = (float)(pos.getY() - camY);
//$$     float z = (float)(pos.getZ() - camZ);
//$$     addBox26(buf, x, y, z, x + 1f, y + 1f, z + 1f, c.r(), c.g(), c.b(), c.a());
//$$ }
//$$ var mesh = buf.build();
//$$ if (mesh != null) { RenderTypes.debugFilledBox().draw(mesh); mesh.close(); }
//#endif
    }

    private static void drawOutlines(Set<BlockPos> positions, Color c, float expand, Matrix4f m,
                                      double camX, double camY, double camZ) {
//#if MC < 12101
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : positions) {
            float x = (float)(pos.getX() - camX) - expand;
            float y = (float)(pos.getY() - camY) - expand;
            float z = (float)(pos.getZ() - camZ) - expand;
            addBoxLines(m, buf, x, y, z, x + 1f + 2*expand, y + 1f + 2*expand, z + 1f + 2*expand, c.r(), c.g(), c.b(), c.a());
        }
        tess.draw();
//#elseif MC < 12111
//$$ RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
//$$ for (BlockPos pos : positions) {
//$$     float x = (float)(pos.getX() - camX) - expand;
//$$     float y = (float)(pos.getY() - camY) - expand;
//$$     float z = (float)(pos.getZ() - camZ) - expand;
//$$     addBoxLines(m, buf, x, y, z, x + 1f + 2*expand, y + 1f + 2*expand, z + 1f + 2*expand, c.r(), c.g(), c.b(), c.a());
//$$ }
//$$ BufferRenderer.drawWithGlobalProgram(buf.end());
//#elseif MC < 260100
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
//$$ for (BlockPos pos : positions) {
//$$     float x = (float)(pos.getX() - camX) - expand;
//$$     float y = (float)(pos.getY() - camY) - expand;
//$$     float z = (float)(pos.getZ() - camZ) - expand;
//$$     addBoxLines12111(buf, x, y, z, x + 1f + 2*expand, y + 1f + 2*expand, z + 1f + 2*expand, c.r(), c.g(), c.b(), c.a());
//$$ }
//$$ var mesh = buf.endNullable();
//$$ if (mesh != null) { RenderLayers.LINES_TRANSLUCENT.draw(mesh); mesh.close(); }
//#else
//$$ var buf = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
//$$ for (BlockPos pos : positions) {
//$$     float x = (float)(pos.getX() - camX) - expand;
//$$     float y = (float)(pos.getY() - camY) - expand;
//$$     float z = (float)(pos.getZ() - camZ) - expand;
//$$     addBoxLines26(buf, x, y, z, x + 1f + 2*expand, y + 1f + 2*expand, z + 1f + 2*expand, c.r(), c.g(), c.b(), c.a());
//$$ }
//$$ var mesh = buf.build();
//$$ if (mesh != null) { RenderTypes.lines().draw(mesh); mesh.close(); }
//#endif
    }

    /** Draws only edges that are on the outer boundary (shared interior edges omitted). */
    private static void drawOuterBoundaryLines(Set<BlockPos> positions, Color c, float expand,
                                                Matrix4f m, double camX, double camY, double camZ) {
//#if MC < 12101
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        addBoundaryEdges(positions, c, expand, m, camX, camY, camZ, buf);
        tess.draw();
//#elseif MC < 12111
//$$ RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
//$$ addBoundaryEdges(positions, c, expand, m, camX, camY, camZ, buf);
//$$ BufferRenderer.drawWithGlobalProgram(buf.end());
//#elseif MC < 260100
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
//$$ addBoundaryEdges12111(positions, c, expand, camX, camY, camZ, buf);
//$$ var mesh = buf.endNullable();
//$$ if (mesh != null) { RenderLayers.LINES_TRANSLUCENT.draw(mesh); mesh.close(); }
//#else
//$$ var buf = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
//$$ addBoundaryEdges26(positions, c, expand, camX, camY, camZ, buf);
//$$ var mesh = buf.build();
//$$ if (mesh != null) { RenderTypes.lines().draw(mesh); mesh.close(); }
//#endif
    }

    // -------------------------------------------------------------------------
    // Vertex helpers - MC < 12111 (matrix-transformed, Yarn names)
    // -------------------------------------------------------------------------

//#if MC < 12101
    private static void addBox(Matrix4f m, VertexConsumer b,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float r, float g, float bl, float a) {
        b.vertex(m,x0,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y0,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z0).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x0,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x0,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z0).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z0).color(r,g,bl,a).next();
    }

    private static void addBoxLines(Matrix4f m, VertexConsumer b,
                                     float x0, float y0, float z0, float x1, float y1, float z1,
                                     float r, float g, float bl, float a) {
        b.vertex(m,x0,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z1).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y0,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y0,z0).color(r,g,bl,a).next();
        b.vertex(m,x0,y1,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y1,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x1,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x0,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z1).color(r,g,bl,a).next();
    }

    private static void addBoundaryEdges(Set<BlockPos> positions, Color c, float expand, Matrix4f m,
                                          double camX, double camY, double camZ, VertexConsumer buf) {
        for (BlockPos pos : positions) {
            double bx = pos.getX() - camX, by = pos.getY() - camY, bz = pos.getZ() - camZ;
            float x0 = (float)(bx - expand), y0 = (float)(by - expand), z0 = (float)(bz - expand);
            float x1 = (float)(bx + 1 + expand), y1 = (float)(by + 1 + expand), z1 = (float)(bz + 1 + expand);
            float r = c.r(), g = c.g(), b = c.b(), a = c.a();
            if (!positions.contains(pos.up())) {
                if (!positions.contains(pos.north()))  { buf.vertex(m,x0,y1,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z0).color(r,g,b,a).next(); }
                if (!positions.contains(pos.south()))  { buf.vertex(m,x0,y1,z1).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.west()))   { buf.vertex(m,x0,y1,z0).color(r,g,b,a).next(); buf.vertex(m,x0,y1,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.east()))   { buf.vertex(m,x1,y1,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z1).color(r,g,b,a).next(); }
            }
            if (!positions.contains(pos.down())) {
                if (!positions.contains(pos.north()))  { buf.vertex(m,x0,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y0,z0).color(r,g,b,a).next(); }
                if (!positions.contains(pos.south()))  { buf.vertex(m,x0,y0,z1).color(r,g,b,a).next(); buf.vertex(m,x1,y0,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.west()))   { buf.vertex(m,x0,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x0,y0,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.east()))   { buf.vertex(m,x1,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y0,z1).color(r,g,b,a).next(); }
            }
            if (!positions.contains(pos.north())) {
                if (!positions.contains(pos.down()))   { buf.vertex(m,x0,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y0,z0).color(r,g,b,a).next(); }
                if (!positions.contains(pos.up()))     { buf.vertex(m,x0,y1,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z0).color(r,g,b,a).next(); }
                if (!positions.contains(pos.west()))   { buf.vertex(m,x0,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x0,y1,z0).color(r,g,b,a).next(); }
                if (!positions.contains(pos.east()))   { buf.vertex(m,x1,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z0).color(r,g,b,a).next(); }
            }
            if (!positions.contains(pos.south())) {
                if (!positions.contains(pos.down()))   { buf.vertex(m,x0,y0,z1).color(r,g,b,a).next(); buf.vertex(m,x1,y0,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.up()))     { buf.vertex(m,x0,y1,z1).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.west()))   { buf.vertex(m,x0,y0,z1).color(r,g,b,a).next(); buf.vertex(m,x0,y1,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.east()))   { buf.vertex(m,x1,y0,z1).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z1).color(r,g,b,a).next(); }
            }
            if (!positions.contains(pos.west())) {
                if (!positions.contains(pos.down()))   { buf.vertex(m,x0,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x0,y0,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.up()))     { buf.vertex(m,x0,y1,z0).color(r,g,b,a).next(); buf.vertex(m,x0,y1,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.north()))  { buf.vertex(m,x0,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x0,y1,z0).color(r,g,b,a).next(); }
                if (!positions.contains(pos.south()))  { buf.vertex(m,x0,y0,z1).color(r,g,b,a).next(); buf.vertex(m,x0,y1,z1).color(r,g,b,a).next(); }
            }
            if (!positions.contains(pos.east())) {
                if (!positions.contains(pos.down()))   { buf.vertex(m,x1,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y0,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.up()))     { buf.vertex(m,x1,y1,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z1).color(r,g,b,a).next(); }
                if (!positions.contains(pos.north()))  { buf.vertex(m,x1,y0,z0).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z0).color(r,g,b,a).next(); }
                if (!positions.contains(pos.south()))  { buf.vertex(m,x1,y0,z1).color(r,g,b,a).next(); buf.vertex(m,x1,y1,z1).color(r,g,b,a).next(); }
            }
        }
    }
//#elseif MC < 12111
//$$ private static void addBox(Matrix4f m, net.minecraft.client.render.VertexConsumer b,
//$$                             float x0, float y0, float z0, float x1, float y1, float z1,
//$$                             float r, float g, float bl, float a) {
//$$     b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x0,y0,z1).color(r,g,bl,a);
//$$     b.vertex(m,x0,y1,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a);
//$$     b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a);
//$$     b.vertex(m,x0,y0,z1).color(r,g,bl,a); b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a); b.vertex(m,x0,y1,z1).color(r,g,bl,a);
//$$     b.vertex(m,x0,y0,z1).color(r,g,bl,a); b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z1).color(r,g,bl,a);
//$$     b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a);
//$$ }
//$$ private static void addBoxLines(Matrix4f m, net.minecraft.client.render.VertexConsumer b,
//$$                                  float x0, float y0, float z0, float x1, float y1, float z1,
//$$                                  float r, float g, float bl, float a) {
//$$     b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z0).color(r,g,bl,a);
//$$     b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z1).color(r,g,bl,a);
//$$     b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x0,y0,z1).color(r,g,bl,a);
//$$     b.vertex(m,x0,y0,z1).color(r,g,bl,a); b.vertex(m,x0,y0,z0).color(r,g,bl,a);
//$$     b.vertex(m,x0,y1,z0).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a);
//$$     b.vertex(m,x1,y1,z0).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a);
//$$     b.vertex(m,x1,y1,z1).color(r,g,bl,a); b.vertex(m,x0,y1,z1).color(r,g,bl,a);
//$$     b.vertex(m,x0,y1,z1).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a);
//$$     b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a);
//$$     b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a);
//$$     b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a);
//$$     b.vertex(m,x0,y0,z1).color(r,g,bl,a); b.vertex(m,x0,y1,z1).color(r,g,bl,a);
//$$ }
//$$ private static void addBoundaryEdges(Set<BlockPos> positions, Color c, float expand, Matrix4f m,
//$$                                       double camX, double camY, double camZ,
//$$                                       net.minecraft.client.render.VertexConsumer buf) {
//$$     for (BlockPos pos : positions) {
//$$         double bx = pos.getX() - camX, by = pos.getY() - camY, bz = pos.getZ() - camZ;
//$$         float x0=(float)(bx-expand), y0=(float)(by-expand), z0=(float)(bz-expand);
//$$         float x1=(float)(bx+1+expand), y1=(float)(by+1+expand), z1=(float)(bz+1+expand);
//$$         float r=c.r(), g=c.g(), b=c.b(), a=c.a();
//$$         if (!positions.contains(pos.up()))    { if (!positions.contains(pos.north())) { buf.vertex(m,x0,y1,z0).color(r,g,b,a); buf.vertex(m,x1,y1,z0).color(r,g,b,a); } if (!positions.contains(pos.south())) { buf.vertex(m,x0,y1,z1).color(r,g,b,a); buf.vertex(m,x1,y1,z1).color(r,g,b,a); } if (!positions.contains(pos.west())) { buf.vertex(m,x0,y1,z0).color(r,g,b,a); buf.vertex(m,x0,y1,z1).color(r,g,b,a); } if (!positions.contains(pos.east())) { buf.vertex(m,x1,y1,z0).color(r,g,b,a); buf.vertex(m,x1,y1,z1).color(r,g,b,a); } }
//$$         if (!positions.contains(pos.down()))  { if (!positions.contains(pos.north())) { buf.vertex(m,x0,y0,z0).color(r,g,b,a); buf.vertex(m,x1,y0,z0).color(r,g,b,a); } if (!positions.contains(pos.south())) { buf.vertex(m,x0,y0,z1).color(r,g,b,a); buf.vertex(m,x1,y0,z1).color(r,g,b,a); } if (!positions.contains(pos.west())) { buf.vertex(m,x0,y0,z0).color(r,g,b,a); buf.vertex(m,x0,y0,z1).color(r,g,b,a); } if (!positions.contains(pos.east())) { buf.vertex(m,x1,y0,z0).color(r,g,b,a); buf.vertex(m,x1,y0,z1).color(r,g,b,a); } }
//$$         if (!positions.contains(pos.north())) { if (!positions.contains(pos.down())) { buf.vertex(m,x0,y0,z0).color(r,g,b,a); buf.vertex(m,x1,y0,z0).color(r,g,b,a); } if (!positions.contains(pos.up())) { buf.vertex(m,x0,y1,z0).color(r,g,b,a); buf.vertex(m,x1,y1,z0).color(r,g,b,a); } if (!positions.contains(pos.west())) { buf.vertex(m,x0,y0,z0).color(r,g,b,a); buf.vertex(m,x0,y1,z0).color(r,g,b,a); } if (!positions.contains(pos.east())) { buf.vertex(m,x1,y0,z0).color(r,g,b,a); buf.vertex(m,x1,y1,z0).color(r,g,b,a); } }
//$$         if (!positions.contains(pos.south())) { if (!positions.contains(pos.down())) { buf.vertex(m,x0,y0,z1).color(r,g,b,a); buf.vertex(m,x1,y0,z1).color(r,g,b,a); } if (!positions.contains(pos.up())) { buf.vertex(m,x0,y1,z1).color(r,g,b,a); buf.vertex(m,x1,y1,z1).color(r,g,b,a); } if (!positions.contains(pos.west())) { buf.vertex(m,x0,y0,z1).color(r,g,b,a); buf.vertex(m,x0,y1,z1).color(r,g,b,a); } if (!positions.contains(pos.east())) { buf.vertex(m,x1,y0,z1).color(r,g,b,a); buf.vertex(m,x1,y1,z1).color(r,g,b,a); } }
//$$         if (!positions.contains(pos.west()))  { if (!positions.contains(pos.down())) { buf.vertex(m,x0,y0,z0).color(r,g,b,a); buf.vertex(m,x0,y0,z1).color(r,g,b,a); } if (!positions.contains(pos.up())) { buf.vertex(m,x0,y1,z0).color(r,g,b,a); buf.vertex(m,x0,y1,z1).color(r,g,b,a); } if (!positions.contains(pos.north())) { buf.vertex(m,x0,y0,z0).color(r,g,b,a); buf.vertex(m,x0,y1,z0).color(r,g,b,a); } if (!positions.contains(pos.south())) { buf.vertex(m,x0,y0,z1).color(r,g,b,a); buf.vertex(m,x0,y1,z1).color(r,g,b,a); } }
//$$         if (!positions.contains(pos.east()))  { if (!positions.contains(pos.down())) { buf.vertex(m,x1,y0,z0).color(r,g,b,a); buf.vertex(m,x1,y0,z1).color(r,g,b,a); } if (!positions.contains(pos.up())) { buf.vertex(m,x1,y1,z0).color(r,g,b,a); buf.vertex(m,x1,y1,z1).color(r,g,b,a); } if (!positions.contains(pos.north())) { buf.vertex(m,x1,y0,z0).color(r,g,b,a); buf.vertex(m,x1,y1,z0).color(r,g,b,a); } if (!positions.contains(pos.south())) { buf.vertex(m,x1,y0,z1).color(r,g,b,a); buf.vertex(m,x1,y1,z1).color(r,g,b,a); } }
//$$     }
//$$ }
//#elseif MC < 260100
//$$ // MC 12111+: no matrix arg; pipeline handles view transform; lineWidth for LINES format
//$$ private static void addBox12111(net.minecraft.client.render.VertexConsumer b,
//$$                                  float x0, float y0, float z0, float x1, float y1, float z1,
//$$                                  float r, float g, float bl, float a) {
//$$     b.vertex(x0,y0,z0).color(r,g,bl,a); b.vertex(x1,y0,z0).color(r,g,bl,a); b.vertex(x1,y0,z1).color(r,g,bl,a); b.vertex(x0,y0,z1).color(r,g,bl,a);
//$$     b.vertex(x0,y1,z1).color(r,g,bl,a); b.vertex(x1,y1,z1).color(r,g,bl,a); b.vertex(x1,y1,z0).color(r,g,bl,a); b.vertex(x0,y1,z0).color(r,g,bl,a);
//$$     b.vertex(x1,y0,z0).color(r,g,bl,a); b.vertex(x0,y0,z0).color(r,g,bl,a); b.vertex(x0,y1,z0).color(r,g,bl,a); b.vertex(x1,y1,z0).color(r,g,bl,a);
//$$     b.vertex(x0,y0,z1).color(r,g,bl,a); b.vertex(x1,y0,z1).color(r,g,bl,a); b.vertex(x1,y1,z1).color(r,g,bl,a); b.vertex(x0,y1,z1).color(r,g,bl,a);
//$$     b.vertex(x0,y0,z1).color(r,g,bl,a); b.vertex(x0,y0,z0).color(r,g,bl,a); b.vertex(x0,y1,z0).color(r,g,bl,a); b.vertex(x0,y1,z1).color(r,g,bl,a);
//$$     b.vertex(x1,y0,z0).color(r,g,bl,a); b.vertex(x1,y0,z1).color(r,g,bl,a); b.vertex(x1,y1,z1).color(r,g,bl,a); b.vertex(x1,y1,z0).color(r,g,bl,a);
//$$ }
//$$ private static void addBoxLines12111(net.minecraft.client.render.VertexConsumer b,
//$$                                       float x0, float y0, float z0, float x1, float y1, float z1,
//$$                                       float r, float g, float bl, float a) {
//$$     b.vertex(x0,y0,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x1,y0,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x1,y0,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x1,y0,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x1,y0,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x0,y0,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x0,y0,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x0,y0,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x0,y1,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x1,y1,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x1,y1,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x1,y1,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x1,y1,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x0,y1,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x0,y1,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x0,y1,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x0,y0,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x0,y1,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x1,y0,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x1,y1,z0).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x1,y0,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x1,y1,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$     b.vertex(x0,y0,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f); b.vertex(x0,y1,z1).color(r,g,bl,a).normal(0,1,0).lineWidth(1.5f);
//$$ }
//$$ private static void addBoundaryEdges12111(Set<BlockPos> positions, Color c, float expand,
//$$                                            double camX, double camY, double camZ,
//$$                                            net.minecraft.client.render.VertexConsumer buf) {
//$$     float r=c.r(), g=c.g(), b=c.b(), a=c.a();
//$$     for (BlockPos pos : positions) {
//$$         double bx=pos.getX()-camX, by=pos.getY()-camY, bz=pos.getZ()-camZ;
//$$         float x0=(float)(bx-expand), y0=(float)(by-expand), z0=(float)(bz-expand);
//$$         float x1=(float)(bx+1+expand), y1=(float)(by+1+expand), z1=(float)(bz+1+expand);
//$$         if (!positions.contains(pos.up()))    { if (!positions.contains(pos.north())) { buf.vertex(x0,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.vertex(x0,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.vertex(x0,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.vertex(x1,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } }
//$$         if (!positions.contains(pos.down()))  { if (!positions.contains(pos.north())) { buf.vertex(x0,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.vertex(x0,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.vertex(x0,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.vertex(x1,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } }
//$$         if (!positions.contains(pos.north())) { if (!positions.contains(pos.down())) { buf.vertex(x0,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.up())) { buf.vertex(x0,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.vertex(x0,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.vertex(x1,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } }
//$$         if (!positions.contains(pos.south())) { if (!positions.contains(pos.down())) { buf.vertex(x0,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.up())) { buf.vertex(x0,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.vertex(x0,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.vertex(x1,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } }
//$$         if (!positions.contains(pos.west()))  { if (!positions.contains(pos.down())) { buf.vertex(x0,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.up())) { buf.vertex(x0,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.north())) { buf.vertex(x0,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.vertex(x0,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x0,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } }
//$$         if (!positions.contains(pos.east()))  { if (!positions.contains(pos.down())) { buf.vertex(x1,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.up())) { buf.vertex(x1,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.north())) { buf.vertex(x1,y0,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z0).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.vertex(x1,y0,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); buf.vertex(x1,y1,z1).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f); } }
//$$     }
//$$ }
//#else
//$$ private static void addBox26(com.mojang.blaze3d.vertex.BufferBuilder b,
//$$                               float x0, float y0, float z0, float x1, float y1, float z1,
//$$                               float r, float g, float bl, float a) {
//$$     b.addVertex(x0,y0,z0).setColor(r,g,bl,a); b.addVertex(x1,y0,z0).setColor(r,g,bl,a); b.addVertex(x1,y0,z1).setColor(r,g,bl,a); b.addVertex(x0,y0,z1).setColor(r,g,bl,a);
//$$     b.addVertex(x0,y1,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z0).setColor(r,g,bl,a); b.addVertex(x0,y1,z0).setColor(r,g,bl,a);
//$$     b.addVertex(x1,y0,z0).setColor(r,g,bl,a); b.addVertex(x0,y0,z0).setColor(r,g,bl,a); b.addVertex(x0,y1,z0).setColor(r,g,bl,a); b.addVertex(x1,y1,z0).setColor(r,g,bl,a);
//$$     b.addVertex(x0,y0,z1).setColor(r,g,bl,a); b.addVertex(x1,y0,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z1).setColor(r,g,bl,a); b.addVertex(x0,y1,z1).setColor(r,g,bl,a);
//$$     b.addVertex(x0,y0,z1).setColor(r,g,bl,a); b.addVertex(x0,y0,z0).setColor(r,g,bl,a); b.addVertex(x0,y1,z0).setColor(r,g,bl,a); b.addVertex(x0,y1,z1).setColor(r,g,bl,a);
//$$     b.addVertex(x1,y0,z0).setColor(r,g,bl,a); b.addVertex(x1,y0,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z1).setColor(r,g,bl,a); b.addVertex(x1,y1,z0).setColor(r,g,bl,a);
//$$ }
//$$ private static void addBoxLines26(com.mojang.blaze3d.vertex.BufferBuilder b,
//$$                                    float x0, float y0, float z0, float x1, float y1, float z1,
//$$                                    float r, float g, float bl, float a) {
//$$     b.addVertex(x0,y0,z0).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x1,y0,z0).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x1,y0,z0).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x1,y0,z1).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x1,y0,z1).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x0,y0,z1).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x0,y0,z1).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x0,y0,z0).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x0,y1,z0).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x1,y1,z0).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x1,y1,z0).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x1,y1,z1).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x1,y1,z1).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x0,y1,z1).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x0,y1,z1).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x0,y1,z0).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x0,y0,z0).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x0,y1,z0).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x1,y0,z0).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x1,y1,z0).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x1,y0,z1).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x1,y1,z1).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$     b.addVertex(x0,y0,z1).setColor(r,g,bl,a).setLineWidth(1.5f); b.addVertex(x0,y1,z1).setColor(r,g,bl,a).setLineWidth(1.5f);
//$$ }
//$$ private static void addBoundaryEdges26(Set<BlockPos> positions, Color c, float expand,
//$$                                         double camX, double camY, double camZ,
//$$                                         com.mojang.blaze3d.vertex.BufferBuilder buf) {
//$$     float r=c.r(), g=c.g(), b=c.b(), a=c.a();
//$$     for (BlockPos pos : positions) {
//$$         double bx=pos.getX()-camX, by=pos.getY()-camY, bz=pos.getZ()-camZ;
//$$         float x0=(float)(bx-expand), y0=(float)(by-expand), z0=(float)(bz-expand);
//$$         float x1=(float)(bx+1+expand), y1=(float)(by+1+expand), z1=(float)(bz+1+expand);
//$$         if (!positions.contains(pos.above()))  { if (!positions.contains(pos.north())) { buf.addVertex(x0,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.addVertex(x0,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.addVertex(x0,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.addVertex(x1,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } }
//$$         if (!positions.contains(pos.below()))  { if (!positions.contains(pos.north())) { buf.addVertex(x0,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.addVertex(x0,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.addVertex(x0,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.addVertex(x1,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); } }
//$$         if (!positions.contains(pos.north()))  { if (!positions.contains(pos.below())) { buf.addVertex(x0,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.above())) { buf.addVertex(x0,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.addVertex(x0,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.addVertex(x1,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); } }
//$$         if (!positions.contains(pos.south()))  { if (!positions.contains(pos.below())) { buf.addVertex(x0,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.above())) { buf.addVertex(x0,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.west())) { buf.addVertex(x0,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.east())) { buf.addVertex(x1,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } }
//$$         if (!positions.contains(pos.west()))   { if (!positions.contains(pos.below())) { buf.addVertex(x0,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.above())) { buf.addVertex(x0,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.north())) { buf.addVertex(x0,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.addVertex(x0,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x0,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } }
//$$         if (!positions.contains(pos.east()))   { if (!positions.contains(pos.below())) { buf.addVertex(x1,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.above())) { buf.addVertex(x1,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.north())) { buf.addVertex(x1,y0,z0).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z0).setColor(r,g,b,a).setLineWidth(1.5f); } if (!positions.contains(pos.south())) { buf.addVertex(x1,y0,z1).setColor(r,g,b,a).setLineWidth(1.5f); buf.addVertex(x1,y1,z1).setColor(r,g,b,a).setLineWidth(1.5f); } }
//$$     }
//$$ }
//#endif

    // -------------------------------------------------------------------------
    // RenderSystem state helpers (no-op for MC >= 12111 where pipelines handle state)
    // -------------------------------------------------------------------------

    private static void setupBlend() {
//#if MC < 12111
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
//#endif
    }

    private static void teardownBlend() {
//#if MC < 12111
        RenderSystem.disableBlend();
//#endif
    }

    private static void disableDepth() {
//#if MC < 12111
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
//#endif
    }

    private static void enableDepth() {
//#if MC < 12111
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
//#endif
    }
}
