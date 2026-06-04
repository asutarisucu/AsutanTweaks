package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.asutarisucu.lib.hotkey.ComboKey;
import org.asutarisucu.lib.hotkey.HotkeyCallback;

public class FeatureConfig implements IConfig<Boolean> {
    private final String name;
    private final boolean defaultEnabled;
    private boolean enabled;
    private final ComboKey hotkey;

    public FeatureConfig(String name, boolean defaultEnabled, String defaultHotkey) {
        this.name = name;
        this.defaultEnabled = defaultEnabled;
        this.enabled = defaultEnabled;
        this.hotkey = new ComboKey(defaultHotkey);
    }

    @Override public String getName() { return name; }
    @Override public Boolean getValue() { return enabled; }
    @Override public void setValue(Boolean v) { enabled = v; }
    @Override public Boolean getDefaultValue() { return defaultEnabled; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { enabled = v; }
    public void toggle() { enabled = !enabled; }

    public ComboKey getHotkey() { return hotkey; }
    public void setHotkeyCallback(HotkeyCallback cb) { hotkey.setCallback(cb); }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", enabled);
        obj.addProperty("hotkey", hotkey.getStorageString());
        return obj;
    }

    @Override
    public void fromJson(JsonElement e) {
        if (e == null || !e.isJsonObject()) return;
        JsonObject obj = e.getAsJsonObject();
        if (obj.has("enabled")) enabled = obj.get("enabled").getAsBoolean();
        if (obj.has("hotkey")) hotkey.setFromString(obj.get("hotkey").getAsString());
    }
}
