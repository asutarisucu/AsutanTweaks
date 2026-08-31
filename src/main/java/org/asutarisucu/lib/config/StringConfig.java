package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringConfig implements IConfig<String> {
    private final String name;
    private final String defaultValue;
    private String value;

    public StringConfig(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override public String getName() { return name; }
    @Override public String getValue() { return value; }
    @Override public void setValue(String v) { value = v == null ? "" : v; }
    @Override public String getDefaultValue() { return defaultValue; }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }

    @Override
    public void fromJson(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) setValue(e.getAsString());
    }
}
