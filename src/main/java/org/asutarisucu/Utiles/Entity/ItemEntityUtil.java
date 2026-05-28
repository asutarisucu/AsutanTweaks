package org.asutarisucu.Utiles.Entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ItemEntityUtil {

    public static List<ItemEntity> getItemEntity(ItemStack stack, double radius, Vec3d center) {
        ClientWorld world = MinecraftClient.getInstance().world;
        Box box = new Box(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
        return world.getEntitiesByClass(
                ItemEntity.class,
                box,
                entity -> entity.getStack().getItem() == stack.getItem()
                       && entity.getStack() != stack
        );
    }
}
