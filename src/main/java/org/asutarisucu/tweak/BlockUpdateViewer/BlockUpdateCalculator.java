package org.asutarisucu.tweak.BlockUpdateViewer;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.mixin.BlockUpdateViewer.MixinAbstractBlockInvoker;
import org.asutarisucu.mixin.BlockUpdateViewer.MixinWorldIsClientAccessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Block update chain simulator.
 *
 * Calls actual block.neighborUpdate() with isClient temporarily set to false on
 * the ClientWorld, so blocks run their server-side logic (redstone, observer detection,
 * gate power, etc.). All world writes are intercepted by Mixin injectors and fed into
 * SimulationCapture; the real world is never modified.
 *
 * scheduleBlockTick calls are captured as PendingTick entries. After draining the
 * neighbor-update queue, each pending tick is resolved immediately by emitting
 * updateNeighborsAlways from that position — approximating what most tick-based
 * blocks do (observers, repeaters, comparators, pistons). This drives full
 * observer-chain and redstone propagation without calling scheduledTick(ServerWorld).
 */
public class BlockUpdateCalculator {

    public static final Set<BlockPos> CORRUPT_ENTITY_POSITIONS = ConcurrentHashMap.newKeySet();
    private static volatile boolean cacheInvalid = false;

    private static final int MAX_OPS = 2048;
    private static final long CACHE_TTL = 5;

    // Each CacheEntry holds all results from one simulation run.
    // Placement and breaking use separate caches so they never overwrite each other's results.
    private record CacheEntry(
            BlockPos pos, Block block, long tick,
            Set<BlockPos> full, Set<BlockPos> instant, Set<BlockPos> suppressions) {}

    private static volatile CacheEntry breakingCache;
    private static volatile CacheEntry placementCache;

    public static Set<BlockPos> compute(World clientWorld, BlockPos source) {
        return compute(clientWorld, source, null);
    }

    public static Set<BlockPos> compute(World clientWorld, BlockPos source, BlockState hypotheticalState) {
        long tick = clientWorld.getTime();
        boolean isPlacement = hypotheticalState != null;
        Block effectiveBlock = isPlacement
                ? hypotheticalState.getBlock()
                : clientWorld.getBlockState(source).getBlock();

        CacheEntry cache = isPlacement ? placementCache : breakingCache;
        boolean valid = !cacheInvalid && cache != null
                && source.equals(cache.pos())
                && effectiveBlock == cache.block()
                && tick - cache.tick() < CACHE_TTL;

        if (!valid) {
            cacheInvalid = false;
            doCompute(clientWorld, source, hypotheticalState);
            cache = isPlacement ? placementCache : breakingCache;
        }
        return FeatureToggle.UPDATE_VIEW_INSTANT_ONLY.getBooleanValue() ? cache.instant() : cache.full();
    }

    /** Suppression positions from the most recent BREAKING compute (hypotheticalState == null). */
    public static Set<BlockPos> getSuppressionPositions() {
        CacheEntry c = breakingCache;
        return c != null ? c.suppressions() : Collections.emptySet();
    }

    /** Full update chain from the most recent BREAKING compute (hypotheticalState == null). */
    public static Set<BlockPos> getCachedFullChain() {
        CacheEntry c = breakingCache;
        return c != null ? c.full() : Collections.emptySet();
    }

    /** Suppression positions from the most recent PLACEMENT compute (hypotheticalState != null). */
    public static Set<BlockPos> getPlacementSuppressionPositions() {
        CacheEntry c = placementCache;
        return c != null ? c.suppressions() : Collections.emptySet();
    }

    /** Full update chain from the most recent PLACEMENT compute (hypotheticalState != null). */
    public static Set<BlockPos> getCachedPlacementChain() {
        CacheEntry c = placementCache;
        return c != null ? c.full() : Collections.emptySet();
    }

