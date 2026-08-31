package org.asutarisucu.lib.hotkey;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComboKey {
    /** Canonical name → GLFW key code. */
    private static final Map<String, Integer> KEY_MAP = new HashMap<>();
    /** GLFW key code → canonical name, the inverse of {@link #KEY_MAP} minus the aliases. */
    private static final Map<Integer, String> NAME_MAP = new HashMap<>();

    /** Prefix for keys GLFW reports but this table does not name, so nothing is silently dropped. */
    private static final String RAW_PREFIX = "KEY_";

    private static void def(String name, int code) {
        KEY_MAP.put(name, code);
        NAME_MAP.putIfAbsent(code, name);
    }

    /** Parse-only spelling kept so hotkeys stored by older versions still load. */
    private static void alias(String name, int code) {
        KEY_MAP.put(name, code);
    }

    static {
        for (char c = 'A'; c <= 'Z'; c++) def(String.valueOf(c), GLFW.GLFW_KEY_A + (c - 'A'));
        for (char c = '0'; c <= '9'; c++) def(String.valueOf(c), GLFW.GLFW_KEY_0 + (c - '0'));
        for (int i = 1; i <= 25; i++)     def("F" + i, GLFW.GLFW_KEY_F1 + (i - 1));
        for (int i = 0; i <= 9; i++)      def("KP_" + i, GLFW.GLFW_KEY_KP_0 + i);

        def("KP_DECIMAL",  GLFW.GLFW_KEY_KP_DECIMAL);
        def("KP_DIVIDE",   GLFW.GLFW_KEY_KP_DIVIDE);
        def("KP_MULTIPLY", GLFW.GLFW_KEY_KP_MULTIPLY);
        def("KP_SUBTRACT", GLFW.GLFW_KEY_KP_SUBTRACT);
        def("KP_ADD",      GLFW.GLFW_KEY_KP_ADD);
        def("KP_ENTER",    GLFW.GLFW_KEY_KP_ENTER);
        def("KP_EQUAL",    GLFW.GLFW_KEY_KP_EQUAL);

        def("LEFT_SHIFT",    GLFW.GLFW_KEY_LEFT_SHIFT);
        def("LEFT_CONTROL",  GLFW.GLFW_KEY_LEFT_CONTROL);
        def("LEFT_ALT",      GLFW.GLFW_KEY_LEFT_ALT);
        def("LEFT_SUPER",    GLFW.GLFW_KEY_LEFT_SUPER);
        def("RIGHT_SHIFT",   GLFW.GLFW_KEY_RIGHT_SHIFT);
        def("RIGHT_CONTROL", GLFW.GLFW_KEY_RIGHT_CONTROL);
        def("RIGHT_ALT",     GLFW.GLFW_KEY_RIGHT_ALT);
        def("RIGHT_SUPER",   GLFW.GLFW_KEY_RIGHT_SUPER);
        alias("LSHIFT", GLFW.GLFW_KEY_LEFT_SHIFT);
        alias("LCTRL",  GLFW.GLFW_KEY_LEFT_CONTROL);
        alias("LALT",   GLFW.GLFW_KEY_LEFT_ALT);

        def("SPACE",     GLFW.GLFW_KEY_SPACE);
        def("ENTER",     GLFW.GLFW_KEY_ENTER);
        def("ESCAPE",    GLFW.GLFW_KEY_ESCAPE);
        def("TAB",       GLFW.GLFW_KEY_TAB);
        def("BACKSPACE", GLFW.GLFW_KEY_BACKSPACE);
        def("UP",        GLFW.GLFW_KEY_UP);
        def("DOWN",      GLFW.GLFW_KEY_DOWN);
        def("LEFT",      GLFW.GLFW_KEY_LEFT);
        def("RIGHT",     GLFW.GLFW_KEY_RIGHT);
        def("HOME",      GLFW.GLFW_KEY_HOME);
        def("END",       GLFW.GLFW_KEY_END);
        def("INSERT",    GLFW.GLFW_KEY_INSERT);
        def("DELETE",    GLFW.GLFW_KEY_DELETE);
        def("PAGE_UP",   GLFW.GLFW_KEY_PAGE_UP);
        def("PAGE_DOWN", GLFW.GLFW_KEY_PAGE_DOWN);
        def("MENU",         GLFW.GLFW_KEY_MENU);
        def("CAPS_LOCK",    GLFW.GLFW_KEY_CAPS_LOCK);
        def("NUM_LOCK",     GLFW.GLFW_KEY_NUM_LOCK);
        def("SCROLL_LOCK",  GLFW.GLFW_KEY_SCROLL_LOCK);
        def("PRINT_SCREEN", GLFW.GLFW_KEY_PRINT_SCREEN);
        def("PAUSE",        GLFW.GLFW_KEY_PAUSE);

        def("MINUS",         GLFW.GLFW_KEY_MINUS);
        def("EQUAL",         GLFW.GLFW_KEY_EQUAL);
        def("BACKSLASH",     GLFW.GLFW_KEY_BACKSLASH);
        def("SEMICOLON",     GLFW.GLFW_KEY_SEMICOLON);
        def("APOSTROPHE",    GLFW.GLFW_KEY_APOSTROPHE);
        def("COMMA",         GLFW.GLFW_KEY_COMMA);
        def("PERIOD",        GLFW.GLFW_KEY_PERIOD);
        def("SLASH",         GLFW.GLFW_KEY_SLASH);
        def("GRAVE_ACCENT",  GLFW.GLFW_KEY_GRAVE_ACCENT);
        def("LEFT_BRACKET",  GLFW.GLFW_KEY_LEFT_BRACKET);
        def("RIGHT_BRACKET", GLFW.GLFW_KEY_RIGHT_BRACKET);
        def("WORLD_1",       GLFW.GLFW_KEY_WORLD_1);
        def("WORLD_2",       GLFW.GLFW_KEY_WORLD_2);
    }

    /**
     * Name for a GLFW key code, for both storage and display.
     *
     * Keys outside the table above fall back to {@code KEY_<code>} rather than
     * returning null — an unnamed key used to be dropped from the combo, which
     * left the binding empty.
     */
    public static String nameOf(int keyCode) {
        String name = NAME_MAP.get(keyCode);
        return name != null ? name : RAW_PREFIX + keyCode;
    }

    /** True for the six shift/control/alt keys, which sort to the front of a combo. */
    public static boolean isModifier(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT   || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT
            || keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL
            || keyCode == GLFW.GLFW_KEY_LEFT_ALT     || keyCode == GLFW.GLFW_KEY_RIGHT_ALT;
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

    /** True while every key of the combo is physically down. Unlike {@link #tick}, this is level-triggered. */
    public boolean isHeld(long windowHandle) {
        if (keyCodes.length == 0) return false;
        for (int key : keyCodes) {
            if (GLFW.glfwGetKey(windowHandle, key) != GLFW.GLFW_PRESS) return false;
        }
        return true;
    }

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
            if (code == null && name.startsWith(RAW_PREFIX)) {
                try { code = Integer.valueOf(name.substring(RAW_PREFIX.length())); }
                catch (NumberFormatException ignored) { }
            }
            if (code != null) codes.add(code);
        }
        return codes.stream().mapToInt(Integer::intValue).toArray();
    }
}
