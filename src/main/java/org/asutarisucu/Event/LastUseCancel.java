package org.asutarisucu.Event;

import fi.dy.masa.malilib.util.InfoUtils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;
import org.asutarisucu.Configs.FeatureToggle;

public class LastUseCancel {
    public static void UseBlockEvents(){
        UseBlockCallback.EVENT.register(((playerEntity, world, hand, blockHitResult) -> {
            if(!FeatureToggle.LAST_USE_CANCEL.getBooleanValue()){
                return ActionResult.PASS;
            }else if(playerEntity.getMainHandStack().getCount()!=1){
                return ActionResult.PASS;
            }
            InfoUtils.printActionbarMessage("Cant Use LastItem");
            return ActionResult.FAIL;
        }));
    }
}
