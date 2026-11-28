package org.asutarisucu.tweak.TrencherVerify;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.config.TrencherAreaSource;
import org.asutarisucu.lib.render.Color;
import org.asutarisucu.lib.render.WorldRenderer;

//#if MC < 12111
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//$$ import net.minecraft.util.math.Vec3d;
//#endif
//#if MC < 260100
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//#if MC >= 12000
//$$ import net.minecraft.block.BrushableBlock;
//#else
import net.minecraft.block.SuspiciousSandBlock;
//#endif
//#if MC >= 12003
//$$ import net.minecraft.block.BulbBlock;
//$$ import net.minecraft.block.CrafterBlock;
//#endif
import net.minecraft.block.CommandBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
//#if MC >= 12111
//$$ import net.minecraft.block.ShelfBlock;
//#endif
import net.minecraft.block.TntBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.multiplayer.ClientLevel;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.core.Direction;
//$$ import net.minecraft.core.registries.BuiltInRegistries;
//$$ import net.minecraft.tags.FluidTags;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import net.minecraft.world.item.Items;
//$$ import net.minecraft.world.level.Level;
//$$ import net.minecraft.world.level.block.BellBlock;
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.world.level.block.Blocks;
//$$ import net.minecraft.world.level.block.BrushableBlock;
//$$ import net.minecraft.world.level.block.CommandBlock;
//$$ import net.minecraft.world.level.block.CopperBulbBlock;
//$$ import net.minecraft.world.level.block.CrafterBlock;
//$$ import net.minecraft.world.level.block.DiodeBlock;
//$$ import net.minecraft.world.level.block.DispenserBlock;
//$$ import net.minecraft.world.level.block.DoorBlock;
//$$ import net.minecraft.world.level.block.FallingBlock;
//$$ import net.minecraft.world.level.block.FenceBlock;
//$$ import net.minecraft.world.level.block.FenceGateBlock;
//$$ import net.minecraft.world.level.block.GlazedTerracottaBlock;
//$$ import net.minecraft.world.level.block.HopperBlock;
//$$ import net.minecraft.world.level.block.NoteBlock;
//$$ import net.minecraft.world.level.block.PoweredRailBlock;
//$$ import net.minecraft.world.level.block.RedStoneWireBlock;
//$$ import net.minecraft.world.level.block.RedstoneLampBlock;
//$$ import net.minecraft.world.level.block.RedstoneTorchBlock;
//$$ import net.minecraft.world.level.block.ShelfBlock;
//$$ import net.minecraft.world.level.block.TntBlock;
//$$ import net.minecraft.world.level.block.TrapDoorBlock;
//$$ import net.minecraft.world.level.block.WallBlock;
//$$ import net.minecraft.world.level.block.piston.PistonBaseBlock;
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.world.level.chunk.LevelChunk;
//#endif
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks the ground a Trencher will dig for blocks that get in its way.
 *
 * The area is the horizontal rectangle of the Litematica selection or of two
 * typed corners, from Y -58 (or the bottom of the world, when that is higher)
 * to Max Y. Blocks above Max Y inside the area are kept apart as "above" so
 * they can be highlighted too. Its shorter side is
 * the width and its longer side the length. Every non-air block inside is
 * listed by type in the order found; the types picked in the screen are
 * outlined through walls, like Litematica's Verify. Waterlogged and immovable
 * states of a block get rows of their own.
 *
 * Immovable blocks, glazed terracotta, blocks a redstone signal changes, walls
 * and fences start picked, and the screen lists them first. In Eater Mode
 * blast-resistant blocks take their place. Outside Eater Mode, columns of
 * falling blocks at least Falling Stack Height tall are highlighted as well.
 *
 * Lava is only looked for in the overworld. There the lava check covers the
 * area plus one block on both sides of the width, or only those two outer
 * strips when Liquid Removed is on. The ends of the length are not widened.
 *
 * Like Litematica's Verify, the chunks of the area are queued and read a few
 * per tick once the client has them loaded. Chunks out of render distance stay
 * in the queue and are read when the player comes near, so the list and the
 * outlines fill in as the player moves along the area.
 */
public final class TrencherVerifier {

    public enum Status { OK, NO_WORLD, NO_LITEMATICA, NO_SELECTION, BAD_COORDS }

