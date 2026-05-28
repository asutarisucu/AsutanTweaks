package org.asutarisucu.mixin.SimpleItemEntityRender;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.ItemEntity;

// Merge cleanup is handled implicitly by the tick-based suppression rebuild in SimpleEntityRender.
@Mixin(ItemEntity.class)
public class MixinItemEntity {
}
