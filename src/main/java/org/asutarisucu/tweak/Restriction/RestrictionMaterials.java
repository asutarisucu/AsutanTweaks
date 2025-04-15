package org.asutarisucu.tweak.Restriction;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.asutarisucu.Utiles.tweakerMore.RestrictionUtils;

import java.util.Optional;

public class RestrictionMaterials {
    public static Object[] getRestrictionMaterials(MinecraftClient client , ItemPlacementContext context){
        //stateToPlace
        PlayerEntity player= client.player;
        ItemStack stackToUse= RestrictionUtils.getPlayerUsingStack(player);
        Optional<BlockState> stateToPlaceOptional = RestrictionUtils.getStateToPlace(context, stackToUse);
        BlockState stateToPlace = stateToPlaceOptional.get();
        //schematicState
        BlockPos pos = context.getBlockPos();
        World schematicWorld = SchematicWorldHandler.getSchematicWorld();
        BlockState schematicState = schematicWorld.getBlockState(pos);
        //schematicBlock

        return new Object[]{stateToPlace,schematicState};
    }
}
