package org.asutarisucu.GUI.glass;

/** Easing curves and frame-rate independent smoothing for the config screen. */
public final class Anim {

    private Anim() {}

    public static float clamp01(float t) {
        return t < 0 ? 0 : (t > 1 ? 1 : t);
    }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1 - t;
        return 1 - u * u * u;
    }

    public static float easeInCubic(float t) {
        t = clamp01(t);
        return t * t * t;
    }

    /** Overshoots a little before settling; used for things that float up into place. */
    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.5f, c3 = c1 + 1;
        float u = t - 1;
        return 1 + c3 * u * u * u + c1 * u * u;
    }

    /**
     * Moves {@code current} toward {@code target}, covering the same share of the
     * gap per second whatever the frame rate.
     */
    public static float approach(float current, float target, float dtSeconds, float rate) {
        float k = 1 - (float) Math.exp(-rate * dtSeconds);
        float next = current + (target - current) * k;
        return Math.abs(target - next) < 0.01f ? target : next;
    }
}
