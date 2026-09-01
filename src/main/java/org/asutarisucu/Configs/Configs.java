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
        public static final IntegerConfig LAZY_ENTITY_SYNC_INTERVAL =
                new IntegerConfig("lazy_entity_sync_interval", 20, 5, 200);

        // ── Pick Block Ultimate ──────────────────────────────────
        public static final IntegerConfig PICK_BLOCK_REACH =
                new IntegerConfig("pick_block_reach", 256, 8, 512);

        // ── WorldEdit GUI ────────────────────────────────────────
        public static final ColorConfig WORLDEDIT_GRID_COLOR =
                new ColorConfig("worldedit_grid_color", "#5500FFFF");
        public static final ColorConfig WORLDEDIT_EDGE_COLOR =
                new ColorConfig("worldedit_edge_color", "#FFFFCC00");
        public static final ColorConfig WORLDEDIT_POS1_COLOR =
                new ColorConfig("worldedit_pos1_color", "#FF33DD55");
        public static final ColorConfig WORLDEDIT_POS2_COLOR =
                new ColorConfig("worldedit_pos2_color", "#FFDD3355");
        public static final IntegerConfig WORLDEDIT_GRID_SPACING =
                new IntegerConfig("worldedit_grid_spacing", 1, 1, 16);
        public static final IntegerConfig WORLDEDIT_GRID_MAX_LINES =
                new IntegerConfig("worldedit_grid_max_lines", 3000, 200, 30000);

        // ── Clear Block Render ───────────────────────────────────
        public static final IntegerConfig CBR_WIDTH =
                new IntegerConfig("cbr_width", 1920, 64, 7680);
        public static final IntegerConfig CBR_HEIGHT =
                new IntegerConfig("cbr_height", 1080, 64, 4320);
        public static final IntegerConfig CBR_FPS =
                new IntegerConfig("cbr_fps", 30, 1, 120);
        /**
         * Playback rate of the finished clip. 1.0 is real time, 0.5 half speed,
         * 2.0 double. Frames are still captured at CBR_FPS; this only changes the
         * rate they are played back at.
         */
        public static final DoubleConfig CBR_SPEED =
                new DoubleConfig("cbr_speed", 1.0, 0.05, 10.0);
        public static final OptionListConfig<ProjectionMode> CBR_PROJECTION =
                new OptionListConfig<>("cbr_projection", ProjectionMode.ISOMETRIC, ProjectionMode.class);
        public static final DoubleConfig CBR_FOV =
                new DoubleConfig("cbr_fov", 70.0, 10.0, 110.0);
        public static final DoubleConfig CBR_ZOOM =
                new DoubleConfig("cbr_zoom", 1.0, 0.1, 10.0);
        public static final DoubleConfig CBR_YAW =
                new DoubleConfig("cbr_yaw", 45.0, -180.0, 180.0);
        public static final DoubleConfig CBR_PITCH =
                new DoubleConfig("cbr_pitch", 30.0, -89.0, 89.0);
        /** Camera shift across its own view plane, in blocks. Driven by dragging the preview. */
        public static final DoubleConfig CBR_PAN_X =
                new DoubleConfig("cbr_pan_x", 0.0, -512.0, 512.0);
        public static final DoubleConfig CBR_PAN_Y =
                new DoubleConfig("cbr_pan_y", 0.0, -512.0, 512.0);
        public static final DoubleConfig CBR_ORBIT_SPEED =
                new DoubleConfig("cbr_orbit_speed", 0.0, -180.0, 180.0);
        public static final BooleanConfig CBR_BLOCK_ENTITIES =
                new BooleanConfig("cbr_block_entities", true);
        public static final BooleanConfig CBR_ENTITIES =
                new BooleanConfig("cbr_entities", true);
        /** Off by default: the player recording is usually standing in the shot. */
        public static final BooleanConfig CBR_PLAYERS =
                new BooleanConfig("cbr_players", false);
        public static final OptionListConfig<VideoFormat> CBR_FORMAT =
                new OptionListConfig<>("cbr_format", VideoFormat.WEBM_VP9, VideoFormat.class);
        public static final StringConfig CBR_FFMPEG_PATH =
                new StringConfig("cbr_ffmpeg_path", "ffmpeg");
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
        ConfigManager.INSTANCE.register("options", Generic.LAZY_ENTITY_SYNC_INTERVAL.getName(), Generic.LAZY_ENTITY_SYNC_INTERVAL);
        ConfigManager.INSTANCE.register("options", Generic.PICK_BLOCK_REACH.getName(),        Generic.PICK_BLOCK_REACH);
        ConfigManager.INSTANCE.register("options", Generic.WORLDEDIT_GRID_COLOR.getName(),    Generic.WORLDEDIT_GRID_COLOR);
        ConfigManager.INSTANCE.register("options", Generic.WORLDEDIT_EDGE_COLOR.getName(),    Generic.WORLDEDIT_EDGE_COLOR);
        ConfigManager.INSTANCE.register("options", Generic.WORLDEDIT_POS1_COLOR.getName(),    Generic.WORLDEDIT_POS1_COLOR);
        ConfigManager.INSTANCE.register("options", Generic.WORLDEDIT_POS2_COLOR.getName(),    Generic.WORLDEDIT_POS2_COLOR);
        ConfigManager.INSTANCE.register("options", Generic.WORLDEDIT_GRID_SPACING.getName(),  Generic.WORLDEDIT_GRID_SPACING);
        ConfigManager.INSTANCE.register("options", Generic.WORLDEDIT_GRID_MAX_LINES.getName(), Generic.WORLDEDIT_GRID_MAX_LINES);
        ConfigManager.INSTANCE.register("options", Generic.CBR_WIDTH.getName(),         Generic.CBR_WIDTH);
        ConfigManager.INSTANCE.register("options", Generic.CBR_HEIGHT.getName(),        Generic.CBR_HEIGHT);
        ConfigManager.INSTANCE.register("options", Generic.CBR_FPS.getName(),           Generic.CBR_FPS);
        ConfigManager.INSTANCE.register("options", Generic.CBR_SPEED.getName(),         Generic.CBR_SPEED);
        ConfigManager.INSTANCE.register("options", Generic.CBR_PROJECTION.getName(),    Generic.CBR_PROJECTION);
        ConfigManager.INSTANCE.register("options", Generic.CBR_FOV.getName(),           Generic.CBR_FOV);
        ConfigManager.INSTANCE.register("options", Generic.CBR_ZOOM.getName(),          Generic.CBR_ZOOM);
        ConfigManager.INSTANCE.register("options", Generic.CBR_YAW.getName(),           Generic.CBR_YAW);
        ConfigManager.INSTANCE.register("options", Generic.CBR_PITCH.getName(),         Generic.CBR_PITCH);
        ConfigManager.INSTANCE.register("options", Generic.CBR_PAN_X.getName(),         Generic.CBR_PAN_X);
        ConfigManager.INSTANCE.register("options", Generic.CBR_PAN_Y.getName(),         Generic.CBR_PAN_Y);
        ConfigManager.INSTANCE.register("options", Generic.CBR_ORBIT_SPEED.getName(),   Generic.CBR_ORBIT_SPEED);
        ConfigManager.INSTANCE.register("options", Generic.CBR_BLOCK_ENTITIES.getName(), Generic.CBR_BLOCK_ENTITIES);
        ConfigManager.INSTANCE.register("options", Generic.CBR_ENTITIES.getName(),      Generic.CBR_ENTITIES);
        ConfigManager.INSTANCE.register("options", Generic.CBR_PLAYERS.getName(),       Generic.CBR_PLAYERS);
        ConfigManager.INSTANCE.register("options", Generic.CBR_FORMAT.getName(),        Generic.CBR_FORMAT);
        ConfigManager.INSTANCE.register("options", Generic.CBR_FFMPEG_PATH.getName(),   Generic.CBR_FFMPEG_PATH);
    }
}
