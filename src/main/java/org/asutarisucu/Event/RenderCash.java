package org.asutarisucu.Event;

import me.fallenbreath.tweakermore.impl.mod_tweaks.serverDataSyncer.ServerDataSyncer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.tweak.SearchItems.HighlightContainer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rescans containers around the player once per second.
 * Iterates each chunk's block entity map instead of probing every block
 * position, so cost scales with the number of block entities in range
 * rather than radius³.
 */
public class RenderCash {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static int tickCounter = 0;

    public static void registerCash() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;
            if (++tickCounter < SCAN_INTERVAL_TICKS) return;
            tickCounter = 0;
            scan(client);
        });
    }

    private static void scan(MinecraftClient mc) {
        World world = mc.world;
        if (world == null || mc.player == null) return;

        if (!Feature.SEARCH_CONTAINER_HIGHLIGHT.isEnabled()) {
            HighlightContainer.positions = Collections.emptySet();
            return;
        }
        Set<String> targetNames = new HashSet<>(Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings());
        if (targetNames.isEmpty()) {
            HighlightContainer.positions = Collections.emptySet();
            return;
        }

        BlockPos center = mc.player.getBlockPos();
        int radius = Configs.Generic.HIGHLIGHT_CONTAINER_RANGE.getIntegerValue();

        // Publish a fresh concurrent set immediately; async sync callbacks keep
        // filling it, so highlights appear as container data arrives.
        Set<BlockPos> result = ConcurrentHashMap.newKeySet();
        HighlightContainer.positions = result;

        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                WorldChunk chunk = world.getChunk(cx, cz);
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (Math.abs(pos.getX() - center.getX()) > radius
                            || Math.abs(pos.getY() - center.getY()) > radius
                            || Math.abs(pos.getZ() - center.getZ()) > radius) continue;
                    if (!(entry.getValue() instanceof Inventory inventory)) continue;
//#if MC < 260200
                    ServerDataSyncer.getInstance().syncBlockEntity(entry.getValue()).thenRun(() -> {
//#else
                    // TweakerMore for 26.2 renamed the one-argument sync methods.
                    //$$ ServerDataSyncer.getInstance().syncBlockEntityToWorld(entry.getValue()).thenRun(() -> {
//#endif
                        for (int i = 0; i < inventory.size(); i++) {
                            ItemStack stack = inventory.getStack(i);
                            if (stack.isEmpty()) continue;
                            if (targetNames.contains(Registries.ITEM.getId(stack.getItem()).getPath())) {
                                result.add(pos);
                                break;
                            }
                        }
                    });
                }
            }
        }
    }
}
