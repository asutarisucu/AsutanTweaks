package org.asutarisucu.Event;

import fi.dy.masa.malilib.util.InfoUtils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;
import org.asutarisucu.Configs.FeatureToggle;

public class LastUseCancel {
    public static void UseBlockEvents(){
        //ブロック設置動作で特定の条件でキャンセルする
        UseBlockCallback.EVENT.register(((playerEntity, world, hand, blockHitResult) -> {
            if(!FeatureToggle.LAST_USE_CANCEL.getBooleanValue()){
                return ActionResult.PASS;
            }
            if(playerEntity.getMainHandStack().getCount()!=1||playerEntity.getMainHandStack().getMaxCount()==1){
                return ActionResult.PASS;
            }
            if(playerEntity.getAbilities().creativeMode){
                return  ActionResult.PASS;
            }
            InfoUtils.printActionbarMessage("Cant Use LastItem");
            return ActionResult.FAIL;
        }));
    }
}
