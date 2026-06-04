package org.asutarisucu.lib.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class StringListConfig implements IConfig<List<String>> {
    private final String name;
    private final List<String> defaultValue;
    private List<String> value;

    public StringListConfig(String name, List<String> defaultValue) {
        this.name = name;
        this.defaultValue = List.copyOf(defaultValue);
        this.value = new ArrayList<>(defaultValue);
    }

    @Override public String getName() { return name; }
    @Override public List<String> getValue() { return value; }
    @Override public void setValue(List<String> v) { value = new ArrayList<>(v); }
    @Override public List<String> getDefaultValue() { return defaultValue; }
    public List<String> getStrings() { return value; }

    @Override
    public JsonElement toJson() {
        JsonArray arr = new JsonArray();
        for (String s : value) arr.add(s);
        return arr;
    }

    @Override
    public void fromJson(JsonElement e) {
        if (e == null || !e.isJsonArray()) return;
        List<String> list = new ArrayList<>();
        for (JsonElement el : e.getAsJsonArray()) list.add(el.getAsString());
        value = list;
    }
}
