package org.asutarisucu.tweak.ClearBlockRender;

import org.asutarisucu.Configs.Configs;

//#if MC < 12111
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
//#if MC >= 12001
//$$ import com.mojang.blaze3d.systems.VertexSorter;
//#endif
//#elseif MC < 260100
//$$ import com.mojang.blaze3d.systems.ProjectionType;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import net.minecraft.client.render.VertexConsumer;
//$$ import net.minecraft.block.BlockRenderType;
//$$ import net.minecraft.block.BlockState;
//$$ import net.minecraft.block.entity.BlockEntity;
//$$ import net.minecraft.block.entity.PistonBlockEntity;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.render.BlockRenderLayers;
//$$ import net.minecraft.client.render.Camera;
//$$ import net.minecraft.client.render.RawProjectionMatrix;
//$$ import net.minecraft.client.render.RenderLayer;
//$$ import net.minecraft.client.render.RenderLayers;
//$$ import net.minecraft.client.render.VertexConsumerProvider;
//$$ import net.minecraft.client.render.block.BlockRenderManager;
//$$ import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
//$$ import net.minecraft.client.render.block.entity.BlockEntityRenderer;
//$$ import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
//$$ import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//$$ import net.minecraft.client.render.command.RenderDispatcher;
//$$ import net.minecraft.client.render.entity.EntityRenderManager;
//$$ import net.minecraft.client.render.entity.state.EntityRenderState;
//$$ import net.minecraft.client.render.model.BlockModelPart;
//$$ import net.minecraft.client.render.state.CameraRenderState;
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.client.world.ClientWorld;
//$$ import net.minecraft.entity.Entity;
//$$ import net.minecraft.entity.player.PlayerEntity;
//$$ import net.minecraft.fluid.FluidState;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.util.math.Box;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import net.minecraft.util.math.random.Random;
//$$ import org.asutarisucu.lib.config.ProjectionMode;
//$$ import org.joml.Matrix4fStack;
//$$ import org.joml.Quaternionf;
//$$ import java.util.ArrayList;
//$$ import java.util.List;
//$$ import java.util.Map;
//#else
//$$ import com.mojang.blaze3d.ProjectionType;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import com.mojang.blaze3d.vertex.VertexConsumer;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.multiplayer.ClientLevel;
//$$ import net.minecraft.client.renderer.ProjectionMatrixBuffer;
//#if MC >= 260200
//$$ import com.mojang.blaze3d.vertex.VertexSorting;
//$$ import net.minecraft.client.renderer.StagedVertexBuffer;
//$$ import org.joml.Matrix4f;
//#else
//$$ import net.minecraft.client.renderer.MultiBufferSource;
//#endif
//$$ import net.minecraft.client.Camera;
//$$ import net.minecraft.client.renderer.block.BlockQuadOutput;
//$$ import net.minecraft.client.renderer.block.BlockStateModelSet;
//$$ import net.minecraft.client.renderer.block.FluidRenderer;
//$$ import net.minecraft.client.renderer.block.FluidStateModelSet;
//$$ import net.minecraft.client.renderer.block.ModelBlockRenderer;
//$$ import net.minecraft.client.renderer.block.MovingBlockRenderState;
//$$ import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
//$$ import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import net.minecraft.core.Holder;
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.entity.player.Player;
//$$ import net.minecraft.world.level.material.FluidState;
//$$ import net.minecraft.world.phys.AABB;
//$$ import net.minecraft.world.level.biome.Biome;
//$$ import net.minecraft.world.level.block.Blocks;
//$$ import net.minecraft.world.level.block.piston.PistonBaseBlock;
//$$ import net.minecraft.world.level.block.piston.PistonHeadBlock;
//$$ import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
//$$ import net.minecraft.world.level.block.RenderShape;
//$$ import net.minecraft.world.level.block.state.properties.PistonType;
//$$ import net.minecraft.client.renderer.rendertype.RenderType;
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.client.renderer.SubmitNodeStorage;
//$$ import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
//$$ import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
//$$ import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
//$$ import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//$$ import net.minecraft.client.renderer.state.level.CameraRenderState;
//$$ import net.minecraft.world.level.block.entity.BlockEntity;
//$$ import net.minecraft.world.phys.Vec3;
//$$ import org.joml.Quaternionf;
//$$ import com.mojang.blaze3d.vertex.QuadInstance;
//$$ import net.minecraft.client.resources.model.geometry.BakedQuad;
//$$ import java.util.Map;
//$$ import org.asutarisucu.lib.config.ProjectionMode;
//$$ import org.joml.Matrix4fStack;
//#endif

/**
 * Draws just the selected region — its blocks, its fluids, and the entities
 * standing in it — from a {@link CaptureCamera}, into whichever framebuffer is
 * currently bound.
 *
 * Geometry is rebuilt every captured frame rather than cached, so anything that
 * moves — redstone, pistons, hoppers — is recorded as it happens. That is
 * affordable because the region is a hand-picked selection, not a chunk radius.
 *
 * Nothing outside the selection contributes: {@link RegionBlockView} reports
 * air there, which both isolates the subject and makes the boundary faces
 * render instead of being culled away.
 */
public final class RegionRenderer {

    private RegionRenderer() {}

//#if MC < 12111
    public static void render(ClientWorld world, int[] bounds, CaptureCamera cam,
                              int width, int height, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        RegionBlockView view = new RegionBlockView(world, bounds);
        BlockRenderManager brm = mc.getBlockRenderManager();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        Matrix4f savedProjection = RenderSystem.getProjectionMatrix();
//#if MC >= 12001
        //$$ VertexSorter savedSorter = RenderSystem.getVertexSorting();
        //$$ RenderSystem.setProjectionMatrix(cam.projectionMatrix(width, height), VertexSorter.BY_DISTANCE);
//#else
        RenderSystem.setProjectionMatrix(cam.projectionMatrix(width, height));
//#endif
        RenderSystem.viewport(0, 0, width, height);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        // The world pass leaves its fog uniforms behind; they would tint the
        // capture (and, zoomed out, wash it out entirely) since our camera sits
        // outside the player's fog distance.
        float savedFogStart = RenderSystem.getShaderFogStart();
        float savedFogEnd = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
        mc.gameRenderer.getLightmapTextureManager().enable();

        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(cam.viewMatrix());

        Random random = Random.create();
        int ox = bounds[0], oy = bounds[1], oz = bounds[2];

        try {
            // Opaque first, then anything with its own renderer, then the see-through
            // layer last — the order the world render uses. Drawing the translucent
            // blocks first instead left them writing depth over the entities behind
            // them, so an entity inside a glass box was missing from the capture.
            renderBlocks(world, view, brm, immediate, matrices, bounds, random, false);
            immediate.draw();
            if (Configs.Generic.CBR_BLOCK_ENTITIES.getBooleanValue()) {
                renderBlockEntities(mc, world, bounds, matrices, immediate, tickDelta);
            }
            renderEntities(mc, world, bounds, cam, matrices, immediate, tickDelta);
            immediate.draw();
            renderBlocks(world, view, brm, immediate, matrices, bounds, random, true);
            immediate.draw();
        } finally {
            mc.gameRenderer.getLightmapTextureManager().disable();
            RenderSystem.setShaderFogStart(savedFogStart);
            RenderSystem.setShaderFogEnd(savedFogEnd);
//#if MC >= 12001
            //$$ RenderSystem.setProjectionMatrix(savedProjection, savedSorter);
//#else
            RenderSystem.setProjectionMatrix(savedProjection);
//#endif
        }
    }

