/*
 * This file is part of the TweakerMore project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * TweakerMore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TweakerMore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TweakerMore.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.asutarisucu.Utiles.tweakerMore;

import me.fallenbreath.tweakermore.mixins.tweaks.features.schematicProPlace.BlockItemAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class RestrictionUtils
{
    /**
     * Just a simple check. Not perfect, but it's safe enough
     */
    public static ItemStack getPlayerUsingStack(PlayerEntity player)
    {
        ItemStack stackToUse = player.getMainHandStack();
        if (stackToUse.isEmpty())
        {
            stackToUse = player.getOffHandStack();
        }
        return stackToUse;
    }

    /**
     * What will the block state be like if the player does the block placement
     */
    public static Optional<BlockState> getStateToPlace(ItemPlacementContext context, ItemStack stackToUse)
    {
        if (stackToUse.getItem() instanceof BlockItem)
        {
            // ref: net.minecraft.item.BlockItem.place(net.minecraft.item.ItemPlacementContext)
            BlockItem blockItem = (BlockItem) stackToUse.getItem();
            ItemPlacementContext ctx = blockItem.getPlacementContext(context);
            if (ctx != null)
            {
                return Optional.ofNullable(((BlockItemAccessor) blockItem).invokeGetPlacementState(ctx));
            }
        }
        return Optional.empty();
    }
}