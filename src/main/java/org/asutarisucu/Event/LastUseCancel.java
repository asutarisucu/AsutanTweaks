package org.asutarisucu.Event;

import fi.dy.masa.malilib.util.InfoUtils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.FeatureToggle;

public class LastUseCancel {
    public static void UseBlockEvents(){
        //ブロック設置動作で特定の条件でキャンセルする
        UseBlockCallback.EVENT.register(((playerEntity, world, hand, blockHitResult) -> {
            if(!FeatureToggle.LAST_USE_CANCEL.getBooleanValue()||
                    playerEntity.getMainHandStack().getCount()!=1||
                    playerEntity.getMainHandStack().getMaxCount()==1||
                    playerEntity.getAbilities().creativeMode||
                    isBlockEntity(world.getBlockState(blockHitResult.getBlockPos()).getBlock())
            )return ActionResult.PASS;
            InfoUtils.printActionbarMessage("Cant Use LastItem");
            return ActionResult.FAIL;
        }));
    }
    private static boolean isBlockEntity(Block block){
        return Configs.Generic.LAST_USE_CANCEL_BLACKLIST.getStrings().contains(Registries.BLOCK.getId(block).toString().substring(10));
    }
}
