package org.asutarisucu.tweak.BlockUpdateViewer;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

/**
 * Thread-local BFS state for block update simulation.
 *
 * While active (begin/end), Mixin interceptors redirect world writes here
 * instead of executing them. scheduleBlockTick is captured as PendingTick
 * rather than cancelled, enabling tick-based propagation (observers, etc.).
 */
public class SimulationCapture {

    public static final ThreadLocal<SimulationCapture> CURRENT = new ThreadLocal<>();

    private final Map<BlockPos, BlockState> overlay = new HashMap<>();
    private final Deque<PendingUpdate> pending = new ArrayDeque<>();
    private final Deque<PendingTick> pendingTicks = new ArrayDeque<>();
    private final Set<BlockPos> notified = new LinkedHashSet<>();

    public static SimulationCapture current() { return CURRENT.get(); }
    public void begin() { CURRENT.set(this); }
    public void end()   { CURRENT.remove(); }

    public void applyChange(BlockPos pos, BlockState state) {
        overlay.put(pos.toImmutable(), state);
    }

    public BlockState getOverlay(BlockPos pos) { return overlay.get(pos); }

    // ---- called by Mixin interceptors ----

    public void captureSetBlockState(BlockPos pos, BlockState state, int flags) {
        overlay.put(pos.toImmutable(), state);
        notified.add(pos.toImmutable());
        // NOTIFY_NEIGHBORS = 0x1
        if ((flags & 1) != 0) {
            pending.add(new PendingUpdate(pos.toImmutable(), state.getBlock(), null, null));
        }
    }

    public void captureUpdateAll(BlockPos source, Block block) {
        pending.add(new PendingUpdate(source.toImmutable(), block, null, null));
    }

    public void captureUpdateExcept(BlockPos source, Block block, Direction except) {
        pending.add(new PendingUpdate(source.toImmutable(), block, except, null));
    }

    public void captureSingleNeighbor(BlockPos sourcePos, Block sourceBlock, BlockPos target) {
        pending.add(new PendingUpdate(sourcePos.toImmutable(), sourceBlock, null, target.toImmutable()));
    }

    private final Set<BlockPos> scheduledPositions = new LinkedHashSet<>();

    // Captured instead of cancelled — BFS processes ticks to propagate observer/gate chains
    public void captureScheduledTick(BlockPos pos, Block block) {
        BlockPos immutable = pos.toImmutable();
        pendingTicks.add(new PendingTick(immutable, block));
        scheduledPositions.add(immutable);
    }

    // Pistons use addSyncedBlockEvent instead of scheduleBlockTick — cancel and track position
    public void captureBlockEvent(BlockPos pos) {
        scheduledPositions.add(pos.toImmutable());
    }

    // ---- BFS helpers ----

    public void addNotified(BlockPos pos)            { notified.add(pos.toImmutable()); }
    public boolean hasPending()                      { return !pending.isEmpty(); }
    public PendingUpdate pollPending()               { return pending.poll(); }
    public boolean hasPendingTicks()                 { return !pendingTicks.isEmpty(); }
    public PendingTick pollPendingTick()             { return pendingTicks.poll(); }
    public Set<BlockPos> getNotified()               { return Collections.unmodifiableSet(notified); }
    public Set<BlockPos> getScheduledPositions()     { return Collections.unmodifiableSet(scheduledPositions); }

    // ---- suppression detection ----

    private final Set<BlockPos> suppressionPositions = new LinkedHashSet<>();

    public void captureSuppressionAt(BlockPos pos) {
        suppressionPositions.add(pos.toImmutable());
    }

    public Set<BlockPos> getSuppressionPositions() {
        return Collections.unmodifiableSet(suppressionPositions);
    }

    // ----

    public static class PendingUpdate {
        public final BlockPos source;
        public final Block block;
        public final Direction except;
        public final BlockPos singleTarget;

        PendingUpdate(BlockPos source, Block block, Direction except, BlockPos singleTarget) {
            this.source = source;
            this.block = block;
            this.except = except;
            this.singleTarget = singleTarget;
        }
    }

    public static class PendingTick {
        public final BlockPos pos;
        public final Block block;

        PendingTick(BlockPos pos, Block block) {
            this.pos = pos;
            this.block = block;
        }
    }
}
//#endif
