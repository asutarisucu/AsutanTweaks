package org.asutarisucu.mixin.RestockItem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import org.asutarisucu.Configs.FeatureToggle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(BlockItem.class)
public abstract class MixinClientPlayer {
    @Inject(method = "useOnBlock",at=@At("TAIL"))
    private void UseItem(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir){
        if(FeatureToggle.ITEM_RESTOCK.getBooleanValue()){
            MinecraftClient client=MinecraftClient.getInstance();

            Screen screen= client.currentScreen;
            ScreenHandler handler=client.player.currentScreenHandler;
            if (screen != null && !(handler instanceof PlayerScreenHandler && !(screen instanceof InventoryScreen))) {
                HandledScreen<? extends ScreenHandler> handledScreen = (HandledScreen<? extends ScreenHandler>) screen;
                ((MixinScreen) handledScreen).throw_items$onMouseClick(handledScreen.getScreenHandler().getSlot(0), 0, 0, SlotActionType.PICKUP);
                ((MixinScreen) handledScreen).throw_items$onMouseClick(handledScreen.getScreenHandler().getSlot(1), 1, 0, SlotActionType.PICKUP);
            }
        }
    }
}
