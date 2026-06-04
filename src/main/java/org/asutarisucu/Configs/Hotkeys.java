package org.asutarisucu.Configs;

import org.asutarisucu.lib.config.ConfigManager;
import org.asutarisucu.lib.config.FeatureConfig;

public class Hotkeys {
    public static final FeatureConfig OPEN_CONFIG_GUI =
            new FeatureConfig("open_config_gui", false, "B,C");
    public static final FeatureConfig CLEAR_ITEM_COUNT =
            new FeatureConfig("clear_item_count", false, "");
    public static final FeatureConfig ADD_HIGHLIGHT_ITEM =
            new FeatureConfig("add_highlight_item", false, "");

    public static void registerAll() {
        ConfigManager.INSTANCE.register("hotkeys", OPEN_CONFIG_GUI.getName(),    OPEN_CONFIG_GUI);
        ConfigManager.INSTANCE.register("hotkeys", CLEAR_ITEM_COUNT.getName(),   CLEAR_ITEM_COUNT);
        ConfigManager.INSTANCE.register("hotkeys", ADD_HIGHLIGHT_ITEM.getName(), ADD_HIGHLIGHT_ITEM);
    }
}
