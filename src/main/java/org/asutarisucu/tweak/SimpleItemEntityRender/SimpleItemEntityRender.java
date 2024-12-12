package org.asutarisucu.tweak.SimpleItemEntityRender;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import org.asutarisucu.Utiles.Entity.ItemEntityUtil;
import org.asutarisucu.Utiles.Render.Renderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SimpleItemEntityRender {

    private static final Map<UUID, ItemEntity> suppressedItems = new HashMap<>();

    public static void shouldRender(ItemEntity itemEntity, CallbackInfo ci,MatrixStack matrixStack, VertexConsumerProvider vertexConsumers) {
        Vec3d itemPos = itemEntity.getPos();
        UUID id =itemEntity.getUuid();
        List<ItemEntity> nearItems= ItemEntityUtil.getItemEntity(itemEntity.getStack(),1,itemPos);
        if(!suppressedItems.containsKey(id)){
            int suppressCount=0;
            for(ItemEntity item:nearItems){
                if (!suppressedItems.containsKey(item.getUuid())){
                    if(itemEntity.getId()<item.getId()){
                        suppressedItems.put(item.getUuid(),item);
                        ci.cancel();
                        break;
                    }
                }
                suppressCount++;
            }
            Renderer.entityMap.put(suppressCount,itemEntity);
        }else ci.cancel();
    }
}
