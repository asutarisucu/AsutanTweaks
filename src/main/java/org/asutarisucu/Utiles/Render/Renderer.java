package org.asutarisucu.Utiles.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Renderer {
    public static ConcurrentHashMap<UUID,Integer> countMap=new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID,ItemEntity> entityMap=new ConcurrentHashMap<>();

    public static void renderCount(Camera camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate immediate=VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        for(Map.Entry<UUID,Integer> entry: countMap.entrySet()){
            if(entry.getValue()!=0){
                TextRenderer textRenderer = client.textRenderer;
                MatrixStack matrixStack = RenderSystem.getModelViewStack();
                matrixStack.multiplyPositionMatrix((new Matrix4f()).rotation(camera.getRotation()));

                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(true);
                //RenderSystem.applyModelViewMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                String text=String.valueOf(entry.getValue());
                Vec3d Pos=camera.getPos().subtract(entityMap.get(entry.getKey()).getPos());
                matrixStack.translate(Pos.x, Pos.y, Pos.z);

                Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                float h = (float) (-textRenderer.getWidth(text) / 2);

                textRenderer.draw(text, h, 0, -1, false, matrix4f,immediate, TextRenderer.TextLayerType.SEE_THROUGH, 1056964608, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                immediate.draw();
                textRenderer.draw(text, h, 0, -1, false, matrix4f, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                immediate.draw();
//            matrixStack.pop();

                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
                //RenderSystem.applyModelViewMatrix();
            }else{
                countMap.remove(entry.getKey());
                entityMap.remove(entry.getKey());
            }
        }
    }
}
