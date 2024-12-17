package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

import org.asutarisucu.Configs.FeatureToggle;

import org.joml.Matrix4f;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer<T extends ItemEntity> extends EntityRenderer<T> {

    protected MixinItemEntityRenderer(EntityRendererFactory.Context context){
        super(context);
    }

    @Shadow @Final private ItemRenderer itemRenderer;

    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at=@At("HEAD"), cancellable = true)
    private void onRender(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
        if(FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue()){
            ci.cancel();

            Camera camera=MinecraftClient.getInstance().gameRenderer.getCamera();
            ItemStack itemStack = itemEntity.getStack();
            BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.world, null, itemEntity.getId());
            matrixStack.push();
            matrixStack.multiplyPositionMatrix((new Matrix4f()).rotation(camera.getRotation()));
            this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
            matrixStack.pop();
            super.render((T) itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }
}