    private static void doCompute(World clientWorld, BlockPos source, BlockState hypotheticalState) {
        long tick = clientWorld.getTime();
        boolean isPlacement = hypotheticalState != null;

        // Read real source state BEFORE flipping isClient (capture not yet active, so no overlay effect)
        BlockState realSourceState = clientWorld.getBlockState(source);
        Block realSourceBlock = realSourceState.getBlock();

        BlockState initialState = isPlacement ? hypotheticalState : Blocks.AIR.getDefaultState();
        Block sourceBlock = isPlacement ? hypotheticalState.getBlock() : realSourceBlock;

        SimulationCapture capture = new SimulationCapture();
        capture.applyChange(source.toImmutable(), initialState);

        // Flip isClient = false so !world.isClient guards in neighborUpdate pass.
        // All writes that result are intercepted by Mixin injectors in SimulationCapture.
        MixinWorldIsClientAccessor worldAccessor = (MixinWorldIsClientAccessor) clientWorld;
        worldAccessor.asutantweaks_setIsClient(false);
        capture.begin();
        Set<BlockPos> phase1Notified = Collections.emptySet();
        try {
            // Seed: as if setBlockState(source, initialState, NOTIFY_ALL) were called.
            capture.captureUpdateAll(source.toImmutable(), sourceBlock);

            // Mirror what World.setBlockState does after updating the chunk:
            //   placement → onBlockAdded (placed block initialises itself, may emit updates)
            //   breaking  → onStateReplaced (removed block cleans up, may emit updates)
            // Both may call setBlockState / updateNeighbors* which our Mixins capture.
            if (isPlacement) {
                ((MixinAbstractBlockInvoker) hypotheticalState.getBlock())
                        .invokeOnBlockAdded(hypotheticalState, clientWorld, source, realSourceState, false);
            } else if (!realSourceState.isAir()) {
                ((MixinAbstractBlockInvoker) realSourceBlock)
                        .invokeOnStateReplaced(realSourceState, clientWorld, source, initialState, false);
            }

            int ops = 0;

            // Phase 1 only: drain the initial neighbor-update wave
            while (capture.hasPending() && ops < MAX_OPS) {
                ops++;
                processOnePending(clientWorld, capture);
            }
            // Snapshot of the notified set after Phase 1 — the instant update range
            phase1Notified = new LinkedHashSet<>(capture.getNotified());

            // Continue alternating Phase 2 (one tick) + Phase 1 for full simulation
            while ((capture.hasPending() || capture.hasPendingTicks()) && ops < MAX_OPS) {

                // Phase 2: process one pending tick, then return to Phase 1.
                if (capture.hasPendingTicks() && ops < MAX_OPS) {
                    ops++;
                    SimulationCapture.PendingTick pendingTick = capture.pollPendingTick();

                    // Simulate the tick's input-reading step for comparators.
                    if (pendingTick.block instanceof ComparatorBlock) {
                        BlockState tickState = clientWorld.getBlockState(pendingTick.pos);
                        if (tickState.getBlock() instanceof ComparatorBlock
                                && tickState.contains(Properties.HORIZONTAL_FACING)) {
                            simulateComparatorRead(clientWorld, capture, pendingTick.pos, tickState);
                        }
                    }

                    capture.captureUpdateAll(pendingTick.pos, pendingTick.block);
                }

                // Phase 1: drain all neighbor updates triggered by the tick
                while (capture.hasPending() && ops < MAX_OPS) {
                    ops++;
                    processOnePending(clientWorld, capture);
                }
            }

        } finally {
            capture.end();
            worldAccessor.asutantweaks_setIsClient(true);
        }

        Set<BlockPos> all = new LinkedHashSet<>(capture.getNotified());
        all.remove(source.toImmutable());
        Set<BlockPos> fullResult = Collections.unmodifiableSet(all);

        // Instant result: Phase 1 update range minus schedulable positions
        phase1Notified.remove(source.toImmutable());
        phase1Notified.removeAll(capture.getScheduledPositions());
        Set<BlockPos> instantResult = Collections.unmodifiableSet(phase1Notified);

        // Suppression detection via shulker box comparator reads
        Set<BlockPos> shulkerSuppressions = new LinkedHashSet<>();
        for (BlockPos notifiedPos : capture.getNotified()) {
            BlockState state = clientWorld.getBlockState(notifiedPos);
            if (!(state.getBlock() instanceof ComparatorBlock)
                    || !state.contains(Properties.HORIZONTAL_FACING)) continue;
            Direction facing = state.get(Properties.HORIZONTAL_FACING);
            BlockPos inputPos = notifiedPos.offset(facing);
            BlockState inputState = clientWorld.getBlockState(inputPos);
            BlockPos shulkerPos = null;
            if (inputState.getBlock() instanceof ShulkerBoxBlock) {
                shulkerPos = inputPos;
            } else if (!inputState.hasComparatorOutput()
                    && inputState.isOpaqueFullCube(clientWorld, inputPos)
                    && clientWorld.getEmittedRedstonePower(inputPos, facing) < 15) {
                BlockPos behindPos = inputPos.offset(facing);
                if (clientWorld.getBlockState(behindPos).getBlock() instanceof ShulkerBoxBlock) {
                    shulkerPos = behindPos;
                }
            }
            if (shulkerPos != null) {
                final BlockPos key = shulkerPos.toImmutable();
                updateCorruptionForShulker(clientWorld, key);
                if (CORRUPT_ENTITY_POSITIONS.contains(key)) {
                    shulkerSuppressions.add(notifiedPos);
                }
            }
        }

        Set<BlockPos> allSuppressions = new LinkedHashSet<>(capture.getSuppressionPositions());
        allSuppressions.addAll(shulkerSuppressions);
        Set<BlockPos> suppressions = Collections.unmodifiableSet(allSuppressions);

        CacheEntry entry = new CacheEntry(source.toImmutable(), sourceBlock, tick,
                fullResult, instantResult, suppressions);
        if (isPlacement) {
            placementCache = entry;
        } else {
            breakingCache = entry;
        }
    }

