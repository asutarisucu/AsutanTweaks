package org.asutarisucu.Event;

import fi.dy.masa.malilib.util.InventoryUtils;
import me.fallenbreath.tweakermore.impl.mod_tweaks.serverDataSyncer.ServerDataSyncer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.tweak.SearchItems.HighlightContainer;

public class RenderCash {
    private static int counter=0;
    public static void registerCash() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                if(counter>=20){
                    HighlightContainer.posList.clear();
                    MinecraftClient mc = MinecraftClient.getInstance();
                    World world=mc.world;
                    if (world == null || MinecraftClient.getInstance().player == null) return;
                    BlockPos center = MinecraftClient.getInstance().player.getBlockPos();
                    int radius = Configs.Generic.HIGHLIGHT_CONTAINER_RANGE.getIntegerValue();

                    for (int x = -radius; x <= radius; x++) {
                        for (int y = -radius; y <= radius; y++) {
                            for (int z = -radius; z <= radius; z++) {
                                BlockPos pos = center.add(x, y, z);
                                BlockEntity blockEntity = world.getBlockEntity(pos);
                                if (blockEntity instanceof Inventory inventory) {
                                    ServerDataSyncer.getInstance().syncBlockEntity(blockEntity).thenRun(() -> {
                                        for (int i = 0; i < inventory.size(); i++) {
                                            ItemStack stack = inventory.getStack(i);
                                            if (!stack.isEmpty()) {
                                                String name = Registries.ITEM.getId(stack.getItem()).getPath();
                                                if (Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings().contains(name)) {
                                                    HighlightContainer.posList.add(pos);
                                                    break;
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                    counter=0;
                }else counter++;
            }
        });
    }
}
