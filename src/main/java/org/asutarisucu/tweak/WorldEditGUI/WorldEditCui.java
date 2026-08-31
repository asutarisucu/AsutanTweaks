package org.asutarisucu.tweak.WorldEditGUI;

import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.Configs.Feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//#if MC < 12005
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
//#if MC >= 12002
//$$ import net.minecraft.network.packet.CustomPayload;
//#endif
//#elseif MC < 12100
//$$ import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.network.PacketByteBuf;
//$$ import net.minecraft.network.codec.PacketCodec;
//$$ import net.minecraft.network.packet.CustomPayload;
//$$ import net.minecraft.util.Identifier;
//#elseif MC < 260100
//$$ import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.network.PacketByteBuf;
//$$ import net.minecraft.network.codec.PacketCodec;
//$$ import net.minecraft.network.packet.CustomPayload;
//$$ import net.minecraft.util.Identifier;
//#else
//$$ import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.network.FriendlyByteBuf;
//$$ import net.minecraft.network.codec.StreamCodec;
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//$$ import net.minecraft.resources.Identifier;
//#endif

/**
 * Client half of WorldEdit's CUI protocol.
 *
 * WorldEdit pushes the player's selection to any client that announced CUI
 * support, over the {@code worldedit:cui} plugin channel. Announcing is done
 * with the {@code //we cui} command rather than a handshake packet: from MC
 * 1.20.5 on a custom payload can only be sent for a payload type this client
 * registered, and WorldEdit — when it is installed client-side, which is the
 * case this feature targets — has already claimed that channel.
 *
 * Receiving goes through {@code MixinClientPlayNetworkHandlerCui} for the same
 * reason: the S2C payload type may belong to WorldEdit, so the bytes are pulled
 * off whatever object the channel decoded into.
 */
public final class WorldEditCui {

    public static final String CHANNEL_NAMESPACE = "worldedit";
    public static final String CHANNEL_PATH = "cui";

    /** Ticks to wait after joining a world before announcing CUI support. */
    private static final int HANDSHAKE_DELAY_TICKS = 40;
    /** Ticks between retries while no CUI message has come back yet. */
    private static final int HANDSHAKE_RETRY_TICKS = 100;
    /**
     * The command that turns CUI on. WorldEdit registers the group under several
     * names; both spellings are tried because which aliases exist has varied
     * between platforms.
     */
    private static final String[] HANDSHAKE_COMMANDS = { "we cui", "worldedit cui" };

    private static Object lastWorld;
    private static boolean lastEnabled;
    private static int handshakeCountdown = -1;
    private static int handshakeAttempts;
    /** Set once any CUI message has been parsed; stops the handshake retries. */
    private static boolean receivedAny;

    private WorldEditCui() {}

    /** Resets the handshake schedule on world change. */
    private static void onWorldChanged(boolean inWorld) {
        WorldEditSelection.clear();
        receivedAny = false;
        handshakeAttempts = 0;
        handshakeCountdown = inWorld ? HANDSHAKE_DELAY_TICKS : -1;
    }

    /**
     * Advances the handshake schedule.
     *
     * @return the command to send this tick, or null when there is nothing to send
     */
    private static String pollHandshake(boolean inWorld, boolean enabled) {
        if (enabled && !lastEnabled && inWorld) {
            handshakeAttempts = 0;
            handshakeCountdown = 1;
        }
        lastEnabled = enabled;
        if (!enabled || !inWorld || handshakeCountdown <= 0) return null;
        if (--handshakeCountdown > 0) return null;

        String command = HANDSHAKE_COMMANDS[handshakeAttempts % HANDSHAKE_COMMANDS.length];
        handshakeAttempts++;
        // Keep retrying, alternating the spelling, until something arrives.
        handshakeCountdown = receivedAny || handshakeAttempts >= 4 ? -1 : HANDSHAKE_RETRY_TICKS;
        AsutanTweaks.LOGGER.info("[WorldEditGUI] announcing CUI support with /{}", command);
        return command;
    }

    /**
     * Whether anything currently needs the selection. Clear Block Render frames its
     * capture on the same selection, so it must not require the grid overlay to be on.
     */
    public static boolean isWanted() {
        return Feature.WORLDEDIT_GUI.isEnabled() || Feature.CLEAR_BLOCK_RENDER.isEnabled();
    }

    /** True once a CUI message has been parsed, so the log stays to one line per session. */
    private static boolean loggedFirstMessage;
    /** True once an unreadable payload has been reported, for the same reason. */
    private static boolean loggedUnreadable;

    /** Feeds one raw CUI payload into the parser. */
    public static void onPayload(byte[] data) {
        if (data == null || data.length == 0) return;
        String message = new String(data, StandardCharsets.UTF_8);
        receivedAny = true;
        if (!loggedFirstMessage) {
            loggedFirstMessage = true;
            AsutanTweaks.LOGGER.info("[WorldEditGUI] receiving CUI messages, first was: \"{}\" ({} bytes)",
                    CuiParser.escape(message), data.length);
        }
        CuiParser.handle(message);
    }

