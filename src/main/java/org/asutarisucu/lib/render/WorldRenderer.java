package org.asutarisucu.lib.render;

//#if MC >= 260200
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import com.mojang.blaze3d.vertex.VertexConsumer;
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes;
//$$ import net.minecraft.core.BlockPos;
//$$ import org.joml.Matrix4f;
//$$
//$$ import java.util.Set;
//$$
//$$ /**
//$$  * World-space block rendering for MC 26.2+.
//$$  *
//$$  * 26.2 removed Tesselator and RenderType.draw(MeshData); geometry now goes to a
//$$  * SubmitNodeCollector, which batches it with the rest of the frame. The collector
//$$  * and pose stack come from the Fabric level render event, so callers hand them
//$$  * over once per frame with {@link #beginFrame} and the render methods keep the
//$$  * signatures the other versions use.
//$$  *
//$$  * Vertices are camera-relative doubles cast to float, as on the older versions,
//$$  * so precision holds up far from the origin. viewRot is unused here — the pose
//$$  * stack already carries the view transform.
//$$  */
//$$ public class WorldRenderer {
//$$
//$$     private static SubmitNodeCollector collector;
//$$     private static PoseStack poseStack;
//$$
//$$     /** Hands over this frame's submit collector. Call before any render method. */
//$$     public static void beginFrame(SubmitNodeCollector nodeCollector, PoseStack stack) {
//$$         collector = nodeCollector;
//$$         poseStack = stack;
//$$     }
//$$
//$$     public static void renderBlockFills(Set<BlockPos> positions, Color color,
//$$                                         boolean depthTest, Matrix4f viewRot,
//$$                                         double camX, double camY, double camZ) {
//$$         if (positions.isEmpty() || collector == null) return;
//$$         submitFills(positions, color, camX, camY, camZ);
//$$     }
//$$
//$$     public static void renderBlockOutlines(Set<BlockPos> positions, Color color,
//$$                                            double expand, Matrix4f viewRot,
//$$                                            double camX, double camY, double camZ) {
//$$         if (positions.isEmpty() || collector == null) return;
//$$         submitOutlines(positions, color, (float) expand, camX, camY, camZ);
//$$     }
//$$
//$$     /** Draws a faint fill plus the outline of the group's outer boundary. */
//$$     public static void renderBlockGroup(Set<BlockPos> positions, Color baseColor,
//$$                                         Matrix4f viewRot,
//$$                                         double camX, double camY, double camZ) {
//$$         if (positions.isEmpty() || collector == null) return;
//$$         submitFills(positions, baseColor.withAlpha(0.04f), camX, camY, camZ);
//$$         submitBoundaryLines(positions, baseColor.withAlpha(0.60f), 0.0025f, camX, camY, camZ);
//$$     }
//$$
//$$     /**
//$$      * Draws arbitrary world-space line segments.
//$$      *
//$$      * @param segs      flat world coordinates, 6 doubles per segment (x0,y0,z0,x1,y1,z1)
//$$      * @param segCount  number of segments to read from the front of {@code segs}
//$$      */
//$$     public static void renderLineSegments(double[] segs, int segCount, Color c, Matrix4f viewRot,
//$$                                           double camX, double camY, double camZ) {
//$$         if (segCount <= 0 || collector == null) return;
//$$         collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buf) -> {
//$$             for (int i = 0; i < segCount; i++) {
//$$                 int o = i * 6;
//$$                 line(pose, buf, c,
//$$                         (float)(segs[o]   - camX), (float)(segs[o+1] - camY), (float)(segs[o+2] - camZ),
//$$                         (float)(segs[o+3] - camX), (float)(segs[o+4] - camY), (float)(segs[o+5] - camZ));
//$$             }
//$$         });
//$$     }
//$$
//$$     // ---------------------------------------------------------------------
//$$
//$$     private static void submitFills(Set<BlockPos> positions, Color c,
//$$                                     double camX, double camY, double camZ) {
//$$         collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buf) -> {
//$$             for (BlockPos pos : positions) {
//$$                 float x0 = (float)(pos.getX() - camX);
//$$                 float y0 = (float)(pos.getY() - camY);
//$$                 float z0 = (float)(pos.getZ() - camZ);
//$$                 box(pose, buf, c, x0, y0, z0, x0 + 1f, y0 + 1f, z0 + 1f);
//$$             }
//$$         });
//$$     }
//$$
//$$     private static void submitOutlines(Set<BlockPos> positions, Color c, float expand,
//$$                                        double camX, double camY, double camZ) {
//$$         collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buf) -> {
//$$             for (BlockPos pos : positions) {
//$$                 float x0 = (float)(pos.getX() - camX) - expand;
//$$                 float y0 = (float)(pos.getY() - camY) - expand;
//$$                 float z0 = (float)(pos.getZ() - camZ) - expand;
//$$                 boxLines(pose, buf, c, x0, y0, z0,
//$$                         x0 + 1f + 2*expand, y0 + 1f + 2*expand, z0 + 1f + 2*expand);
//$$             }
//$$         });
//$$     }
//$$
//$$     /** Only edges on the outer boundary of the group; shared interior edges are skipped. */
//$$     private static void submitBoundaryLines(Set<BlockPos> positions, Color c, float expand,
//$$                                             double camX, double camY, double camZ) {
//$$         collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buf) -> {
//$$             for (BlockPos pos : positions) {
//$$                 double bx = pos.getX() - camX, by = pos.getY() - camY, bz = pos.getZ() - camZ;
//$$                 float x0 = (float)(bx - expand), y0 = (float)(by - expand), z0 = (float)(bz - expand);
//$$                 float x1 = (float)(bx + 1 + expand), y1 = (float)(by + 1 + expand), z1 = (float)(bz + 1 + expand);
//$$                 boolean up = positions.contains(pos.above()),    down  = positions.contains(pos.below());
//$$                 boolean north = positions.contains(pos.north()), south = positions.contains(pos.south());
//$$                 boolean west = positions.contains(pos.west()),   east  = positions.contains(pos.east());
//$$                 if (!up) {
//$$                     if (!north) line(pose, buf, c, x0, y1, z0, x1, y1, z0);
//$$                     if (!south) line(pose, buf, c, x0, y1, z1, x1, y1, z1);
//$$                     if (!west)  line(pose, buf, c, x0, y1, z0, x0, y1, z1);
//$$                     if (!east)  line(pose, buf, c, x1, y1, z0, x1, y1, z1);
//$$                 }
//$$                 if (!down) {
//$$                     if (!north) line(pose, buf, c, x0, y0, z0, x1, y0, z0);
//$$                     if (!south) line(pose, buf, c, x0, y0, z1, x1, y0, z1);
//$$                     if (!west)  line(pose, buf, c, x0, y0, z0, x0, y0, z1);
//$$                     if (!east)  line(pose, buf, c, x1, y0, z0, x1, y0, z1);
//$$                 }
//$$                 if (!north) {
//$$                     if (!west)  line(pose, buf, c, x0, y0, z0, x0, y1, z0);
//$$                     if (!east)  line(pose, buf, c, x1, y0, z0, x1, y1, z0);
//$$                 }
//$$                 if (!south) {
//$$                     if (!west)  line(pose, buf, c, x0, y0, z1, x0, y1, z1);
//$$                     if (!east)  line(pose, buf, c, x1, y0, z1, x1, y1, z1);
//$$                 }
//$$             }
//$$         });
//$$     }
//$$
//$$     private static void line(PoseStack.Pose pose, VertexConsumer buf, Color c,
//$$                              float x0, float y0, float z0, float x1, float y1, float z1) {
//$$         float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
//$$         float len = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
//$$         if (len == 0f) return;
//$$         dx /= len; dy /= len; dz /= len;
//$$         buf.addVertex(pose, x0, y0, z0).setNormal(pose, dx, dy, dz)
//$$                 .setColor(c.r(), c.g(), c.b(), c.a()).setLineWidth(1.5f);
//$$         buf.addVertex(pose, x1, y1, z1).setNormal(pose, dx, dy, dz)
//$$                 .setColor(c.r(), c.g(), c.b(), c.a()).setLineWidth(1.5f);
//$$     }
//$$
//$$     private static void boxLines(PoseStack.Pose pose, VertexConsumer b, Color c,
//$$                                  float x0, float y0, float z0, float x1, float y1, float z1) {
//$$         line(pose, b, c, x0,y0,z0, x1,y0,z0); line(pose, b, c, x1,y0,z0, x1,y0,z1);
//$$         line(pose, b, c, x1,y0,z1, x0,y0,z1); line(pose, b, c, x0,y0,z1, x0,y0,z0);
//$$         line(pose, b, c, x0,y1,z0, x1,y1,z0); line(pose, b, c, x1,y1,z0, x1,y1,z1);
//$$         line(pose, b, c, x1,y1,z1, x0,y1,z1); line(pose, b, c, x0,y1,z1, x0,y1,z0);
//$$         line(pose, b, c, x0,y0,z0, x0,y1,z0); line(pose, b, c, x1,y0,z0, x1,y1,z0);
//$$         line(pose, b, c, x1,y0,z1, x1,y1,z1); line(pose, b, c, x0,y0,z1, x0,y1,z1);
//$$     }
//$$
//$$     private static void box(PoseStack.Pose pose, VertexConsumer b, Color c,
//$$                             float x0, float y0, float z0, float x1, float y1, float z1) {
//$$         quad(pose, b, c, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1);
//$$         quad(pose, b, c, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0);
//$$         quad(pose, b, c, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0);
//$$         quad(pose, b, c, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1);
//$$         quad(pose, b, c, x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1);
//$$         quad(pose, b, c, x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0);
//$$     }
//$$
//$$     private static void quad(PoseStack.Pose pose, VertexConsumer b, Color c,
//$$                              float ax, float ay, float az, float bx, float by, float bz,
//$$                              float cx, float cy, float cz, float dx, float dy, float dz) {
//$$         b.addVertex(pose, ax, ay, az).setColor(c.r(), c.g(), c.b(), c.a());
//$$         b.addVertex(pose, bx, by, bz).setColor(c.r(), c.g(), c.b(), c.a());
//$$         b.addVertex(pose, cx, cy, cz).setColor(c.r(), c.g(), c.b(), c.a());
//$$         b.addVertex(pose, dx, dy, dz).setColor(c.r(), c.g(), c.b(), c.a());
//$$     }
//$$ }
//#else

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
    // Free line segments
    // -------------------------------------------------------------------------

    /**
     * Draws arbitrary world-space line segments.
     *
     * @param segs      flat world coordinates, 6 doubles per segment (x0,y0,z0,x1,y1,z1)
     * @param segCount  number of segments to read from the front of {@code segs}
     */
    public static void renderLineSegments(double[] segs, int segCount, Color c, Matrix4f viewRot,
                                          double camX, double camY, double camZ) {
        if (segCount <= 0) return;
        setupBlend();
        float r = c.r(), g = c.g(), b = c.b(), a = c.a();
//#if MC < 12101
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < segCount; i++) {
            int o = i * 6;
            buf.vertex(viewRot, (float)(segs[o]   - camX), (float)(segs[o+1] - camY), (float)(segs[o+2] - camZ)).color(r,g,b,a).next();
            buf.vertex(viewRot, (float)(segs[o+3] - camX), (float)(segs[o+4] - camY), (float)(segs[o+5] - camZ)).color(r,g,b,a).next();
        }
        tess.draw();
