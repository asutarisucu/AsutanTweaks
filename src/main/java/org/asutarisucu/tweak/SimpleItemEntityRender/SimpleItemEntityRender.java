package org.asutarisucu.tweak.SimpleItemEntityRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import org.asutarisucu.Utiles.Entity.ItemEntityUtil;
import org.asutarisucu.Utiles.Entity.MobEntityUtil;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

public class SimpleItemEntityRender {
    //レンダリングを抑制しているEntityのUUIDと原因のEntity
    public static WeakHashMap<UUID,Entity> EntityUUID=new WeakHashMap<>();
    public static void suppressRenderItem(ItemEntity item,CallbackInfo ci){
        List<Entity> nearItems= ItemEntityUtil.getItemEntity(item.getStack(),1,item.getPos());
        if(!nearItems.isEmpty()){
            if(shouldRender(nearItems, item))ci.cancel();
        }
    }
    public static void suppressRenderMob(MobEntity mob,CallbackInfo ci){
        List<Entity> nearMobs= MobEntityUtil.getMobEntity(mob,1,mob.getPos());
        if(!nearMobs.isEmpty()){
            if(shouldRender(nearMobs, mob))ci.cancel();
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
        }return false;
    }
}
