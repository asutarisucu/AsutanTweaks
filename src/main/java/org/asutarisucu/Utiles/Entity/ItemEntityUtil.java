package org.asutarisucu.Utiles.Entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ItemEntityUtil {

    public static List<ItemEntity> getItemEntity(ItemStack stack, double radius, Vec3d center){
        ClientWorld world= MinecraftClient.getInstance().world;
        return StreamSupport.stream(world.getEntities().spliterator(), false) // IterableをStreamに変換
                .filter(entity -> entity instanceof net.minecraft.entity.ItemEntity) // ItemEntityのみ
                .map(entity -> (net.minecraft.entity.ItemEntity) entity)             // キャスト
                .filter(itemEntity -> itemEntity.getPos().isInRange(center, radius)) // 距離チェック
                .collect(Collectors.toList());
    }


}
