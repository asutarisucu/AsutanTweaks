package org.asutarisucu.Utiles;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ClickType;

public class Inventorys {
    public static int findMatchingItemStack(PlayerInventory inventory, ItemStack targetStack) {
        for (int i = 9; i < inventory.main.size(); i++) {
            ItemStack inventoryStack = inventory.getStack(i);
            if (ItemStack.canCombine(targetStack, inventoryStack)) {
                return i;
            }
        }
        return -1; // 見つからない場合
    }
    public static void refillHotbarSlot(PlayerEntity client, ItemStack refillStack) {
        // インベントリのスロットをホットバーに移動する
        client.onPickupSlotClick(client.getMainHandStack(),refillStack, ClickType.LEFT);
    }

}
