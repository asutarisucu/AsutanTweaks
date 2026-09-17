package org.asutarisucu.GUI.glass;

import org.lwjgl.glfw.GLFW;

import java.util.function.IntPredicate;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
//#else
//$$ import net.minecraft.client.Minecraft;
//#endif

/**
 * One line of editable text: cursor movement, deletion and paste.
 *
 * Characters come in through {@link #type}, which each screen calls from its
 * version's char callback, so input methods such as Japanese IMEs work.
 */
public final class TextField {

    private String text = "";
    private int cursor;
    private int scroll;
    private final int maxLength;
    private final IntPredicate allowed;

    public TextField(int maxLength, IntPredicate allowed) {
        this.maxLength = maxLength;
        this.allowed = allowed;
    }

    public static TextField plain() {
        return new TextField(512, c -> c >= 32 && c != 127);
    }

    public static TextField numeric() {
        return new TextField(32, c -> (c >= '0' && c <= '9') || c == '-' || c == '.');
    }

    public static TextField hex() {
        return new TextField(9, c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == '#');
    }

    public String text() {
        return text;
    }

    public void set(String s) {
        text = s == null ? "" : s;
        cursor = text.length();
        scroll = 0;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    /** Inserts typed or pasted text at the cursor, dropping characters the field does not take. */
    public void type(String s) {
        StringBuilder in = new StringBuilder();
        s.codePoints().filter(allowed).forEach(in::appendCodePoint);
        if (in.length() == 0) return;
        int room = maxLength - text.length();
        if (room <= 0) return;
        String add = in.length() > room ? in.substring(0, room) : in.toString();
        text = text.substring(0, cursor) + add + text.substring(cursor);
        cursor += add.length();
    }

    /** @return true when the key was an editing key and has been handled */
    public boolean key(int keyCode) {
        boolean ctrl = ctrlDown();
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (cursor > 0) {
                    int from = ctrl ? wordStart(cursor) : cursor - 1;
                    text = text.substring(0, from) + text.substring(cursor);
                    cursor = from;
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (cursor < text.length()) text = text.substring(0, cursor) + text.substring(cursor + 1);
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (cursor > 0) cursor = ctrl ? wordStart(cursor) : cursor - 1;
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (cursor < text.length()) cursor++;
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                cursor = 0;
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                cursor = text.length();
                return true;
            }
            case GLFW.GLFW_KEY_V -> {
                if (!ctrl) return false;
                type(clipboard().replace('\n', ' ').replace('\r', ' '));
                return true;
            }
            case GLFW.GLFW_KEY_A -> {
                // No selection support: Ctrl+A clears so the field can be retyped.
                if (!ctrl) return false;
                set("");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private int wordStart(int from) {
        int i = from;
        while (i > 0 && text.charAt(i - 1) == ' ') i--;
        while (i > 0 && text.charAt(i - 1) != ' ') i--;
        return i;
    }

    /**
     * Draws the text clipped to {@code w}, scrolled so the cursor stays visible,
     * with a blinking cursor when focused.
     */
    public void draw(Gfx g, int x, int y, int w, int color, String placeholder, int placeholderColor, boolean focused) {
        if (text.isEmpty()) {
            if (!placeholder.isEmpty()) g.text(g.ellipsize(placeholder, w), x, y, placeholderColor, false, false);
            if (focused && blink()) g.fill(x, y - 1, x + 1, y + 9, color);
            return;
        }
        // Keep the cursor inside the box.
        if (cursor < scroll) scroll = cursor;
        while (scroll < cursor && g.width(text.substring(scroll, cursor)) > w - 2) scroll++;
        String visible = text.substring(scroll);
        while (!visible.isEmpty() && g.width(visible) > w) visible = visible.substring(0, visible.length() - 1);
        g.text(visible, x, y, color, false, false);
        if (focused && blink()) {
            int cx = x + g.width(text.substring(scroll, Math.min(cursor, scroll + visible.length())));
            g.fill(cx, y - 1, cx + 1, y + 9, color);
        }
    }

    private static boolean blink() {
        return (System.currentTimeMillis() / 530) % 2 == 0;
    }

    public static boolean ctrlDown() {
        long win = windowHandle();
        return GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SUPER) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SUPER) == GLFW.GLFW_PRESS;
    }

    public static boolean shiftDown() {
        long win = windowHandle();
        return GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private static long windowHandle() {
//#if MC < 260100
        return MinecraftClient.getInstance().getWindow().getHandle();
//#else
//$$ return Minecraft.getInstance().getWindow().handle();
//#endif
    }

    private static String clipboard() {
        try {
//#if MC < 260100
            return MinecraftClient.getInstance().keyboard.getClipboard();
//#else
//$$ return Minecraft.getInstance().keyboardHandler.getClipboard();
//#endif
        } catch (Throwable t) {
            return "";
        }
    }
}