    public enum LavaCheck { NONE, OUTSIDE_ONLY, INSIDE_AND_OUTSIDE }

    /** Time spent reading chunks per tick. At least one chunk is read whenever one is ready. */
    private static final long TICK_BUDGET_NS = 4_000_000L;

    /**
     * Blast resistance from which a block survives TNT going off next to it.
     * A TNT ray starts at no more than 4 x 1.3 = 5.2 and loses
     * (resistance + 0.3) x 0.3 on each step into a block, so from 17.04 the
     * first step already stops it.
     */
    private static final float TNT_PROOF_RESISTANCE = 17.04f;

    /**
     * Lowest Y looked at. Overworld bedrock reaches up to Y -59 and a Trencher
     * cannot dig through it, so the layers below are left out.
     */
    private static final int MIN_Y = -58;

    /** Opacity of the fill drawn over highlighted blocks. */
    private static final float FILL_ALPHA = 0.35f;

    /** Highlighted positions looked at again per tick to drop blocks that were removed. */
    private static final int SWEEP_PER_TICK = 4096;

    /** One block type found inside the area. */
    public static final class Entry {
        public final Block block;
        public final String id;
        public final String name;
        public final boolean waterlogged, immovable;
        /** Whether the card starts picked, outside and in Eater Mode. */
        private final boolean defaultOn, defaultOnEater;
        private final LongArrayList positions = new LongArrayList();
        private ItemStack icon;

        private Entry(Block block, String id, String name, boolean waterlogged, boolean immovable,
                      boolean defaultOn, boolean defaultOnEater) {
            this.block = block;
            this.id = id;
            this.name = name;
            this.waterlogged = waterlogged;
            this.immovable = immovable;
            this.defaultOn = defaultOn;
            this.defaultOnEater = defaultOnEater;
        }

        public int count() { return positions.size(); }

        /** The block's item; empty for blocks without one, such as water. */
        public ItemStack icon() {
            if (icon == null) icon = new ItemStack(block.asItem());
            return icon;
        }
    }

    /** One verification run. Filled in by {@link #tick} on the client thread. */
    public static final class Result {
        public final int minX, minZ, maxX, maxZ;
        public final int width, length;
        /** Highest Y the area covers, from Max Y. Blocks higher up are collected as "above". */
        public final int maxY;
        public final LavaCheck lavaCheck;
        public final int totalChunks;
        private final boolean widthIsZ;
        private final int bottom, top;
        private final Object world;
        /** Entry of each block state met so far, so the per-block work is a single lookup. */
        private final Map<BlockState, Entry> byState = new IdentityHashMap<>();
        private final Map<String, Entry> byId = new HashMap<>();
        private final List<Entry> entries = new ArrayList<>();
        private final LongArrayList lava = new LongArrayList();
        /** Blocks above {@link #maxY} inside the area. */
        private final LongArrayList above = new LongArrayList();
        /** Falling blocks in columns at least {@link #stackHeight} tall inside the area. */
        private final LongArrayList stacks = new LongArrayList();
        private final int stackHeight;
        /** Chunk keys (x, z packed as in ChunkPos) still to be read. */
        private final LongArrayList pending = new LongArrayList();

        private Result(int minX, int minZ, int maxX, int maxZ, boolean widthIsZ, LavaCheck lavaCheck,
                       int bottom, int top, int maxY, int stackHeight, Object world) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
            int sizeX = maxX - minX + 1, sizeZ = maxZ - minZ + 1;
            this.width = Math.min(sizeX, sizeZ);
            this.length = Math.max(sizeX, sizeZ);
            this.widthIsZ = widthIsZ;
            this.lavaCheck = lavaCheck;
            this.bottom = bottom;
            this.top = top;
            this.maxY = maxY;
            this.stackHeight = stackHeight;
            this.world = world;
            for (int cx = scanMinX() >> 4; cx <= scanMaxX() >> 4; cx++) {
                for (int cz = scanMinZ() >> 4; cz <= scanMaxZ() >> 4; cz++) {
                    pending.add(chunkKey(cx, cz));
                }
            }
            this.totalChunks = pending.size();
        }

