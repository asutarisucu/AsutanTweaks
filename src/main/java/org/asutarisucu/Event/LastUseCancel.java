package org.asutarisucu.Event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.GUI.HudLogger;
import org.asutarisucu.lib.util.MessageUtils;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
//#else
//$$ import net.minecraft.world.InteractionResult;
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.core.registries.BuiltInRegistries;
//#endif

public class LastUseCancel {

//#if MC < 260100
    public static void UseBlockEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> {
            if (!Feature.LAST_USE_CANCEL.isEnabled()
                    || player.getMainHandStack().getCount() != 1
                    || player.getMainHandStack().getMaxCount() == 1
                    || player.getAbilities().creativeMode
                    || isBlockOnBlacklist(world.getBlockState(blockHitResult.getBlockPos()).getBlock())) {
                return ActionResult.PASS;
            }
            HudLogger.INSTANCE.log("Can't use last item");
            return ActionResult.FAIL;
        });
    }
//#else
//$$ public static void UseBlockEvents() {
//$$     UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> {
//$$         if (!Feature.LAST_USE_CANCEL.isEnabled()
//$$                 || player.getMainHandItem().getCount() != 1
//$$                 || player.getMainHandItem().getItem().getDefaultMaxStackSize() == 1
//$$                 || player.getAbilities().instabuild
//$$                 || isBlockOnBlacklist(world.getBlockState(blockHitResult.getBlockPos()).getBlock())) {
//$$             return InteractionResult.PASS;
//$$         }
//$$         MessageUtils.sendActionBar("Can't use last item");
//$$         return InteractionResult.FAIL;
//$$     });
//$$ }
//#endif

    private static boolean isBlockOnBlacklist(Block block) {
//#if MC < 260100
        return Configs.Generic.LAST_USE_CANCEL_BLACKLIST.getStrings()
                .contains(Registries.BLOCK.getId(block).getPath());
//#else
//$$ return Configs.Generic.LAST_USE_CANCEL_BLACKLIST.getStrings()
//$$         .contains(BuiltInRegistries.BLOCK.getKey(block).getPath());
//#endif
    }
}
