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

import java.util.Map;

public class Renderer {
    public static Map<Integer, ItemEntity> entityMap;

    public static void renderCount(Camera camera) {
        MinecraftClient client = MinecraftClient.getInstance();

        VertexConsumerProvider.Immediate immediate=VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        TextRenderer textRenderer = client.textRenderer;
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.multiplyPositionMatrix((new Matrix4f()).rotation(camera.getRotation()));
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        //RenderSystem.applyModelViewMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix4f = AffineTransformation.identity().getMatrix();

        for(Map.Entry<Integer, ItemEntity> entry:entityMap.entrySet()){
            String text = String.valueOf(entry.getKey());
            Vec3d blockPos = entry.getValue().getPos();
            matrixStack.translate(blockPos.x, blockPos.y, blockPos.z);

            float h = (float) (-textRenderer.getWidth(text)/ 2);
            textRenderer.draw(
                    text,
                    h,
                    0,
                    -1,
                    false,
                    matrix4f,
                    immediate,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    1056964608,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE);
            immediate.draw();
            textRenderer.draw(text, h, 0, -1, false, matrix4f, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            immediate.draw();
            matrixStack.pop();

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }
}