        private int pad() { return lavaCheck == LavaCheck.NONE ? 0 : 1; }
        private int scanMinX() { return minX - (widthIsZ ? 0 : pad()); }
        private int scanMaxX() { return maxX + (widthIsZ ? 0 : pad()); }
        private int scanMinZ() { return minZ - (widthIsZ ? pad() : 0); }
        private int scanMaxZ() { return maxZ + (widthIsZ ? pad() : 0); }

        /** Block types found so far, in the order found. */
        public List<Entry> entries() { return entries; }

        public int lavaCount() { return lava.size(); }

        public int aboveCount() { return above.size(); }

        public int stackCount() { return stacks.size(); }

        public int doneChunks() { return totalChunks - pending.size(); }

        public boolean finished() { return pending.isEmpty(); }
    }

    private static ItemStack lavaIcon;

    public static ItemStack lavaIcon() {
        if (lavaIcon == null) lavaIcon = new ItemStack(Items.LAVA_BUCKET);
        return lavaIcon;
    }

    private static ItemStack aboveIcon;

    public static ItemStack aboveIcon() {
        if (aboveIcon == null) aboveIcon = new ItemStack(Items.SCAFFOLDING);
        return aboveIcon;
    }

    private static ItemStack stackIcon;

    public static ItemStack stackIcon() {
        if (stackIcon == null) stackIcon = new ItemStack(Items.SAND);
        return stackIcon;
    }

    private static Result result;
    /**
     * Cards turned on or off by hand, by block id, kept across runs so a new run
     * shows the same types. Other cards follow {@link Entry#defaultOn}.
     */
    private static final Map<String, Boolean> picked = new HashMap<>();
    /** Eater Mode as last seen by {@link #tick}, to notice when it is switched. */
    private static boolean lastEaterMode;
    private static boolean lavaSelected = true;
    /** Highlighted position and the entry it was found as. */
    private static final Map<BlockPos, Entry> highlighted = new HashMap<>();
    private static final Set<BlockPos> highlightedLava = new HashSet<>();
    private static boolean aboveSelected = true;
    private static final Set<BlockPos> highlightedAbove = new HashSet<>();
    private static boolean stackSelected = true;
    private static final Set<BlockPos> highlightedStacks = new HashSet<>();
    /** Snapshot of the highlighted positions that {@link #sweep} walks through. */
    private static final List<BlockPos> sweepList = new ArrayList<>();
    private static int sweepIndex;

    private TrencherVerifier() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    public static Result result() { return result; }

    public static boolean eaterMode() { return Configs.Generic.TRENCHER_EATER_MODE.getBooleanValue(); }

    /** Whether the card starts picked in the current mode. */
    public static boolean isDefaultOn(Entry e) { return eaterMode() ? e.defaultOnEater : e.defaultOn; }

    public static boolean isSelected(Entry e) {
        Boolean p = picked.get(e.id);
        return p != null ? p : isDefaultOn(e);
    }

    public static void toggle(Entry e) {
        picked.put(e.id, !isSelected(e));
        rebuildHighlight();
    }

    public static boolean isLavaSelected() { return lavaSelected; }

    /** Picks or drops several entries at once. */
    public static void setSelected(List<Entry> entries, boolean on) {
        for (Entry e : entries) picked.put(e.id, on);
        rebuildHighlight();
    }

    public static void setLavaSelected(boolean on) {
        lavaSelected = on;
        rebuildHighlight();
    }

    public static boolean isAboveSelected() { return aboveSelected; }

    public static void setAboveSelected(boolean on) {
        aboveSelected = on;
        rebuildHighlight();
    }

    public static boolean isStackSelected() { return stackSelected; }

    public static void setStackSelected(boolean on) {
        stackSelected = on;
        rebuildHighlight();
    }

    /** Stops the run and drops its result and every outline. */
    public static void clear() {
        result = null;
        rebuildHighlight();
    }

    // ── area ─────────────────────────────────────────────────────

    /** @return {x1, z1, x2, z2}, or null with the reason in {@code why[0]} */
    private static int[] corners(Status[] why) {
        if (Configs.Generic.TRENCHER_AREA_SOURCE.getValue() == TrencherAreaSource.LITEMATICA) {
            if (!FabricLoader.getInstance().isModLoaded("litematica")) {
                why[0] = Status.NO_LITEMATICA;
                return null;
            }
            int[] c = LitematicaArea.corners();
            if (c == null) why[0] = Status.NO_SELECTION;
            return c;
        }
        int[] p1 = parseXZ(Configs.Generic.TRENCHER_POS1.getValue());
        int[] p2 = parseXZ(Configs.Generic.TRENCHER_POS2.getValue());
        if (p1 == null || p2 == null) {
            why[0] = Status.BAD_COORDS;
            return null;
        }
        return new int[] { p1[0], p1[1], p2[0], p2[1] };
    }

    /** "x y z" or "x z", separated by spaces or commas. Y is not used. */
    static int[] parseXZ(String s) {
        String[] parts = s.trim().split("[,\\s]+");
        if (parts.length != 2 && parts.length != 3) return null;
        try {
            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[parts.length - 1]);
            return new int[] { x, z };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long chunkKey(int cx, int cz) {
        return ((long) cx & 0xFFFFFFFFL) | (((long) cz & 0xFFFFFFFFL) << 32);
    }

    // ── run ──────────────────────────────────────────────────────

    /** Replaces the current run with a new one over the configured area. */
    public static Status start() {
//#if MC < 260100
        ClientWorld world = MinecraftClient.getInstance().world;
//#else
//$$     ClientLevel world = Minecraft.getInstance().level;
//#endif
        if (world == null) return Status.NO_WORLD;
        Status[] why = new Status[1];
        int[] c = corners(why);
        if (c == null) return why[0];

        int minX = Math.min(c[0], c[2]), maxX = Math.max(c[0], c[2]);
        int minZ = Math.min(c[1], c[3]), maxZ = Math.max(c[1], c[3]);
        // The width is the shorter side; on a square area it is Z.
        boolean widthIsZ = maxX - minX >= maxZ - minZ;

//#if MC < 260100
        boolean overworld = world.getRegistryKey() == World.OVERWORLD;
        int bottom = world.getBottomY();
//#else
//$$     boolean overworld = world.dimension() == Level.OVERWORLD;
//$$     int bottom = world.getMinY();
//#endif
        int top = bottom + world.getHeight() - 1;
        bottom = Math.max(bottom, MIN_Y);
        LavaCheck lavaCheck = !overworld ? LavaCheck.NONE
                : Configs.Generic.TRENCHER_LIQUID_REMOVED.getBooleanValue() ? LavaCheck.OUTSIDE_ONLY
                : LavaCheck.INSIDE_AND_OUTSIDE;

        result = new Result(minX, minZ, maxX, maxZ, widthIsZ, lavaCheck, bottom, top,
                Configs.Generic.TRENCHER_MAX_Y.getIntegerValue(),
                Configs.Generic.TRENCHER_STACK_HEIGHT.getIntegerValue(), world);
        rebuildHighlight();
        return Status.OK;
    }

    /** Reads queued chunks the client has loaded, within the tick budget. */
    private static void tick() {
        boolean eater = eaterMode();
        if (eater != lastEaterMode) {
            // Switching Eater Mode puts every card back to its default for the new mode.
            lastEaterMode = eater;
            picked.clear();
            rebuildHighlight();
        }
        Result r = result;
        if (r == null) return;
//#if MC < 260100
        ClientWorld world = MinecraftClient.getInstance().world;
//#else
//$$     ClientLevel world = Minecraft.getInstance().level;
//#endif
        // A run belongs to the world it was started in; it waits while the player is elsewhere.
        if (world == null || world != r.world) return;
        sweep(r, world);
        if (r.pending.isEmpty()) return;

        long start = System.nanoTime();
        for (int i = 0; i < r.pending.size(); ) {
            long key = r.pending.getLong(i);
            int cx = (int) key, cz = (int) (key >>> 32);
//#if MC < 260100
            if (!world.getChunkManager().isChunkLoaded(cx, cz)) {
//#else
//$$         if (!world.getChunkSource().hasChunk(cx, cz)) {
//#endif
                i++;
                continue;
            }
            readChunk(r, world, cx, cz);
            r.pending.removeLong(i);
            if (System.nanoTime() - start > TICK_BUDGET_NS) break;
        }
    }

//#if MC < 260100
    private static void readChunk(Result r, ClientWorld world, int cx, int cz) {
        WorldChunk chunk = world.getChunk(cx, cz);
        BlockPos.Mutable pos = new BlockPos.Mutable();
//#else
//$$ private static void readChunk(Result r, ClientLevel world, int cx, int cz) {
//$$     LevelChunk chunk = world.getChunk(cx, cz);
//$$     BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
//#endif
        int x0 = Math.max(r.scanMinX(), cx << 4), x1 = Math.min(r.scanMaxX(), (cx << 4) + 15);
        int z0 = Math.max(r.scanMinZ(), cz << 4), z1 = Math.min(r.scanMaxZ(), (cz << 4) + 15);
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean inside = x >= r.minX && x <= r.maxX && z >= r.minZ && z <= r.maxZ;
                boolean lookForLava = r.lavaCheck == LavaCheck.INSIDE_AND_OUTSIDE
                        || (r.lavaCheck == LavaCheck.OUTSIDE_ONLY && !inside);
                int yTop = inside ? r.top : Math.min(r.top, r.maxY);
                // Height and lowest Y of the column of falling blocks being climbed.
                int run = 0, runBottom = 0;
                for (int y = r.bottom; y <= yTop; y++) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (inside) {
                        if (falls(state.getBlock())) {
                            if (run++ == 0) runBottom = y;
                        } else {
                            addStack(r, x, z, runBottom, run);
                            run = 0;
                        }
                    }
                    if (state.isAir()) continue;
                    long packed = BlockPos.asLong(x, y, z);
                    if (y > r.maxY) {
                        // The bedrock roof of the nether is out of reach, so it is not counted.
//#if MC < 260100
                        if (state.isOf(Blocks.BEDROCK)) continue;
//#else
//$$                     if (state.is(Blocks.BEDROCK)) continue;
//#endif
                        r.above.add(packed);
                        if (aboveSelected) highlightedAbove.add(unpack(packed));
                        continue;
                    }
                    if (inside) {
                        Entry e = r.byState.get(state);
                        if (e == null) {
                            e = entryFor(r, state, world, pos);
                            r.byState.put(state, e);
                        }
                        e.positions.add(packed);
                        if (isSelected(e)) highlighted.put(unpack(packed), e);
                    }
//#if MC < 260100
                    if (lookForLava && state.getFluidState().isIn(FluidTags.LAVA)) {
//#else
//$$                 if (lookForLava && state.getFluidState().is(FluidTags.LAVA)) {
//#endif
                        r.lava.add(packed);
                        if (lavaSelected) highlightedLava.add(unpack(packed));
                    }
                }
                if (inside) addStack(r, x, z, runBottom, run);
            }
        }
    }

    /** Keeps a column of {@code height} falling blocks from {@code y0} up when it is tall enough. */
    private static void addStack(Result r, int x, int z, int y0, int height) {
        if (height < r.stackHeight) return;
        for (int y = y0; y < y0 + height; y++) {
            long packed = BlockPos.asLong(x, y, z);
            r.stacks.add(packed);
            if (stackSelected) highlightedStacks.add(unpack(packed));
        }
    }

    /** Blocks that fall when nothing holds them up, such as sand, gravel and concrete powder. */
    private static boolean falls(Block block) {
//#if MC >= 12000
//$$    return block instanceof FallingBlock || block instanceof BrushableBlock;
//#else
        return block instanceof FallingBlock || block instanceof SuspiciousSandBlock;
//#endif
    }

    /** Blocks that change when a redstone signal reaches them, such as copper bulbs and trapdoors. */
    private static boolean reactsToRedstone(Block b) {
//#if MC < 260100
        if (b instanceof TrapdoorBlock || b instanceof PistonBlock || b instanceof RedstoneWireBlock
                || b instanceof AbstractRedstoneGateBlock) return true;
//#if MC >= 12003
//$$    if (b instanceof BulbBlock || b instanceof CrafterBlock) return true;
//#endif
//#if MC >= 12111
//$$    if (b instanceof ShelfBlock) return true;
//#endif
//#else
//$$ if (b instanceof TrapDoorBlock || b instanceof PistonBaseBlock || b instanceof RedStoneWireBlock
//$$         || b instanceof DiodeBlock || b instanceof CopperBulbBlock || b instanceof CrafterBlock
//$$         || b instanceof ShelfBlock) return true;
//#endif
        return b instanceof DoorBlock || b instanceof FenceGateBlock || b instanceof NoteBlock
                || b instanceof DispenserBlock || b instanceof RedstoneLampBlock || b instanceof TntBlock
                || b instanceof PoweredRailBlock || b instanceof HopperBlock || b instanceof BellBlock
                || b instanceof RedstoneTorchBlock || b instanceof CommandBlock;
    }

    /**
     * Looks at a slice of the highlighted positions and drops the ones whose block
     * is no longer what was found there, so a removed block loses its fill.
     * Positions in unloaded chunks are left as they are.
     */
