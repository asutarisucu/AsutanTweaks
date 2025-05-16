package org.asutarisucu.mixin.SearchItems;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.tweak.SearchItems.HighlightBlock;
import org.asutarisucu.tweak.SearchItems.HighlightContainer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=weather"))
    private void onRenderTail(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if(FeatureToggle.SEARCH_BLOCK_HIGHLIGHT.getBooleanValue()){
            HighlightBlock.renderHighlightBlock();
        }
        if(FeatureToggle.SEARCH_CONTAINER_HIGHLIGHT.getBooleanValue()){
            HighlightContainer.renderHighlightContainer();
        }
    }
}