package org.asutarisucu.tweak.AutoFillInventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
//#if MC >= 260100
//$$ import net.minecraft.world.inventory.InventoryMenu;
//$$ import net.minecraft.world.inventory.AbstractContainerMenu;
//#else
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
//#endif
import org.asutarisucu.Configs.FeatureToggle;

public class RestickItem {
    public static void restockItem(MinecraftClient client, Screen screen, int fromSlot){
        int toSlot=client.player.getInventory().getSlotWithStack(client.player.getMainHandStack())+36;
//#if MC >= 260100
//$$ net.minecraft.world.inventory.AbstractContainerMenu handler=client.player.containerMenu;
//$$ if (screen == null && handler instanceof net.minecraft.world.inventory.InventoryMenu) {
//$$     client.gameMode.handleContainerInput(handler.containerId, fromSlot, 0, net.minecraft.world.inventory.ContainerInput.PICKUP, client.player);
//$$     if(FeatureToggle.LAST_USE_CANCEL.getBooleanValue())client.gameMode.handleContainerInput(handler.containerId, fromSlot, 1, net.minecraft.world.inventory.ContainerInput.PICKUP, client.player);
//$$     client.gameMode.handleContainerInput(handler.containerId, toSlot, 0, net.minecraft.world.inventory.ContainerInput.PICKUP, client.player);
//$$     if(!handler.getCarried().isEmpty())client.gameMode.handleContainerInput(handler.containerId, fromSlot, 0, net.minecraft.world.inventory.ContainerInput.PICKUP, client.player);
//$$ }
//#else
        ScreenHandler handler=client.player.currentScreenHandler;
        if ((screen == null && handler instanceof PlayerScreenHandler)) {
            client.interactionManager.clickSlot(handler.syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
            if(FeatureToggle.LAST_USE_CANCEL.getBooleanValue())client.interactionManager.clickSlot(handler.syncId, fromSlot, 1, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(handler.syncId, toSlot, 0, SlotActionType.PICKUP, client.player);
            if(!handler.getCursorStack().isEmpty())client.interactionManager.clickSlot(handler.syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
        }
//#endif
    }
}
