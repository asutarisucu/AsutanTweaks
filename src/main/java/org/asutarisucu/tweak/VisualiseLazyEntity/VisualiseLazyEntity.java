package org.asutarisucu.tweak.VisualiseLazyEntity;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;

import me.fallenbreath.tweakermore.impl.mod_tweaks.serverDataSyncer.ServerDataSyncer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

//#if MC >= 260100
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.entity.LivingEntity;
//$$ import net.minecraft.server.level.ServerLevel;
//#else
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
//#endif

/**
 * VisualiseLazyEntity.
 *
 * Entities sitting in chunks the server does not simulate are not ticked server-side, so they
 * stay still server-side. The client, however, keeps ticking them locally (gravity, TNT fuse,
 * despawn timers...) without ever receiving authoritative position updates. The visible result
 * is that items / falling blocks fall away, and primed TNT vanishes once the client-side fuse
 * runs out and the client {@code discard()}s it. Living entities are already client-interpolated
 * (server-authoritative) and behave correctly without any help, so they are left untouched.
 *
 * <p>Detection is distance-independent (it must also work when chunk loading is disabled and the
 * lazy entities sit within the simulation distance). An entity is considered "lazy/frozen" when
 * the locally simulated position diverges from the authoritative server position; it is released
 * again once the server position actually changes (i.e. the server resumed simulating it). To
 * keep the divergence comparison meaningful the client position is captured at the moment a server
 * sample is requested, then compared against the server position once the (async) sample arrives.
 *
 * <p>Authoritative positions come from:
 *  - Singleplayer: the integrated server world (read on the server thread).
 *  - Multiplayer:  TweakerMore's {@link ServerDataSyncer} (requires query permission).
 *
 * <p>For frozen entities the position is snapped back every client tick. Self-removing entities
 * (falling blocks, TNT) additionally have their client tick cancelled via mixins so they neither
 * fall, count down their fuse, nor despawn.
 */
public class VisualiseLazyEntity {

    private static final double EPS_SQ = 0.5 * 0.5; // divergence threshold, blocks squared

    // id -> authoritative position {x,y,z} of currently frozen entities (read by the tick-cancel mixins)
    private static final ConcurrentHashMap<Integer, double[]> FROZEN = new ConcurrentHashMap<>();
    // async server samples land here and are processed on the client thread next tick
    private static final ConcurrentHashMap<Integer, double[]> pendingServerPos = new ConcurrentHashMap<>();
    // client-thread bookkeeping
    private static final HashMap<Integer, double[]> clientPosAtRequest = new HashMap<>();
    private static final HashMap<Integer, double[]> lastServerPos = new HashMap<>();
    private static final HashMap<Integer, Integer> lastSampleTick = new HashMap<>();

    private static int clientTick = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    /** Whether the entity with this network id is currently pinned (queried by the tick-cancel mixins). */
    public static boolean isFrozen(int entityId) {
        return FROZEN.containsKey(entityId);
    }

    /** Mixin hook: should this entity's client-side tick be cancelled right now? */
    public static boolean shouldFreezeTick(Entity entity) {
        return Feature.VISUALISE_LAZY_ENTITY.isEnabled()
                && isClientEntity(entity)
                && FROZEN.containsKey(entity.getId());
    }

    /** Mixin hook: feature is on and this is a client-side entity (regardless of frozen state). */
    public static boolean isActiveClientEntity(Entity entity) {
        return Feature.VISUALISE_LAZY_ENTITY.isEnabled() && isClientEntity(entity);
    }

