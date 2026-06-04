package org.asutarisucu.Configs;

import java.util.Arrays;
import java.util.List;

/**
 * Backward-compatibility shim. All values forward to {@link Feature}.
 * New code should use {@link Feature} directly.
 */
public final class FeatureToggle {
    public static final Feature LAST_USE_CANCEL                     = Feature.LAST_USE_CANCEL;
    public static final Feature ITEM_RESTOCK                        = Feature.ITEM_RESTOCK;
    public static final Feature AUTO_FILL_INVENTORY                 = Feature.AUTO_FILL_INVENTORY;
    public static final Feature DISABLE_VOID_DIVE                   = Feature.DISABLE_VOID_DIVE;
    public static final Feature SCHEMATIC_RESTRICTION_STATE_WHITELIST = Feature.SCHEMATIC_RESTRICTION_STATE_WHITELIST;
    public static final Feature SIMPLE_ITEM_ENTITY_RENDER           = Feature.SIMPLE_ITEM_ENTITY_RENDER;
    public static final Feature SIMPLE_MOB_ENTITY_RENDER            = Feature.SIMPLE_MOB_ENTITY_RENDER;
    public static final Feature SIMPLE_ENTITY_RENDER_COUNT          = Feature.SIMPLE_ENTITY_RENDER_COUNT;
    public static final Feature ENDERCHEST_MATERIALLIST             = Feature.ENDERCHEST_MATERIALLIST;
    public static final Feature SEARCH_BLOCK_HIGHLIGHT              = Feature.SEARCH_BLOCK_HIGHLIGHT;
    public static final Feature SEARCH_CONTAINER_HIGHLIGHT          = Feature.SEARCH_CONTAINER_HIGHLIGHT;
    public static final Feature PLACEMENT_UPDATE_VIEWER             = Feature.PLACEMENT_UPDATE_VIEWER;
    public static final Feature BREAKING_UPDATE_VIEWER              = Feature.BREAKING_UPDATE_VIEWER;
    public static final Feature UPDATE_VIEW_INSTANT_ONLY            = Feature.UPDATE_VIEW_INSTANT_ONLY;
    public static final Feature UPDATE_SUPPRESSION_VIEW             = Feature.UPDATE_SUPPRESSION_VIEW;

    public static final List<Feature> VALUES = Arrays.asList(Feature.values());

    private FeatureToggle() {}
}
