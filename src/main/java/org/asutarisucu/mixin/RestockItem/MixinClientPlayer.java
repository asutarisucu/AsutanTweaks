package org.asutarisucu.mixin.RestockItem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
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

            client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId,
                    0,1, SlotActionType.PICKUP_ALL,client.player
            );
            client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId,
                    1,1, SlotActionType.THROW,client.player
            );

//            if(user.getMainHandStack().getCount()<= Configs.Generic.RESTOCK_COUNT.getIntegerValue()){
//                int refillSlot= Inventorys.findMatchingItemStack(user.getInventory(),user.getMainHandStack());
//                if(refillSlot!=-1){
//                    Inventorys.refillHotbarSlot(user,user.getInventory().getStack(refillSlot));
//                }
//            }
        }
    }
}
