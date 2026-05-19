package org.asutarisucu.Utiles.Render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
//#if MC < 260100
import net.minecraft.client.render.LightmapTextureManager;
//#endif
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class Renderer {

    public static void renderCount(Entity entity,MatrixStack matrices,VertexConsumerProvider vertexConsumers,TextRenderer textRenderer,String text) {
        MinecraftClient client=MinecraftClient.getInstance();
        Camera camera=client.gameRenderer.getCamera();
        //クライアントのカメラの動きについてくるようにカメラのクオータニオンを加工
        Quaternionf quaternionf=new Quaternionf(
                -camera.getRotation().x,
                -camera.getRotation().y,
                camera.getRotation().z,
                camera.getRotation().w
        );
        float h = (float) (-textRenderer.getWidth(text) / 2);


        //マトリックスを変更していく
        matrices.push();
        //高さを変更
        matrices.translate(0,entity.getBoundingBox().getYLength()+0.5,0);
        //大きさを指定
        matrices.scale(-0.025F, -0.025F, 0.025F);
        //回転を指定
        matrices.multiplyPositionMatrix((new Matrix4f()).rotation(quaternionf));
        //描画
//#if MC >= 260100
//$$ textRenderer.drawInBatch(text, h, 0, -1, false, matrices.last().pose(), vertexConsumers, Font.DisplayMode.SEE_THROUGH, 1056964608, 0xF000F0);
//$$ textRenderer.drawInBatch(text, h, 0, -1, false, matrices.last().pose(), vertexConsumers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
//#else
        textRenderer.draw(text, h, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 1056964608, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        textRenderer.draw(text, h, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
//#endif
        //マトリックスを変更前に戻して終了
        matrices.pop();
    }
}
