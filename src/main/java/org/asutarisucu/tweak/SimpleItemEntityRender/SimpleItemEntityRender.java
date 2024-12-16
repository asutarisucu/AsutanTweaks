package org.asutarisucu.tweak.SimpleItemEntityRender;

import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;

import org.asutarisucu.Utiles.Entity.ItemEntityUtil;
import org.asutarisucu.Utiles.Entity.MobEntityUtil;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

public class SimpleItemEntityRender {
    //レンダリングを抑制しているEntityのUUIDと原因のEntity
    public static WeakHashMap<UUID,Entity> EntityUUID=new WeakHashMap<>();

    public static boolean checkSuppressed(Entity entity){
        //抑制リストに既に存在するか
        if (SimpleItemEntityRender.EntityUUID.containsKey(entity.getUuid())) {
            //抑制の原因は近くに存在するか
            if (SimpleItemEntityRender.EntityUUID.get(entity.getUuid()).getPos().distanceTo(entity.getPos()) > 1) {
                //存在しないなら抑制リストから削除
                SimpleItemEntityRender.EntityUUID.remove(entity.getUuid());
            }
            return false;
        }else return true;
    }

    public static void suppressRenderItem(ItemEntity item,CallbackInfo ci){
        List<Entity> nearItems= ItemEntityUtil.getItemEntity(item.getStack(),1,item.getPos());
        if(SimpleItemEntityRender.EntityUUID.containsKey(item.getUuid())){
            //抑制の原因が近くにいないならリストから削除
            if(nearItems.isEmpty())EntityUUID.remove(item.getUuid());
            //まだいるならレンダリングを停止
            ci.cancel();
        }else{
            if(!nearItems.isEmpty()){
                if(shouldRender(nearItems,item))ci.cancel();
            }
        }
    }
    public static void suppressRenderMob(MobEntity mob,CallbackInfo ci){
        //近くのMobを取得
        List<Entity> nearMobs= MobEntityUtil.getMobEntity(mob,1,mob.getPos());
        if (SimpleItemEntityRender.EntityUUID.containsKey(mob.getUuid())){
            //抑制の原因が近くにいないならリストから削除
            if(nearMobs.isEmpty())EntityUUID.remove(mob.getUuid());
            //まだいるならレンダリングを停止
            ci.cancel();
        }else{
            if(!nearMobs.isEmpty()){
                if(shouldRender(nearMobs,mob))ci.cancel();
            }
        }


    }
    public static boolean shouldRender(List<Entity> entities,Entity entity) {
        for(Entity nearEntity:entities){
            //近くのアイテムとどちらが先に生成されたか
            if(entity.getId()>nearEntity.getId()){
                //近くのアイテムのほうが先に生成されていた場合
                //自分を抑制リストに追加する
                EntityUUID.put(entity.getUuid(),nearEntity);
                //自分が抑制していたものを近くのアイテムが抑制していることにする
                EntityUUID.entrySet().stream()
                        .filter(entry -> entry.getValue()==entity)
                        .forEach(entry ->EntityUUID.put(entry.getKey(),nearEntity));
                //レンダリングをキャンセル
                return true;
            }
        }
        //カウントのレンダリング
        return false;
    }
}