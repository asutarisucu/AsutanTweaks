package org.asutarisucu.Configs;

import org.asutarisucu.GUI.HudLogger;
import org.asutarisucu.lib.util.MessageUtils;
import org.asutarisucu.lib.hotkey.HotkeyManager;
import org.asutarisucu.tweak.ClearBlockRender.ClearBlockRender;
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
        Hotkeys.CLEAR_BLOCK_RENDER_TOGGLE.setHotkeyCallback(Callbacks::openClearBlockRenderGui);
        Hotkeys.CLEAR_BLOCK_RENDER_RECORD.setHotkeyCallback(ClearBlockRender::toggle);

        HotkeyManager.INSTANCE.register(Hotkeys.OPEN_CONFIG_GUI.getHotkey());
        HotkeyManager.INSTANCE.register(Hotkeys.CLEAR_ITEM_COUNT.getHotkey());
        HotkeyManager.INSTANCE.register(Hotkeys.ADD_HIGHLIGHT_ITEM.getHotkey());
        HotkeyManager.INSTANCE.register(Hotkeys.CLEAR_BLOCK_RENDER_TOGGLE.getHotkey());
        HotkeyManager.INSTANCE.register(Hotkeys.CLEAR_BLOCK_RENDER_RECORD.getHotkey());
        // PICK_BLOCK_ULTIMATE_COMPONENT is polled while held, not on activation,
        // so it is deliberately not registered with the edge-triggered manager.
    }

    private static void openConfigGui() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.send(() -> mc.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.currentScreen)));
//#elseif MC < 260200
//$$ Minecraft mc = Minecraft.getInstance();
//$$ mc.execute(() -> mc.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.screen)));
//#else
//$$ // MC 26.2 moved the current screen from Minecraft onto Minecraft.gui.
//$$ Minecraft mc = Minecraft.getInstance();
//$$ mc.execute(() -> mc.gui.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.gui.screen())));
//#endif
    }

    private static void openClearBlockRenderGui() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.send(() -> mc.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.currentScreen, true)));
//#elseif MC < 260200
//$$ Minecraft mc = Minecraft.getInstance();
//$$ mc.execute(() -> mc.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.screen, true)));
//#else
//$$ Minecraft mc = Minecraft.getInstance();
//$$ mc.execute(() -> mc.gui.setScreen(new org.asutarisucu.GUI.TweaksConfigScreen(mc.gui.screen(), true)));
//#endif
    }
}
