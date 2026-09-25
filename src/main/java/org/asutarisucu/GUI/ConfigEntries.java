package org.asutarisucu.GUI;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.Configs.Hotkeys;
import org.asutarisucu.GUI.glass.Lang;
import org.asutarisucu.GUI.glass.Suggestions;
import org.asutarisucu.lib.config.FeatureConfig;
import org.asutarisucu.lib.config.IConfig;
import org.asutarisucu.lib.config.StringListConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The panels of the config screen: what each one edits, its texts and its order. */
public final class ConfigEntries {

    private ConfigEntries() {}

    public enum Kind { FEATURE, OPTION, HOTKEY }

    /** Buttons that are not backed by a config value. */
    public enum Action { NONE, HUD_LOG_POSITION, METER_POSITION }

    public static final class Entry {
        public final Kind kind;
        public final String key;
        public final Feature feature;
        public final IConfig<?> config;
        public final FeatureConfig hotkey;
        public final Action action;
        public final Suggestions.Kind listKind;
        private final String fallbackName;

        private Entry(Kind kind, String key, Feature feature, IConfig<?> config, FeatureConfig hotkey,
                      Action action, Suggestions.Kind listKind, String fallbackName) {
            this.kind = kind;
            this.key = key;
            this.feature = feature;
            this.config = config;
            this.hotkey = hotkey;
            this.action = action;
            this.listKind = listKind;
            this.fallbackName = fallbackName;
        }

        private String prefix() {
            return switch (kind) {
                case FEATURE -> "feature.";
                case OPTION -> "option.";
                case HOTKEY -> "hotkey.";
            };
        }

        /** Names stay in English whatever the language; only the texts about them are translated. */
        public String name() {
            return Lang.english(prefix() + key, fallbackName);
        }

        /** One line for the feature panel. */
        public String summary() {
            return Lang.get(prefix() + key + ".desc", feature != null ? feature.description : "");
        }

        public String detail() {
            return Lang.get(prefix() + key + ".detail", summary());
        }

        /** Unique across kinds, for remembering state per panel. */
        public String id() {
            return kind.name() + ":" + key;
        }

        public boolean matches(String query) {
            if (query.isEmpty()) return true;
            String q = Suggestions.normalize(query);
            return Suggestions.normalize(name()).contains(q)
                    || Suggestions.normalize(summary()).contains(q)
                    || key.contains(q.replace(' ', '_'));
        }
    }

    private static Entry feature(Feature f) {
        return new Entry(Kind.FEATURE, f.config.getName(), f, null, f.config, Action.NONE, null, f.displayName);
    }

    private static Entry option(IConfig<?> c) {
        return option(c, null);
    }

    private static Entry option(IConfig<?> c, Suggestions.Kind listKind) {
        return new Entry(Kind.OPTION, c.getName(), null, c, null, Action.NONE, listKind, c.getName());
    }

    private static Entry action(String key, Action action, String fallback) {
        return new Entry(Kind.OPTION, key, null, null, null, action, null, fallback);
    }

    private static Entry hotkey(FeatureConfig c, String fallback) {
        return new Entry(Kind.HOTKEY, c.getName(), null, null, c, Action.NONE, null, fallback);
    }

    public static List<Entry> features() {
        List<Entry> list = new ArrayList<>();
        for (Feature f : Feature.values()) list.add(feature(f));
        return ordered(list, Configs.Ui.ORDER_FEATURES);
    }

