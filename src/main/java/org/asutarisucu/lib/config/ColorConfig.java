package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.asutarisucu.lib.render.Color;

public class ColorConfig implements IConfig<Integer> {
    private final String name;
    private final int defaultValue;
    private int value;

    public ColorConfig(String name, String hexDefault) {
        this.name = name;
        this.defaultValue = parseHex(hexDefault);
        this.value = this.defaultValue;
    }

    @Override public String getName() { return name; }
    @Override public Integer getValue() { return value; }
    @Override public void setValue(Integer v) { value = v; }
    @Override public Integer getDefaultValue() { return defaultValue; }

    public Color getColor() { return Color.fromArgb(value); }

    private static int parseHex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() == 6) h = "FF" + h;
        return (int) Long.parseLong(h, 16);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(String.format("#%08X", value));
    }

    @Override
    public void fromJson(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) {
            try { value = parseHex(e.getAsString()); } catch (Exception ignored) {}
        }
    }
}
