package org.asutarisucu.Utiles.Inventory;

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
                    }
                }
            }
            //コンテナの残量を計測する
            int slot=0,fillSlot=0;
            for(int i=0;i<=handler.slots.size()-37;i++){
                if(!handler.getSlot(i).getStack().isEmpty())fillSlot++;
                slot++;
            }
            //残量をアクションバーに表示
            String child=String.valueOf(fillSlot);
            String GreenTXT=GuiBase.TXT_GREEN;
            String RedTXT=GuiBase.TXT_RED;
            child=(fillSlot==0?GreenTXT:RedTXT)+child;
            String parent=GuiBase.TXT_GREEN+slot;
            InfoUtils.printActionbarMessage("Auto Restocked!!(%s/%s)",child,parent);
            //コンテナ画面を閉じる
            client.setScreen(null);
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