    public static List<Entry> options() {
        List<Entry> list = new ArrayList<>();
        list.add(option(Configs.Generic.RESTOCK_COUNT));
        list.add(option(Configs.Generic.VOID_HEIGHT_OW));
        list.add(option(Configs.Generic.VOID_HEIGHT_NE));
        list.add(option(Configs.Generic.VOID_HEIGHT_END));
        list.add(option(Configs.Generic.VOID_DISCONNECT));
        list.add(option(Configs.Generic.RESTRICTION_STATE_WHITELIST, Suggestions.Kind.STATE));
        list.add(option(Configs.Generic.RESTRICTION_WHITELIST_MESSAGE_TYPE));
        list.add(option(Configs.Generic.LAST_USE_CANCEL_BLACKLIST, Suggestions.Kind.BLOCK));
        list.add(option(Configs.Generic.ENDERCHEST_MATERIALLIST_WHITELIST, Suggestions.Kind.DYE));
        list.add(option(Configs.Generic.ENDERCHEST_MATERIALLIST_BLACKLIST, Suggestions.Kind.DYE));
        list.add(option(Configs.Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE));
        list.add(option(Configs.Generic.HIGHLIGHT_ITEM_LIST, Suggestions.Kind.ITEM));
        list.add(option(Configs.Generic.HIGHLIGHT_BLOCK_COLOR));
        list.add(option(Configs.Generic.HIGHLIGHT_BLOCK_RANGE));
        list.add(option(Configs.Generic.HIGHLIGHT_CONTAINER_COLOR));
        list.add(option(Configs.Generic.HIGHLIGHT_CONTAINER_RANGE));
        list.add(option(Configs.Generic.HUD_LOG_TIMEOUT));
        list.add(option(Configs.Generic.HUD_LOG_WIDTH));
        list.add(option(Configs.Generic.HUD_LOG_ALIGN));
        list.add(action("hud_log_position", Action.HUD_LOG_POSITION, "HUD Log Position"));
        list.add(action("progress_meter_position", Action.METER_POSITION, "Meter Position"));
        list.add(option(Configs.Generic.LAZY_ENTITY_SYNC_INTERVAL));
        list.add(option(Configs.Generic.PICK_BLOCK_REACH));
        list.add(option(Configs.Generic.WORLDEDIT_GRID_COLOR));
        list.add(option(Configs.Generic.WORLDEDIT_EDGE_COLOR));
        list.add(option(Configs.Generic.WORLDEDIT_POS1_COLOR));
        list.add(option(Configs.Generic.WORLDEDIT_POS2_COLOR));
        list.add(option(Configs.Generic.WORLDEDIT_GRID_SPACING));
        list.add(option(Configs.Generic.WORLDEDIT_GRID_MAX_LINES));
        list.add(option(Configs.Generic.COMPARATOR_SIGNAL_RANGE));
        list.add(option(Configs.Generic.COMPARATOR_SIGNAL_SYNC_INTERVAL));
        // Clear Block Render options are edited in its own screen, opened by its
        // hotkey, rather than from this list.
        return ordered(list, Configs.Ui.ORDER_OPTIONS);
    }

    public static List<Entry> cbrOptions() {
        List<Entry> list = new ArrayList<>();
        list.add(option(Configs.Generic.CBR_WIDTH));
        list.add(option(Configs.Generic.CBR_HEIGHT));
        list.add(option(Configs.Generic.CBR_FPS));
        list.add(option(Configs.Generic.CBR_SPEED));
        list.add(option(Configs.Generic.CBR_PROJECTION));
        list.add(option(Configs.Generic.CBR_FOV));
        list.add(option(Configs.Generic.CBR_ZOOM));
        list.add(option(Configs.Generic.CBR_YAW));
        list.add(option(Configs.Generic.CBR_PITCH));
        list.add(option(Configs.Generic.CBR_PAN_X));
        list.add(option(Configs.Generic.CBR_PAN_Y));
        list.add(option(Configs.Generic.CBR_ORBIT_SPEED));
        list.add(option(Configs.Generic.CBR_TURNTABLE_FRAMES));
        list.add(option(Configs.Generic.CBR_BLOCK_ENTITIES));
        list.add(option(Configs.Generic.CBR_ENTITIES));
        list.add(option(Configs.Generic.CBR_PLAYERS));
        list.add(option(Configs.Generic.CBR_FORMAT));
        list.add(option(Configs.Generic.CBR_FFMPEG_PATH));
        return ordered(list, Configs.Ui.ORDER_CBR);
    }

    public static List<Entry> hotkeys() {
        List<Entry> list = new ArrayList<>();
        list.add(hotkey(Hotkeys.OPEN_CONFIG_GUI, "Open Config GUI"));
        list.add(hotkey(Hotkeys.CLEAR_ITEM_COUNT, "Clear Item Count"));
        list.add(hotkey(Hotkeys.ADD_HIGHLIGHT_ITEM, "Add Highlight Item"));
        list.add(hotkey(Hotkeys.PICK_BLOCK_ULTIMATE_COMPONENT, "Pick Block State"));
        list.add(hotkey(Hotkeys.CLEAR_BLOCK_RENDER_TOGGLE, "Clear Block Render Screen"));
        list.add(hotkey(Hotkeys.CLEAR_BLOCK_RENDER_RECORD, "Clear Block Render Rec"));
        return ordered(list, Configs.Ui.ORDER_HOTKEYS);
    }

    /**
     * Applies a saved order. Entries the saved order does not know, such as
     * ones added in a later version, keep their default position.
     */
    private static List<Entry> ordered(List<Entry> defaults, StringListConfig order) {
        Map<String, Entry> byKey = new HashMap<>();
        for (Entry e : defaults) byKey.put(e.key, e);
        List<Entry> out = new ArrayList<>();
        for (String k : order.getStrings()) {
            Entry e = byKey.remove(k);
            if (e != null) out.add(e);
        }
        for (int i = 0; i < defaults.size(); i++) {
            Entry e = defaults.get(i);
            if (byKey.containsKey(e.key)) out.add(Math.min(i, out.size()), e);
        }
        return out;
    }

    public static void saveOrder(StringListConfig order, List<Entry> entries) {
        List<String> keys = new ArrayList<>();
        for (Entry e : entries) keys.add(e.key);
        order.setValue(keys);
    }
}
