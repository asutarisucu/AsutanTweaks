package org.asutarisucu.Configs;

import org.asutarisucu.GUI.HudLogger;
import org.asutarisucu.lib.util.MessageUtils;
import org.asutarisucu.lib.hotkey.HotkeyManager;
import org.asutarisucu.tweak.SearchItems.RegisterItem;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
//#else
//$$ import net.minecraft.client.Minecraft;
//#endif

public class Callbacks {

    public static void init() {
        // Feature toggle hotkeys
        for (Feature f : Feature.values()) {
            final Feature feature = f;
            f.config.setHotkeyCallback(() -> {
                feature.toggle();
                String state = feature.isEnabled()
                        ? MessageUtils.colorGreen("ON")
                        : MessageUtils.colorRed("OFF");
                HudLogger.INSTANCE.log(feature.displayName + " " + state);
            });
            HotkeyManager.INSTANCE.register(f.config.getHotkey());
        }

        // Standalone hotkeys
        Hotkeys.OPEN_CONFIG_GUI.setHotkeyCallback(() -> openConfigGui());
        Hotkeys.CLEAR_ITEM_COUNT.setHotkeyCallback(() -> {
            SimpleEntityRender.clearAll();
            HudLogger.INSTANCE.log(MessageUtils.colorGreen("Count cleared!"));
        });
        Hotkeys.ADD_HIGHLIGHT_ITEM.setHotkeyCallback(() -> RegisterItem.addHandItem());

        HotkeyManager.INSTANCE.register(Hotkeys.OPEN_CONFIG_GUI.getHotkey());
        HotkeyManager.INSTANCE.register(Hotkeys.CLEAR_ITEM_COUNT.getHotkey());
        HotkeyManager.INSTANCE.register(Hotkeys.ADD_HIGHLIGHT_ITEM.getHotkey());
    }

    private static void openConfigGui() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.send(() -> mc.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.currentScreen)));
//#else
//$$ Minecraft mc = Minecraft.getInstance();
//$$ mc.execute(() -> mc.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.screen)));
//#endif
    }
}
