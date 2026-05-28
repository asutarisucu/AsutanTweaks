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
//#endif

//#if MC < 260100
@Mixin(ClientPlayNetworkHandler.class)
//#endif
public class MixinClientPlayNbtResponse {

//#if MC < 260100
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
//#endif
}
