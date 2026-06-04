package org.asutarisucu;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.asutarisucu.Configs.Callbacks;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.Configs.Hotkeys;
import org.asutarisucu.lib.config.ConfigManager;
import org.asutarisucu.lib.hotkey.HotkeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsutanTweaks implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

    @Override
    public void onInitializeClient() {
        suppressSpuriousGlErrors();

        // Register all config sections
        Feature.registerAll();
        Configs.registerAll();
        Hotkeys.registerAll();

        // Load persisted values
        ConfigManager.INSTANCE.load();

        // Wire up hotkey callbacks and register with tick event
        Callbacks.init();
        HotkeyManager.INSTANCE.init();

        // Register render events and other game events
        Reference.LoadEvent();

        // Save on game exit
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING
                .register(client -> ConfigManager.INSTANCE.save());
    }

    private static void suppressSpuriousGlErrors() {
        try {
            RegexFilter filter = RegexFilter.createFilter(
                    "OpenGL debug message.*in \\(null\\).*",
                    null, false,
                    RegexFilter.Result.DENY,
                    RegexFilter.Result.NEUTRAL);
            if (filter != null) {
                ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(filter);
            }
        } catch (Exception ignored) {
        }
    }
}
