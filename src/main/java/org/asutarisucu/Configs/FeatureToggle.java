package org.asutarisucu.Configs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigNotifiable;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.StringUtils;
import org.asutarisucu.AsutanTweaks;

public enum FeatureToggle implements IHotkeyTogglable, IConfigNotifiable<IConfigBoolean> {

    //toggleボタンで表示させたいConfig項目
    LAST_USE_CANCEL("LastUseCancel",false,"","You cant Use LastItem"),
    ITEM_RESTOCK("ItemRestock",false,"","Item Restock to Use Item"),
    AUTO_FILL_INVENTORY("AutoFillInventory",false,"","Fill Material in Your Inventory"),
    DISABLE_VOID_DIVE("DisableVoidDive",false,"","Protects you from VoidDive"),
    SCHEMATIC_RESTRICTION_STATE_WHITELIST("SchematicRestrictionStateWhiteList",false,"","Only one can be placed per square.to use Restriction"),
    SIMPLE_ITEM_ENTITY_RENDER("SimpleItemEntityRender",false,"","Combine many ItemEntities into one"),
    SIMPLE_MOB_ENTITY_RENDER("SimpleMobEntityRender",false,"","many MobEntity into one"),
    SIMPLE_ENTITY_RENDER_COUNT("SimpleEntityRenderCount",false,"","SimpleEntityRender nearItem Count Display");

    public static final ImmutableList<FeatureToggle> VALUES = ImmutableList.copyOf(values());

    private final String name;
    private final String comment;
    private final String prettyName;
    private final IKeybind keybind;
    private final boolean defaultValueBoolean;
    private final boolean singlePlayer;
    private boolean valueBoolean;
    private IValueChangeCallback<IConfigBoolean> callback;

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment) {
        this(name, defaultValue, false, defaultHotkey, KeybindSettings.DEFAULT, comment);
    }
    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment) {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT, comment);
    }
    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, KeybindSettings settings, String comment) {
        this(name, defaultValue, false, defaultHotkey, settings, comment);
    }
    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, KeybindSettings settings, String comment) {
        this(name, defaultValue, singlePlayer, defaultHotkey, settings, comment, StringUtils.splitCamelCase(name.substring(5)));
    }
    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment, String prettyName) {
        this(name, defaultValue, false, defaultHotkey, comment, prettyName);
    }
    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment, String prettyName) {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT, comment, prettyName);
    }
    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, KeybindSettings settings, String comment, String prettyName) {
        this.name = name;
        this.valueBoolean = defaultValue;
        this.defaultValueBoolean = defaultValue;
        this.singlePlayer = singlePlayer;
        this.comment = comment;
        this.prettyName = prettyName;
        this.keybind = KeybindMulti.fromStorageString(defaultHotkey, settings);
        this.keybind.setCallback(new KeyCallbackToggleBooleanConfigWithMessage(this));
    }
    @Override
    public ConfigType getType() {
        return ConfigType.HOTKEY;
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public String getConfigGuiDisplayName() {
        String name = StringUtils.getTranslatedOrFallback("config.name." + this.getName().toLowerCase(), this.getName());
        if (this.singlePlayer) {
            return GuiBase.TXT_GOLD + name + GuiBase.TXT_RST;
        }
        return name;
    }
    @Override
    public String getPrettyName() {
        return this.prettyName;
    }
    @Override
    public String getStringValue() {
        return String.valueOf(this.valueBoolean);
    }
    @Override
    public String getDefaultStringValue() {
        return String.valueOf(this.defaultValueBoolean);
    }
    @Override
    public void setValueFromString(String value) {
    }
    @Override
    public void onValueChanged() {
        if (this.callback != null) {
            this.callback.onValueChanged(this);
        }
    }
    @Override
    public void setValueChangeCallback(IValueChangeCallback<IConfigBoolean> callback) {
        this.callback = callback;
    }
    @Override
    public String getComment() {
        String comment = StringUtils.getTranslatedOrFallback("config.comment." + this.getName().toLowerCase(), this.comment);
        if (comment != null && this.singlePlayer) {
            return comment + "\n" + StringUtils.translate("tweakeroo.label.config_comment.single_player_only");
        }
        return comment;
    }
    @Override
    public IKeybind getKeybind() {
        return this.keybind;
    }
    @Override
    public boolean getBooleanValue() {
        return this.valueBoolean;
    }
    @Override
    public boolean getDefaultBooleanValue() {
        return this.defaultValueBoolean;
    }
    @Override
    public void setBooleanValue(boolean value) {
        boolean oldValue = this.valueBoolean;
        this.valueBoolean = value;
        if (oldValue != this.valueBoolean) {
            this.onValueChanged();
        }
    }
    @Override
    public boolean isModified() {
        return this.valueBoolean != this.defaultValueBoolean;
    }
    @Override
    public boolean isModified(String newValue) {
        return Boolean.parseBoolean(newValue) != this.defaultValueBoolean;
    }
    @Override
    public void resetToDefault() {
        this.valueBoolean = this.defaultValueBoolean;
    }
    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(this.valueBoolean);
    }
    @Override
    public void setValueFromJsonElement(JsonElement element) {
        try {
            if (element.isJsonPrimitive()) {
                this.valueBoolean = element.getAsBoolean();
            } else {
                AsutanTweaks.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element);
            }
        }
        catch (Exception e) {
            AsutanTweaks.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element, e);
        }
    }
}