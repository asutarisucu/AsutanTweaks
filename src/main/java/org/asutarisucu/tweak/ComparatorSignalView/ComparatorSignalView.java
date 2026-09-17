package org.asutarisucu.tweak.ComparatorSignalView;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.mixin.ComparatorSignalView.MixinComparatorBlockInvoker;

import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.impl.mod_tweaks.serverDataSyncer.ServerDataSyncer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;

//#if MC < 12111
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.Vec3d;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//$$ import net.minecraft.client.font.TextRenderer;
//$$ import net.minecraft.util.math.Vec3d;
//#elseif MC < 260200
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//$$ import net.minecraft.client.gui.Font;
//$$ import net.minecraft.client.renderer.MultiBufferSource;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//$$ import net.minecraft.client.gui.Font;
//$$ import net.minecraft.network.chat.Style;
//$$ import net.minecraft.util.FormattedCharSequence;
//#endif

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ComparatorSignalView.
 *
 * Writes the output signal of every comparator in range flat on its top face.
 *
 * <p>The output is stored only in the server-side {@link ComparatorBlockEntity}. The block state
 * carries nothing but POWERED, and the chunk data sent to the client leaves the output at 0.
 * Values therefore come from, in order:
 *  - Singleplayer: the integrated server's block entity, read every tick.
 *  - Multiplayer with TweakerMore's serverDataSyncer on and query permission: {@link ServerDataSyncer},
 *    which writes the server value into the client block entity.
 *  - Otherwise: vanilla's own calculateOutputSignal run against the client world. Inputs the client
 *    does not have (container contents, the output of other comparators) read as 0, so a powered
 *    comparator whose estimate comes out as 0 is shown as "?".
 * Estimated values are drawn in a different colour from values read from the server.
 */
public class ComparatorSignalView {

    private static final int EXACT_COLOR = 0xFF000000;
    private static final int ESTIMATE_COLOR = 0xFF0000AA;
    private static final int UNKNOWN = -1;
    private static final int FULL_BRIGHT = 0xF000F0;
    // Blocks per font unit. Digits are 7 units tall, about 6px of the 16px face.
    private static final float SCALE = 1.0f / 18.0f;
    // The comparator slab is 2px tall.
    private static final double SURFACE_Y = 2.0 / 16.0 + 0.001;
    // The free area of the face lies between the front torch (2..4px) and the back torches
    // (11..13px); its centre is 0.5px from the block centre toward the output.
    private static final double CENTRE_SHIFT = 0.5 / 16.0;
    // Glyphs span y..y+7, so this centres them on the anchor.
    private static final float TEXT_Y = -3.5f;
    private static final int SYNC_TIMEOUT_TICKS = 100;

    private record Engraving(BlockPos pos, Direction output, String text, int color) {}

