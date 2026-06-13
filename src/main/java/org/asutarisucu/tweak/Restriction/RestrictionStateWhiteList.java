package org.asutarisucu.tweak.Restriction;

import com.google.common.collect.Lists;
import org.asutarisucu.GUI.HudLogger;
import org.asutarisucu.lib.util.MessageUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Property;

import org.asutarisucu.Configs.Configs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class RestrictionStateWhiteList {
    public static void SchematicRestrictionWhiteList(MinecraftClient client , ItemPlacementContext context, CallbackInfoReturnable<Boolean> cir){
        Object[] restrictionMaterials=RestrictionMaterials.getRestrictionMaterials(client,context);
        BlockState stateToPlace=(BlockState) restrictionMaterials[0];
        BlockState schematicState=(BlockState) restrictionMaterials[1];

        //ConfigからPropertyのリストを作成
        List<Property<?>> PROPERTIES=Lists.newArrayList();
        for (String state: Configs.Generic.RESTRICTION_STATE_WHITELIST.getStrings()){
            Property<?> property= org.asutarisucu.Utiles.Block.BlockState.getProperty(state);
            if(property!=null)PROPERTIES.add(property);
        }

        for (Property<?> property :PROPERTIES) {
            if (stateToPlace.contains(property) && schematicState.contains(property) && stateToPlace.get(property) != schematicState.get(property)) {
                String message = "wrongBlockState:" + property.getName().toUpperCase();
                switch (Configs.Generic.RESTRICTION_WHITELIST_MESSAGE_TYPE.getValue()) {
                    case LOG       -> HudLogger.INSTANCE.log(message);
                    case ACTIONBAR -> MessageUtils.sendActionBar(message);
                    case NONE      -> {}
                }
                cir.setReturnValue(false);
            }
        }
    }
}
