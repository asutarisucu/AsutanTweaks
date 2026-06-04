package org.asutarisucu.lib.hotkey;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComboKey {
    private static final Map<String, Integer> KEY_MAP = new HashMap<>();

    static {
        // Letters
        for (char c = 'A'; c <= 'Z'; c++) KEY_MAP.put(String.valueOf(c), GLFW.GLFW_KEY_A + (c - 'A'));
        // Digits
        for (char c = '0'; c <= '9'; c++) KEY_MAP.put(String.valueOf(c), GLFW.GLFW_KEY_0 + (c - '0'));
        // Function keys
        for (int i = 1; i <= 12; i++) KEY_MAP.put("F" + i, GLFW.GLFW_KEY_F1 + (i - 1));
        // Modifiers
        KEY_MAP.put("LEFT_SHIFT",   GLFW.GLFW_KEY_LEFT_SHIFT);
        KEY_MAP.put("LEFT_CONTROL", GLFW.GLFW_KEY_LEFT_CONTROL);
        KEY_MAP.put("LEFT_ALT",     GLFW.GLFW_KEY_LEFT_ALT);
        KEY_MAP.put("RIGHT_SHIFT",  GLFW.GLFW_KEY_RIGHT_SHIFT);
        KEY_MAP.put("RIGHT_CONTROL",GLFW.GLFW_KEY_RIGHT_CONTROL);
        KEY_MAP.put("RIGHT_ALT",    GLFW.GLFW_KEY_RIGHT_ALT);
        KEY_MAP.put("LSHIFT",  GLFW.GLFW_KEY_LEFT_SHIFT);
        KEY_MAP.put("LCTRL",   GLFW.GLFW_KEY_LEFT_CONTROL);
        KEY_MAP.put("LALT",    GLFW.GLFW_KEY_LEFT_ALT);
        // Other common keys
        KEY_MAP.put("SPACE",  GLFW.GLFW_KEY_SPACE);
        KEY_MAP.put("ENTER",  GLFW.GLFW_KEY_ENTER);
        KEY_MAP.put("ESCAPE", GLFW.GLFW_KEY_ESCAPE);
        KEY_MAP.put("TAB",    GLFW.GLFW_KEY_TAB);
        KEY_MAP.put("UP",     GLFW.GLFW_KEY_UP);
        KEY_MAP.put("DOWN",   GLFW.GLFW_KEY_DOWN);
        KEY_MAP.put("LEFT",   GLFW.GLFW_KEY_LEFT);
        KEY_MAP.put("RIGHT",  GLFW.GLFW_KEY_RIGHT);
        KEY_MAP.put("HOME",   GLFW.GLFW_KEY_HOME);
        KEY_MAP.put("END",    GLFW.GLFW_KEY_END);
        KEY_MAP.put("INSERT", GLFW.GLFW_KEY_INSERT);
        KEY_MAP.put("DELETE", GLFW.GLFW_KEY_DELETE);
        KEY_MAP.put("PAGE_UP",   GLFW.GLFW_KEY_PAGE_UP);
        KEY_MAP.put("PAGE_DOWN", GLFW.GLFW_KEY_PAGE_DOWN);
        KEY_MAP.put("MINUS",     GLFW.GLFW_KEY_MINUS);
        KEY_MAP.put("EQUAL",     GLFW.GLFW_KEY_EQUAL);
        KEY_MAP.put("BACKSLASH", GLFW.GLFW_KEY_BACKSLASH);
        KEY_MAP.put("SEMICOLON", GLFW.GLFW_KEY_SEMICOLON);
        KEY_MAP.put("APOSTROPHE",GLFW.GLFW_KEY_APOSTROPHE);
        KEY_MAP.put("COMMA",     GLFW.GLFW_KEY_COMMA);
        KEY_MAP.put("PERIOD",    GLFW.GLFW_KEY_PERIOD);
        KEY_MAP.put("SLASH",     GLFW.GLFW_KEY_SLASH);
        KEY_MAP.put("GRAVE_ACCENT", GLFW.GLFW_KEY_GRAVE_ACCENT);
        KEY_MAP.put("LEFT_BRACKET",  GLFW.GLFW_KEY_LEFT_BRACKET);
        KEY_MAP.put("RIGHT_BRACKET", GLFW.GLFW_KEY_RIGHT_BRACKET);
        KEY_MAP.put("KP_0", GLFW.GLFW_KEY_KP_0); KEY_MAP.put("KP_1", GLFW.GLFW_KEY_KP_1);
        KEY_MAP.put("KP_2", GLFW.GLFW_KEY_KP_2); KEY_MAP.put("KP_3", GLFW.GLFW_KEY_KP_3);
        KEY_MAP.put("KP_4", GLFW.GLFW_KEY_KP_4); KEY_MAP.put("KP_5", GLFW.GLFW_KEY_KP_5);
        KEY_MAP.put("KP_6", GLFW.GLFW_KEY_KP_6); KEY_MAP.put("KP_7", GLFW.GLFW_KEY_KP_7);
        KEY_MAP.put("KP_8", GLFW.GLFW_KEY_KP_8); KEY_MAP.put("KP_9", GLFW.GLFW_KEY_KP_9);
    }

    private int[] keyCodes;
    private String storageString;
    private HotkeyCallback callback;
    private boolean wasPressed;

    public ComboKey(String storageString) {
        setFromString(storageString);
    }

    public void setFromString(String s) {
        this.storageString = s == null ? "" : s.trim();
        this.keyCodes = parse(this.storageString);
        this.wasPressed = false;
    }

    public String getStorageString() { return storageString; }
    public boolean isEmpty() { return keyCodes.length == 0; }
    public void setCallback(HotkeyCallback callback) { this.callback = callback; }

    void tick(long windowHandle) {
        if (keyCodes.length == 0) { wasPressed = false; return; }
        boolean allDown = true;
        for (int key : keyCodes) {
            if (GLFW.glfwGetKey(windowHandle, key) != GLFW.GLFW_PRESS) {
                allDown = false;
                break;
            }
        }
        if (allDown && !wasPressed && callback != null) {
            callback.onActivate();
        }
        wasPressed = allDown;
    }

    private static int[] parse(String s) {
        if (s == null || s.isBlank()) return new int[0];
        String[] parts = s.split(",");
        List<Integer> codes = new ArrayList<>();
        for (String part : parts) {
            String name = part.trim().toUpperCase();
            Integer code = KEY_MAP.get(name);
            if (code != null) codes.add(code);
        }
        return codes.stream().mapToInt(Integer::intValue).toArray();
    }
}
