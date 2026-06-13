package org.asutarisucu.Configs;

import org.asutarisucu.lib.config.*;

import java.util.List;

public class Configs {

    public static class Generic {
        public static final IntegerConfig RESTOCK_COUNT =
                new IntegerConfig("restock_count", 32, 0, 64);
        public static final DoubleConfig VOID_HEIGHT_OW =
                new DoubleConfig("void_height_ow", -80, -200, -64);
        public static final DoubleConfig VOID_HEIGHT_NE =
                new DoubleConfig("void_height_ne", -50, -100, 0);
        public static final DoubleConfig VOID_HEIGHT_END =
                new DoubleConfig("void_height_end", -50, -100, 0);
        public static final BooleanConfig VOID_DISCONNECT =
                new BooleanConfig("void_disconnect", false);
        public static final StringListConfig RESTRICTION_STATE_WHITELIST =
                new StringListConfig("restriction_state_whitelist",
                        List.of("UP", "DOWN", "NORTH", "EAST", "SOUTH", "WEST"));
        public static final OptionListConfig<RestrictionWhitelistMessageType> RESTRICTION_WHITELIST_MESSAGE_TYPE =
                new OptionListConfig<>("restriction_whitelist_message_type",
                        RestrictionWhitelistMessageType.LOG, RestrictionWhitelistMessageType.class);
        public static final StringListConfig LAST_USE_CANCEL_BLACKLIST =
                new StringListConfig("last_use_cancel_blacklist",
                        List.of("chest", "shulker_box"));
        public static final StringListConfig ENDERCHEST_MATERIALLIST_WHITELIST =
                new StringListConfig("enderchest_materiallist_whitelist", List.of("white"));
        public static final StringListConfig ENDERCHEST_MATERIALLIST_BLACKLIST =
                new StringListConfig("enderchest_materiallist_blacklist", List.of("white"));
        public static final OptionListConfig<FilterMode> ENDERCHEST_MATERIALLIST_FILTERTYPE =
                new OptionListConfig<>("enderchest_materiallist_filtertype",
                        FilterMode.NONE, FilterMode.class);
        public static final StringListConfig HIGHLIGHT_ITEM_LIST =
                new StringListConfig("highlight_item_list", List.of());
        public static final ColorConfig HIGHLIGHT_BLOCK_COLOR =
                new ColorConfig("highlight_block_color", "#FFFF0000");
        public static final IntegerConfig HIGHLIGHT_BLOCK_RANGE =
                new IntegerConfig("highlight_block_range", 32, 0, 64);
        public static final ColorConfig HIGHLIGHT_CONTAINER_COLOR =
                new ColorConfig("highlight_container_color", "#FF0000FF");
        public static final IntegerConfig HIGHLIGHT_CONTAINER_RANGE =
                new IntegerConfig("highlight_container_range", 32, 0, 64);
        public static final IntegerConfig HUD_LOG_TIMEOUT =
                new IntegerConfig("hud_log_timeout", 4, 1, 30);
        public static final IntegerConfig HUD_LOG_WIDTH =
                new IntegerConfig("hud_log_width", 200, 80, 600);
        public static final OptionListConfig<AlignMode> HUD_LOG_ALIGN =
                new OptionListConfig<>("hud_log_align", AlignMode.LEFT, AlignMode.class);
        public static final DoubleConfig HUD_LOG_X =
                new DoubleConfig("hud_log_x", 0.02, 0.0, 1.0);
        public static final DoubleConfig HUD_LOG_Y =
                new DoubleConfig("hud_log_y", 0.80, 0.0, 1.0);
        public static final DoubleConfig PROGRESS_METER_X =
                new DoubleConfig("progress_meter_x", 0.5, 0.0, 1.0);
        public static final DoubleConfig PROGRESS_METER_Y =
                new DoubleConfig("progress_meter_y", 0.02, 0.0, 1.0);
    }

