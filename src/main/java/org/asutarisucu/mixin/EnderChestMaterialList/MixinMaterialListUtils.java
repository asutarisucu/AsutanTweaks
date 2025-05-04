package org.asutarisucu.mixin.EnderChestMaterialList;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.malilib.util.ItemType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.tweak.EnderChestMaterialList.EnderChestCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(MaterialListUtils.class)
public class MixinMaterialListUtils {
    @Inject(method = "getMaterialList",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lfi/dy/masa/litematica/materials/MaterialListUtils;getInventoryItemCounts(Lnet/minecraft/inventory/Inventory;)Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;",
                    ordinal = 0
            )
    )
    private static void addEnderchestItemCount(
            Object2IntOpenHashMap<BlockState> countsTotal,
            Object2IntOpenHashMap<BlockState> countsMissing,
            Object2IntOpenHashMap<BlockState> countsMismatch,
            PlayerEntity player,
            CallbackInfoReturnable<List<MaterialListEntry>> cir,
            @Local(ordinal = 3) Object2IntOpenHashMap<ItemType> itemTypesTotal
    ) {
        Object2IntOpenHashMap<ItemType> enderChestItems = EnderChestCache.getEnderChestItems(player);
        if (enderChestItems != null) {
//            enderChestItems.forEach(itemTypesTotal::addTo);
        }
    }

    @Inject(method = "updateAvailableCounts", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectUpdateAvailableCounts(
            List<MaterialListEntry> list,
            PlayerEntity player,
            CallbackInfo ci,
            Object2IntOpenHashMap<ItemType> playerInvItems
    ) {
        Object2IntOpenHashMap<ItemType> enderChestItems = EnderChestCache.getEnderChestItems();
        if(enderChestItems!=null){
            enderChestItems.forEach(playerInvItems::addTo);
        }
        AsutanTweaks.LOGGER.info(playerInvItems.toString());
    }

}
