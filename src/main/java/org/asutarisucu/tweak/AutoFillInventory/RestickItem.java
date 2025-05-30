package org.asutarisucu.tweak.AutoFillInventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.asutarisucu.Configs.FeatureToggle;

public class RestickItem {
    public static void restockItem(MinecraftClient client, Screen screen, int fromSlot){
        int toSlot=client.player.getInventory().getSlotWithStack(client.player.getMainHandStack())+36;
        ScreenHandler handler=client.player.currentScreenHandler;
        if ((screen == null && handler instanceof PlayerScreenHandler)) {
            client.interactionManager.clickSlot(handler.syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
            if(FeatureToggle.LAST_USE_CANCEL.getBooleanValue())client.interactionManager.clickSlot(handler.syncId, fromSlot, 1, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(handler.syncId, toSlot, 0, SlotActionType.PICKUP, client.player);
            if(!handler.getCursorStack().isEmpty())client.interactionManager.clickSlot(handler.syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
        }
    }
}
