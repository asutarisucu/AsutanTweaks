package org.asutarisucu.Utiles.Entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;

import java.util.List;
import java.util.stream.Collectors;

public class ItemEntityUtil {

    public static List<Entity> getItemEntity(ItemStack stack, double radius, Vec3d center){
        ClientWorld world= MinecraftClient.getInstance().world;
        Box box=new Box(
                center.x-radius,center.y-radius,center.z-radius,
                center.x+radius,center.y+radius,center.z+radius
        );
        return world.getEntitiesByClass(
            Entity.class,
            box,
            entity ->!SimpleEntityRender.EntityUUID.containsKey(entity.getUuid())&&entity instanceof ItemEntity
        ).stream()
                .map(entity -> (ItemEntity) entity)// キャスト
                .filter(itemEntity -> itemEntity.getStack().getItem()==stack.getItem())
                .filter(itemEntity -> itemEntity.getStack()!=stack)
                .collect(Collectors.toList());
    }
}
