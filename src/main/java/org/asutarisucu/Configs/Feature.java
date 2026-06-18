package org.asutarisucu.Configs;

import org.asutarisucu.lib.config.ConfigManager;
import org.asutarisucu.lib.config.FeatureConfig;

public enum Feature {

    LAST_USE_CANCEL("last_use_cancel", false, "",
            "Last Use Cancel", "Prevent using the last item in a stack"),
    ITEM_RESTOCK("item_restock", false, "",
            "Item Restock", "Restock item when you use the last one"),
    AUTO_FILL_INVENTORY("auto_fill_inventory", false, "",
            "Auto Fill Inventory", "Fill material in your inventory"),
    DISABLE_VOID_DIVE("disable_void_dive", false, "",
            "Disable Void Dive", "Protects you from falling into the void"),
    SCHEMATIC_RESTRICTION_STATE_WHITELIST("schematic_restriction_state_whitelist", false, "",
            "Restriction State Whitelist", "Only one can be placed per square (schematic restriction)"),
    SIMPLE_ITEM_ENTITY_RENDER("simple_item_entity_render", false, "",
            "Simple Item Entity Render", "Combine many item entities into one for rendering"),
    SIMPLE_MOB_ENTITY_RENDER("simple_mob_entity_render", false, "",
            "Simple Mob Entity Render", "Combine many mob entities into one for rendering"),
    SIMPLE_ENTITY_RENDER_COUNT("simple_entity_render_count", false, "",
            "Simple Entity Render Count", "Display item count near entity cluster"),
    ENDERCHEST_MATERIALLIST("enderchest_materiallist", false, "",
            "Ender Chest Material List", "Include ender chest items in the material list"),
    SEARCH_BLOCK_HIGHLIGHT("search_block_highlight", false, "",
            "Search Block Highlight", "Highlight registered blocks in the world"),
    SEARCH_CONTAINER_HIGHLIGHT("search_container_highlight", false, "",
            "Search Container Highlight", "Highlight containers with a registered item inside"),
    PLACEMENT_UPDATE_VIEWER("placement_update_viewer", false, "",
            "Placement Update Viewer", "Show block update range when placing a block (red)"),
    BREAKING_UPDATE_VIEWER("breaking_update_viewer", false, "",
            "Breaking Update Viewer", "Show block update range when breaking a block (blue)"),
    UPDATE_VIEW_INSTANT_ONLY("update_view_instant_only", false, "",
            "Update View Instant Only", "Limit update viewer to instant updates only"),
    UPDATE_SUPPRESSION_VIEW("update_suppression_view", false, "",
            "Update Suppression View", "Show warning when update chain contains suppression-prone positions"),
    THIRD_EYE("third_eye", false, "",
            "Third Eye", "Render the world from a second viewpoint in a separate window"),
    THIRD_EYE_MOVEMENT("third_eye_movement", false, "",
            "Third Eye Movement", "WASD/Space/Ctrl move the Third Eye camera while enabled"),
    VISUALISE_LAZY_ENTITY("visualise_lazy_entity", false, "",
            "Visualise Lazy Entity", "Render entities in lazy chunks at their real server position");

    public final FeatureConfig config;
    public final String displayName;
    public final String description;

    Feature(String key, boolean defaultEnabled, String defaultHotkey,
            String displayName, String description) {
        this.config = new FeatureConfig(key, defaultEnabled, defaultHotkey);
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isEnabled() { return config.isEnabled(); }
    public void setEnabled(boolean v) { config.setEnabled(v); }
    public void toggle() { config.toggle(); }

    /** Compatibility alias for existing call sites. */
    public boolean getBooleanValue() { return config.isEnabled(); }
    public void setBooleanValue(boolean v) { config.setEnabled(v); }

    public static void registerAll() {
        for (Feature f : values()) {
            ConfigManager.INSTANCE.register("features", f.config.getName(), f.config);
        }
    }
}
