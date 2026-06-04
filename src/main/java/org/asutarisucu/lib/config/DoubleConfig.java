package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DoubleConfig implements IConfig<Double> {
    private final String name;
    private final double defaultValue;
    private final double min;
    private final double max;
    private double value;

    public DoubleConfig(String name, double defaultValue, double min, double max) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override public String getName() { return name; }
    @Override public Double getValue() { return value; }
    @Override public void setValue(Double v) { value = Math.max(min, Math.min(max, v)); }
    @Override public Double getDefaultValue() { return defaultValue; }
    public double getDoubleValue() { return value; }
    public double getMin() { return min; }
    public double getMax() { return max; }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }

    @Override
    public void fromJson(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) setValue(e.getAsDouble());
    }
}