    /**
     * Draws one half of the region's blocks and fluids.
     *
     * @param translucentPass false for the opaque and cut-out layers, true for the
     *                        see-through one
     */
    private static void renderBlocks(ClientWorld world, RegionBlockView view, BlockRenderManager brm,
                                     VertexConsumerProvider.Immediate immediate, MatrixStack matrices,
                                     int[] bounds, Random random, boolean translucentPass) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        OffsetVertexConsumer fluidSink = new OffsetVertexConsumer();
        RenderLayer translucent = RenderLayer.getTranslucentMovingBlock();
        int ox = bounds[0], oy = bounds[1], oz = bounds[2];
        for (int y = bounds[1]; y <= bounds[4]; y++) {
            for (int z = bounds[2]; z <= bounds[5]; z++) {
                for (int x = bounds[0]; x <= bounds[3]; x++) {
                    pos.set(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.getRenderType() == BlockRenderType.MODEL) {
                        RenderLayer layer = RenderLayers.getMovingBlockLayer(state);
                        if ((layer == translucent) == translucentPass) {
                            matrices.push();
                            matrices.translate(x - ox, y - oy, z - oz);
                            brm.renderBlock(state, pos, view, matrices,
                                    immediate.getBuffer(layer), true, random);
                            matrices.pop();
                        }
                    }
                    FluidState fluid = state.getFluidState();
                    if (!fluid.isEmpty()) {
                        RenderLayer layer = fluidLayer(fluid);
                        if ((layer == translucent) == translucentPass) {
                            brm.renderFluid(pos, view,
                                    fluidSink.set(immediate.getBuffer(layer),
                                            (x - ox) - (x & 15), (y - oy) - (y & 15), (z - oz) - (z & 15)),
                                    state, fluid);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws chests, signs, banners and the rest, which have no block model.
     *
     * The renderer is called directly rather than through
     * BlockEntityRenderDispatcher.render, which first asks
     * isInRenderDistance(be, playerCameraPos) and drops anything past
     * getRenderDistance() — 64 blocks for most. Recording a selection from
     * further away than that left every block entity out of the shot.
     * Everything the player picked should be recorded, wherever they stand.
     */
    private static void renderBlockEntities(MinecraftClient mc, ClientWorld world, int[] bounds,
                                            MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                                            float tickDelta) {
        int ox = bounds[0], oy = bounds[1], oz = bounds[2];
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int y = bounds[1]; y <= bounds[4]; y++) {
            for (int z = bounds[2]; z <= bounds[5]; z++) {
                for (int x = bounds[0]; x <= bounds[3]; x++) {
                    pos.set(x, y, z);
                    if (!world.getBlockState(pos).hasBlockEntity()) continue;
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be == null) continue;
                    // The two sanity checks the dispatcher makes; only its distance
                    // check is the one being skipped here.
                    if (!be.hasWorld() || !be.getType().supports(be.getCachedState())) continue;
                    BlockEntityRenderer<BlockEntity> renderer = mc.getBlockEntityRenderDispatcher().get(be);
                    if (renderer == null) continue;
                    matrices.push();
                    matrices.translate(x - ox, y - oy, z - oz);
                    renderer.render(be, tickDelta, matrices, immediate,
                            WorldRenderer.getLightmapCoordinates(world, pos), OverlayTexture.DEFAULT_UV);
                    matrices.pop();
                }
            }
        }
    }

    /**
     * The layer to put a fluid in.
     *
     * The translucent terrain layer is swapped for the moving-block one, as
     * RenderLayers.getMovingBlockLayer does for blocks: on fabulous graphics the
     * terrain layer redirects its draw to the world renderer's own translucency
     * framebuffer, which would take the water out of the capture.
     */
    private static RenderLayer fluidLayer(FluidState fluid) {
        RenderLayer layer = RenderLayers.getFluidLayer(fluid);
        return layer == RenderLayer.getTranslucent() ? RenderLayer.getTranslucentMovingBlock() : layer;
    }

    /**
     * Draws the entities standing in the selection.
     *
     * Whatever the entities are drawn against, the dispatcher's camera is
     * pointed at the capture for the length of the pass — see
     * {@link CaptureCameraView}.
     */
    private static void renderEntities(MinecraftClient mc, ClientWorld world, int[] bounds, CaptureCamera cam,
                                       MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                                       float tickDelta) {
        boolean players = Configs.Generic.CBR_PLAYERS.getBooleanValue();
        boolean others = Configs.Generic.CBR_ENTITIES.getBooleanValue();
        if (!players && !others) return;

        int ox = bounds[0], oy = bounds[1], oz = bounds[2];
        Box region = new Box(bounds[0], bounds[1], bounds[2],
                bounds[3] + 1.0, bounds[4] + 1.0, bounds[5] + 1.0);
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Camera savedCamera = dispatcher.camera;
        dispatcher.camera = captureView(cam, ox, oy, oz);
        try {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof PlayerEntity ? players : others)) continue;
                if (!entity.getBoundingBox().intersects(region)) continue;
                double ex = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
                double ey = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
                double ez = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
                float yaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
                dispatcher.render(entity, ex - ox, ey - oy, ez - oz, yaw, tickDelta, matrices, immediate,
                        dispatcher.getLight(entity, tickDelta));
            }
        } finally {
            dispatcher.camera = savedCamera;
        }
    }

    private static CaptureCameraView cameraView;

