package org.asutarisucu.Utiles.Entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class MobEntityUtil {

    public static List<MobEntity> getMobEntity(MobEntity mob, double radius, Vec3d center) {
        ClientWorld world = MinecraftClient.getInstance().world;
        Box box = new Box(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
        return world.getEntitiesByClass(
                MobEntity.class,
                box,
                entity -> entity != mob && entity.getType() == mob.getType()
        );
    }
}
