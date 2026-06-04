package org.asutarisucu.tweak.ThirdEye;

import org.lwjgl.glfw.GLFW;

public class ThirdEyeCamera {
    public static double x, y, z;
    public static float yaw, pitch;
    public static boolean initialized = false;

    private static final double MOVE_SPEED = 0.2;
    private static final double FAST_MULT  = 5.0;

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
    }

    // Called every tick when THIRD_EYE_MOVEMENT is enabled.
    // Uses GLFW key state so it works regardless of screen focus.
    public static void tick(long window) {
        boolean fast  = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT)   == GLFW.GLFW_PRESS;
        double  speed = MOVE_SPEED * (fast ? FAST_MULT : 1.0);

        float yr = (float) Math.toRadians(yaw);
        float pr = (float) Math.toRadians(pitch);

        double fwd  = key(window, GLFW.GLFW_KEY_W) - key(window, GLFW.GLFW_KEY_S);
        double str  = key(window, GLFW.GLFW_KEY_D) - key(window, GLFW.GLFW_KEY_A);
        double vert = key(window, GLFW.GLFW_KEY_SPACE) - key(window, GLFW.GLFW_KEY_LEFT_CONTROL);

        // forward vector (pitch-aware)
        double fx = -Math.sin(yr) * Math.cos(pr);
        double fy = -Math.sin(pr);
        double fz =  Math.cos(yr) * Math.cos(pr);
        // horizontal right vector
        double rx =  Math.cos(yr);
        double rz =  Math.sin(yr);

        x += (fx * fwd + rx * str) * speed;
        y += (fy * fwd + vert)     * speed;
        z += (fz * fwd + rz * str) * speed;
    }

    private static int key(long window, int keyCode) {
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS ? 1 : 0;
    }
}
