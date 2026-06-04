package org.asutarisucu.lib.hotkey;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
//#else
//$$ import net.minecraft.client.Minecraft;
//#endif

import java.util.ArrayList;
import java.util.List;

public class HotkeyManager {
    public static final HotkeyManager INSTANCE = new HotkeyManager();

    private final List<ComboKey> hotkeys = new ArrayList<>();

    private HotkeyManager() {}

    public void register(ComboKey key) {
        hotkeys.add(key);
    }

    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    private void tick() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) return;
        long window = mc.getWindow().getHandle();
//#else
//$$ Minecraft mc = Minecraft.getInstance();
//$$ if (mc.screen != null) return;
//$$ long window = mc.getWindow().handle();
//#endif
        for (ComboKey key : hotkeys) {
            key.tick(window);
        }
    }
}
