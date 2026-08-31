package org.asutarisucu.mixin.SimpleItemEntityRender;

//#if MC < 12111
/**
 * Not used before MC 1.21.11. There the hitbox is drawn inside
 * EntityRenderDispatcher.render, which MixinEntityRenderDispatcher already
 * cancels, so this class is left out of the mixin config for those versions.
 */
public final class MixinEntityHitboxDebugRenderer {
    private MixinEntityHitboxDebugRenderer() {}
}
//#elseif MC < 260100
//$$ import org.asutarisucu.Configs.FeatureToggle;
//$$ import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
//$$ import net.minecraft.entity.Entity;
//$$ import net.minecraft.entity.ItemEntity;
//$$ import net.minecraft.entity.mob.MobEntity;
//$$
//$$ /**
//$$  * Hides the F3+B hitbox of entities merged away by Simple Entity Render.
//$$  *
//$$  * From MC 1.21.11 the hitbox is no longer drawn by EntityRenderDispatcher —
//$$  * it is emitted separately from the debug renderer — so cancelling the entity
//$$  * render leaves the box behind.
//$$  */
//$$ @Mixin(EntityHitboxDebugRenderer.class)
//$$ public class MixinEntityHitboxDebugRenderer {
//$$
//$$     @Inject(method = "drawHitbox", at = @At("HEAD"), cancellable = true)
//$$     private void asutantweaks$hideSuppressedHitbox(Entity entity, float tickDelta, boolean server, CallbackInfo ci) {
//$$         if (!SimpleEntityRender.isSuppressed(entity.getId())) return;
//$$         if (entity instanceof ItemEntity) {
//$$             if (FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue()) ci.cancel();
//$$         } else if (entity instanceof MobEntity) {
//$$             if (FeatureToggle.SIMPLE_MOB_ENTITY_RENDER.getBooleanValue()) ci.cancel();
//$$         }
//$$     }
//$$ }
//#else
//$$ import org.asutarisucu.Configs.FeatureToggle;
//$$ import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.entity.Mob;
//$$ import net.minecraft.world.entity.item.ItemEntity;
//$$
//$$ /**
//$$  * Hides the F3+B hitbox of entities merged away by Simple Entity Render.
//$$  *
//$$  * From MC 1.21.11 the hitbox is no longer drawn by EntityRenderDispatcher —
//$$  * it is emitted separately from the debug renderer — so cancelling the entity
//$$  * render leaves the box behind.
//$$  */
//$$ @Mixin(EntityHitboxDebugRenderer.class)
//$$ public class MixinEntityHitboxDebugRenderer {
//$$
//$$     @Inject(method = "showHitboxes", at = @At("HEAD"), cancellable = true)
//$$     private void asutantweaks$hideSuppressedHitbox(Entity entity, float tickDelta, boolean server, CallbackInfo ci) {
//$$         if (!SimpleEntityRender.isSuppressed(entity.getId())) return;
//$$         if (entity instanceof ItemEntity) {
//$$             if (FeatureToggle.SIMPLE_ITEM_ENTITY_RENDER.getBooleanValue()) ci.cancel();
//$$         } else if (entity instanceof Mob) {
//$$             if (FeatureToggle.SIMPLE_MOB_ENTITY_RENDER.getBooleanValue()) ci.cancel();
//$$         }
//$$     }
//$$ }
//#endif
