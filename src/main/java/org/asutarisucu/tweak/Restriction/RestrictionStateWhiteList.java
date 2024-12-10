package org.asutarisucu.tweak.Restriction;

import com.google.common.collect.Lists;
import fi.dy.masa.malilib.util.InfoUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Property;
import net.minecraft.state.property.Properties;

import org.asutarisucu.Configs.Configs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;

public class RestrictionStateWhiteList {
    public static void SchematicRestrictionWhiteList(MinecraftClient client , ItemPlacementContext context, CallbackInfoReturnable<Boolean> cir){
        Object[] restrictionMaterials=RestrictionMaterials.getRestrictionMaterials(client,context);
        BlockState stateToPlace=(BlockState) restrictionMaterials[0];
        BlockState schematicState=(BlockState) restrictionMaterials[1];

        //ConfigからPropertyのリストを作成
        List<Property<?>> PROPERTIES=Lists.newArrayList();
        for (String state: Configs.Generic.RESTRICTION_STATE_WHITELIST.getStrings()){
            Property<?> property=getPropertyByName(state);
            if(property!=null)PROPERTIES.add(property);
        }

        for (Property<?> property :PROPERTIES) {
            if (stateToPlace.contains(property) && schematicState.contains(property) && stateToPlace.get(property) != schematicState.get(property)) {
                InfoUtils.printActionbarMessage("wrongBlockState:"+property.getName().toUpperCase());
                cir.setReturnValue(false);
            }
        }
    }
    private static Property<?> getPropertyByName(String fieldName) {
        try {
            // Propertiesクラスからフィールドを取得
            Field field = Properties.class.getDeclaredField(fieldName);

            // フィールドがProperty型か確認
            if (Property.class.isAssignableFrom(field.getType())) {
                return (Property<?>) field.get(null); // 静的フィールドなのでnullを渡す
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Error accessing field: " + fieldName);
        }
        return null; // フィールドが見つからない場合
    }
}