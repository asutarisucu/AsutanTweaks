package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class OptionListConfig<E extends Enum<E>> implements IConfig<E> {
    private final String name;
    private final Class<E> enumClass;
    private E value;
    private final E defaultValue;

    public OptionListConfig(String name, E defaultValue, Class<E> enumClass) {
        this.name = name;
        this.enumClass = enumClass;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    @Override public String getName() { return name; }
    @Override public E getValue() { return value; }
    @Override public void setValue(E v) { value = v; }
    @Override public E getDefaultValue() { return defaultValue; }

    public E[] getAllValues() { return enumClass.getEnumConstants(); }

    @Override
    public JsonElement toJson() { return new JsonPrimitive(value.name().toLowerCase()); }

    @Override
    public void fromJson(JsonElement e) {
        if (e == null || !e.isJsonPrimitive()) return;
        try { value = Enum.valueOf(enumClass, e.getAsString().toUpperCase()); }
        catch (Exception ignored) { value = defaultValue; }
    }
}
