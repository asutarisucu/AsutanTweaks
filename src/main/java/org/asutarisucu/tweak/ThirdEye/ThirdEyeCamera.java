package org.asutarisucu.tweak.ThirdEye;

import fi.dy.masa.tweakeroo.config.Configs;
import org.lwjgl.glfw.GLFW;

public class ThirdEyeCamera {
    public static double x, y, z;
    public static float yaw, pitch;
    public static boolean initialized = false;

    /**
     * Minimum move speed ≈ vanilla walking speed (4.32 blocks/s ÷ 20 ticks).
     * tweakeroo's fly-speed presets can raise the speed above this, but the
     * camera never moves slower than a walking player.
     */
    private static final double WALK_SPEED = 0.22;
    private static final double FAST_MULT  = 5.0;

    /** glfwGetTime() at the last movement update; 0 means "no previous frame". */
    private static double lastMoveTime = 0.0;
    /** Ignore frame gaps longer than this (seconds) so a stall cannot teleport the camera. */
    private static final double MAX_FRAME_SECONDS = 0.25;

    /**
     * Returns tweakeroo's currently-selected fly speed preset value.
     * FLY_SPEED_PRESET is 0-indexed (0..3) → FLY_SPEED_PRESET_1..4.
     */
    private static double tweakerooFlySpeed() {
        int preset = Configs.Internal.FLY_SPEED_PRESET.getIntegerValue();
        switch (preset) {
            case 0:  return Configs.Generic.FLY_SPEED_PRESET_1.getDoubleValue();
            case 1:  return Configs.Generic.FLY_SPEED_PRESET_2.getDoubleValue();
            case 2:  return Configs.Generic.FLY_SPEED_PRESET_3.getDoubleValue();
            case 3:  return Configs.Generic.FLY_SPEED_PRESET_4.getDoubleValue();
            default: return WALK_SPEED;
        }
    }

    public static void initFromPlayer() {
//#if MC < 260100
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return;
        x = mc.player.getX();
        y = mc.player.getEyeY();
        z = mc.player.getZ();
        yaw   = mc.player.getYaw();
        pitch = mc.player.getPitch();
//#else
//$$ var mc = net.minecraft.client.Minecraft.getInstance();
//$$ if (mc.player == null) return;
//$$ x = mc.player.getX();
//$$ y = mc.player.getEyeY();
//$$ z = mc.player.getZ();
//$$ yaw   = mc.player.getYRot();
//$$ pitch = mc.player.getXRot();
//#endif
        initialized = true;
        lastMoveTime = 0.0;
    }

    // Called once per rendered frame while ThirdEye movement is active.
    //
    // Deliberately frame-based rather than tick-based: the speed values are
    // per-tick, so they are scaled by the elapsed frame time. Stepping the
    // position 20 times a second instead made the ThirdEye view stutter, since
    // the rotation (driven by Mouse.updateMouse) already updates every frame.
    //
    // Uses GLFW key state so it works regardless of screen focus.
    // Key mapping mirrors creative-mode flying: Space=up, Shift=down, Ctrl=sprint.
    public static void updateMovement(long window) {
        double now = GLFW.glfwGetTime();
        double elapsed = lastMoveTime == 0.0 ? 0.0 : now - lastMoveTime;
        lastMoveTime = now;
        if (elapsed <= 0.0) return;
        double ticks = Math.min(elapsed, MAX_FRAME_SECONDS) * 20.0;

        boolean fast  = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
        double  speed = Math.max(tweakerooFlySpeed(), WALK_SPEED) * (fast ? FAST_MULT : 1.0) * ticks;

        float yr = (float) Math.toRadians(yaw);
        float pr = (float) Math.toRadians(pitch);

        double fwd  = key(window, GLFW.GLFW_KEY_W) - key(window, GLFW.GLFW_KEY_S);
        double str  = key(window, GLFW.GLFW_KEY_A) - key(window, GLFW.GLFW_KEY_D);
        double vert = key(window, GLFW.GLFW_KEY_SPACE) - key(window, GLFW.GLFW_KEY_LEFT_SHIFT);

        // forward vector (pitch-aware)
        double fx = -Math.sin(yr) * Math.cos(pr);
        double fy = -Math.sin(pr);
        double fz =  Math.cos(yr) * Math.cos(pr);
        // horizontal right vector
        double rx =  Math.cos(yr);
        double rz =  Math.sin(yr);

        // horizontal movement sign is negated relative to the standard MC direction
        // because the ThirdEye FBO rendering inverts the horizontal world-space mapping
        x += (fx * fwd + rx * str) * speed;
        y += (fy * fwd + vert)     * speed;
        z += (fz * fwd + rz * str) * speed;
    }

    private static int key(long window, int keyCode) {
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS ? 1 : 0;
    }
}
