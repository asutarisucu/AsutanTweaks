package org.asutarisucu.tweak.EnderChestMaterialList;

import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.malilib.util.InventoryUtils;
//#if MC >= 260100
//$$ import fi.dy.masa.malilib.util.data.ItemType;
//#else
import fi.dy.masa.malilib.util.ItemType;
//#endif
import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fallenbreath.tweakermore.impl.mod_tweaks.mlShulkerBoxPreviewSupportEnderChest.EnderChestItemFetcher;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.FeatureToggle;

import java.util.Optional;

public class EnderChestCache {
    // Sentinel: 27 empty slots — returned by EnderChestItemFetcher when the EC hasn't been opened yet.
    private static final Optional<DefaultedList<ItemStack>> UNLOADED_SENTINEL =
            Optional.of(DefaultedList.ofSize(27, ItemStack.EMPTY));
    static Optional<DefaultedList<ItemStack>> cachedItems = Optional.empty();

    public static Object2IntOpenHashMap<ItemType> getEnderChestItems() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !FeatureToggle.ENDERCHEST_MATERIALLIST.getBooleanValue()) return null;
        try {
            Optional<DefaultedList<ItemStack>> optional = EnderChestItemFetcher.fetch();
            if (optional.isPresent() && optional.get().equals(UNLOADED_SENTINEL.get())) {
                if (cachedItems.isPresent()) {
                    optional = cachedItems;
                } else {
                    return null;
                }
            }
            cachedItems = optional;
            if (optional.isEmpty()) return null;

            Object2IntOpenHashMap<ItemType> map = new Object2IntOpenHashMap<>();
            UsageRestriction.ListType filterType = (UsageRestriction.ListType) Configs.Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE.getOptionListValue();
            for (ItemStack stack : optional.get()) {
                map.addTo(new ItemType(stack, true, false), stack.getCount());
                if (!(stack.getItem() instanceof BlockItem blockItem)) continue;
                if (!(blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBox)) continue;
                if (!InventoryUtils.shulkerBoxHasItems(stack)) continue;

                boolean include;
                if (filterType == UsageRestriction.ListType.NONE) {
                    include = true;
                } else {
                    DyeColor color = shulkerBox.getColor();
                    if (color == null) {
                        include = true;
                    } else if (filterType == UsageRestriction.ListType.WHITELIST) {
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
