package org.asutarisucu.Utiles.Entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleItemEntityRender;

import java.util.List;
import java.util.stream.Collectors;

public class MobEntityUtil {

    public static List<Entity> getMobEntity(MobEntity mob, double radius, Vec3d center){
        ClientWorld world= MinecraftClient.getInstance().world;
        Box box=new Box(
                center.x-radius,center.y-radius,center.z-radius,
                center.x+radius,center.y+radius,center.z+radius
        );
        return  world.getEntitiesByClass(
                Entity.class,
                box,
                entity ->!SimpleItemEntityRender.EntityUUID.containsKey(mob.getUuid())&&entity instanceof MobEntity&&entity!=mob
        ).stream()
                .map(entity -> (MobEntity) entity)// キャスト
                .filter(mobEntity -> mobEntity.getType()==mob.getType())
                .filter(mobEntity->mobEntity!=mob)
                .collect(Collectors.toList());
    }
}
