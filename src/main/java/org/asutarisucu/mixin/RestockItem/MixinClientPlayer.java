package org.asutarisucu.mixin.RestockItem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.Utiles.Inventory.Inventorys;
import org.asutarisucu.tweak.AutoFillInventory.RestickItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayer {
    @Inject(method = "interactBlock",at=@At("HEAD"))
    private void UseItemAfter(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir){
        if(FeatureToggle.ITEM_RESTOCK.getBooleanValue()){
            MinecraftClient client=MinecraftClient.getInstance();
            Screen screen= client.currentScreen;
            ItemStack MainItem=player.getMainHandStack();
            if(MainItem.getCount()< Configs.Generic.RESTOCK_COUNT.getIntegerValue()
            &&MainItem.getMaxCount()!=1){
                int refillSlot=Inventorys.findMatchingItemStack(player.getInventory(),MainItem);
                if(refillSlot>0) RestickItem.restockItem(client,screen,refillSlot);
            }
        }
    }

    @Inject(method = "interactBlock",at=@At("TAIL"))
    private void UseUnstackbleItemAfter(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir){
        if(FeatureToggle.ITEM_RESTOCK.getBooleanValue()){
            MinecraftClient client=MinecraftClient.getInstance();
            Screen screen= client.currentScreen;
            ItemStack MainItem=player.getMainHandStack();
            if(MainItem.getCount()< Configs.Generic.RESTOCK_COUNT.getIntegerValue()
                    &&MainItem.getMaxCount()==1){
                int refillSlot=Inventorys.findMatchingItemStack(player.getInventory(),MainItem);
                if(refillSlot>0) RestickItem.restockItem(client,screen,refillSlot);
            }
        }
    }
}
