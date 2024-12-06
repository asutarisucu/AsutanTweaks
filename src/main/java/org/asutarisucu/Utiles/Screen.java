package org.asutarisucu.Utiles;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.InfoUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

public class Screen {
    public static void AutoFillInventory(ScreenHandler handler){
        MinecraftClient client = MinecraftClient.getInstance();
        int hasItems=0,FilledItem=0;
        if(
                handler instanceof GenericContainerScreenHandler||
                handler instanceof Generic3x3ContainerScreenHandler||
                handler instanceof ShulkerBoxScreenHandler||
                handler instanceof HopperScreenHandler
        ){
            for(int i=handler.slots.size()-1;i>=handler.slots.size()-client.player.getInventory().size();i--){
                if(!handler.getSlot(i).getStack().isEmpty()){
                    if(handler.getSlot(i).getStack().getCount()<handler.getSlot(i).getMaxItemCount()){
                        int matchSlot=matchItem(handler.slots,i,client.player.getInventory().size());
                        if(matchSlot!=-1){
                            int count=handler.slots.get(matchSlot).getStack().getCount()+handler.slots.get(i).getStack().getCount();
                            client.interactionManager.clickSlot(handler.syncId, matchSlot, 0, SlotActionType.PICKUP, client.player);
                            client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, client.player);
                            if(count>=handler.slots.get(matchSlot).getMaxItemCount()){
                                client.interactionManager.clickSlot(handler.syncId, matchSlot, 0, SlotActionType.PICKUP, client.player);
                                FilledItem++;
                            } else {
                                i++;
                                hasItems--;
                            }
                        }
                        hasItems++;
                    }
                }
            }
            String child=String.valueOf(FilledItem);
            String GreenTXT=GuiBase.TXT_GREEN;
            String RedTXT=GuiBase.TXT_RED;
            child=(hasItems==FilledItem?GreenTXT:RedTXT)+child;
            String parent=GuiBase.TXT_GREEN+hasItems;
            InfoUtils.printActionbarMessage("Auto Restocked!!(%s/%s)",child,parent);
            client.setScreen(null);
        }
    }

    private static int matchItem(DefaultedList<Slot> slots, int slot,int inventorySize){
        ItemStack targetItem = slots.get(slot).getStack();
        for(int i=0;i<slots.size()-inventorySize;i++){
            ItemStack inventoryItem=slots.get(i).getStack();
            if(ItemStack.canCombine(targetItem,inventoryItem)){
                return i;
            }
        }
        return -1;
    }
}