    public static void registerAll() {
        ConfigManager.INSTANCE.register("options", Generic.RESTOCK_COUNT.getName(),            Generic.RESTOCK_COUNT);
        ConfigManager.INSTANCE.register("options", Generic.VOID_HEIGHT_OW.getName(),           Generic.VOID_HEIGHT_OW);
        ConfigManager.INSTANCE.register("options", Generic.VOID_HEIGHT_NE.getName(),           Generic.VOID_HEIGHT_NE);
        ConfigManager.INSTANCE.register("options", Generic.VOID_HEIGHT_END.getName(),          Generic.VOID_HEIGHT_END);
        ConfigManager.INSTANCE.register("options", Generic.VOID_DISCONNECT.getName(),          Generic.VOID_DISCONNECT);
        ConfigManager.INSTANCE.register("options", Generic.RESTRICTION_STATE_WHITELIST.getName(),    Generic.RESTRICTION_STATE_WHITELIST);
        ConfigManager.INSTANCE.register("options", Generic.RESTRICTION_WHITELIST_MESSAGE_TYPE.getName(), Generic.RESTRICTION_WHITELIST_MESSAGE_TYPE);
        ConfigManager.INSTANCE.register("options", Generic.LAST_USE_CANCEL_BLACKLIST.getName(),      Generic.LAST_USE_CANCEL_BLACKLIST);
        ConfigManager.INSTANCE.register("options", Generic.ENDERCHEST_MATERIALLIST_WHITELIST.getName(), Generic.ENDERCHEST_MATERIALLIST_WHITELIST);
        ConfigManager.INSTANCE.register("options", Generic.ENDERCHEST_MATERIALLIST_BLACKLIST.getName(), Generic.ENDERCHEST_MATERIALLIST_BLACKLIST);
        ConfigManager.INSTANCE.register("options", Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE.getName(), Generic.ENDERCHEST_MATERIALLIST_FILTERTYPE);
        ConfigManager.INSTANCE.register("options", Generic.HIGHLIGHT_ITEM_LIST.getName(),      Generic.HIGHLIGHT_ITEM_LIST);
        ConfigManager.INSTANCE.register("options", Generic.HIGHLIGHT_BLOCK_COLOR.getName(),    Generic.HIGHLIGHT_BLOCK_COLOR);
        ConfigManager.INSTANCE.register("options", Generic.HIGHLIGHT_BLOCK_RANGE.getName(),    Generic.HIGHLIGHT_BLOCK_RANGE);
        ConfigManager.INSTANCE.register("options", Generic.HIGHLIGHT_CONTAINER_COLOR.getName(), Generic.HIGHLIGHT_CONTAINER_COLOR);
        ConfigManager.INSTANCE.register("options", Generic.HIGHLIGHT_CONTAINER_RANGE.getName(), Generic.HIGHLIGHT_CONTAINER_RANGE);
        ConfigManager.INSTANCE.register("options", Generic.HUD_LOG_TIMEOUT.getName(), Generic.HUD_LOG_TIMEOUT);
        ConfigManager.INSTANCE.register("options", Generic.HUD_LOG_WIDTH.getName(),   Generic.HUD_LOG_WIDTH);
        ConfigManager.INSTANCE.register("options", Generic.HUD_LOG_ALIGN.getName(),   Generic.HUD_LOG_ALIGN);
        ConfigManager.INSTANCE.register("options", Generic.HUD_LOG_X.getName(),           Generic.HUD_LOG_X);
        ConfigManager.INSTANCE.register("options", Generic.HUD_LOG_Y.getName(),           Generic.HUD_LOG_Y);
        ConfigManager.INSTANCE.register("options", Generic.PROGRESS_METER_X.getName(),    Generic.PROGRESS_METER_X);
        ConfigManager.INSTANCE.register("options", Generic.PROGRESS_METER_Y.getName(),    Generic.PROGRESS_METER_Y);
    }
}
