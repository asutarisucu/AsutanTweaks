package org.asutarisucu.mixin.SearchItems;

// Search highlight rendering is now done via WorldRenderEvents.AFTER_ENTITIES in Reference.java
// for all versions. This mixin is no longer needed.
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
}