//#if MC < 260100
    private static void sweep(Result r, ClientWorld world) {
//#else
//$$ private static void sweep(Result r, ClientLevel world) {
//#endif
        if (sweepIndex >= sweepList.size()) {
            sweepList.clear();
            sweepList.addAll(highlighted.keySet());
            sweepList.addAll(highlightedLava);
            sweepList.addAll(highlightedAbove);
            sweepList.addAll(highlightedStacks);
            sweepIndex = 0;
        }
        int end = Math.min(sweepList.size(), sweepIndex + SWEEP_PER_TICK);
        for (; sweepIndex < end; sweepIndex++) {
            BlockPos pos = sweepList.get(sweepIndex);
//#if MC < 260100
            if (!world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) continue;
//#else
//$$         if (!world.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;
//#endif
            BlockState state = world.getBlockState(pos);
            Entry e = highlighted.get(pos);
            if (e != null && r.byState.get(state) != e) {
                highlighted.remove(pos);
                e.positions.rem(pos.asLong());
            }
//#if MC < 260100
            if (highlightedLava.contains(pos) && !state.getFluidState().isIn(FluidTags.LAVA)) {
//#else
//$$         if (highlightedLava.contains(pos) && !state.getFluidState().is(FluidTags.LAVA)) {
//#endif
                highlightedLava.remove(pos);
                r.lava.rem(pos.asLong());
            }
            if (state.isAir() && highlightedAbove.remove(pos)) r.above.rem(pos.asLong());
            if (!falls(state.getBlock()) && highlightedStacks.remove(pos)) r.stacks.rem(pos.asLong());
        }
    }

    /**
     * The entry for a state not met before: its block plus whether it holds water
     * and whether a piston can move it. Immovable follows PistonBlock.isMovable
     * with breaking allowed, so blocks a piston breaks count as movable.
     * Whether its card starts picked is decided here too.
     */