    /** The capture camera as a {@link Camera}, in world space. Held for the life of the game. */
    private static CaptureCameraView captureView(CaptureCamera cam, int ox, int oy, int oz) {
        if (cameraView == null) cameraView = new CaptureCameraView();
        cameraView.place(ox + cam.camX, oy + cam.camY, oz + cam.camZ, cam.yaw, cam.pitch);
        return cameraView;
    }
//#elseif MC < 260100
//$$ /**
//$$  * MC 1.21.11 still has an immediate-mode block renderer, but the projection
//$$  * moved into a uniform buffer and the model-view matrix is read from
//$$  * RenderSystem rather than the MatrixStack, so the camera is set up the way the
//$$  * 26.x pass does it. Block entities go through the render command queue.
//$$  */
//$$ public static void render(ClientWorld world, int[] bounds, CaptureCamera cam,
//$$                           int width, int height, float tickDelta) {
//$$     MinecraftClient mc = MinecraftClient.getInstance();
//$$     RegionBlockView view = new RegionBlockView(world, bounds);
//$$     BlockRenderManager brm = mc.getBlockRenderManager();
//$$     VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
//$$
//$$     RenderSystem.backupProjectionMatrix();
//$$     RenderSystem.setProjectionMatrix(
//$$             projectionBuffer().set(cam.projectionMatrix(width, height)),
//$$             Configs.Generic.CBR_PROJECTION.getValue() == ProjectionMode.ISOMETRIC
//$$                     ? ProjectionType.ORTHOGRAPHIC : ProjectionType.PERSPECTIVE);
//$$
//$$     // RenderLayer.draw writes RenderSystem.getModelViewMatrix() into the dynamic
//$$     // uniform and the shader applies it, so the camera belongs in the model-view
//$$     // stack. The MatrixStack carries only the per-block offset.
//$$     Matrix4fStack modelView = RenderSystem.getModelViewStack();
//$$     modelView.pushMatrix();
//$$     modelView.set(cam.viewMatrix());
//$$
//$$     MatrixStack matrices = new MatrixStack();
//$$     Random random = Random.create();
//$$     List<BlockModelPart> parts = new ArrayList<>();
//$$     BlockPos.Mutable pos = new BlockPos.Mutable();
//$$     OffsetVertexConsumer fluidSink = new OffsetVertexConsumer();
//$$     int ox = bounds[0], oy = bounds[1], oz = bounds[2];
//$$
//$$     try {
//$$         // One walk of the region per layer. Immediate draws and resets its shared
//$$         // buffer whenever the layer changes, so walking once and switching layer
//$$         // per block would flush constantly; a repeated block state read is cheaper.
//$$         //
//$$         // Opaque first, then anything with its own renderer, then the see-through
//$$         // layer last — the order the world render uses. Drawing the translucent
//$$         // blocks first instead left them writing depth over the entities behind
//$$         // them, so an entity inside a glass box was missing from the capture.
//$$         RenderLayer[] opaque = { RenderLayers.solid(), RenderLayers.cutout() };
//$$         for (RenderLayer layer : opaque) {
//$$             renderBlocks(world, view, brm, immediate, matrices, bounds, random, parts, pos, fluidSink, layer);
//$$         }
//$$         immediate.draw();
//$$         renderCommands(mc, world, bounds, cam, matrices, tickDelta);
//$$         immediate.draw();
//$$         renderBlocks(world, view, brm, immediate, matrices, bounds, random, parts, pos, fluidSink,
//$$                 RenderLayers.translucentMovingBlock());
//$$         immediate.draw();
//$$     } finally {
//$$         modelView.popMatrix();
//$$         RenderSystem.restoreProjectionMatrix();
//$$     }
//$$ }
//$$
//$$ /** Draws the region's blocks and fluids that belong to one layer. */
//$$ private static void renderBlocks(ClientWorld world, RegionBlockView view, BlockRenderManager brm,
//$$                                  VertexConsumerProvider.Immediate immediate, MatrixStack matrices,
//$$                                  int[] bounds, Random random, List<BlockModelPart> parts,
//$$                                  BlockPos.Mutable pos, OffsetVertexConsumer fluidSink, RenderLayer layer) {
//$$     VertexConsumer buf = immediate.getBuffer(layer);
//$$     int ox = bounds[0], oy = bounds[1], oz = bounds[2];
//$$     for (int y = bounds[1]; y <= bounds[4]; y++) {
//$$         for (int z = bounds[2]; z <= bounds[5]; z++) {
//$$             for (int x = bounds[0]; x <= bounds[3]; x++) {
//$$                 pos.set(x, y, z);
//$$                 BlockState state = world.getBlockState(pos);
//$$                 if (state.isAir()) continue;
//$$                 // Only blocks that have a model, as the chunk compiler checks.
//$$                 // getModel answers with the missing model for a state it has no
//$$                 // entry for, which is not something to hand a tesselator.
//$$                 if (state.getRenderType() == BlockRenderType.MODEL
//$$                         && BlockRenderLayers.getMovingBlockLayer(state) == layer) {
//$$                     parts.clear();
//$$                     random.setSeed(state.getRenderingSeed(pos));
//$$                     brm.getModel(state).addParts(random, parts);
//$$                     matrices.push();
//$$                     matrices.translate(x - ox, y - oy, z - oz);
//$$                     brm.renderBlock(state, pos, view, matrices, buf, true, parts);
//$$                     matrices.pop();
//$$                 }
//$$                 FluidState fluid = state.getFluidState();
//$$                 if (!fluid.isEmpty() && fluidLayer(fluid) == layer) {
//$$                     brm.renderFluid(pos, view,
//$$                             fluidSink.set(buf, (x - ox) - (x & 15), (y - oy) - (y & 15), (z - oz) - (z & 15)),
//$$                             state, fluid);
//$$                 }
//$$             }
//$$         }
//$$     }
//$$ }
//$$
//$$ /**
//$$  * Draws block entities, the blocks a piston is carrying, and the entities
//$$  * standing in the selection.
//$$  *
//$$  * All three go through the render command queue in 1.21.11: a piston's moving
//$$  * blocks are not in the world's block states, and PistonBlockEntityRenderer is
//$$  * what puts them on screen, so pistons are drawn even with block entities
//$$  * turned off.
//$$  *
//$$  * The block entity renderers are driven directly rather than through
//$$  * BlockEntityRenderManager.getRenderState, which drops anything past
//$$  * getRenderDistance() — 64 blocks for most — from the camera it was configured
//$$  * with. Everything the player selected should be in the shot.
//$$  */
//$$ private static void renderCommands(MinecraftClient mc, ClientWorld world, int[] bounds,
//$$                                    CaptureCamera cam, MatrixStack matrices, float tickDelta) {
//$$     boolean allBlockEntities = Configs.Generic.CBR_BLOCK_ENTITIES.getBooleanValue();
//$$     BlockEntityRenderManager manager = mc.getBlockEntityRenderDispatcher();
//$$     RenderDispatcher dispatcher = mc.gameRenderer.getEntityRenderDispatcher();
//$$     OrderedRenderCommandQueue queue = dispatcher.getQueue();
//$$     int ox = bounds[0], oy = bounds[1], oz = bounds[2];
//$$     Vec3d eye = new Vec3d(ox + cam.camX, oy + cam.camY, oz + cam.camZ);
//$$     CaptureCameraView view = captureView(cam, ox, oy, oz);
//$$
//$$     CameraRenderState camState = new CameraRenderState();
//$$     camState.initialized = true;
//$$     camState.pos = eye;
//$$     camState.entityPos = eye;
//$$     camState.blockPos = BlockPos.ofFloored(eye);
//$$     // Taken from the camera rather than built here: each version rotates its
//$$     // own way, and this is what everything billboarded is turned by.
//$$     camState.orientation = new Quaternionf(view.getRotation());
//$$
//$$     boolean any = false;
//$$     // Read the chunks' block entity maps rather than probing every position:
//$$     // a 64-cube region is 262144 lookups a frame, almost all of them misses.
//$$     for (int cx = bounds[0] >> 4; cx <= bounds[3] >> 4; cx++) {
//$$         for (int cz = bounds[2] >> 4; cz <= bounds[5] >> 4; cz++) {
//$$             for (Map.Entry<BlockPos, BlockEntity> entry : world.getChunk(cx, cz).getBlockEntities().entrySet()) {
//$$                 BlockPos p = entry.getKey();
//$$                 if (p.getX() < bounds[0] || p.getX() > bounds[3]
//$$                         || p.getY() < bounds[1] || p.getY() > bounds[4]
//$$                         || p.getZ() < bounds[2] || p.getZ() > bounds[5]) continue;
//$$                 BlockEntity be = entry.getValue();
//$$                 if (!allBlockEntities && !(be instanceof PistonBlockEntity)) continue;
//$$                 // The two sanity checks the manager makes; only its distance check
//$$                 // is the one being skipped here.
//$$                 if (!be.hasWorld() || !be.getType().supports(be.getCachedState())) continue;
//$$                 BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = manager.get(be);
//$$                 if (renderer == null) continue;
//$$                 BlockEntityRenderState beState = renderer.createRenderState();
//$$                 renderer.updateRenderState(be, beState, tickDelta, eye, null);
//$$                 matrices.push();
//$$                 matrices.translate(p.getX() - ox, p.getY() - oy, p.getZ() - oz);
//$$                 renderer.render(beState, matrices, queue, camState);
//$$                 matrices.pop();
//$$                 any = true;
//$$             }
//$$         }
//$$     }
//$$     if (renderEntities(mc, world, bounds, view, camState, matrices, queue, tickDelta)) any = true;
//$$     // render() turns the queued commands into draws on the same Immediate the
//$$     // block pass uses, then clears the queue.
//$$     if (any) dispatcher.render();
//$$ }
//$$
//$$ /** @return whether anything was queued */
//$$ private static boolean renderEntities(MinecraftClient mc, ClientWorld world, int[] bounds,
//$$                                       CaptureCameraView view, CameraRenderState camState,
//$$                                       MatrixStack matrices, OrderedRenderCommandQueue queue,
//$$                                       float tickDelta) {
//$$     boolean players = Configs.Generic.CBR_PLAYERS.getBooleanValue();
//$$     boolean others = Configs.Generic.CBR_ENTITIES.getBooleanValue();
//$$     if (!players && !others) return false;
//$$
//$$     int ox = bounds[0], oy = bounds[1], oz = bounds[2];
//$$     Box region = new Box(bounds[0], bounds[1], bounds[2],
//$$             bounds[3] + 1.0, bounds[4] + 1.0, bounds[5] + 1.0);
//$$     EntityRenderManager entities = mc.getEntityRenderDispatcher();
//$$     Camera savedCamera = entities.camera;
//$$     entities.camera = view;
//$$     boolean any = false;
//$$     try {
//$$         for (Entity entity : world.getEntities()) {
//$$             if (!(entity instanceof PlayerEntity ? players : others)) continue;
//$$             if (!entity.getBoundingBox().intersects(region)) continue;
//$$             EntityRenderState state = entities.getAndUpdateRenderState(entity, tickDelta);
//$$             entities.render(state, camState, state.x - ox, state.y - oy, state.z - oz, matrices, queue);
//$$             any = true;
//$$         }
//$$     } finally {
//$$         entities.camera = savedCamera;
//$$     }
//$$     return any;
//$$ }
//$$
//$$ /**
//$$  * The layer to put a fluid in.
//$$  *
//$$  * The translucent terrain layer is swapped for the moving-block one, as
//$$  * BlockRenderLayers.getMovingBlockLayer does for blocks, so the draw stays on
//$$  * the capture's own target.
//$$  */
//$$ private static RenderLayer fluidLayer(FluidState fluid) {
//$$     return switch (BlockRenderLayers.getFluidLayer(fluid)) {
//$$         case CUTOUT -> RenderLayers.cutout();
//$$         case TRANSLUCENT -> RenderLayers.translucentMovingBlock();
//$$         default -> RenderLayers.solid();
//$$     };
//$$ }
//$$
//$$ private static CaptureCameraView cameraView;
//$$
//$$ /** The capture camera as a Camera, in world space. Held for the life of the game. */
//$$ private static CaptureCameraView captureView(CaptureCamera cam, int ox, int oy, int oz) {
//$$     if (cameraView == null) cameraView = new CaptureCameraView();
//$$     cameraView.place(ox + cam.camX, oy + cam.camY, oz + cam.camZ, cam.yaw, cam.pitch);
//$$     return cameraView;
//$$ }
//$$
//$$ private static RawProjectionMatrix projectionBuffer;
//$$
//$$ /** Held for the life of the game; the buffer is reused by every capture. */
//$$ private static RawProjectionMatrix projectionBuffer() {
//$$     if (projectionBuffer == null) projectionBuffer = new RawProjectionMatrix("AsutanTweaks CBR");
//$$     return projectionBuffer;
//$$ }
//$$
//$$ /** No geometry counters on this version; the log line is a no-op. */
//$$ public static void requestGeometryLog() { }
//#else
//$$ /**
//$$  * MC 26.2 has no immediate-mode block renderer and no RenderType.draw(MeshData),
//$$  * so the pass is built out of the pieces the submit pipeline is made of:
//$$  * ModelBlockRenderer writes the block's quads into a StagedVertexBuffer, and each
//$$  * layer's RenderType is prepared and drawn from that buffer directly.
//$$  */
//$$ public static void render(ClientLevel level, int[] bounds, CaptureCamera cam,
//$$                           int width, int height, float tickDelta) {
//$$     Minecraft mc = Minecraft.getInstance();
//$$     RegionBlockView view = new RegionBlockView(level, bounds);
//$$     BlockStateModelSet models = mc.getModelManager().getBlockStateModelSet();
//$$     FluidStateModelSet fluidModels = mc.getModelManager().getFluidStateModelSet();
//$$     boolean ambientOcclusion = mc.options.ambientOcclusion().get();
//$$     ModelBlockRenderer blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, mc.getBlockColors());
//$$     FluidRenderer fluidRenderer = new FluidRenderer(fluidModels);
//$$
//$$     RenderSystem.backupProjectionMatrix();
//$$     RenderSystem.setProjectionMatrix(
//$$             projectionBuffer().getBuffer(cam.projectionMatrix(width, height)),
//$$             Configs.Generic.CBR_PROJECTION.getValue() == ProjectionMode.ISOMETRIC
//$$                     ? ProjectionType.ORTHOGRAPHIC : ProjectionType.PERSPECTIVE);
//$$
//$$     // The camera goes in the model-view stack, not the PoseStack: RenderType.prepare()
//$$     // snapshots RenderSystem.getModelViewMatrixCopy() into the DynamicTransforms
//$$     // uniform, and the shader applies that on top of the vertices. Baking the view
//$$     // into the vertices instead left them transformed twice, by the player's camera.
//$$     Matrix4fStack modelView = RenderSystem.getModelViewStack();
//$$     modelView.pushMatrix();
//$$     modelView.set(cam.viewMatrix());
//$$
//$$     RenderType solidType = RenderTypes.solidMovingBlock();
//$$     RenderType cutoutType = RenderTypes.cutoutMovingBlock();
//$$     RenderType translucentType = RenderTypes.translucentMovingBlock();
//$$
//#if MC >= 260200
//$$     // Kept alive between captures rather than closed each time: drawFromBuffer only
//$$     // records commands, and closing the pools here would free the vertex buffers
//$$     // before the GPU ran them. Vanilla holds its StagedVertexBuffer the same way.
//$$     StagedVertexBuffer staged = stagedBuffer();
//$$     try {
//$$         StagedVertexBuffer.Draw solidDraw = staged.appendDraw(solidType.format(), solidType.primitiveTopology());
//$$         StagedVertexBuffer.Draw cutoutDraw = staged.appendDraw(cutoutType.format(), cutoutType.primitiveTopology());
//$$         // The see-through layer writes depth, so its quads have to reach the GPU
//$$         // back to front or the nearest one drawn wins and everything behind it is
//$$         // rejected. Water inside a glass box vanished that way. The render type
//$$         // asks for the sort itself; RenderTypeFeatureRenderer reads that flag the
//$$         // same way when it appends its own draws.
//$$         StagedVertexBuffer.Draw translucentDraw = staged.appendDraw(
//$$                 translucentType.format(), translucentType.primitiveTopology(),
//$$                 translucentType.sortOnUpload() ? translucentSorting(cam) : null);
//$$
//$$         // Tesselate once, then replay per layer. StagedVertexBuffer keeps a single
//$$         // vertex builder alive at a time — asking it for a second draw's builder
//$$         // finishes the first — so the three cannot be written interleaved. The
//$$         // fluids of a layer go into that layer's builder while it is still open.
//$$         collectQuads(mc, level, view, models, fluidModels, blockRenderer, bounds, tickDelta);
//$$         VertexConsumer solidBuf = staged.getVertexBuilder(solidDraw);
//$$         replayLayer(solidBuf, 0);
//$$         tesselateFluids(fluidRenderer, level, view, bounds, solidBuf, 0);
//$$         VertexConsumer cutoutBuf = staged.getVertexBuilder(cutoutDraw);
//$$         replayLayer(cutoutBuf, 1);
//$$         tesselateFluids(fluidRenderer, level, view, bounds, cutoutBuf, 1);
//$$         VertexConsumer translucentBuf = staged.getVertexBuilder(translucentDraw);
//$$         replayLayer(translucentBuf, 2);
//$$         tesselateFluids(fluidRenderer, level, view, bounds, translucentBuf, 2);
//$$
//$$         // upload() finishes the open vertex builder itself. endDraw() is teardown —
//$$         // it empties the draw list, so calling it before upload leaves nothing to draw.
//$$         staged.upload();
//$$         // Opaque first, then anything with its own renderer, then the see-through
//$$         // layer last — the order the world render uses. Drawing the translucent
//$$         // blocks first instead left them writing depth over the entities behind
//$$         // them, so an entity inside a glass box was missing from the capture.
//$$         drawnLayers = 0;
//$$         drawStaged(staged, solidDraw, solidType, 0);
//$$         drawStaged(staged, cutoutDraw, cutoutType, 1);
//$$         submitFeatures(mc, level, bounds, cam, tickDelta);
//$$         drawStaged(staged, translucentDraw, translucentType, 2);
//$$         logGeometry();
//$$         staged.endFrame();
//$$     } finally {
//$$         modelView.popMatrix();
//$$         RenderSystem.restoreProjectionMatrix();
//$$     }
//#else
//$$     // MC 26.1 still has the immediate buffer source, so the quads go straight
//$$     // into it and endBatch draws them; StagedVertexBuffer only arrives in 26.2.
//$$     MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
//$$     try {
//$$         // Tesselate once, then replay per layer. The buffer source draws and
//$$         // resets its shared buffer whenever the render type changes, so the three
//$$         // layers cannot be written interleaved without a flush on every switch.
//$$         collectQuads(mc, level, view, models, fluidModels, blockRenderer, bounds, tickDelta);
//$$         VertexConsumer solidBuf = buffers.getBuffer(solidType);
//$$         replayLayer(solidBuf, 0);
//$$         tesselateFluids(fluidRenderer, level, view, bounds, solidBuf, 0);
//$$         VertexConsumer cutoutBuf = buffers.getBuffer(cutoutType);
//$$         replayLayer(cutoutBuf, 1);
//$$         tesselateFluids(fluidRenderer, level, view, bounds, cutoutBuf, 1);
//$$         buffers.endBatch();
//$$
//$$         // Opaque first, then anything with its own renderer, then the see-through
//$$         // layer last — the order the world render uses. Drawing the translucent
//$$         // blocks first instead left them writing depth over the entities behind
//$$         // them, so an entity inside a glass box was missing from the capture.
//$$         submitFeatures(mc, level, bounds, cam, tickDelta);
//$$         buffers.endBatch();
//$$
//$$         VertexConsumer translucentBuf = buffers.getBuffer(translucentType);
//$$         replayLayer(translucentBuf, 2);
//$$         tesselateFluids(fluidRenderer, level, view, bounds, translucentBuf, 2);
//$$         buffers.endBatch();
//$$         logGeometry();
//$$     } finally {
//$$         modelView.popMatrix();
//$$         RenderSystem.restoreProjectionMatrix();
//$$     }
//#endif
//$$ }
//$$
//$$ /**
//$$  * The quads of one capture, flattened and reused between frames.
//$$  *
//$$  * The region used to be walked once per layer, tesselating every block three
//$$  * times and throwing away two thirds of the result each pass. It is walked once
//$$  * now and the quads are replayed per layer. ModelBlockRenderer hands out a
//$$  * single QuadInstance that it overwrites on the next quad, so its nine values
//$$  * are copied out here rather than held by reference.
//$$  */
//$$ private static int emitCount;
//$$ private static float[] emitPos = new float[0];
//$$ private static BakedQuad[] emitQuad = new BakedQuad[0];
//$$ private static int[] emitAttr = new int[0];
//$$ private static byte[] emitLayer = new byte[0];
//$$
//$$ private static void growEmits(int needed) {
//$$     if (needed <= emitQuad.length) return;
//$$     int size = Math.max(1024, Math.max(needed, emitQuad.length * 2));
//$$     emitPos = java.util.Arrays.copyOf(emitPos, size * 3);
//$$     emitQuad = java.util.Arrays.copyOf(emitQuad, size);
//$$     emitAttr = java.util.Arrays.copyOf(emitAttr, size * 9);
//$$     emitLayer = java.util.Arrays.copyOf(emitLayer, size);
//$$ }
//$$
//$$ /**
//$$  * Where the region's fluids are, and which layer each belongs to.
//$$  *
//$$  * A fluid cannot be flattened into quads the way a block model can — the
//$$  * renderer writes vertices straight into the layer's builder — so only the
//$$  * positions are collected on the one walk, and each layer's pass runs the
//$$  * renderer over its own share.
//$$  */
//$$ private static int fluidCount;
//$$ /** One sink for the whole capture, so its tally covers all three layer passes. */
//$$ private static final OffsetVertexConsumer fluidSink = new OffsetVertexConsumer();
//$$ private static int[] fluidPos = new int[0];
//$$ private static byte[] fluidLayer = new byte[0];
//$$
//$$ private static void growFluids(int needed) {
//$$     if (needed <= fluidLayer.length) return;
//$$     int size = Math.max(256, Math.max(needed, fluidLayer.length * 2));
//$$     fluidPos = java.util.Arrays.copyOf(fluidPos, size * 3);
//$$     fluidLayer = java.util.Arrays.copyOf(fluidLayer, size);
//$$ }
//$$
//$$ /** Tesselates every block in the region once, tagging each quad with its layer. */
//$$ private static void collectQuads(Minecraft mc, ClientLevel level, RegionBlockView view,
//$$                                  BlockStateModelSet models, FluidStateModelSet fluidModels,
//$$                                  ModelBlockRenderer blockRenderer, int[] bounds, float tickDelta) {
//$$     emitCount = 0;
//$$     fluidCount = 0;
//$$     fluidSink.reset();
//$$     boolean cutoutLeaves = mc.options.cutoutLeaves().get();
//$$     // Leaves drawn opaque are forced into the solid layer, as the chunk compiler does.
//$$     boolean[] forceSolid = new boolean[1];
//$$
//$$     BlockQuadOutput output = (x, y, z, quad, instance) -> {
//$$         growEmits(emitCount + 1);
//$$         int i = emitCount++;
//$$         emitPos[i * 3] = x;
//$$         emitPos[i * 3 + 1] = y;
//$$         emitPos[i * 3 + 2] = z;
//$$         emitQuad[i] = quad;
//$$         int a = i * 9;
//$$         for (int v = 0; v < 4; v++) {
//$$             emitAttr[a + v] = instance.getColor(v);
//$$             emitAttr[a + 4 + v] = instance.getLightCoords(v);
//$$         }
//$$         emitAttr[a + 8] = instance.overlayCoords();
//$$         emitLayer[i] = (byte) (forceSolid[0] ? 0 : switch (quad.materialInfo().layer()) {
//$$             case CUTOUT -> 1;
//$$             case TRANSLUCENT -> 2;
//$$             default -> 0;
//$$         });
//$$     };
//$$
//$$     BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
//$$     int ox = bounds[0], oy = bounds[1], oz = bounds[2];
//$$     for (int y = bounds[1]; y <= bounds[4]; y++) {
//$$         for (int z = bounds[2]; z <= bounds[5]; z++) {
//$$             for (int x = bounds[0]; x <= bounds[3]; x++) {
//$$                 pos.set(x, y, z);
//$$                 BlockState state = level.getBlockState(pos);
//$$                 if (state.isAir()) continue;
//$$                 FluidState fluid = state.getFluidState();
//$$                 if (!fluid.isEmpty()) {
//$$                     growFluids(fluidCount + 1);
//$$                     fluidPos[fluidCount * 3] = x;
//$$                     fluidPos[fluidCount * 3 + 1] = y;
//$$                     fluidPos[fluidCount * 3 + 2] = z;
//$$                     fluidLayer[fluidCount] = (byte) switch (fluidModels.get(fluid).layer()) {
//$$                         case CUTOUT -> 1;
//$$                         case TRANSLUCENT -> 2;
//$$                         default -> 0;
//$$                     };
//$$                     fluidCount++;
//$$                 }
//$$                 // Only blocks that have a model, as the chunk compiler checks.
//$$                 // BlockStateModelSet.get answers with the missing model for a state
//$$                 // it has no entry for, which is not something to hand a tesselator.
//$$                 if (state.getRenderShape() == RenderShape.MODEL) {
//$$                     forceSolid[0] = ModelBlockRenderer.forceOpaque(cutoutLeaves, state);
//$$                     blockRenderer.tesselateBlock(output, x - ox, y - oy, z - oz,
//$$                             view, pos, state, models.get(state), state.getSeed(pos));
//$$                 }
//$$                 // Only moving_piston carries a PistonMovingBlockEntity, and checking
//$$                 // the state first skips a block entity lookup on every other block.
//$$                 if (state.is(Blocks.MOVING_PISTON)
//$$                         && level.getBlockEntity(pos) instanceof PistonMovingBlockEntity piston) {
//$$                     tesselateMovingBlocks(output, forceSolid, piston, level, models, blockRenderer,
//$$                             cutoutLeaves, x - ox, y - oy, z - oz, tickDelta);
//$$                 }
//$$             }
//$$         }
//$$     }
//$$ }
//$$
//$$ /**
//$$  * Writes the collected quads of one layer into {@code draw}.
//$$  *
//$$  * @param layer 0 solid, 1 cutout, 2 translucent
//$$  */
//$$ private static void replayLayer(VertexConsumer buf, int layer) {
//$$     PoseStack pose = new PoseStack();
//$$     QuadInstance instance = new QuadInstance();
//$$     for (int i = 0; i < emitCount; i++) {
//$$         if (emitLayer[i] != layer) continue;
//$$         int a = i * 9;
//$$         for (int v = 0; v < 4; v++) {
//$$             instance.setColor(v, emitAttr[a + v]);
//$$             instance.setLightCoords(v, emitAttr[a + 4 + v]);
//$$         }
//$$         instance.setOverlayCoords(emitAttr[a + 8]);
//$$         pose.pushPose();
//$$         pose.translate(emitPos[i * 3], emitPos[i * 3 + 1], emitPos[i * 3 + 2]);
//$$         buf.putBakedQuad(pose.last(), emitQuad[i], instance);
//$$         pose.popPose();
//$$     }
//$$ }
//$$
//$$ /**
//$$  * Draws the blocks a piston is carrying.
//$$  *
//$$  * They are not part of the world's block states — the source block is
//$$  * {@code moving_piston}, which has no model — so iterating block states alone
//$$  * drops them and a recording loses everything a piston pushes. This mirrors
//$$  * PistonHeadRenderer, including its handling of the head and the retracting base.
//$$  */
//$$ private static void tesselateMovingBlocks(BlockQuadOutput output, boolean[] forceSolid,
//$$                                           PistonMovingBlockEntity piston, ClientLevel level,
//$$                                           BlockStateModelSet models, ModelBlockRenderer blockRenderer,
//$$                                           boolean cutoutLeaves, float dx, float dy, float dz,
//$$                                           float tickDelta) {
//$$     BlockState moved = piston.getMovedState();
//$$     if (moved.isAir()) return;
//$$     BlockPos sourcePos = piston.getBlockPos().relative(piston.getMovementDirection().getOpposite());
//$$     Holder<Biome> biome = level.getBiome(sourcePos);
//$$     float progress = piston.getProgress(tickDelta);
//$$     float ox = dx + piston.getXOff(tickDelta);
//$$     float oy = dy + piston.getYOff(tickDelta);
//$$     float oz = dz + piston.getZOff(tickDelta);
//$$
//$$     if (moved.is(Blocks.PISTON_HEAD) && progress <= 4.0f) {
//$$         BlockState head = moved.setValue(PistonHeadBlock.SHORT, progress <= 0.5f);
//$$         emitMoving(output, forceSolid, models, blockRenderer, cutoutLeaves, level, sourcePos, biome, head, ox, oy, oz);
//$$     } else if (piston.isSourcePiston() && !piston.isExtending()) {
//$$         PistonType type = moved.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
//$$         BlockState head = Blocks.PISTON_HEAD.defaultBlockState()
//$$                 .setValue(PistonHeadBlock.TYPE, type)
//$$                 .setValue(PistonHeadBlock.FACING, moved.getValue(PistonBaseBlock.FACING))
//$$                 .setValue(PistonHeadBlock.SHORT, progress >= 0.5f);
//$$         emitMoving(output, forceSolid, models, blockRenderer, cutoutLeaves, level, sourcePos, biome, head, ox, oy, oz);
//$$         // The base is drawn without the movement offset, as in PistonHeadRenderer.
//$$         BlockPos basePos = sourcePos.relative(piston.getMovementDirection());
//$$         BlockState base = moved.setValue(PistonBaseBlock.EXTENDED, true);
//$$         emitMoving(output, forceSolid, models, blockRenderer, cutoutLeaves, level, basePos, biome, base, dx, dy, dz);
//$$     } else {
//$$         emitMoving(output, forceSolid, models, blockRenderer, cutoutLeaves, level, sourcePos, biome, moved, ox, oy, oz);
//$$     }
//$$ }
//$$
//$$ private static void emitMoving(BlockQuadOutput output, boolean[] forceSolid,
//$$                                BlockStateModelSet models, ModelBlockRenderer blockRenderer,
//$$                                boolean cutoutLeaves, ClientLevel level, BlockPos pos,
//$$                                Holder<Biome> biome, BlockState state, float x, float y, float z) {
//$$     MovingBlockRenderState moving = new MovingBlockRenderState();
//$$     moving.randomSeedPos = pos;
//$$     moving.blockPos = pos;
//$$     moving.blockState = state;
//$$     moving.biome = biome;
//$$     moving.cardinalLighting = level.cardinalLighting();
//$$     moving.lightEngine = level.getLightEngine();
//$$     forceSolid[0] = ModelBlockRenderer.forceOpaque(cutoutLeaves, state);
//$$     blockRenderer.tesselateBlock(output, x, y, z, moving, pos, state, models.get(state), state.getSeed(pos));
//$$ }
//$$
//$$ /**
//$$  * Draws everything with its own renderer instead of a block model: chests,
//$$  * signs, banners and the rest, plus the entities standing in the selection.
//$$  *
//$$  * Those renderers only submit nodes, so the submissions are collected and then
//$$  * handed to the game's feature dispatcher to be turned into draws. The pose
//$$  * carries just the block offset — the camera lives in the model-view stack,
//$$  * which is what RenderType.prepare() snapshots for the shader.
//$$  *
//$$  * Each block entity renderer is driven directly rather than through
//$$  * BlockEntityRenderDispatcher.tryExtractRenderState, which applies two gates
//$$  * that do not belong here. It drops anything further than getViewDistance()
//$$  * (64 blocks by default) from the camera, which a wide selection exceeds on its
//$$  * own; and it splits renderers by shouldRenderOffScreen(), so the false passed
//$$  * for it silently lost beacons and end gateways. Everything the player picked
//$$  * should be recorded, whatever it is and however far away the shot is framed.
//$$  */
//$$ private static void submitFeatures(Minecraft mc, ClientLevel level, int[] bounds,
//$$                                    CaptureCamera cam, float tickDelta) {
//$$     boolean blockEntities = Configs.Generic.CBR_BLOCK_ENTITIES.getBooleanValue();
//$$     boolean players = Configs.Generic.CBR_PLAYERS.getBooleanValue();
//$$     boolean entities = Configs.Generic.CBR_ENTITIES.getBooleanValue();
//$$     if (!blockEntities && !players && !entities) return;
//$$
//$$     BlockEntityRenderDispatcher dispatcher = mc.getBlockEntityRenderDispatcher();
//$$     int ox = bounds[0], oy = bounds[1], oz = bounds[2];
//$$     Vec3 eye = new Vec3(ox + cam.camX, oy + cam.camY, oz + cam.camZ);
//$$     CaptureCameraView view = captureView(cam, ox, oy, oz);
//$$
//$$     CameraRenderState camState = new CameraRenderState();
//$$     camState.initialized = true;
//$$     camState.pos = eye;
//$$     camState.blockPos = BlockPos.containing(eye);
//$$     camState.xRot = cam.pitch;
//$$     camState.yRot = cam.yaw;
//$$     // Taken from the camera rather than built here: each version rotates its
//$$     // own way, and this is what everything billboarded is turned by.
//$$     camState.orientation = new Quaternionf(view.rotation());
//$$
//#if MC >= 260200
//$$     SubmitNodeStorage storage = new SubmitNodeStorage();
//#else
//$$     // MC 26.1's dispatcher owns the storage and renders out of it; there is no
//$$     // renderAllFeatures(storage) overload to hand one in.
//$$     FeatureRenderDispatcher features = mc.gameRenderer.getFeatureRenderDispatcher();
//$$     SubmitNodeStorage storage = features.getSubmitNodeStorage();
//#endif
//$$     PoseStack pose = new PoseStack();
//$$     boolean any = false;
//$$     if (blockEntities) {
//$$         // Read the chunks' block entity maps rather than probing every position:
//$$         // a 64-cube region is 262144 lookups a frame, almost all of them misses.
//$$         for (int cx = bounds[0] >> 4; cx <= bounds[3] >> 4; cx++) {
//$$             for (int cz = bounds[2] >> 4; cz <= bounds[5] >> 4; cz++) {
//$$                 for (Map.Entry<BlockPos, BlockEntity> entry : level.getChunk(cx, cz).getBlockEntities().entrySet()) {
//$$                     BlockPos p = entry.getKey();
//$$                     if (p.getX() < bounds[0] || p.getX() > bounds[3]
//$$                             || p.getY() < bounds[1] || p.getY() > bounds[4]
//$$                             || p.getZ() < bounds[2] || p.getZ() > bounds[5]) continue;
//$$                     BlockEntity be = entry.getValue();
//$$                     // Pistons are already drawn by the moving-block pass above, which
//$$                     // runs whether or not block entities are enabled.
//$$                     if (be instanceof PistonMovingBlockEntity) continue;
//$$                     if (!be.hasLevel() || !be.getType().isValid(be.getBlockState())) continue;
//$$                     BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.getRenderer(be);
//$$                     if (renderer == null) continue;
//$$                     BlockEntityRenderState beState = renderer.createRenderState();
//$$                     renderer.extractRenderState(be, beState, tickDelta, eye, null);
//$$                     pose.pushPose();
//$$                     pose.translate(p.getX() - ox, p.getY() - oy, p.getZ() - oz);
//$$                     dispatcher.submit(beState, pose, storage, camState);
//$$                     pose.popPose();
//$$                     any = true;
//$$                 }
//$$             }
//$$         }
//$$     }
//$$     if (players || entities) {
//$$         AABB region = new AABB(bounds[0], bounds[1], bounds[2],
//$$                 bounds[3] + 1.0, bounds[4] + 1.0, bounds[5] + 1.0);
//$$         EntityRenderDispatcher entityDispatcher = mc.getEntityRenderDispatcher();
//$$         Camera savedCamera = entityDispatcher.camera;
//$$         Entity savedPick = entityDispatcher.crosshairPickEntity;
//$$         entityDispatcher.prepare(view, null);
//$$         try {
//$$             for (Entity entity : level.entitiesForRendering()) {
//$$                 if (!(entity instanceof Player ? players : entities)) continue;
//$$                 if (!entity.getBoundingBox().intersects(region)) continue;
//$$                 EntityRenderState state = entityDispatcher.extractEntity(entity, tickDelta);
//$$                 entityDispatcher.submit(state, camState,
//$$                         state.x - ox, state.y - oy, state.z - oz, pose, storage);
//$$                 any = true;
//$$             }
//$$         } finally {
//$$             entityDispatcher.prepare(savedCamera, savedPick);
//$$         }
//$$     }
//#if MC >= 260200
//$$     if (any) mc.gameRenderer.featureRenderDispatcher().renderAllFeatures(storage);
//#else
//$$     // renderAllFeatures() clears the storage on its way out, so nothing of ours
//$$     // is left behind for the next world frame.
//$$     if (any) features.renderAllFeatures();
//#endif
//$$ }
//$$
//$$ /**
//$$  * Draws the fluids collected for one layer into {@code buf}.
//$$  *
//$$  * FluidRenderer asks its output for the builder of whichever layer the fluid
//$$  * belongs to; only that layer's fluids are run here, so the answer is always
//$$  * the builder this pass already holds open.
//$$  *
//$$  * @param layer 0 solid, 1 cutout, 2 translucent
//$$  */
//$$ private static void tesselateFluids(FluidRenderer fluidRenderer, ClientLevel level,
//$$                                     RegionBlockView view, int[] bounds,
//$$                                     VertexConsumer buf, int layer) {
//$$     OffsetVertexConsumer sink = fluidSink;
//$$     FluidRenderer.Output output = requested -> sink;
//$$     BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
//$$     int ox = bounds[0], oy = bounds[1], oz = bounds[2];
//$$     for (int i = 0; i < fluidCount; i++) {
//$$         if (fluidLayer[i] != layer) continue;
//$$         int x = fluidPos[i * 3], y = fluidPos[i * 3 + 1], z = fluidPos[i * 3 + 2];
//$$         pos.set(x, y, z);
//$$         BlockState state = level.getBlockState(pos);
//$$         FluidState fluid = state.getFluidState();
//$$         if (fluid.isEmpty()) continue;
//$$         sink.set(buf, (x - ox) - (x & 15), (y - oy) - (y & 15), (z - oz) - (z & 15));
//$$         fluidRenderer.tesselate(view, pos, output, state, fluid);
//$$     }
//$$ }
//$$
//$$ private static CaptureCameraView cameraView;
//$$
//$$ /** The capture camera as a Camera, in world space. Held for the life of the game. */
//$$ private static CaptureCameraView captureView(CaptureCamera cam, int ox, int oy, int oz) {
//$$     if (cameraView == null) cameraView = new CaptureCameraView();
//$$     cameraView.place(ox + cam.camX, oy + cam.camY, oz + cam.camZ, cam.yaw, cam.pitch);
//$$     return cameraView;
//$$ }
//$$
//$$ private static boolean logNextCapture;
//$$ /** Which of the three layer draws had anything to execute, for the log line. */
//$$ private static int drawnLayers;
//$$
//$$ /** Asks for one line about the next capture's geometry. Called for stills and at record start. */
//$$ public static void requestGeometryLog() {
//$$     logNextCapture = true;
//$$ }
//$$
//$$ /** Reports how much geometry each layer produced, so a blank capture can be placed. */
//$$ private static void logGeometry() {
//$$     if (!logNextCapture) return;
//$$     logNextCapture = false;
//$$     int solid = 0, cutout = 0, translucent = 0;
//$$     for (int i = 0; i < emitCount; i++) {
//$$         switch (emitLayer[i]) {
//$$             case 0 -> solid++;
//$$             case 1 -> cutout++;
//$$             default -> translucent++;
//$$         }
//$$     }
//$$     int fluidSolid = 0, fluidCutout = 0, fluidTranslucent = 0;
//$$     for (int i = 0; i < fluidCount; i++) {
//$$         switch (fluidLayer[i]) {
//$$             case 0 -> fluidSolid++;
//$$             case 1 -> fluidCutout++;
//$$             default -> fluidTranslucent++;
//$$         }
//$$     }
//$$     org.asutarisucu.AsutanTweaks.LOGGER.info(
//$$             "[ClearBlockRender] capture geometry — quads solid: {}, cutout: {}, translucent: {};"
//$$                     + " fluid blocks solid: {}, cutout: {}, translucent: {}; fluids: {}; drawn layers: {}",
//$$             solid, cutout, translucent,
//$$             fluidSolid, fluidCutout, fluidTranslucent, fluidSink.describe(),
//$$             Integer.toBinaryString(drawnLayers));
//$$ }
//$$
//#if MC >= 260200
//$$ /**
//$$  * Back-to-front order for the see-through layer.
//$$  *
//$$  * The game sorts with ProjectionType.vertexSorting(), which assumes vertices
//$$  * already sit in camera space. The capture's do not — they are region-local and
//$$  * the camera lives in the model-view stack — so the same two orderings are
//$$  * spelled out against the capture camera instead: distance from the eye under
//$$  * perspective, depth along the view axis under an orthographic projection.
//$$  */
//$$ private static VertexSorting translucentSorting(CaptureCamera cam) {
//$$     if (Configs.Generic.CBR_PROJECTION.getValue() == ProjectionMode.PERSPECTIVE) {
//$$         return VertexSorting.byDistance(cam.camX, cam.camY, cam.camZ);
//$$     }
//$$     Matrix4f view = cam.viewMatrix();
//$$     return VertexSorting.byDistance(point -> -view.transformPosition(point).z());
//$$ }
//$$
//$$ private static StagedVertexBuffer staged;
//$$
//$$ private static StagedVertexBuffer stagedBuffer() {
//$$     if (staged == null) staged = new StagedVertexBuffer(() -> "AsutanTweaks ClearBlockRender", 1 << 20);
//$$     return staged;
//$$ }
//$$
//$$ private static void drawStaged(StagedVertexBuffer staged, StagedVertexBuffer.Draw draw, RenderType type, int layer) {
//$$     StagedVertexBuffer.ExecuteInfo info = staged.getExecuteInfo(draw);
//$$     if (info == null) return;
//$$     drawnLayers |= 1 << layer;
//$$     type.prepare().drawFromBuffer(info);
//$$ }
//#endif
//$$
//$$ private static ProjectionMatrixBuffer projectionBuffer;
//$$
//$$ /** Held for the life of the game; the buffer is reused by every capture. */
//$$ private static ProjectionMatrixBuffer projectionBuffer() {
//$$     if (projectionBuffer == null) projectionBuffer = new ProjectionMatrixBuffer("AsutanTweaks CBR");
//$$     return projectionBuffer;
//$$ }
//#endif
}
