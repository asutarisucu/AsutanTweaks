package org.asutarisucu.tweak.BlockUpdateViewer;

//#if MC < 260100
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
//#if MC >= 12101
//$$ import net.minecraft.client.render.BufferRenderer;
//#endif
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.asutarisucu.Configs.FeatureToggle;
import org.joml.Matrix4f;

import java.util.Set;
//#else
//$$ import com.mojang.blaze3d.pipeline.DepthStencilState;
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import com.mojang.blaze3d.platform.CompareOp;
//$$ import fi.dy.masa.malilib.render.MaLiLibPipelines;
//$$ import fi.dy.masa.malilib.render.RenderContext;
//$$ import fi.dy.masa.malilib.render.RenderUtils;
//$$ import fi.dy.masa.malilib.util.data.Color4f;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.resources.Identifier;
//$$ import net.minecraft.world.item.BlockItem;
//$$ import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//$$ import net.minecraft.world.phys.BlockHitResult;
//$$ import net.minecraft.world.phys.HitResult;
//$$ import org.asutarisucu.Configs.FeatureToggle;
//#endif

public class BlockUpdateViewer {

    public static void register() {
//#if MC < 260100
        WorldRenderEvents.AFTER_ENTITIES.register(BlockUpdateViewer::renderOverlay);
//#endif
    }

//#if MC >= 260100
//$$ private static RenderPipeline LEQUAL_DEPTH_WRITE_PIPELINE;
//$$ private static RenderPipeline getLequalDepthWritePipeline() {
//$$     if (LEQUAL_DEPTH_WRITE_PIPELINE == null) {
//$$         LEQUAL_DEPTH_WRITE_PIPELINE = RenderPipeline
//$$             .builder(new RenderPipeline.Snippet[]{ MaLiLibPipelines.POSITION_COLOR_MASA_STAGE })
//$$             .withLocation(Identifier.fromNamespaceAndPath("asutantweaks", "buv_lequal_depth_write"))
//$$             .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
//$$             .build();
//$$     }
//$$     return LEQUAL_DEPTH_WRITE_PIPELINE;
//$$ }
//$$
//$$ public static void renderOverlay26() {
//$$     var mc = Minecraft.getInstance();
//$$     if (mc.player == null || mc.level == null) return;
//$$
//$$     boolean placementEnabled = FeatureToggle.PLACEMENT_UPDATE_VIEWER.getBooleanValue();
//$$     boolean breakingEnabled  = FeatureToggle.BREAKING_UPDATE_VIEWER.getBooleanValue();
//$$     boolean suppressionNeeds = FeatureToggle.UPDATE_SUPPRESSION_VIEW.getBooleanValue();
//$$     if (!placementEnabled && !breakingEnabled && !suppressionNeeds) return;
//$$
//$$     if (!(mc.hitResult instanceof BlockHitResult blockHit)) return;
//$$     if (blockHit.getType() == HitResult.Type.MISS) return;
//$$
//$$     var hitPos = blockHit.getBlockPos();
//$$     var heldItem = mc.player.getMainHandItem();
//$$
//$$     boolean holdingBlockItem = heldItem.getItem() instanceof BlockItem;
//$$
//$$     if ((placementEnabled || suppressionNeeds) && holdingBlockItem) {
//$$         var blockItem = (BlockItem) heldItem.getItem();
//$$         var placedPos = hitPos.relative(blockHit.getDirection());
//$$         var placedBlock = blockItem.getBlock();
//$$         var approxState = placedBlock.defaultBlockState();
//$$         if (approxState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
//$$             approxState = approxState.setValue(BlockStateProperties.HORIZONTAL_FACING,
//$$                     mc.player.getDirection().getOpposite());
//$$         }
//$$         var updateSet = BlockUpdateCalculator.compute(mc.level, placedPos, approxState);
//$$         if (placementEnabled) renderBoxes26(updateSet, 1f, 0f, 0f);
//$$     }
//$$
//$$     if ((breakingEnabled && !holdingBlockItem) || (suppressionNeeds && !holdingBlockItem)) {
//$$         var updateSet = BlockUpdateCalculator.compute(mc.level, hitPos);
//$$         if (breakingEnabled) renderBoxes26(updateSet, 0f, 0.3f, 1f);
//$$     }
//$$ }
//$$
//$$ private static void renderBoxes26(java.util.Set<BlockPos> positions, float r, float g, float b) {
//$$     if (positions.isEmpty()) return;
//$$     // alpha must be > 0: int_position_color.fsh discards when alpha == 0.0
//$$     Color4f depthColor = new Color4f(r, g, b, 0.001f);
//$$     Color4f solid   = new Color4f(r, g, b, 0.04f);
//$$     Color4f through = new Color4f(r, g, b, 0.02f);
//$$     Color4f outline = new Color4f(r, g, b, 0.6f);
//$$     net.minecraft.world.phys.Vec3 cam = RenderUtils.camPos();
//$$     // Pre-pass: LEQUAL + depth write ON (cull ON default = front faces only).
//$$     // Nearest front face per pixel wins, same as old RenderSystem.enableDepthTest().
//$$     try (var ctx = new RenderContext(() -> "BUV/depth", getLequalDepthWritePipeline())) {
//$$         var buf = ctx.getBuilder();
//$$         for (var pos : positions) RenderUtils.drawBlockBoundingBoxSidesBatchedQuads(pos, cam, depthColor, 0.0, buf);
//$$         var mesh = buf.build();
//$$         if (mesh != null) { ctx.draw(mesh, false, true); mesh.close(); }
//$$     } catch (Exception ignored) {}
//$$     try (var ctx = new RenderContext(() -> "BUV/solid", MaLiLibPipelines.POSITION_COLOR_MASA_LEQUAL_DEPTH_NO_CULL)) {
//$$         var buf = ctx.getBuilder();
//$$         for (var pos : positions) RenderUtils.drawBlockBoundingBoxSidesBatchedQuads(pos, cam, solid, 0.0, buf);
//$$         var mesh = buf.build();
//$$         if (mesh != null) { ctx.draw(mesh, false, true); mesh.close(); }
//$$     } catch (Exception ignored) {}
//$$     try (var ctx = new RenderContext(() -> "BUV/through", MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_NO_DEPTH_NO_CULL)) {
//$$         var buf = ctx.getBuilder();
//$$         for (var pos : positions) RenderUtils.drawBlockBoundingBoxSidesBatchedQuads(pos, cam, through, 0.0, buf);
//$$         var mesh = buf.build();
//$$         if (mesh != null) { ctx.draw(mesh, false, true); mesh.close(); }
//$$     } catch (Exception ignored) {}
//$$     // Outline: adjacency-filtered so shared faces between highlighted blocks have no inner border
//$$     try (var ctx = new RenderContext(() -> "BUV/outline", MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_LEQUAL_DEPTH)) {
//$$         var buf = ctx.getBuilder();
//$$         drawOuterBoundaryLines(positions, outline, 0.0025, cam, buf);
//$$         var mesh = buf.build();
//$$         if (mesh != null) { ctx.draw(mesh, false, true); mesh.close(); }
//$$     } catch (Exception ignored) {}
//$$ }
//$$
//$$ // Draws only the outer boundary edges of the block group, hiding shared interior borders.
//$$ // For each block, each exterior face contributes an edge only when the adjacent block
//$$ // in that edge's perpendicular direction is also not in the set.
//$$ private static void drawOuterBoundaryLines(java.util.Set<BlockPos> positions, Color4f color, double offset,
//$$                                              net.minecraft.world.phys.Vec3 cam, com.mojang.blaze3d.vertex.BufferBuilder buf) {
//$$     for (var pos : positions) {
//$$         double bx = pos.getX() - cam.x, by = pos.getY() - cam.y, bz = pos.getZ() - cam.z;
//$$         float x0 = (float)(bx - offset), y0 = (float)(by - offset), z0 = (float)(bz - offset);
//$$         float x1 = (float)(bx + 1 + offset), y1 = (float)(by + 1 + offset), z1 = (float)(bz + 1 + offset);
//$$         if (!positions.contains(pos.above())) {
//$$             if (!positions.contains(pos.north()))  addLine26(buf,x0,y1,z0,x1,y1,z0,color);
//$$             if (!positions.contains(pos.south()))  addLine26(buf,x0,y1,z1,x1,y1,z1,color);
//$$             if (!positions.contains(pos.west()))   addLine26(buf,x0,y1,z0,x0,y1,z1,color);
//$$             if (!positions.contains(pos.east()))   addLine26(buf,x1,y1,z0,x1,y1,z1,color);
//$$         }
//$$         if (!positions.contains(pos.below())) {
//$$             if (!positions.contains(pos.north()))  addLine26(buf,x0,y0,z0,x1,y0,z0,color);
//$$             if (!positions.contains(pos.south()))  addLine26(buf,x0,y0,z1,x1,y0,z1,color);
//$$             if (!positions.contains(pos.west()))   addLine26(buf,x0,y0,z0,x0,y0,z1,color);
//$$             if (!positions.contains(pos.east()))   addLine26(buf,x1,y0,z0,x1,y0,z1,color);
//$$         }
//$$         if (!positions.contains(pos.north())) {
//$$             if (!positions.contains(pos.below()))  addLine26(buf,x0,y0,z0,x1,y0,z0,color);
//$$             if (!positions.contains(pos.above()))  addLine26(buf,x0,y1,z0,x1,y1,z0,color);
//$$             if (!positions.contains(pos.west()))   addLine26(buf,x0,y0,z0,x0,y1,z0,color);
//$$             if (!positions.contains(pos.east()))   addLine26(buf,x1,y0,z0,x1,y1,z0,color);
//$$         }
//$$         if (!positions.contains(pos.south())) {
//$$             if (!positions.contains(pos.below()))  addLine26(buf,x0,y0,z1,x1,y0,z1,color);
//$$             if (!positions.contains(pos.above()))  addLine26(buf,x0,y1,z1,x1,y1,z1,color);
//$$             if (!positions.contains(pos.west()))   addLine26(buf,x0,y0,z1,x0,y1,z1,color);
//$$             if (!positions.contains(pos.east()))   addLine26(buf,x1,y0,z1,x1,y1,z1,color);
//$$         }
//$$         if (!positions.contains(pos.west())) {
//$$             if (!positions.contains(pos.below()))  addLine26(buf,x0,y0,z0,x0,y0,z1,color);
//$$             if (!positions.contains(pos.above()))  addLine26(buf,x0,y1,z0,x0,y1,z1,color);
//$$             if (!positions.contains(pos.north()))  addLine26(buf,x0,y0,z0,x0,y1,z0,color);
//$$             if (!positions.contains(pos.south()))  addLine26(buf,x0,y0,z1,x0,y1,z1,color);
//$$         }
//$$         if (!positions.contains(pos.east())) {
//$$             if (!positions.contains(pos.below()))  addLine26(buf,x1,y0,z0,x1,y0,z1,color);
//$$             if (!positions.contains(pos.above()))  addLine26(buf,x1,y1,z0,x1,y1,z1,color);
//$$             if (!positions.contains(pos.north()))  addLine26(buf,x1,y0,z0,x1,y1,z0,color);
//$$             if (!positions.contains(pos.south()))  addLine26(buf,x1,y0,z1,x1,y1,z1,color);
//$$         }
//$$     }
//$$ }
//$$
//$$ private static void addLine26(com.mojang.blaze3d.vertex.BufferBuilder buf,
//$$                                float x0, float y0, float z0, float x1, float y1, float z1, Color4f c) {
//$$     buf.addVertex(x0, y0, z0).setColor(c.r, c.g, c.b, c.a).setLineWidth(1.5f);
//$$     buf.addVertex(x1, y1, z1).setColor(c.r, c.g, c.b, c.a).setLineWidth(1.5f);
//$$ }
//#endif

//#if MC < 260100
    private static void renderOverlay(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        boolean placementEnabled = FeatureToggle.PLACEMENT_UPDATE_VIEWER.getBooleanValue();
        boolean breakingEnabled  = FeatureToggle.BREAKING_UPDATE_VIEWER.getBooleanValue();
        boolean suppressionNeeds = FeatureToggle.UPDATE_SUPPRESSION_VIEW.getBooleanValue();
        if (!placementEnabled && !breakingEnabled && !suppressionNeeds) return;

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit)) return;
        if (blockHit.getType() == HitResult.Type.MISS) return;

        BlockPos hitPos = blockHit.getBlockPos();
        ItemStack heldItem = client.player.getMainHandStack();
        Vec3d camPos = context.camera().getPos();

        boolean holdingBlockItem = heldItem.getItem() instanceof BlockItem;
        boolean holdingTool      = heldItem.getItem() instanceof MiningToolItem;

        // Placement: run when placementEnabled OR suppression needs placement chain
        if ((placementEnabled || suppressionNeeds) && holdingBlockItem) {
            BlockItem blockItem = (BlockItem) heldItem.getItem();
            BlockPos placedPos = hitPos.offset(blockHit.getSide());
            Block placedBlock = blockItem.getBlock();
            BlockState approxState = placedBlock.getDefaultState();
            if (approxState.contains(Properties.HORIZONTAL_FACING)) {
                approxState = approxState.with(Properties.HORIZONTAL_FACING,
                        client.player.getHorizontalFacing().getOpposite());
            }
            Set<BlockPos> updateSet = BlockUpdateCalculator.compute(client.world, placedPos, approxState);
            if (placementEnabled) renderBoxes(context.matrixStack(), camPos, updateSet, 1f, 0f, 0f);
        }

        // Breaking: run when breakingEnabled (with tool) OR suppression needs breaking chain
        // When holding a BlockItem the player intends to place, not break — skip breaking sim.
        if ((breakingEnabled && holdingTool) || (suppressionNeeds && !holdingBlockItem)) {
            Set<BlockPos> updateSet = BlockUpdateCalculator.compute(client.world, hitPos);
            if (breakingEnabled && holdingTool) renderBoxes(context.matrixStack(), camPos, updateSet, 0f, 0.3f, 1f);
        }
    }

    private static void renderBoxes(MatrixStack matrices, Vec3d camPos,
                                    Set<BlockPos> positions, float r, float g, float b) {
        if (positions.isEmpty()) return;

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f viewMat = new Matrix4f(matrices.peek().getPositionMatrix());
        matrices.pop();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableCull();

//#if MC >= 12101
        //$$ RenderSystem.enableDepthTest();
        //$$ BufferBuilder solid = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        //$$ for (BlockPos pos : positions) {
        //$$     float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        //$$     addBox(viewMat, solid, x, y, z, x + 1f, y + 1f, z + 1f, r, g, b, 0.04f);
        //$$ }
        //$$ BufferRenderer.drawWithGlobalProgram(solid.end());
        //$$
        //$$ RenderSystem.disableDepthTest();
        //$$ BufferBuilder through = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        //$$ for (BlockPos pos : positions) {
        //$$     float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        //$$     addBox(viewMat, through, x, y, z, x + 1f, y + 1f, z + 1f, r, g, b, 0.02f);
        //$$ }
        //$$ BufferRenderer.drawWithGlobalProgram(through.end());
        //$$
        //$$ RenderSystem.enableDepthTest();
        //$$ BufferBuilder outline = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        //$$ for (BlockPos pos : positions) {
        //$$     float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        //$$     addBoxLines(viewMat, outline, x, y, z, x + 1f, y + 1f, z + 1f, r, g, b, 0.6f);
        //$$ }
        //$$ BufferRenderer.drawWithGlobalProgram(outline.end());
//#else
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        RenderSystem.enableDepthTest();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : positions) {
            float x = pos.getX(), y = pos.getY(), z = pos.getZ();
            addBox(viewMat, buf, x, y, z, x + 1f, y + 1f, z + 1f, r, g, b, 0.04f);
        }
        tess.draw();

        RenderSystem.disableDepthTest();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : positions) {
            float x = pos.getX(), y = pos.getY(), z = pos.getZ();
            addBox(viewMat, buf, x, y, z, x + 1f, y + 1f, z + 1f, r, g, b, 0.02f);
        }
        tess.draw();

        RenderSystem.enableDepthTest();
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : positions) {
            float x = pos.getX(), y = pos.getY(), z = pos.getZ();
            addBoxLines(viewMat, buf, x, y, z, x + 1f, y + 1f, z + 1f, r, g, b, 0.6f);
        }
        tess.draw();