//#if MC < 260100
    private static Entry entryFor(Result r, BlockState state, ClientWorld world, BlockPos pos) {
        Block block = state.getBlock();
        boolean water = !state.isOf(Blocks.WATER) && state.getFluidState().isIn(FluidTags.WATER);
        boolean immovable = !PistonBlock.isMovable(state, world, pos, Direction.NORTH, true, Direction.NORTH);
        float resistance = Math.max(block.getBlastResistance(), state.getFluidState().getBlastResistance());
//#else
//$$ private static Entry entryFor(Result r, BlockState state, ClientLevel world, BlockPos pos) {
//$$     Block block = state.getBlock();
//$$     boolean water = !state.is(Blocks.WATER) && state.getFluidState().is(FluidTags.WATER);
//$$     boolean immovable = !PistonBaseBlock.isPushable(state, world, pos, Direction.NORTH, true, Direction.NORTH);
//$$     float resistance = Math.max(block.getExplosionResistance(), state.getFluidState().getExplosionResistance());
//#endif
        String id = blockId(block) + (water ? "#waterlogged" : "") + (immovable ? "#immovable" : "");
        Entry e = r.byId.get(id);
        if (e == null) {
            boolean defaultOn = immovable || block instanceof GlazedTerracottaBlock || reactsToRedstone(block)
                    || block instanceof WallBlock || block instanceof FenceBlock;
            e = new Entry(block, id, block.getName().getString(), water, immovable,
                    defaultOn, resistance >= TNT_PROOF_RESISTANCE);
            r.byId.put(id, e);
            r.entries.add(e);
        }
        return e;
    }

    private static String blockId(Block block) {
//#if MC < 260100
        return Registries.BLOCK.getId(block).toString();
//#else
//$$ return BuiltInRegistries.BLOCK.getKey(block).toString();
//#endif
    }

    private static void rebuildHighlight() {
        highlighted.clear();
        highlightedLava.clear();
        highlightedAbove.clear();
        highlightedStacks.clear();
        sweepList.clear();
        sweepIndex = 0;
        Result r = result;
        if (r == null) return;
        for (Entry e : r.entries) {
            if (!isSelected(e)) continue;
            for (int i = 0; i < e.positions.size(); i++) highlighted.put(unpack(e.positions.getLong(i)), e);
        }
        if (lavaSelected) addAll(highlightedLava, r.lava);
        if (aboveSelected) addAll(highlightedAbove, r.above);
        if (stackSelected) addAll(highlightedStacks, r.stacks);
    }

    private static void addAll(Set<BlockPos> out, LongArrayList packed) {
        for (int i = 0; i < packed.size(); i++) out.add(unpack(packed.getLong(i)));
    }

    private static BlockPos unpack(long packed) {
//#if MC < 260100
        return BlockPos.fromLong(packed);
//#else
//$$ return BlockPos.of(packed);
//#endif
    }

    // ── render ───────────────────────────────────────────────────

    /** The outlines belong to the world they were found in; nothing is drawn elsewhere. */
    private static boolean sameWorld() {
        Result r = result;
        if (r == null) return false;
//#if MC < 260100
        return r.world == MinecraftClient.getInstance().world;
//#else
//$$ return r.world == Minecraft.getInstance().level;
//#endif
    }

