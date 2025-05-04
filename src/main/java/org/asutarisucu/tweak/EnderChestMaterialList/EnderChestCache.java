package org.asutarisucu.tweak.EnderChestMaterialList;

import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.ItemType;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fallenbreath.tweakermore.impl.mod_tweaks.mlShulkerBoxPreviewSupportEnderChest.EnderChestItemFetcher;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.FeatureToggle;

import java.util.Optional;

public class EnderChestCache {
    private static final Optional<DefaultedList<ItemStack>> empty= Optional.of(DefaultedList.ofSize(27, ItemStack.EMPTY));;
    public static Optional<DefaultedList<ItemStack>> cachedItems = Optional.empty();
    public static Object2IntOpenHashMap<ItemType> getEnderChestItems(PlayerEntity player) {
        try {
            Optional<DefaultedList<ItemStack>> optional = EnderChestItemFetcher.fetch();
            if (optional.isPresent()&& empty.isPresent()&&optional.get().equals(empty.get())) {
                if(cachedItems.isPresent()){
                    optional= cachedItems;
                }else return null;
            }
            cachedItems=optional;
            Object2IntOpenHashMap<ItemType> map = new Object2IntOpenHashMap<>();
            if(optional.isEmpty())return null;
            for (ItemStack stack : optional.get()) {
                map.addTo(new ItemType(stack, true, false), stack.getCount());
                if (stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() instanceof ShulkerBoxBlock && InventoryUtils.shulkerBoxHasItems(stack)){
                    Object2IntOpenHashMap<ItemType> boxCounts=new Object2IntOpenHashMap<>();
                        if(Configs.Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE.getOptionListValue()==UsageRestriction.ListType.NONE) {
                            boxCounts = MaterialListUtils.getStoredItemCounts(stack);
                        } else if (stack.getItem() instanceof BlockItem blockItem
                                && blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBox) {
                            DyeColor color = shulkerBox.getColor();
                            if(color==null){
                                boxCounts = MaterialListUtils.getStoredItemCounts(stack);
                            }else {
                                if (Configs.Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE.getOptionListValue() == UsageRestriction.ListType.WHITELIST
                                        && Configs.Generic.ENDERCHEST_MATERIALLIST_WHITELIST.getStrings().contains(color.toString())) {
                                    boxCounts = MaterialListUtils.getStoredItemCounts(stack);
                                } else if (Configs.Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE.getOptionListValue() == UsageRestriction.ListType.BLACKLIST
                                        &&!Configs.Generic.ENDERCHEST_MATERIALLIST_BLACKLIST.getStrings().contains(color.toString())) {
                                    boxCounts = MaterialListUtils.getStoredItemCounts(stack);
                                }
                            }

                        }
                    for (ItemType type : boxCounts.keySet()) {
                        map.addTo(type, boxCounts.getInt(type));
                    }
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object2IntOpenHashMap<ItemType> getEnderChestItems() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null&& FeatureToggle.ENDERCHEST_MATERIALLIST.getBooleanValue()) {
            return getEnderChestItems(player);
        }
        return null;
    }
}