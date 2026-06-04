package org.asutarisucu.lib.util;

//#if MC < 12005
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
//#elseif MC < 260100
//$$ import net.minecraft.component.DataComponentTypes;
//$$ import net.minecraft.item.ItemStack;
//#else
//$$ import net.minecraft.core.component.DataComponents;
//$$ import net.minecraft.world.item.ItemStack;
//#endif

public class ShulkerBoxUtil {

    public static boolean hasItems(ItemStack stack) {
        if (stack.isEmpty()) return false;
//#if MC < 12005
        NbtCompound tag = stack.getNbt();
        if (tag == null) return false;
        NbtCompound blockEntity = tag.getCompound("BlockEntityTag");
        NbtList items = blockEntity.getList("Items", 10);
        return !items.isEmpty();
//#elseif MC < 260100
//$$ var container = stack.get(DataComponentTypes.CONTAINER);
//$$ if (container == null) return false;
//$$ return container.stream().anyMatch(s -> !s.isEmpty());
//#else
//$$ var container = stack.get(DataComponents.CONTAINER);
//$$ if (container == null) return false;
//$$ return container.nonEmptyItemCopyStream().findAny().isPresent();
//#endif
    }
}
