package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanConfig implements IConfig<Boolean> {
    private final String name;
    private final boolean defaultValue;
    private boolean value;

    public BooleanConfig(String name, boolean defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    @Override public String getName() { return name; }
    @Override public Boolean getValue() { return value; }
    @Override public void setValue(Boolean v) { value = v; }
    @Override public Boolean getDefaultValue() { return defaultValue; }

    public boolean getBooleanValue() { return value; }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }

    @Override
    public void fromJson(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) value = e.getAsBoolean();
    }
}