//#endif

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

//#if MC >= 12101
    //$$ private static void addBox(Matrix4f m, VertexConsumer b,
    //$$                             float x0, float y0, float z0, float x1, float y1, float z1,
    //$$                             float r, float g, float bl, float a) {
    //$$     b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x0,y0,z1).color(r,g,bl,a);
    //$$     b.vertex(m,x0,y1,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a);
    //$$     b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a);
    //$$     b.vertex(m,x0,y0,z1).color(r,g,bl,a); b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a); b.vertex(m,x0,y1,z1).color(r,g,bl,a);
    //$$     b.vertex(m,x0,y0,z1).color(r,g,bl,a); b.vertex(m,x0,y0,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z0).color(r,g,bl,a); b.vertex(m,x0,y1,z1).color(r,g,bl,a);
    //$$     b.vertex(m,x1,y0,z0).color(r,g,bl,a); b.vertex(m,x1,y0,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z1).color(r,g,bl,a); b.vertex(m,x1,y1,z0).color(r,g,bl,a);
    //$$ }
    //$$ private static void addBoxLines(Matrix4f m, VertexConsumer b,
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
//#else
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
        // bottom
        b.vertex(m,x0,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y0,z1).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y0,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y0,z0).color(r,g,bl,a).next();
        // top
        b.vertex(m,x0,y1,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y1,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x1,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y1,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z0).color(r,g,bl,a).next();
        // verticals
        b.vertex(m,x0,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z0).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z0).color(r,g,bl,a).next();
        b.vertex(m,x1,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x1,y1,z1).color(r,g,bl,a).next();
        b.vertex(m,x0,y0,z1).color(r,g,bl,a).next(); b.vertex(m,x0,y1,z1).color(r,g,bl,a).next();
    }
//#endif
//#endif
}