    private static volatile List<Engraving> engravings = List.of();
    // Server-side output per position. Written from the server thread or from syncer callbacks.
    private static final ConcurrentHashMap<BlockPos, Integer> serverValues = new ConcurrentHashMap<>();
    private static final Set<BlockPos> awaitingReply = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean singleplayerBatchQueued = new AtomicBoolean();
    // client thread only
    private static final HashMap<BlockPos, Integer> lastRequestTick = new HashMap<>();
    private static Object lastWorld = null;
    private static int clientTick = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ComparatorSignalView::tick);
    }

    private static void tick(MinecraftClient mc) {
        World world = mc.world;
        if (!Feature.COMPARATOR_SIGNAL_VIEW.isEnabled() || world == null || mc.player == null) {
            clear();
            return;
        }
        if (world != lastWorld) {
            clear();
            lastWorld = world;
        }
        clientTick++;

        List<ComparatorBlockEntity> comparators = collect(world, mc.player.getBlockPos(),
                Configs.Generic.COMPARATOR_SIGNAL_RANGE.getIntegerValue());
        Set<BlockPos> inRange = new HashSet<>();
        for (ComparatorBlockEntity be : comparators) inRange.add(be.getPos());

        if (mc.getServer() != null) {
            sampleSingleplayer(mc, new ArrayList<>(inRange));
        } else if (syncerAvailable()) {
            int interval = Configs.Generic.COMPARATOR_SIGNAL_SYNC_INTERVAL.getIntegerValue();
            for (ComparatorBlockEntity be : comparators) requestSync(be, interval);
        } else {
            serverValues.clear();
        }

        List<Engraving> result = new ArrayList<>(comparators.size());
        for (ComparatorBlockEntity be : comparators) {
            BlockPos pos = be.getPos();
            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof ComparatorBlock)) continue;
            Direction output = state.get(Properties.HORIZONTAL_FACING).getOpposite();
            Integer exact = serverValues.get(pos);
            if (exact != null) {
                result.add(new Engraving(pos, output, String.valueOf(exact), EXACT_COLOR));
            } else {
                int value = estimate(world, pos, state);
                result.add(new Engraving(pos, output,
                        value == UNKNOWN ? "?" : String.valueOf(value), ESTIMATE_COLOR));
            }
        }
        engravings = result;

        serverValues.keySet().retainAll(inRange);
        awaitingReply.retainAll(inRange);
        lastRequestTick.keySet().retainAll(inRange);
    }

    private static void clear() {
        if (!engravings.isEmpty()) engravings = List.of();
        serverValues.clear();
        awaitingReply.clear();
        lastRequestTick.clear();
        lastWorld = null;
    }

    private static List<ComparatorBlockEntity> collect(World world, BlockPos center, int radius) {
        List<ComparatorBlockEntity> found = new ArrayList<>();
        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius) >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                WorldChunk chunk = world.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof ComparatorBlockEntity comparator)) continue;
                    BlockPos pos = be.getPos();
                    if (Math.abs(pos.getX() - center.getX()) > radius
                            || Math.abs(pos.getY() - center.getY()) > radius
                            || Math.abs(pos.getZ() - center.getZ()) > radius) continue;
                    found.add(comparator);
                }
            }
        }
        return found;
    }

    /** Output computed on the client, or {@link #UNKNOWN} when the comparator is lit but the estimate says 0. */
    private static int estimate(World world, BlockPos pos, BlockState state) {
        // Vanilla sets POWERED in the same call that stores the output, and only while the output is above 0.
        if (!state.get(Properties.POWERED)) return 0;
        int value;
        try {
            value = ((MixinComparatorBlockInvoker) state.getBlock()).invokeCalculateOutputSignal(world, pos, state);
        } catch (RuntimeException e) {
            // Vanilla only runs this on the server; a modded input block may not handle a client world.
            return UNKNOWN;
        }
        return value > 0 ? value : UNKNOWN;
    }

    private static void sampleSingleplayer(MinecraftClient mc, List<BlockPos> positions) {
        var server = mc.getServer();
        if (server == null || positions.isEmpty()) return;
        // Skip this tick if the previous batch has not run yet.
        if (!singleplayerBatchQueued.compareAndSet(false, true)) return;
        var key = mc.world.getRegistryKey();
        server.execute(() -> {
            try {
                ServerWorld sw = server.getWorld(key);
                if (sw == null) return;
                for (BlockPos pos : positions) {
                    // getBlockEntity would load an unloaded chunk.
                    if (sw.isChunkLoaded(pos) && sw.getBlockEntity(pos) instanceof ComparatorBlockEntity be) {
                        serverValues.put(pos, be.getOutputSignal());
                    } else {
                        serverValues.remove(pos);
                    }
                }
            } finally {
                singleplayerBatchQueued.set(false);
            }
        });
    }

    /**
     * Queries one comparator at most once per interval, and not again while a query is unanswered.
     * TweakerMore sends up to 512 queries a tick by default, so re-querying on every reply would
     * put a constant load on the server.
     */
    private static void requestSync(ComparatorBlockEntity be, int interval) {
        BlockPos pos = be.getPos();
        Integer last = lastRequestTick.get(pos);
        if (last != null) {
            int age = clientTick - last;
            if (age < interval) return;
            if (awaitingReply.contains(pos) && age < SYNC_TIMEOUT_TICKS) return;
        }
        lastRequestTick.put(pos, clientTick);
        awaitingReply.add(pos);
        try {
//#if MC < 260200
            ServerDataSyncer.getInstance().syncBlockEntity(be).thenRun(() -> {
//#else
            // TweakerMore for 26.2 renamed the one-argument sync methods.
            //$$ ServerDataSyncer.getInstance().syncBlockEntityToWorld(be).thenRun(() -> {
//#endif
                serverValues.put(pos, be.getOutputSignal());
                awaitingReply.remove(pos);
            });
        } catch (Throwable ignored) {
            awaitingReply.remove(pos);
        }
    }

    /** TweakerMore's own info views also skip syncing while serverDataSyncer is off. */
    private static boolean syncerAvailable() {
        try {
            return TweakerMoreConfigs.SERVER_DATA_SYNCER.getBooleanValue() && ServerDataSyncer.hasEnoughPermission();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Font space to camera-relative world space, lying on the top face. The text reads upright for a
     * player at the input side facing the output: font +X runs to that player's right, font +Y (down
     * the glyphs) points back at the input, and the glyph front (font -Z) faces up.
     */
    private static Matrix4f engraveMatrix(Engraving e, double camX, double camY, double camZ) {
//#if MC < 260100
        int dx = e.output().getOffsetX();
        int dz = e.output().getOffsetZ();
//#else
//$$     int dx = e.output().getStepX();
//$$     int dz = e.output().getStepZ();
//#endif
        float x = (float) (e.pos().getX() + 0.5 + dx * CENTRE_SHIFT - camX);
        float y = (float) (e.pos().getY() + SURFACE_Y - camY);
        float z = (float) (e.pos().getZ() + 0.5 + dz * CENTRE_SHIFT - camZ);
        float s = SCALE;
        return new Matrix4f(
                -dz * s, 0, dx * s, 0,
                -dx * s, 0, -dz * s, 0,
                0, -s, 0, 0,
                x, y, z, 1);
    }

    // The advance width includes 1 unit of trailing space.
    private static float textX(int width) {
        return (1 - width) / 2f;
    }

//#if MC < 260100
    /** Text goes to the entity buffers, which the world renderer flushes after this event. */
    public static void render(WorldRenderContext context) {
        List<Engraving> list = engravings;
        if (list.isEmpty()) return;
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
//#if MC < 12111
        Vec3d cam = context.camera().getPos();
        Matrix4f base = context.matrixStack().peek().getPositionMatrix();
//#else
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     Matrix4f base = context.matrices().peek().getPositionMatrix();
//#endif
        for (Engraving e : list) {
            Matrix4f m = new Matrix4f(base).mul(engraveMatrix(e, cam.x, cam.y, cam.z));
            font.draw(e.text(), textX(font.getWidth(e.text())), TEXT_Y, e.color(), false, m,
                    context.consumers(), TextRenderer.TextLayerType.POLYGON_OFFSET, 0, FULL_BRIGHT);
        }
    }
//#elseif MC < 260200
//$$ /** Text goes to the shared buffer source, which is flushed right after BEFORE_GIZMOS. */
//$$ public static void render(LevelRenderContext context) {
//$$     List<Engraving> list = engravings;
//$$     if (list.isEmpty()) return;
//$$     var mc = net.minecraft.client.Minecraft.getInstance();
//$$     Font font = mc.font;
//$$     var cam = context.gameRenderer().getMainCamera().position();
//$$     Matrix4f base = context.poseStack().last().pose();
//$$     MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
//$$     for (Engraving e : list) {
//$$         Matrix4f m = new Matrix4f(base).mul(engraveMatrix(e, cam.x, cam.y, cam.z));
//$$         font.drawInBatch(e.text(), textX(font.width(e.text())), TEXT_Y, e.color(), false, m,
//$$                 buffers, Font.DisplayMode.POLYGON_OFFSET, 0, FULL_BRIGHT);
//$$     }
//$$ }
//#else
//$$ /** BEFORE_GIZMOS runs inside submitFeatures, so the text is drawn with the frame's other submitted features. */
//$$ public static void render(LevelRenderContext context) {
//$$     List<Engraving> list = engravings;
//$$     if (list.isEmpty()) return;
//$$     Font font = net.minecraft.client.Minecraft.getInstance().font;
//$$     var cam = context.gameRenderer().mainCamera().position();
//$$     PoseStack poseStack = context.poseStack();
//$$     for (Engraving e : list) {
//$$         poseStack.pushPose();
//$$         poseStack.mulPose(engraveMatrix(e, cam.x, cam.y, cam.z));
//$$         context.submitNodeCollector().submitText(poseStack, textX(font.width(e.text())), TEXT_Y,
//$$                 FormattedCharSequence.forward(e.text(), Style.EMPTY), false,
//$$                 Font.DisplayMode.POLYGON_OFFSET, FULL_BRIGHT, e.color(), 0, 0);
//$$         poseStack.popPose();
//$$     }
//$$ }
//#endif
}
