package org.asutarisucu.mixin.WorldEditGUI;

//#if MC < 12002
import org.asutarisucu.tweak.WorldEditGUI.WorldEditCui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

/**
 * Taps the worldedit:cui plugin channel.
 *
 * Before MC 1.20.2 the packet always carries the raw buffer, so the channel can
 * be read straight off it. Fabric's receiver is not enough on its own: only one
 * receiver can be registered per channel, and with WorldEdit installed
 * client-side it claims worldedit:cui first, leaving this mod's registration
 * silently rejected.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandlerCui {

    @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V",
            at = @At("HEAD"))
    private void asutantweaks$readCui(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (!WorldEditCui.isWanted()) return;
        if (!WorldEditCui.channelId().equals(packet.getChannel())) return;
        PacketByteBuf buf = packet.getData();
        byte[] bytes = new byte[buf.readableBytes()];
        // Peek rather than read: whoever the buffer really belongs to still needs it.
        buf.getBytes(buf.readerIndex(), bytes);
        WorldEditCui.onPayload(bytes);
    }
}
//#elseif MC < 260100
//$$ import org.asutarisucu.tweak.WorldEditGUI.WorldEditCui;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import net.minecraft.client.network.ClientCommonNetworkHandler;
//$$ import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
//$$
//$$ /**
//$$  * Taps the worldedit:cui plugin channel.
//$$  *
//$$  * Injected on the packet handler rather than the payload handler so the payload
//$$  * is seen before Fabric decides whether a receiver is registered for it — with
//$$  * WorldEdit installed client-side the channel belongs to WorldEdit's payload
//$$  * type, and this mod has no receiver Fabric would dispatch to.
//$$  */
//$$ @Mixin(ClientCommonNetworkHandler.class)
//$$ public class MixinClientPlayNetworkHandlerCui {
//$$
//$$     // The descriptor is spelled out because the class also declares the
//$$     // abstract onCustomPayload(CustomPayload) overload.
//$$     @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V",
//$$             at = @At("HEAD"))
//$$     private void asutantweaks$readCui(CustomPayloadS2CPacket packet, CallbackInfo ci) {
//$$         if (!WorldEditCui.isWanted()) return;
//$$         WorldEditCui.acceptPayload(packet.payload());
//$$     }
//$$ }
//#else
//$$ import org.asutarisucu.tweak.WorldEditGUI.WorldEditCui;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
//$$ import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
//$$
//$$ /**
//$$  * Taps the worldedit:cui plugin channel.
//$$  *
//$$  * Injected on the packet handler rather than the payload handler so the payload
//$$  * is seen before Fabric decides whether a receiver is registered for it — with
//$$  * WorldEdit installed client-side the channel belongs to WorldEdit's payload
//$$  * type, and this mod has no receiver Fabric would dispatch to.
//$$  */
//$$ @Mixin(ClientCommonPacketListenerImpl.class)
//$$ public class MixinClientPlayNetworkHandlerCui {
//$$
//$$     // The descriptor is spelled out because the class also declares the
//$$     // abstract handleCustomPayload(CustomPacketPayload) overload.
//$$     @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V",
//$$             at = @At("HEAD"))
//$$     private void asutantweaks$readCui(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
//$$         if (!WorldEditCui.isWanted()) return;
//$$         WorldEditCui.acceptPayload(packet.payload());
//$$     }
//$$ }
//#endif