    /**
     * Replicates ComparatorBlock.getInputSignal() during Phase 2 tick processing.
     * Calls getComparatorOutput() to catch any CCE thrown while reading a container
     * (e.g. corrupt ShulkerBox data) inside the simulation context.
     */
    private static void simulateComparatorRead(World world, SimulationCapture capture,
                                                BlockPos comparatorPos, BlockState comparatorState) {
        Direction facing = comparatorState.get(Properties.HORIZONTAL_FACING);
        BlockPos inputPos = comparatorPos.offset(facing);
        BlockState inputState = world.getBlockState(inputPos);

        if (inputState.hasComparatorOutput()) {
            try {
                inputState.getComparatorOutput(world, inputPos);
            } catch (Exception e) {
                capture.captureSuppressionAt(comparatorPos);
            }
        } else if (inputState.isOpaqueFullCube(world, inputPos)
                && world.getEmittedRedstonePower(inputPos, facing) < 15) {
            BlockPos behindPos = inputPos.offset(facing);
            BlockState behindState = world.getBlockState(behindPos);
            if (behindState.hasComparatorOutput()) {
                try {
                    behindState.getComparatorOutput(world, behindPos);
                } catch (Exception e) {
                    capture.captureSuppressionAt(comparatorPos);
                }
            }
        }
    }

    private static void processOnePending(World world, SimulationCapture capture) {
        SimulationCapture.PendingUpdate update = capture.pollPending();
        if (update.singleTarget != null) {
            dispatchNeighborUpdate(world, capture, update.singleTarget, update.block, update.source);
        } else {
            for (Direction dir : Direction.values()) {
                if (update.except != null && dir == update.except) continue;
                dispatchNeighborUpdate(world, capture, update.source.offset(dir), update.block, update.source);
            }
        }
    }

    private static void updateCorruptionForShulker(World clientWorld, BlockPos shulkerPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getServer() == null) return;
        final BlockPos key = shulkerPos.toImmutable();
        final var worldKey = clientWorld.getRegistryKey();
        // getBlockEntity must run on the server thread; the result updates CORRUPT_ENTITY_POSITIONS
        // asynchronously. cacheInvalid ensures compute() redraws on the next frame after the check.
        client.getServer().execute(() -> {
            ServerWorld serverWorld = client.getServer().getWorld(worldKey);
            if (serverWorld == null) return;
            BlockEntity be = serverWorld.getBlockEntity(key);
            boolean corrupt = be != null && !(be instanceof ShulkerBoxBlockEntity);
            boolean changed = corrupt != CORRUPT_ENTITY_POSITIONS.contains(key);
            if (corrupt) {
                CORRUPT_ENTITY_POSITIONS.add(key);
            } else {
                CORRUPT_ENTITY_POSITIONS.remove(key);
            }
            if (changed) cacheInvalid = true;
        });
        // Multiplayer: CORRUPT_ENTITY_POSITIONS is populated by MixinClientPlayNbtResponse
        // intercepting BlockEntityUpdateS2CPacket as chunks load or block entities change.
    }

    private static void dispatchNeighborUpdate(World world, SimulationCapture capture,
                                                BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
        capture.addNotified(pos);
        // getBlockState is intercepted: returns overlay state if present, real state otherwise.
        BlockState state = world.getBlockState(pos);
        // neighborUpdate is protected on AbstractBlock; MixinAbstractBlockInvoker exposes it.
        // Catch any exception: a crash here means this position is a potential update suppression point.
        try {
            ((MixinAbstractBlockInvoker) state.getBlock())
                    .invokeNeighborUpdate(state, world, pos, sourceBlock, sourcePos, false);
        } catch (Exception e) {
            capture.captureSuppressionAt(pos);
        }

    }
}
//#endif
