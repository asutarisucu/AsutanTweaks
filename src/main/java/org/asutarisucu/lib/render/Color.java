package org.asutarisucu.lib.render;

public final class Color {
    private final int argb;

    private Color(int argb) {
        this.argb = argb;
    }

    public static Color fromArgb(int argb) {
        return new Color(argb);
    }

    public static Color fromRgb(float r, float g, float b) {
        return fromRgba(r, g, b, 1f);
    }

    public static Color fromRgba(float r, float g, float b, float a) {
        int ai = (int)(a * 255f) & 0xFF;
        int ri = (int)(r * 255f) & 0xFF;
        int gi = (int)(g * 255f) & 0xFF;
        int bi = (int)(b * 255f) & 0xFF;
        return new Color((ai << 24) | (ri << 16) | (gi << 8) | bi);
    }

    public static Color fromHex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() == 6) h = "FF" + h;
        return new Color((int) Long.parseLong(h, 16));
    }

    public int argb()  { return argb; }
    public float a()   { return ((argb >> 24) & 0xFF) / 255f; }
    public float r()   { return ((argb >> 16) & 0xFF) / 255f; }
    public float g()   { return ((argb >> 8)  & 0xFF) / 255f; }
    public float b()   { return  (argb        & 0xFF) / 255f; }

    public int aInt()  { return (argb >> 24) & 0xFF; }
    public int rInt()  { return (argb >> 16) & 0xFF; }
    public int gInt()  { return (argb >> 8)  & 0xFF; }
    public int bInt()  { return  argb        & 0xFF; }

    public Color withAlpha(float a) {
        return fromRgba(r(), g(), b(), a);
    }

    @Override
    public String toString() {
        return String.format("#%08X", argb);
    }
}