//#elseif MC < 12111
//$$ RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
//$$ for (int i = 0; i < segCount; i++) {
//$$     int o = i * 6;
//$$     buf.vertex(viewRot, (float)(segs[o]   - camX), (float)(segs[o+1] - camY), (float)(segs[o+2] - camZ)).color(r,g,b,a);
//$$     buf.vertex(viewRot, (float)(segs[o+3] - camX), (float)(segs[o+4] - camY), (float)(segs[o+5] - camZ)).color(r,g,b,a);
//$$ }
//$$ BufferRenderer.drawWithGlobalProgram(buf.end());
//#elseif MC < 260100
//$$ var buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
//$$ for (int i = 0; i < segCount; i++) {
//$$     int o = i * 6;
//$$     buf.vertex((float)(segs[o]   - camX), (float)(segs[o+1] - camY), (float)(segs[o+2] - camZ)).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f);
//$$     buf.vertex((float)(segs[o+3] - camX), (float)(segs[o+4] - camY), (float)(segs[o+5] - camZ)).color(r,g,b,a).normal(0,1,0).lineWidth(1.5f);
//$$ }
//$$ var mesh = buf.endNullable();
//$$ if (mesh != null) { RenderLayers.LINES_TRANSLUCENT.draw(mesh); mesh.close(); }
//#else
//$$ var buf = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
//$$ for (int i = 0; i < segCount; i++) {
//$$     int o = i * 6;
//$$     buf.addVertex((float)(segs[o]   - camX), (float)(segs[o+1] - camY), (float)(segs[o+2] - camZ)).setColor(r,g,b,a).setLineWidth(1.5f);
//$$     buf.addVertex((float)(segs[o+3] - camX), (float)(segs[o+4] - camY), (float)(segs[o+5] - camZ)).setColor(r,g,b,a).setLineWidth(1.5f);
//$$ }
//$$ var mesh = buf.build();
//$$ if (mesh != null) { RenderTypes.lines().draw(mesh); mesh.close(); }
//#endif
        teardownBlend();
    }

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
//#endif
