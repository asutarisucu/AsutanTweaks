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
//#endif

public class BlockUpdateViewer {

    public static void register() {
//#if MC < 260100
        WorldRenderEvents.AFTER_ENTITIES.register(BlockUpdateViewer::renderOverlay);
//#endif
    }

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
