package org.asutarisucu.Utiles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.asutarisucu.Configs.Configs;

public class PlayerUtils {
    public static void safeVoidDive(String dim){
        MinecraftClient client=MinecraftClient.getInstance();
        PlayerEntity player=client.player;
        if(player!=null){
            double height=-300;
            double playerHeight=player.getY();
            switch (dim){
                case "minecraft:overworld"->height= Configs.Generic.VOID_HEIGHT_OW.getDoubleValue();
                case "minecraft:the_nether"->height=Configs.Generic.VOID_HEIGHT_NE.getDoubleValue();
                case "minecraft:the_end"->height=Configs.Generic.VOID_HEIGHT_END.getDoubleValue();
            }
            if (playerHeight<height){
                int RocketSlot=findRocketSlot(client);
                if(RocketSlot!=-1&&player.getEquippedStack(EquipmentSlot.CHEST).getItem()==Items.ELYTRA){
                    client.options.jumpKey.setPressed(!player.isFallFlying());
                    Screen screen=client.currentScreen;
                    ScreenHandler handler=player.currentScreenHandler;
                    int mainSlot=player.getInventory().selectedSlot;
                    if(screen==null&&handler instanceof PlayerScreenHandler&&player.getVelocity().y<0){
                        client.interactionManager.clickSlot(handler.syncId,RocketSlot,mainSlot, SlotActionType.SWAP,player );
                        player.setPitch(-90);
                        client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                    }
                }else if(Configs.Generic.VOID_DISCONNECT.getBooleanValue())disconnectFromServer(client);
            }
        }
    }
    private static int findRocketSlot(MinecraftClient client){
        if(client.player!=null){
            ScreenHandler handler=client.player.currentScreenHandler;
            for(Slot slot:handler.slots){
                if(slot.getStack().getItem()== Items.FIREWORK_ROCKET)return slot.id;
            }
        }
        return -1;
    }
    private static void disconnectFromServer(MinecraftClient client) {
        int[] pos ={(int) client.player.getPos().x,
                (int) client.player.getPos().y,
                (int) client.player.getPos().z};
        // サーバーに接続中の場合
        if (client.getNetworkHandler() != null) {
            ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
            networkHandler.getConnection().disconnect(Text.of(String.format("Disconnect from %d,%d,%d",pos[0],pos[1],pos[2])));
        }
        // レンダースレッドで画面遷移を行う
        client.execute(() -> {
            // タイトル画面に遷移
            client.setScreen(new TitleScreen());
        });
    }

}
