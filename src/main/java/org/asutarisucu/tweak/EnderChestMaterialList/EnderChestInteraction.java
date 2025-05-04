package org.asutarisucu.tweak.EnderChestMaterialList;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.Optional;

public class EnderChestInteraction {
    public static boolean isOpen=false;
    public static void open(){
        isOpen=true;
    }
    public static void close(HandledScreen<?> screen){
        if(isOpen){
            List<ItemStack> enderChestItemStacks = screen.getScreenHandler().getStacks().subList(0,27);
            EnderChestCache.cachedItems=listItemStack(enderChestItemStacks);
            isOpen=false;
        }
    }
    private static Optional<DefaultedList<ItemStack>> listItemStack(List<ItemStack> stacks) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            list.set(i, stacks.get(i).copy());
        }
        return Optional.of(list);
    }
}
