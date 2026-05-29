package org.asutarisucu.mixin.BlockUpdateViewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC < 260100
import net.minecraft.world.World;

/**
 * Exposes World.isClient as a settable field so BlockUpdateCalculator can
 * temporarily set it to false on the ClientWorld during simulation.
 * This causes blocks guarded by (!world.isClient) to run their server-side
 * neighborUpdate logic, enabling accurate redstone and observer propagation.
 */
@Mixin(World.class)
public interface MixinWorldIsClientAccessor {

    @Accessor("isClient")
    boolean asutantweaks_isClient();

    @Mutable
    @Accessor("isClient")
    void asutantweaks_setIsClient(boolean isClient);
}
//#else
//$$ import net.minecraft.world.level.Level;
//$$
//$$ @Mixin(Level.class)
//$$ public interface MixinWorldIsClientAccessor {
//$$
//$$     @Accessor("isClientSide")
//$$     boolean asutantweaks_isClient();
//$$
//$$     @Mutable
//$$     @Accessor("isClientSide")
//$$     void asutantweaks_setIsClient(boolean isClient);
//$$ }
//#endif
