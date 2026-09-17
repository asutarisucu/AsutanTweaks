package org.asutarisucu.GUI.glass;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.config.UiLanguage;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
//#else
//$$ import net.minecraft.client.Minecraft;
//#endif

/**
 * Strings of the config screen.
 *
 * The files are read straight from the jar rather than through the game's
 * language, so the screen can be switched between English and Japanese without
 * changing the language of the whole game.
 */
public final class Lang {

    private static final String PREFIX = "asutantweaks.";
    private static final Map<String, Map<String, String>> TABLES = new HashMap<>();

    private Lang() {}

    public static boolean japanese() {
        UiLanguage l = Configs.Ui.LANGUAGE.getValue();
        if (l == UiLanguage.AUTO) return gameLanguage().startsWith("ja");
        return l == UiLanguage.JAPANESE;
    }

    /** The game's language code, such as "ja_jp". */
    public static String gameLanguage() {
        try {
//#if MC < 260100
            return MinecraftClient.getInstance().getLanguageManager().getLanguage().toLowerCase(Locale.ROOT);
//#else
//$$ return Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase(Locale.ROOT);
//#endif
        } catch (Throwable t) {
            return "en_us";
        }
    }

    /** Looks up {@code asutantweaks.<key>}; falls back to English, then to {@code fallback}. */
    public static String get(String key, String fallback) {
        String full = PREFIX + key;
        String v = null;
        if (japanese()) v = table("ja_jp").get(full);
        if (v == null) v = table("en_us").get(full);
        return v != null ? v : fallback;
    }

    public static String get(String key) {
        return get(key, key);
    }

    public static String format(String key, Object... args) {
        return String.format(get(key), args);
    }

    /** The English text, whatever the language. */
    public static String english(String key, String fallback) {
        String v = table("en_us").get(PREFIX + key);
        return v != null ? v : fallback;
    }

    public static String enumName(Enum<?> e) {
        String key = "enum." + e.getDeclaringClass().getSimpleName().toLowerCase(Locale.ROOT)
                + "." + e.name().toLowerCase(Locale.ROOT);
        return get(key, e.name());
    }

    private static Map<String, String> table(String code) {
        return TABLES.computeIfAbsent(code, Lang::load);
    }

    private static Map<String, String> load(String code) {
        Map<String, String> map = new HashMap<>();
        String path = "/assets/asutantweaks/lang/" + code + ".json";
        try (InputStream in = Lang.class.getResourceAsStream(path)) {
            if (in == null) return map;
            JsonObject obj = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                map.put(e.getKey(), e.getValue().getAsString());
            }
        } catch (Exception e) {
            AsutanTweaks.LOGGER.warn("[Lang] Failed to read {}", path, e);
        }
        return map;
    }
}