    /**
     * Reads one custom payload and, when it belongs to the CUI channel, feeds it
     * to the parser.
     *
     * The payload object is our own {@code CuiPayload} only when this mod claimed
     * the channel; when WorldEdit is installed client-side it registered the type
     * first, so the bytes have to be pulled out of its object by reflection.
     */
    public static void acceptPayload(Object payload) {
        if (payload == null) return;
        if (!isCuiPayload(payload)) return;
        byte[] data = extractPayloadBytes(payload);
        if (data == null || data.length == 0) {
            if (!loggedUnreadable) {
                loggedUnreadable = true;
                AsutanTweaks.LOGGER.warn(
                        "[WorldEditGUI] worldedit:cui payload arrived as {} but no message could be read from it; "
                                + "fields: {}", payload.getClass().getName(), describeFields(payload));
            }
            return;
        }
        onPayload(data);
    }

    private static String describeFields(Object payload) {
        StringBuilder sb = new StringBuilder();
        for (Field f : payload.getClass().getDeclaredFields()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(f.getType().getSimpleName()).append(' ').append(f.getName());
        }
        return sb.toString();
    }

    /**
     * Rebuilds the CUI message from a decoded payload object.
     *
     * WorldEdit's own payload keeps the event id and its parameters in separate
     * members rather than one pre-joined string, so every text-ish member is
     * collected in declaration order and joined with the protocol's `|`.
     * Reading only the first String yielded just {@code "s"}.
     */
    private static byte[] extractPayloadBytes(Object payload) {
        if (payload instanceof CuiPayloadHolder holder) return holder.cuiData();
        try {
            List<String> parts = new ArrayList<>();
            for (Field f : payload.getClass().getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                collectText(f.get(payload), parts);
            }
            if (parts.isEmpty()) return null;
            return String.join("|", parts).getBytes(StandardCharsets.UTF_8);
        } catch (Throwable t) {
            AsutanTweaks.LOGGER.debug("[WorldEditGUI] could not read CUI payload", t);
        }
        return null;
    }

    /** Flattens strings, string collections/arrays and raw buffers into {@code out}. */
    private static void collectText(Object value, List<String> out) {
        if (value == null) return;
        if (value instanceof String s) {
            out.add(s);
        } else if (value instanceof byte[] b) {
            out.add(new String(b, StandardCharsets.UTF_8));
        } else if (value instanceof io.netty.buffer.ByteBuf buf) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            out.add(new String(bytes, StandardCharsets.UTF_8));
        } else if (value instanceof Object[] array) {
            for (Object element : array) collectText(element, out);
        } else if (value instanceof Iterable<?> iterable) {
            for (Object element : iterable) collectText(element, out);
        }
        // Numbers and booleans are skipped on purpose: WorldEdit's payload carries a
        // multi-event flag alongside the message, and folding it in produced
        // "false|s|cuboid" where the message is "s|cuboid".
    }

    /** Implemented by this mod's own payload record so no reflection is needed for it. */
    public interface CuiPayloadHolder {
        byte[] cuiData();
    }

//#if MC < 12005
//#if MC < 12002
    /** Before 1.20.2 the packet carries raw bytes, so the mixin reads it without a payload object. */
    private static boolean isCuiPayload(Object payload) {
        return false;
    }
//#else
    //$$ private static boolean isCuiPayload(Object payload) {
    //$$     return payload instanceof CustomPayload p && channelId().equals(p.id());
    //$$ }
//#endif

    public static void register() {
        // Fabric allows one receiver per channel and does not say so by throwing:
        // registerGlobalReceiver just returns false when the channel is taken. With
        // WorldEdit installed client-side it claims worldedit:cui first, so this
        // registration is only the fallback for when WorldEdit is server-side only;
        // MixinClientPlayNetworkHandlerCui is what makes the other case work.
        boolean claimed = false;
        try {
            claimed = ClientPlayNetworking.registerGlobalReceiver(channelId(), (client, handler, buf, sender) -> {
                if (!isWanted()) return;
                byte[] bytes = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), bytes);
                onPayload(bytes);
            });
        } catch (Throwable t) {
            AsutanTweaks.LOGGER.info("[WorldEditGUI] could not claim worldedit:cui", t);
        }
        if (!claimed) {
            AsutanTweaks.LOGGER.info("[WorldEditGUI] worldedit:cui already registered, reading it as-is");
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    public static Identifier channelId() {
        return new Identifier(CHANNEL_NAMESPACE, CHANNEL_PATH);
    }

    private static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != lastWorld) {
            lastWorld = mc.world;
            onWorldChanged(mc.world != null);
        }
        String command = pollHandshake(mc.world != null, isWanted());
        if (command != null && mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand(command);
        }
    }
