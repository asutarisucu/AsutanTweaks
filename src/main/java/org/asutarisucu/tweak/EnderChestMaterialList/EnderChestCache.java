package org.asutarisucu.tweak.EnderChestMaterialList;

import fi.dy.masa.litematica.materials.MaterialListUtils;
//#if MC >= 260100
//$$ import fi.dy.masa.malilib.util.data.ItemType;
//#else
import fi.dy.masa.malilib.util.ItemType;
//#endif
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fallenbreath.tweakermore.impl.mod_tweaks.mlShulkerBoxPreviewSupportEnderChest.EnderChestItemFetcher;
import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.lib.config.FilterMode;
import org.asutarisucu.lib.util.ShulkerBoxUtil;

//#if MC < 260100
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.world.item.BlockItem;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import net.minecraft.world.level.block.ShulkerBoxBlock;
//$$ import net.minecraft.world.item.DyeColor;
//$$ import net.minecraft.core.NonNullList;
//#endif

import java.util.Optional;

public class EnderChestCache {

//#if MC < 260100
    private static final Optional<DefaultedList<ItemStack>> UNLOADED_SENTINEL =
            Optional.of(DefaultedList.ofSize(27, ItemStack.EMPTY));
//#else
//$$ private static final Optional<NonNullList<ItemStack>> UNLOADED_SENTINEL =
//$$         Optional.of(NonNullList.withSize(27, ItemStack.EMPTY));
//#endif

//#if MC < 260100
    static Optional<DefaultedList<ItemStack>> cachedItems = Optional.empty();
//#else
//$$ static Optional<NonNullList<ItemStack>> cachedItems = Optional.empty();
//#endif

    public static Object2IntOpenHashMap<ItemType> getEnderChestItems() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
//#else
//$$ Minecraft mc = Minecraft.getInstance();
//#endif
        if (mc.player == null || !Feature.ENDERCHEST_MATERIALLIST.isEnabled()) return null;
        try {
//#if MC < 260100
            Optional<DefaultedList<ItemStack>> optional = EnderChestItemFetcher.fetch();
//#else
//$$ Optional<NonNullList<ItemStack>> optional = EnderChestItemFetcher.fetch();
//#endif
            if (optional.isPresent() && optional.get().equals(UNLOADED_SENTINEL.get())) {
                optional = cachedItems.isPresent() ? cachedItems : Optional.empty();
                if (optional.isEmpty()) return null;
            }
            cachedItems = optional;
            if (optional.isEmpty()) return null;

            Object2IntOpenHashMap<ItemType> map = new Object2IntOpenHashMap<>();
            FilterMode filterMode = Configs.Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE.getValue();

            for (ItemStack stack : optional.get()) {
                map.addTo(new ItemType(stack, true, false), stack.getCount());
                if (!(stack.getItem() instanceof BlockItem blockItem)) continue;
//#if MC < 260100
                if (!(blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBox)) continue;
//#else
//$$ if (!(blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBox)) continue;
//#endif
                if (!ShulkerBoxUtil.hasItems(stack)) continue;

                boolean include;
                if (filterMode == FilterMode.NONE) {
                    include = true;
                } else {
//#if MC < 260100
                    DyeColor color = shulkerBox.getColor();
//#else
//$$ DyeColor color = shulkerBox.getColor();
//#endif
                    if (color == null) {
                        include = true;
                    } else if (filterMode == FilterMode.WHITELIST) {
                        include = Configs.Generic.ENDERCHEST_MATERIALLIST_WHITELIST.getStrings().contains(color.toString());
                    } else {
                        include = !Configs.Generic.ENDERCHEST_MATERIALLIST_BLACKLIST.getStrings().contains(color.toString());
                    }
                }
                if (include) {
                    Object2IntOpenHashMap<ItemType> boxCounts = MaterialListUtils.getStoredItemCounts(stack);
                    for (ItemType type : boxCounts.keySet()) {
                        map.addTo(type, boxCounts.getInt(type));
                    }
                }
            }
            return map;
        } catch (Exception e) {
            AsutanTweaks.LOGGER.warn("[EnderChestCache] failed to fetch ender chest items", e);
            return null;
        }
    }
}
