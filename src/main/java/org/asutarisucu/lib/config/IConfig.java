package org.asutarisucu.lib.config;

import com.google.gson.JsonElement;

public interface IConfig<T> {
    String getName();
    T getValue();
    void setValue(T value);
    T getDefaultValue();
    default void reset() { setValue(getDefaultValue()); }
    JsonElement toJson();
    void fromJson(JsonElement element);
}
