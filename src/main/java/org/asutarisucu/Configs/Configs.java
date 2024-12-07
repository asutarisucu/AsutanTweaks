package org.asutarisucu.Configs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
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

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                RESTOCK_COUNT,
                VOID_HEIGHT_OW,
                VOID_HEIGHT_NE,
                VOID_HEIGHT_END,
                VOID_DISCONNECT
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