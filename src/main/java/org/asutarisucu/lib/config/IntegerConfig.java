package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntegerConfig implements IConfig<Integer> {
    private final String name;
    private final int defaultValue;
    private final int min;
    private final int max;
    private int value;

    public IntegerConfig(String name, int defaultValue, int min, int max) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override public String getName() { return name; }
    @Override public Integer getValue() { return value; }
    @Override public void setValue(Integer v) { value = Math.max(min, Math.min(max, v)); }
    @Override public Integer getDefaultValue() { return defaultValue; }
    public int getIntegerValue() { return value; }
    public int getMin() { return min; }
    public int getMax() { return max; }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }

    @Override
    public void fromJson(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) setValue(e.getAsInt());
    }
}
