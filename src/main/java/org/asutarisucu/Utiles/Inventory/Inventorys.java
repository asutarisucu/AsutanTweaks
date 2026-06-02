package org.asutarisucu.Utiles.Inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class Inventorys {
    public static int findMatchingItemStack(PlayerInventory inventory, ItemStack targetStack) {
//#if MC >= 260100
//$$         for (int i = 9; i < inventory.getContainerSize(); i++) {
//#elseif MC >= 12111
//$$ for (int i = 9; i < inventory.getMainStacks().size(); i++) {
//#else
        for (int i = 9; i < inventory.main.size(); i++) {
//#endif
            ItemStack inventoryStack = inventory.getStack(i);
            if (ItemStack.canCombine(targetStack, inventoryStack)&&inventoryStack.getCount()!=1) {
                return i;
            }
        }
        return -1; // 見つからない場合
    }
    public static int findItemSlot(MinecraftClient client, Item item){
        if(client.player!=null){
            ScreenHandler handler=client.player.currentScreenHandler;
            for(Slot slot:handler.slots){
                if(slot.getStack().getItem()==item)return slot.id;
            }
        }
        return -1;
    }
}