//#if MC < 12111
    public static void render(WorldRenderContext context) {
        if (!sameWorld()) return;
        Vec3d cam = context.camera().getPos();
        Matrix4f viewRot = context.matrixStack().peek().getPositionMatrix();
        draw(viewRot, cam.x, cam.y, cam.z);
    }
//#elseif MC < 260100
//$$ public static void render(WorldRenderContext context) {
//$$     if (!sameWorld()) return;
//$$     Vec3d cam = context.gameRenderer().getCamera().getCameraPos();
//$$     Matrix4f viewRot = context.matrices().peek().getPositionMatrix();
//$$     draw(viewRot, cam.x, cam.y, cam.z);
//$$ }
//#else
//$$ public static void render(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
//$$     if (!sameWorld()) return;
//#if MC < 260200
//$$     var cam = context.gameRenderer().getMainCamera().position();
//#else
//$$     var cam = context.gameRenderer().mainCamera().position();
//$$     WorldRenderer.beginFrame(context.submitNodeCollector(), context.poseStack());
//#endif
//$$     draw(new Matrix4f(), cam.x, cam.y, cam.z);
//$$ }
//#endif

    private static void draw(Matrix4f viewRot, double camX, double camY, double camZ) {
        drawSet(highlighted.keySet(), Configs.Generic.TRENCHER_HIGHLIGHT_COLOR.getColor(), viewRot, camX, camY, camZ);
        drawSet(highlightedLava, Configs.Generic.TRENCHER_LAVA_COLOR.getColor(), viewRot, camX, camY, camZ);
        drawSet(highlightedAbove, Configs.Generic.TRENCHER_ABOVE_COLOR.getColor(), viewRot, camX, camY, camZ);
        // Falling stacks are still collected in Eater Mode, but not shown.
        if (!eaterMode()) {
            drawSet(highlightedStacks, Configs.Generic.TRENCHER_STACK_COLOR.getColor(), viewRot, camX, camY, camZ);
        }
    }

    /** A see-through fill so the blocks stand out from afar, plus the outline. */
    private static void drawSet(Set<BlockPos> set, Color color, Matrix4f viewRot,
                                double camX, double camY, double camZ) {
        if (set.isEmpty()) return;
        WorldRenderer.renderBlockFills(set, color.withAlpha(FILL_ALPHA), false, viewRot, camX, camY, camZ);
        WorldRenderer.renderBlockOutlinesThroughWalls(set, color, 0.0025, viewRot, camX, camY, camZ);
    }
}
