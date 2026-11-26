package org.asutarisucu.mixin.SearchItems;

//#if MC < 260100
/**
 * Only needed from MC 26.1, where RenderType.create is package-private.
 * Left out of the mixin config before that.
 */
public final class MixinRenderTypeInvoker {
    private MixinRenderTypeInvoker() {}
}
//#else
//$$ import net.minecraft.client.renderer.rendertype.RenderSetup;
//$$ import net.minecraft.client.renderer.rendertype.RenderType;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//$$
//$$ @Mixin(RenderType.class)
//$$ public interface MixinRenderTypeInvoker {
//$$     @Invoker("create")
//$$     static RenderType invokeCreate(String name, RenderSetup setup) {
//$$         throw new AssertionError();
//$$     }
//$$ }
//#endif