//#elseif MC < 260100
//$$ /** Raw-bytes payload for worldedit:cui, used only when WorldEdit has not claimed the channel. */
//$$ public record CuiPayload(byte[] data) implements CustomPayload, CuiPayloadHolder {
//$$     public static final CustomPayload.Id<CuiPayload> ID =
//$$             new CustomPayload.Id<>(channelId());
//$$     public static final PacketCodec<PacketByteBuf, CuiPayload> CODEC = CustomPayload.codecOf(
//$$             (value, buf) -> buf.writeBytes(value.data()),
//$$             buf -> {
//$$                 byte[] bytes = new byte[buf.readableBytes()];
//$$                 buf.readBytes(bytes);
//$$                 return new CuiPayload(bytes);
//$$             });
//$$
//$$     @Override
//$$     public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
//$$
//$$     @Override
//$$     public byte[] cuiData() { return data; }
//$$ }
//$$
//$$ private static boolean isCuiPayload(Object payload) {
//$$     return payload instanceof CustomPayload p && channelId().equals(p.getId().id());
//$$ }
//$$
//$$ public static void register() {
//$$     // Only succeeds when WorldEdit is not on the client; otherwise its own
//$$     // payload type stays in place and the mixin reads that instead.
//$$     try {
//$$         PayloadTypeRegistry.playS2C().register(CuiPayload.ID, CuiPayload.CODEC);
//$$     } catch (Throwable t) {
//$$         AsutanTweaks.LOGGER.info("[WorldEditGUI] worldedit:cui already registered, reading it as-is");
//$$     }
//$$     ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
//$$ }
//$$
//$$ public static Identifier channelId() {
//#if MC < 12100
//$$     return new Identifier(CHANNEL_NAMESPACE, CHANNEL_PATH);
//#else
//$$     return Identifier.of(CHANNEL_NAMESPACE, CHANNEL_PATH);
//#endif
//$$ }
//$$
//$$ private static void tick() {
//$$     MinecraftClient mc = MinecraftClient.getInstance();
//$$     if (mc.world != lastWorld) {
//$$         lastWorld = mc.world;
//$$         onWorldChanged(mc.world != null);
//$$     }
//$$     String command = pollHandshake(mc.world != null, isWanted());
//$$     if (command != null && mc.player != null && mc.player.networkHandler != null) {
//$$         mc.player.networkHandler.sendChatCommand(command);
//$$     }
//$$ }
//#else
//$$ /** Raw-bytes payload for worldedit:cui, used only when WorldEdit has not claimed the channel. */
//$$ public record CuiPayload(byte[] data) implements CustomPacketPayload, CuiPayloadHolder {
//$$     public static final CustomPacketPayload.Type<CuiPayload> TYPE =
//$$             new CustomPacketPayload.Type<>(channelId());
//$$     public static final StreamCodec<FriendlyByteBuf, CuiPayload> CODEC = CustomPacketPayload.codec(
//$$             (value, buf) -> buf.writeBytes(value.data()),
//$$             buf -> {
//$$                 byte[] bytes = new byte[buf.readableBytes()];
//$$                 buf.readBytes(bytes);
//$$                 return new CuiPayload(bytes);
//$$             });
//$$
//$$     @Override
//$$     public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
//$$
//$$     @Override
//$$     public byte[] cuiData() { return data; }
//$$ }
//$$
//$$ private static boolean isCuiPayload(Object payload) {
//$$     return payload instanceof CustomPacketPayload p && channelId().equals(p.type().id());
//$$ }
//$$
//$$ public static void register() {
//$$     // Only succeeds when WorldEdit is not on the client; otherwise its own
//$$     // payload type stays in place and the mixin reads that instead.
//$$     try {
//$$         PayloadTypeRegistry.clientboundPlay().register(CuiPayload.TYPE, CuiPayload.CODEC);
//$$     } catch (Throwable t) {
//$$         AsutanTweaks.LOGGER.info("[WorldEditGUI] worldedit:cui already registered, reading it as-is");
//$$     }
//$$     ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
//$$ }
//$$
//$$ public static Identifier channelId() {
//$$     return Identifier.fromNamespaceAndPath(CHANNEL_NAMESPACE, CHANNEL_PATH);
//$$ }
//$$
//$$ private static void tick() {
//$$     Minecraft mc = Minecraft.getInstance();
//$$     if (mc.level != lastWorld) {
//$$         lastWorld = mc.level;
//$$         onWorldChanged(mc.level != null);
//$$     }
//$$     String command = pollHandshake(mc.level != null, isWanted());
//$$     if (command != null && mc.player != null && mc.player.connection != null) {
//$$         mc.player.connection.sendCommand(command);
//$$     }
//$$ }
//#endif
}