    private static void tick() {
        if (!Feature.VISUALISE_LAZY_ENTITY.isEnabled()) {
            if (!FROZEN.isEmpty()) FROZEN.clear();
            if (!pendingServerPos.isEmpty()) pendingServerPos.clear();
            if (!clientPosAtRequest.isEmpty()) clientPosAtRequest.clear();
            if (!lastServerPos.isEmpty()) lastServerPos.clear();
            if (!lastSampleTick.isEmpty()) lastSampleTick.clear();
            return;
        }

        Entity player = player();
        if (player == null) return;

        clientTick++;
        int interval = Configs.Generic.LAZY_ENTITY_SYNC_INTERVAL.getIntegerValue();
        boolean singleplayer = isSingleplayer();
        boolean canSample = singleplayer || syncerAvailable();

        processSamples();

        HashSet<Integer> present = new HashSet<>();
        for (Entity e : allEntities()) {
            // Living entities are already server-authoritative on the client; leave them alone.
            if (e == player || e instanceof LivingEntity) {
                FROZEN.remove(e.getId());
                continue;
            }
            int id = e.getId();
            present.add(id);

            double[] target = FROZEN.get(id);
            if (target != null) freeze(e, target);

            if (canSample) {
                Integer last = lastSampleTick.get(id);
                if (last == null || clientTick - last >= interval) {
                    lastSampleTick.put(id, clientTick);
                    clientPosAtRequest.put(id, pos(e));
                    if (singleplayer) sampleSingleplayer(e); else sampleMultiplayer(e);
                }
            }
        }

        FROZEN.keySet().removeIf(id -> !present.contains(id));
        lastServerPos.keySet().removeIf(id -> !present.contains(id));
        lastSampleTick.keySet().removeIf(id -> !present.contains(id));
        clientPosAtRequest.keySet().removeIf(id -> !present.contains(id));
    }

    /** Fold freshly arrived server samples into the freeze decision (runs on the client thread). */
    private static void processSamples() {
        if (pendingServerPos.isEmpty()) return;
        for (Integer id : new HashSet<>(pendingServerPos.keySet())) {
            double[] sample = pendingServerPos.remove(id);
            if (sample == null) continue;
            double[] server = {sample[0], sample[1], sample[2]};
            double flag = sample[3]; // 1 = lazy (SP), 0 = ticking (SP), -1 = unknown (MP heuristic)

            if (flag == 1.0) {
                // Singleplayer ground truth: the server is not ticking this entity → pin it.
                FROZEN.put(id, server);
            } else if (flag == 0.0) {
                // Singleplayer ground truth: the server is ticking it → release.
                FROZEN.remove(id);
            } else {
                // Multiplayer heuristic: start pinning when the local simulation drifts away from the
                // server position; release once the server position itself starts changing again.
                double[] prev = lastServerPos.get(id);
                if (FROZEN.containsKey(id)) {
                    boolean serverMoved = prev != null && farther(server, prev, EPS_SQ);
                    if (serverMoved) FROZEN.remove(id);
                    else FROZEN.put(id, server);
                } else {
                    double[] atRequest = clientPosAtRequest.get(id);
                    if (atRequest != null && farther(atRequest, server, EPS_SQ)) FROZEN.put(id, server);
                }
            }
            lastServerPos.put(id, server);
        }
    }

    private static boolean farther(double[] a, double[] b, double epsSq) {
        double dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
        return dx * dx + dy * dy + dz * dz > epsSq;
    }

    /** Snap the entity to a known position and kill its local velocity so it stops drifting. */
    private static void freeze(Entity e, double[] p) {
//#if MC >= 260100
//$$ e.snapTo(p[0], p[1], p[2]);
//$$ e.setDeltaMovement(0.0, 0.0, 0.0);
//#else
        e.refreshPositionAfterTeleport(p[0], p[1], p[2]);
        e.setVelocity(0.0, 0.0, 0.0);
//#endif
    }

    private static double[] pos(Entity e) {
//#if MC >= 260100
//$$ var p = e.position();
//$$ return new double[]{p.x, p.y, p.z};
//#elseif MC >= 12111
//$$ var p = e.getEntityPos();
//$$ return new double[]{p.x, p.y, p.z};
//#else
        var p = e.getPos();
        return new double[]{p.x, p.y, p.z};
//#endif
    }

    private static boolean isClientEntity(Entity e) {
//#if MC >= 260100
//$$ return e.level().isClientSide();
//#else
        return e.getEntityWorld().isClient();
//#endif
    }

