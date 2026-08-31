package org.asutarisucu.tweak.WorldEditGUI;

import org.asutarisucu.AsutanTweaks;

/**
 * Parser for WorldEdit's CUI protocol messages.
 *
 * Messages are pipe-separated UTF-8 text, e.g. {@code s|cuboid} or
 * {@code p|0|12|64|-30|1}. A leading {@code +} marks a "multi" event and is
 * simply stripped. Everything we do not understand is ignored.
 */
public final class CuiParser {

    private static boolean loggedSelection;

    private CuiParser() {}

    public static void handle(String message) {
        if (message == null || message.isEmpty()) return;
        // A packet may carry several events separated by the ASCII record separator.
        for (String part : message.split("\\x1E")) {
            handleSingle(part);
        }
        if (!loggedSelection) {
            int[] bounds = WorldEditSelection.getBounds();
            if (bounds != null) {
                loggedSelection = true;
                AsutanTweaks.LOGGER.info("[WorldEditGUI] selection parsed: {},{},{} .. {},{},{}",
                        bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
            }
        }
    }

    private static void handleSingle(String raw) {
        String msg = trimToEvent(raw);
        if (msg.isEmpty()) return;
        String[] p = msg.split("\\|");
        switch (p[0]) {
            case "s" -> {
                if (p.length >= 2) WorldEditSelection.setShape(p[1]);
            }
            case "p" -> {
                if (p.length < 5) return;
                Integer id = parseInt(p[1]);
                Integer x = parseCoord(p[2]);
                Integer y = parseCoord(p[3]);
                Integer z = parseCoord(p[4]);
                if (id != null && x != null && y != null && z != null) {
                    WorldEditSelection.setPoint(id, x, y, z);
                }
            }
            default -> AsutanTweaks.LOGGER.debug("[WorldEditGUI] ignoring CUI event {}", escape(msg));
        }
    }

    /** Event ids this parser acts on. */
    private static final java.util.Set<String> EVENT_IDS = java.util.Set.of("s", "p");

    /**
     * Drops anything before the event id.
     *
     * The message does not always start at the beginning of the decoded text —
     * WorldEdit's payload keeps a flag next to it, and some platforms frame the
     * body with a length prefix. Rather than guess at the shape, scan for the
     * first token that is an event id and read from there.
     */
    private static String trimToEvent(String raw) {
        String msg = raw.startsWith("+") ? raw.substring(1) : raw;
        int offset = 0;
        while (offset < msg.length()) {
            int pipe = msg.indexOf('|', offset);
            if (pipe < 0) break;
            if (EVENT_IDS.contains(msg.substring(offset, pipe))) return msg.substring(offset);
            offset = pipe + 1;
        }
        return msg;
    }

    /** Renders control characters visibly, for log lines about messages we could not use. */
    static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == 0x7F) sb.append(String.format("\\x%02X", (int) c));
            else sb.append(c);
        }
        return sb.toString();
    }

    private static Integer parseInt(String s) {
        try { return Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    /** Coordinates are integers for cuboids but WorldEdit sends doubles for some shapes. */
    private static Integer parseCoord(String s) {
        try { return (int) Math.floor(Double.parseDouble(s.trim())); }
        catch (NumberFormatException e) { return null; }
    }
}
