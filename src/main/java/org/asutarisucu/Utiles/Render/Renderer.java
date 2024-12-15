package org.asutarisucu.Utiles.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleItemEntityRender;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Renderer {

    public static void renderCount(Camera camera) {
        //クライアントを取得
        MinecraftClient client = MinecraftClient.getInstance();
        //クライアントワールドを取得
        World world=client.world;
        //レンダラーを取得
        TextRenderer textRenderer = client.textRenderer;
        VertexConsumerProvider.Immediate immediate=client.getBufferBuilders().getEffectVertexConsumers();
        //RenderSystemの設定
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        MatrixStack matrixStack = RenderSystem.getModelViewStack();

        //抑制している原因と抑制リストへの出現回数のMap
        Map<Entity, Long> entityCountMap=SimpleItemEntityRender.EntityUUID.values().stream()
                .filter(entity -> !entity.isRemoved())
                .collect(Collectors.groupingBy(value->value,Collectors.counting()));
        //リストのEntityの数だけ実行
        for(Map.Entry<Entity,Long> entry: entityCountMap.entrySet()){
            //登録されているUUIDを持つentityが存在しなければレンダリングせず削除
            if(!entry.getKey().isRemoved()){
                //カウントのレンダリング準備
                String text=String.valueOf(entry.getValue()+1);
                //相対座標を取得
                Vec3d Pos=entry.getKey().getPos().subtract(camera.getPos());

                matrixStack.push();
                matrixStack.translate(Pos.x, Pos.y+0.5, Pos.z);
                matrixStack.multiplyPositionMatrix((new Matrix4f()).rotation(camera.getRotation()));
                matrixStack.scale(-0.025F, -0.025F, 0.025F);

                Matrix4f matrix4f = AffineTransformation.identity().getMatrix();
                float h = (float) (-textRenderer.getWidth(text) / 2);

                RenderSystem.applyModelViewMatrix();

                textRenderer.draw(text, h, 0, -1, false, matrix4f,immediate, TextRenderer.TextLayerType.SEE_THROUGH, 1056964608, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                immediate.draw();
                textRenderer.draw(text, h, 0, -1, false, matrix4f, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                immediate.draw();

                matrixStack.pop();
                RenderSystem.applyModelViewMatrix();
            }else{
                SimpleItemEntityRender.EntityUUID.entrySet().removeIf(entity-> entity.getValue()==entry.getKey());
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
