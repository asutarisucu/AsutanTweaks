package org.asutarisucu.mixin.SimpleItemEntityRender;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
//#if MC < 260200
import net.minecraft.client.render.VertexConsumerProvider;
//#endif
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
//#if MC < 12111
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
//#endif
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
//#if MC >= 260100
//$$ public abstract class MixinItemEntityRenderer extends net.minecraft.client.renderer.entity.EntityRenderer<net.minecraft.world.entity.item.ItemEntity, net.minecraft.client.renderer.entity.state.ItemEntityRenderState> {
//$$
//$$     protected MixinItemEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) { super(context); }
//$$
//#elseif MC >= 12111
//$$ public abstract class MixinItemEntityRenderer extends net.minecraft.client.render.entity.EntityRenderer<net.minecraft.entity.ItemEntity, net.minecraft.client.render.entity.state.ItemEntityRenderState> {
//$$
//$$     protected MixinItemEntityRenderer(net.minecraft.client.render.entity.EntityRendererFactory.Context context) { super(context); }
//$$
//#else
public abstract class MixinItemEntityRenderer<T extends ItemEntity> extends EntityRenderer<T> {

    protected MixinItemEntityRenderer(EntityRendererFactory.Context context){
        super(context);
    }

    @Shadow @Final private ItemRenderer itemRenderer;
    @SuppressWarnings("unchecked")
    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at=@At("HEAD"), cancellable = true)
    private void onRender(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
        if(FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue()){
            //元のrenderメソッドをキャンセル
            ci.cancel();
            //アイテムが動かないようなrenderを実装
            Camera camera=MinecraftClient.getInstance().gameRenderer.getCamera();
            ItemStack itemStack = itemEntity.getStack();
            BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.getWorld(), null, itemEntity.getId());
            matrixStack.push();
            matrixStack.multiplyPositionMatrix((new Matrix4f()).rotation(camera.getRotation()));
            this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
            matrixStack.pop();
            //EntityRendererのrenderを呼び出す(これがないとMixinEntityRendererに刺してるコードが動かない)
            super.render((T) itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }
//#endif
}
