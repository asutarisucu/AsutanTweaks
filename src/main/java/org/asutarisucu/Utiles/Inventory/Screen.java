package org.asutarisucu.Utiles.Inventory;

import org.asutarisucu.GUI.ProgressMeter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
//#if MC >= 260100
//$$ import net.minecraft.world.inventory.*;
//#else
import net.minecraft.screen.*;
import net.minecraft.screen.slot.SlotActionType;
//#endif
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

public class Screen {
    public static void AutoFillInventory(ScreenHandler handler){
        MinecraftClient client = MinecraftClient.getInstance();
        if(
            //チェスト、シュルカーボックス、3*3のコンテナ、ホッパーで動作する
                handler instanceof GenericContainerScreenHandler||
                        handler instanceof Generic3x3ContainerScreenHandler||
                        handler instanceof ShulkerBoxScreenHandler||
                        handler instanceof HopperScreenHandler
        ){
            //Playerのinventoryを右下から取得して空でないかつMaxでないなら処理
            for(int i=handler.slots.size()-1;i>=handler.slots.size()-36;i--){
                if(!handler.getSlot(i).getStack().isEmpty()){
                    if(handler.getSlot(i).getStack().getCount()<handler.getSlot(i).getMaxItemCount()){
                        //取得したアイテムに重ねられるアイテムのスロットを取得
                        int matchSlot=matchItem(handler.slots,i);
//#if MC >= 260100
//$$ if(matchSlot!=-1){
//$$     client.gameMode.handleContainerInput(handler.containerId, matchSlot, 0, net.minecraft.world.inventory.ContainerInput.PICKUP, client.player);
//$$     client.gameMode.handleContainerInput(handler.containerId, i, 0, net.minecraft.world.inventory.ContainerInput.PICKUP, client.player);
//$$     if(!handler.getCarried().isEmpty()){
//$$         client.gameMode.handleContainerInput(handler.containerId, matchSlot, 0, net.minecraft.world.inventory.ContainerInput.PICKUP, client.player);
//$$     } else {
//$$         i++;
//$$     }
//$$ }
//#else
                        if(matchSlot!=-1){
                            client.interactionManager.clickSlot(handler.syncId, matchSlot, 0, SlotActionType.PICKUP, client.player);
                            client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, client.player);
                            //カーソルにアイテムがあるならコンテナに戻す
                            if(!handler.getCursorStack().isEmpty()){
                                client.interactionManager.clickSlot(handler.syncId, matchSlot, 0, SlotActionType.PICKUP, client.player);
                                //まだそのアイテムがStack上限に達していないならもう一度同じ処理を行う
                                //たとえぴったりだった場合でも2つ目のifで弾かれるので問題ない
                            } else {
                                i++;
                            }
                        }
//#endif
                    }
                }
            }
            //コンテナの残量を計測する
            int slot=0,fillSlot=0;
            for(int i=0;i<=handler.slots.size()-37;i++){
                if(!handler.getSlot(i).getStack().isEmpty())fillSlot++;
                slot++;
            }
            //残量をProgressMeterに表示
            float pct = slot > 0 ? (float) fillSlot / slot : 0f;
            int barColor = pct > 0.75f ? 0xFF28B856
                         : pct > 0.50f ? 0xFF3A58B8
                         : pct > 0.25f ? 0xFFCCAA44
                         : 0xFFA04040;
            ProgressMeter.INSTANCE.show("AutoFill", pct, fillSlot + " / " + slot + " slots", barColor);
            //コンテナ画面を閉じる
//#if MC < 260200
            client.setScreen(null);
//#else
            //$$ client.gui.setScreen(null);
//#endif
        }
    }

    private static int matchItem(DefaultedList<Slot> slots, int slot){
        ItemStack targetItem = slots.get(slot).getStack();
        for(int i=0;i<slots.size()-36;i++){
            ItemStack inventoryItem=slots.get(i).getStack();
            if(ItemStack.canCombine(targetItem,inventoryItem)){
                return i;
            }
        }
        //見つからなかった場合
        return -1;
    }
}
