package org.asutarisucu.Utiles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

public class Screen {
    public static void AutoFillInventory(ScreenHandler handler){
        MinecraftClient client = MinecraftClient.getInstance();
        if(
                handler instanceof GenericContainerScreenHandler||
                handler instanceof Generic3x3ContainerScreenHandler||
                handler instanceof ShulkerBoxScreenHandler||
                handler instanceof HopperScreenHandler
        ){
            for(int i=handler.slots.size()-1;i>=handler.slots.size()-client.player.getInventory().size();i--){
                if(!handler.getSlot(i).getStack().isEmpty()){
                    ItemStack slot=handler.slots.get(i).getStack();
                    int matchSlot=matchItem(handler.slots,i);
                    if(matchSlot!=-1){
                        int count=handler.slots.get(matchSlot).getStack().getCount()+handler.slots.get(i).getStack().getCount();
                        client.interactionManager.clickSlot(handler.syncId, matchSlot, 0, SlotActionType.PICKUP, client.player);
                        client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, client.player);
                        if(count>handler.slots.get(matchSlot).getMaxItemCount()){
                            client.interactionManager.clickSlot(handler.syncId, matchSlot, 0, SlotActionType.PICKUP, client.player);
                        }
                    }
                }
            }
        }
    }

    private static int matchItem(DefaultedList<Slot> slots, int slot){
        for(int i=0;i<slots.size()-36;i++){
            if(ItemStack.canCombine(slots.get(i).getStack(),slots.get(slot).getStack())){
                return i;
            }
        }
        return -1;
    }
}
