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
    /**
     * Held (not tapped) while middle-clicking to also copy the block's state
     * properties. Defaults to Alt rather than Ctrl so that a plain ctrl+pick still
     * reaches vanilla, which copies block entity NBT instead.
     */
    public static final FeatureConfig PICK_BLOCK_ULTIMATE_COMPONENT =
            new FeatureConfig("pick_block_ultimate_component", false, "LEFT_ALT");
    /** Opens the capture screen. */
    public static final FeatureConfig CLEAR_BLOCK_RENDER_TOGGLE =
            new FeatureConfig("clear_block_render_toggle", false, "");
    /**
     * Starts and stops the capture without opening anything, so a circuit can be
     * recorded the moment it is set up.
     */
    public static final FeatureConfig CLEAR_BLOCK_RENDER_RECORD =
            new FeatureConfig("clear_block_render_record", false, "");

    public static void registerAll() {
        ConfigManager.INSTANCE.register("hotkeys", OPEN_CONFIG_GUI.getName(),    OPEN_CONFIG_GUI);
        ConfigManager.INSTANCE.register("hotkeys", CLEAR_ITEM_COUNT.getName(),   CLEAR_ITEM_COUNT);
        ConfigManager.INSTANCE.register("hotkeys", ADD_HIGHLIGHT_ITEM.getName(), ADD_HIGHLIGHT_ITEM);
        ConfigManager.INSTANCE.register("hotkeys", PICK_BLOCK_ULTIMATE_COMPONENT.getName(), PICK_BLOCK_ULTIMATE_COMPONENT);
        ConfigManager.INSTANCE.register("hotkeys", CLEAR_BLOCK_RENDER_TOGGLE.getName(), CLEAR_BLOCK_RENDER_TOGGLE);
        ConfigManager.INSTANCE.register("hotkeys", CLEAR_BLOCK_RENDER_RECORD.getName(), CLEAR_BLOCK_RENDER_RECORD);
    }
}
