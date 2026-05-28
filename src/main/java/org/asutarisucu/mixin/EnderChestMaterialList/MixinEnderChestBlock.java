package org.asutarisucu.mixin.EnderChestMaterialList;

//#if MC >= 260100
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.world.level.block.EnderChestBlock;
//$$ import net.minecraft.world.entity.player.Player;
//$$ import net.minecraft.world.InteractionResult;
//$$ import net.minecraft.world.phys.BlockHitResult;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.Level;
//#else
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//#endif
import org.asutarisucu.tweak.EnderChestMaterialList.EnderChestInteraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderChestBlock.class)
public class MixinEnderChestBlock {
//#if MC >= 260100
//$$    @Inject(method = "useWithoutItem", at = @At(value = "HEAD"))
//$$    protected void injectOnUse(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
//#else
    @Inject(method = "onUse", at = @At(value = "HEAD"))
    protected void injectOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
//#endif
        EnderChestInteraction.open();
    }
}
