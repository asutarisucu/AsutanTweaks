package org.asutarisucu.Utiles.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Renderer {
    public static ConcurrentHashMap<UUID,Integer> countMap=new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID,ItemEntity> entityMap=new ConcurrentHashMap<>();

    public static void renderCount(Camera camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate immediate=client.getBufferBuilders().getEffectVertexConsumers();

        TextRenderer textRenderer = client.textRenderer;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        MatrixStack matrixStack = RenderSystem.getModelViewStack();

        for(Map.Entry<UUID,Integer> entry: countMap.entrySet()){
            //登録されているUUIDを持つentityが存在しなければレンダリングせず削除
            if(entry.getValue()!=0&&!entityMap.get(entry.getKey()).isRemoved()){
                String text=String.valueOf(entry.getValue()+1);
                Vec3d Pos=entityMap.get(entry.getKey()).getPos().subtract(camera.getPos());

                matrixStack.push();
                matrixStack.translate(Pos.x, Pos.y, Pos.z);
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
                countMap.remove(entry.getKey());
                entityMap.remove(entry.getKey());
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