    private static Entity player() {
//#if MC >= 260100
//$$ return Minecraft.getInstance().player;
//#else
        return MinecraftClient.getInstance().player;
//#endif
    }

    private static Iterable<Entity> allEntities() {
//#if MC >= 260100
//$$ return Minecraft.getInstance().level.entitiesForRendering();
//#else
        return MinecraftClient.getInstance().world.getEntities();
//#endif
    }

    private static boolean isSingleplayer() {
//#if MC >= 260100
//$$ return Minecraft.getInstance().getSingleplayerServer() != null;
//#else
        return MinecraftClient.getInstance().getServer() != null;
//#endif
    }

    /**
     * Read the authoritative position off the integrated server entity (server thread) and, as
     * ground truth, whether the server is actually ticking it. This catches resting lazy entities
     * (e.g. TNT sitting on a block) that never move and so never trip the divergence heuristic.
     */
    private static void sampleSingleplayer(Entity e) {
        int id = e.getId();
//#if MC >= 260100
//$$ Minecraft mc = Minecraft.getInstance();
//$$ var server = mc.getSingleplayerServer();
//$$ if (server == null || mc.level == null) return;
//$$ var key = mc.level.dimension();
//$$ server.execute(() -> {
//$$     ServerLevel sl = server.getLevel(key);
//$$     if (sl == null) return;
//$$     Entity se = sl.getEntity(id);
//$$     if (se == null) return;
//$$     double lazy = sl.isPositionEntityTicking(se.blockPosition()) ? 0.0 : 1.0;
//$$     pendingServerPos.put(id, new double[]{se.getX(), se.getY(), se.getZ(), lazy});
//$$ });
//#elseif MC >= 12111
//$$ MinecraftClient mc = MinecraftClient.getInstance();
//$$ var server = mc.getServer();
//$$ if (server == null || mc.world == null) return;
//$$ var key = mc.world.getRegistryKey();
//$$ server.execute(() -> {
//$$     ServerWorld sw = server.getWorld(key);
//$$     if (sw == null) return;
//$$     Entity se = sw.getEntityById(id);
//$$     if (se == null) return;
//$$     double lazy = sw.shouldTickEntityAt(se.getBlockPos()) ? 0.0 : 1.0;
//$$     pendingServerPos.put(id, new double[]{se.getX(), se.getY(), se.getZ(), lazy});
//$$ });
//#else
        MinecraftClient mc = MinecraftClient.getInstance();
        var server = mc.getServer();
        if (server == null || mc.world == null) return;
        var key = mc.world.getRegistryKey();
        server.execute(() -> {
            ServerWorld sw = server.getWorld(key);
            if (sw == null) return;
            Entity se = sw.getEntityById(id);
            if (se == null) return;
            double lazy = sw.shouldTickEntity(se.getBlockPos()) ? 0.0 : 1.0;
            pendingServerPos.put(id, new double[]{se.getX(), se.getY(), se.getZ(), lazy});
        });
//#endif
    }

    /**
     * Ask TweakerMore to sync the entity from the server, then read back the corrected position.
     * No server tick-status is available here, so the multiplayer path falls back to the divergence
     * heuristic (flag -1) and only catches entities that visibly drift on the client.
     */
    private static void sampleMultiplayer(Entity e) {
        int id = e.getId();
        try {
//#if MC < 260200
            ServerDataSyncer.getInstance().syncEntity(e).thenRun(() -> {
//#else
            // TweakerMore for 26.2 renamed the one-argument sync methods.
            //$$ ServerDataSyncer.getInstance().syncEntityToWorld(e).thenRun(() -> {
//#endif
                double[] p = pos(e);
                pendingServerPos.put(id, new double[]{p[0], p[1], p[2], -1.0});
            });
        } catch (Throwable ignored) {
            // TweakerMore unavailable / feature disabled; nothing we can do.
        }
    }

    private static boolean syncerAvailable() {
        try {
            return ServerDataSyncer.hasEnoughPermission();
        } catch (Throwable t) {
            return false;
        }
    }
}
