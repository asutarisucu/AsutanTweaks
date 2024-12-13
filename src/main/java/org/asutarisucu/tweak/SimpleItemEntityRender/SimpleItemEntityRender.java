package org.asutarisucu.tweak.SimpleItemEntityRender;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import org.asutarisucu.Utiles.Entity.ItemEntityUtil;
import org.asutarisucu.Utiles.Render.Renderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SimpleItemEntityRender {

    //レンダリングを抑制されてるアイテムのUUIDと実態
    public static Map<UUID, ItemEntity> suppressedItems = new HashMap<>();
    //レンダリングを抑制されてるアイテムと抑制している原因のアイテムのUUID
    public static final Map<UUID,UUID> ERmap=new HashMap<>();

    public static void shouldRender(ItemEntity itemEntity, CallbackInfo ci) {
        Vec3d itemPos = itemEntity.getPos();
        UUID id =itemEntity.getUuid();
        List<ItemEntity> nearItems= ItemEntityUtil.getItemEntity(itemEntity.getStack(),1,itemPos);
        int suppressCount=0;
        boolean flag=true;
        //周囲のItemEntityを取得
        for(ItemEntity item:nearItems) {
            if (item != itemEntity) {
                //既に非表示リストに入ってるかどうか
                if (!suppressedItems.containsKey(id)) {
                    //取得したアイテムが既に非表示リストに入っている場合比較しない
                    if (!suppressedItems.containsKey(item.getUuid())) {
                        //取得したアイテムのほうが先に生成されていた場合、非表示にする
                        if (itemEntity.getId() > item.getId()) {
                            //描画されていた場合カウント表示を非表示に
                            Renderer.countMap.remove(id);
                            Renderer.entityMap.remove(id);
                            //非表示リストに追加
                            suppressedItems.put(id, itemEntity);
                            //自分を非表示にしたItemEntityのUUIDとセットで保管
                            ERmap.put(id,item.getUuid());
                            ci.cancel();
                            flag=false;
                        }
                    }
                    //既に非表示の場合
                } else {
                    if (ERmap.containsKey(id) && ERmap.containsValue(item.getUuid())) {
                        //自分を非表示にした親がまだ存在する場合
                        Renderer.countMap.remove(id);
                        Renderer.entityMap.remove(id);
                        ci.cancel();
                        flag=false;
                        break;
                    }
                }
                suppressCount++;
            }
        }
        if(flag){
            //親が存在しない場合、親のカウントを削除する
            UUID deleteId=ERmap.get(id);
            if(deleteId!=null){
                Renderer.countMap.remove(deleteId);
                Renderer.entityMap.remove(deleteId);
                suppressedItems.remove(id);
                ERmap.remove(id);
            }
        }
        //表示するのでカウンタを準備
        Renderer.countMap.put(id,suppressCount);
        Renderer.entityMap.put(id,itemEntity);
    }
}
