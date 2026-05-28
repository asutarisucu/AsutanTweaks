package org.asutarisucu.tweak.SimpleItemEntityRender;

import org.asutarisucu.Configs.FeatureToggle;

//#if MC < 260100
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
//#endif

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Suppression state rebuilt once per game tick in O(n_entities).
 * Rendering simply does an O(1) map lookup — no spatial queries per frame.
 *
 * Grouping rule: entities of the same type at the same block position are
 * treated as a stack. The oldest entity (lowest network ID) is the suppressor
 * and is rendered; all others are suppressed.
 */
public class SimpleEntityRender {

    // suppressed entity network ID → suppressor entity network ID
    private static final HashMap<Integer, Integer> suppressedById = new HashMap<>();
    // suppressor entity network ID → count of entities it suppresses
    private static final HashMap<Integer, Integer> suppressorCount = new HashMap<>();

    public static boolean isSuppressed(int entityId) {
        return suppressedById.containsKey(entityId);
    }

    public static int getSuppressCount(int entityId) {
        return suppressorCount.getOrDefault(entityId, 0);
    }

    public static void clearAll() {
        suppressedById.clear();
        suppressorCount.clear();
    }

    public static void register() {
//#if MC < 260100
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) rebuild(client);
        });
//#endif
    }

//#if MC < 260100

    private record ItemGroupKey(Item item, int x, int y, int z) {}
    private record MobGroupKey(EntityType<?> type, int x, int y, int z) {}

    private static void rebuild(MinecraftClient client) {
        suppressedById.clear();
        suppressorCount.clear();

        boolean items = FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue()
                     || FeatureToggle.SIMPLE_ENTITY_RENDER_COUNT.getBooleanValue();
        boolean mobs  = FeatureToggle.SIMPLE_MOB_ENTITY_RENDER.getBooleanValue();
        if (!items && !mobs) return;

        // Query loaded entities within render distance.
        // Groups are keyed by (type + block position); within each group the entity
        // with the lowest network ID (oldest) becomes the suppressor.
        Vec3d playerPos = client.player.getPos();
        double range = client.options.getViewDistance().getValue() * 16.0 + 16;
        Box searchBox = Box.of(playerPos, range * 2, range * 2, range * 2);

        HashMap<Object, ArrayList<Integer>> groups = new HashMap<>();
        if (items) {
            for (ItemEntity ie : client.world.getEntitiesByClass(ItemEntity.class, searchBox, e -> true)) {
                groups.computeIfAbsent(
                    new ItemGroupKey(ie.getStack().getItem(),
                                     ie.getBlockX(), ie.getBlockY(), ie.getBlockZ()),
                    k -> new ArrayList<>()
                ).add(ie.getId());
            }
        }
        if (mobs) {
            for (MobEntity mob : client.world.getEntitiesByClass(MobEntity.class, searchBox, e -> true)) {
                groups.computeIfAbsent(
                    new MobGroupKey(mob.getType(),
                                    mob.getBlockX(), mob.getBlockY(), mob.getBlockZ()),
                    k -> new ArrayList<>()
                ).add(mob.getId());
            }
        }

        for (ArrayList<Integer> ids : groups.values()) {
            if (ids.size() < 2) continue;
            ids.sort(null);              // ascending → lowest ID first = oldest entity
            int suppressorId = ids.get(0);
            for (int i = 1; i < ids.size(); i++) {
                suppressedById.put(ids.get(i), suppressorId);
            }
            suppressorCount.put(suppressorId, ids.size() - 1);
        }
    }

//#endif
}
