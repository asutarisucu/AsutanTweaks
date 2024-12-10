package org.asutarisucu.mixin.SchematicRestrictionAddon;

import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.config.options.TweakerMoreConfigBooleanHotkeyed;
import me.fallenbreath.tweakermore.impl.features.schematicProPlace.restrict.PlacementRestrictor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.hit.BlockHitResult;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.tweak.Restriction.RestrictionStateWhiteList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacementRestrictor.class)
public class MixinPlacementRestrictor {
    @Unique
    private static boolean inCHECK_FACING=false;
    @Redirect(method = "canDoBlockPlacement",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/fallenbreath/tweakermore/config/options/TweakerMoreConfigBooleanHotkeyed;getBooleanValue()Z"
            ))
    private static boolean isInCHECK_FACING(TweakerMoreConfigBooleanHotkeyed instance){
        if(instance== TweakerMoreConfigs.SCHEMATIC_BLOCK_PLACEMENT_RESTRICTION_CHECK_FACING){
            inCHECK_FACING=true;
        }else inCHECK_FACING=false;
        return instance.getBooleanValue();
    }
    @Inject(
            method = "canDoBlockPlacement",
            at= @At(value = "INVOKE",
                    target = "Lme/fallenbreath/tweakermore/config/options/TweakerMoreConfigBooleanHotkeyed;getBooleanValue()Z",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true)
    private static void onAfterCheckFacing(MinecraftClient mc, BlockHitResult hitResult, ItemPlacementContext context, CallbackInfoReturnable<Boolean> cir){
        if(inCHECK_FACING&& FeatureToggle.SCHEMATIC_RESTRICTION_STATE_WHITELIST.getBooleanValue()){
            RestrictionStateWhiteList.SchematicRestrictionWhiteList(mc,context,cir);
        }
    }
}
