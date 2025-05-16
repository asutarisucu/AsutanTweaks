package org.asutarisucu.Configs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;

import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import org.asutarisucu.Reference;

import java.io.File;

public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";
    //Genericタブに表示させたいConfig項目を書く
    public static class Generic{
        public static final ConfigInteger RESTOCK_COUNT=new ConfigInteger("RestockCount",32,0,64,"MainHand Item Count using ItemRestock");
        public static final ConfigDouble VOID_HEIGHT_OW=new ConfigDouble("VoidHeight_OW",-80,-200,-64,"In OverWorld,Below this is Void");
        public static final ConfigDouble VOID_HEIGHT_NE=new ConfigDouble("VoidHeight_NE",-50,-100,0,"In The Nether,Below this is Void");
        public static final ConfigDouble VOID_HEIGHT_END=new ConfigDouble("VoidHeight_END",-50,-100,0,"In The End,Below this is Void");
        public static final ConfigBoolean VOID_DISCONNECT=new ConfigBoolean("VoidDisconnect",false,"if you cant safe yourself,disconnect world");
        public static final ConfigStringList RESTRICTION_STATE_WHITELIST=new ConfigStringList("RestrictionStateWhiteList",ImmutableList.of("UP","DOWN","NORTH","EAST","SOUTH","WEST"),"Restriction Check State WhiteList");
        public static final ConfigStringList LAST_USE_CANCEL_BLACKLIST=new ConfigStringList("LastUseCancelBlackList",ImmutableList.of("chest","Shulker_box"),"ignore RestockItem Interact this List Block");
        public static final ConfigStringList ENDERCHEST_MATERIALLIST_WHITELIST=new ConfigStringList("EnderChestMaterialListWhiteList",ImmutableList.of("white"),"MaterialList Check box WhiteList");
        public static final ConfigStringList ENDERCHEST_MATERIALLIST_BLACKLIST=new ConfigStringList("EnderChestMaterialListBlackList",ImmutableList.of("white"),"MaterialList Check box BlackList");
        public static final ConfigOptionList ENDERCHEST_MATERIALLIST_FILTERTYPE=new ConfigOptionList("EnderChestMaterialListFilter", UsageRestriction.ListType.NONE,"EnderChestMaterialList Check box Whitelist or Blacklist");
        public static final ConfigStringList HIGHLIGHT_ITEM_LIST=new ConfigStringList("HighLightItemList",ImmutableList.of(),"HighLight something name list");
        public static final ConfigColor HIGHLIGHT_BLOCK_COLOR=new ConfigColor("HighlightBlockColor","#FFFF0000","Highlight color BlockOutline");
        public static final ConfigInteger HIGHLIGHT_BLOCK_RANGE=new ConfigInteger("HighlightBlockRange",32,0,64,true,"Range to Search for blocks to highlight");
        public static final ConfigColor HIGHLIGHT_CONTAINER_COLOR=new ConfigColor("HighlightContainerColor","#FF0000FF","Highlight color ContainerOutline");
        public static final ConfigInteger HIGHLIGHT_CONTAINER_RANGE=new ConfigInteger("HighlightBlockRange",32,0,64,true,"Range to Search for Container to highlight");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                RESTOCK_COUNT,
                VOID_HEIGHT_OW,
                VOID_HEIGHT_NE,
                VOID_HEIGHT_END,
                VOID_DISCONNECT,
                RESTRICTION_STATE_WHITELIST,
                LAST_USE_CANCEL_BLACKLIST,
                ENDERCHEST_MATERIALLIST_WHITELIST,
                ENDERCHEST_MATERIALLIST_BLACKLIST,
                ENDERCHEST_MATERIALLIST_FILTERTYPE,
                HIGHLIGHT_ITEM_LIST,
                HIGHLIGHT_BLOCK_COLOR,
                HIGHLIGHT_BLOCK_RANGE,
                HIGHLIGHT_CONTAINER_COLOR,
                HIGHLIGHT_CONTAINER_RANGE
        );
    }

    public static void loadFromFile() {
        File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);
        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                ConfigUtils.readHotkeys(root, "GenericHotkeys", Hotkeys.HOTKEY_LIST);
                ConfigUtils.readConfigBase(root,"Option", Generic.OPTIONS);
                ConfigUtils.readHotkeyToggleOptions(root, "TweakHotkeys", "TweakToggles", FeatureToggle.VALUES);
            }
        }
    }

    public static void saveToFile() {
        File dir = FileUtils.getConfigDirectory();
        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            JsonObject root = new JsonObject();

            ConfigUtils.writeHotkeys(root, "GenericHotkeys", Hotkeys.HOTKEY_LIST);
            ConfigUtils.writeConfigBase(root,"Option", Generic.OPTIONS);
            ConfigUtils.writeHotkeyToggleOptions(root, "TweakHotkeys", "TweakToggles", FeatureToggle.VALUES);

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }
}