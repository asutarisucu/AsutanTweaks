package org.asutarisucu.lib.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.asutarisucu.AsutanTweaks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {
    public static final ConfigManager INSTANCE = new ConfigManager();

    private static final int VERSION = 1;
    private static final String FILE_NAME = "AsutanTweaks.json";

    private final Map<String, Map<String, IConfig<?>>> sections = new LinkedHashMap<>();

    private ConfigManager() {}

    public void register(String section, String key, IConfig<?> config) {
        sections.computeIfAbsent(section, k -> new LinkedHashMap<>()).put(key, config);
    }

    public void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (!Files.exists(file)) return;
        try {
            String json = Files.readString(file);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            for (Map.Entry<String, Map<String, IConfig<?>>> section : sections.entrySet()) {
                if (!root.has(section.getKey())) continue;
                JsonElement sectionEl = root.get(section.getKey());
                if (!sectionEl.isJsonObject()) continue;
                JsonObject sectionObj = sectionEl.getAsJsonObject();
                for (Map.Entry<String, IConfig<?>> entry : section.getValue().entrySet()) {
                    if (sectionObj.has(entry.getKey())) {
                        entry.getValue().fromJson(sectionObj.get(entry.getKey()));
                    }
                }
            }
        } catch (Exception e) {
            AsutanTweaks.LOGGER.warn("[ConfigManager] Failed to load config", e);
        }
    }

    public void save() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        try {
            Files.createDirectories(dir);
            JsonObject root = new JsonObject();
            root.addProperty("version", VERSION);
            for (Map.Entry<String, Map<String, IConfig<?>>> section : sections.entrySet()) {
                JsonObject sectionObj = new JsonObject();
                for (Map.Entry<String, IConfig<?>> entry : section.getValue().entrySet()) {
                    sectionObj.add(entry.getKey(), entry.getValue().toJson());
                }
                root.add(section.getKey(), sectionObj);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(dir.resolve(FILE_NAME), gson.toJson(root));
        } catch (IOException e) {
            AsutanTweaks.LOGGER.warn("[ConfigManager] Failed to save config", e);
        }
    }
}
