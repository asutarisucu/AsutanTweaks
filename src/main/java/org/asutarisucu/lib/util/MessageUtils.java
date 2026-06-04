package org.asutarisucu.lib.util;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.network.chat.Component;
//#endif

public class MessageUtils {

    public static void sendActionBar(String message) {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.of(message), true);
//#else
//$$ Minecraft mc = Minecraft.getInstance();
//$$ if (mc.player != null) mc.player.sendOverlayMessage(Component.literal(message));
//#endif
    }

    public static String colorGreen(String text) { return "§a" + text + "§r"; }
    public static String colorRed(String text)   { return "§c" + text + "§r"; }
    public static String colorGold(String text)  { return "§6" + text + "§r"; }
}
