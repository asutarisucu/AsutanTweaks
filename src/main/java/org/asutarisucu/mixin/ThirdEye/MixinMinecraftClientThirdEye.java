package org.asutarisucu.mixin.ThirdEye;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor interface mixed into MinecraftClient to allow getting and setting
 * the framebuffer field used by ThirdEye to redirect rendering.
 */
@Mixin(MinecraftClient.class)
public interface MixinMinecraftClientThirdEye {
    @Accessor("framebuffer")
    Framebuffer thirdeye$getFramebuffer();

    @Accessor("framebuffer")
    void thirdeye$setFramebuffer(Framebuffer fb);
}
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import com.mojang.blaze3d.pipeline.RenderTarget;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$
//$$ @Mixin(Minecraft.class)
//$$ public interface MixinMinecraftClientThirdEye {
//$$     @Accessor("mainRenderTarget")
//$$     RenderTarget thirdeye$getFramebuffer();
//$$
//$$     @Accessor("mainRenderTarget")
//$$     void thirdeye$setFramebuffer(RenderTarget rt);
//$$ }
//#endif
