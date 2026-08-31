package org.asutarisucu.mixin.BlockUpdateViewer;

//#if MC < 260100
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNbtResponse {

    @Inject(method = "onBlockEntityUpdate", at = @At("HEAD"))
    private void asutantweaks_onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        BlockPos pos = packet.getPos();
        if (!(client.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock)) return;
        if (packet.getBlockEntityType() != BlockEntityType.SHULKER_BOX) {
            BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.add(pos.toImmutable());
        } else {
            BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.remove(pos.toImmutable());
        }
    }
}
//#else
//$$ import net.minecraft.world.level.block.ShulkerBoxBlock;
//$$ import net.minecraft.world.level.block.entity.BlockEntityType;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.multiplayer.ClientPacketListener;
//$$ import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
//$$ import net.minecraft.core.BlockPos;
//$$ import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateCalculator;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ @Mixin(ClientPacketListener.class)
//$$ public class MixinClientPlayNbtResponse {
//$$
//$$     @Inject(method = "handleBlockEntityData", at = @At("HEAD"), require = 0)
//$$     private void asutantweaks_onBlockEntityUpdate(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
//$$         var mc = Minecraft.getInstance();
//$$         if (mc.level == null) return;
//$$         BlockPos pos = packet.getPos();
//$$         if (!(mc.level.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock)) return;
//#if MC < 260200
//$$         if (packet.getType() != BlockEntityType.SHULKER_BOX) {
//#else
//$$         // MC 26.2 moved the BlockEntityType constants into BlockEntityTypes.
//$$         if (packet.getType() != net.minecraft.world.level.block.entity.BlockEntityTypes.SHULKER_BOX) {
//#endif
//$$             BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.add(pos.immutable());
//$$         } else {
//$$             BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.remove(pos.immutable());
//$$         }
//$$     }
//$$ }
//#endif
