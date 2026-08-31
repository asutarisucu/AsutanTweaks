package org.asutarisucu.tweak.ThirdEye;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.asutarisucu.Configs.Feature;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import com.mojang.blaze3d.pipeline.TextureTarget;
//#endif

/**
 * ThirdEye – per-frame second world render displayed in a secondary OS window.
 *
 * Rendering flow (MC < 12111):
 *   1. MixinGameRendererThirdEye fires at HEAD of renderWorld().
 *   2. If feature enabled and not re-entrant:
 *      a. Swap client.framebuffer → thirdEyeFbo (via accessor mixin).
 *      b. Set isRenderingThirdEye = true.
 *      c. Call renderWorld() recursively (shadow call in the mixin).
 *         MixinCameraThirdEye overrides camera position/rotation.
 *      d. Restore client.framebuffer.
 *      e. Blit thirdEyeFbo.colorAttachment → secondary window.
 *   3. The outer renderWorld() then renders the player view normally.
 */
public class ThirdEye {

    /** Guard against re-entrance in the renderWorld injection. */
    public static volatile boolean isRenderingThirdEye = false;

    /**
     * True while camera control belongs to ThirdEye: the feature and its movement
     * option are both on AND the secondary window is actually open. The input
     * mixins have to test this rather than THIRD_EYE_MOVEMENT alone — otherwise
     * leaving the movement option enabled with ThirdEye off swallows the player's
     * mouse and keyboard input entirely.
     */
    public static boolean isMovementActive() {
        return Feature.THIRD_EYE.isEnabled()
                && Feature.THIRD_EYE_MOVEMENT.isEnabled()
                && ThirdEyeWindow.isOpen();
    }

//#if MC < 260100
    /**
     * The MC framebuffer used as the render target for the ThirdEye pass.
     * Null until the feature is first enabled in-world.
     */
    public static ThirdEyeFbo thirdEyeFbo = null;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
        // Closes the ThirdEye window immediately when the player disconnects
        // from a world (Save & Quit, server disconnect, etc.). Relying on the
        // tick-based world==null check alone is fragile because the tick event
        // may not fire promptly after disconnect, and on some flows mc.world
        // is briefly non-null while the disconnect screen renders.
        // DISCONNECT fires on the Netty IO thread, where no GL context is
        // current. Schedule shutdown() on the render thread via execute() so
        // the FBO/window GL teardown happens with a valid context.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> shutdown(client)));
    }

    /** Tear down all ThirdEye resources. Safe to call repeatedly. */
    private static void shutdown(MinecraftClient mc) {
        isRenderingThirdEye = false;
        if (mc != null && ThirdEyeWindow.exists()) {
            ThirdEyeWindow.close(mc.getWindow().getHandle());
        }
        if (thirdEyeFbo != null) {
            thirdEyeFbo.delete();
            thirdEyeFbo = null;
        }
        ThirdEyeCamera.initialized = false;
    }

    private static void tick(MinecraftClient mc) {
        // Defensive: rendering should never be in progress at tick time. If a
        // prior frame's recursive render somehow left this flag set (e.g. via
        // an exception bypassing the try/finally in MixinGameRendererThirdEye),
        // clear it here so subsequent frames don't keep treating the main
        // render pass as a ThirdEye pass.
        isRenderingThirdEye = false;

        if (!Feature.THIRD_EYE.isEnabled()) {
            shutdown(mc);
            return;
        }

        if (mc.world == null || mc.player == null) {
            shutdown(mc);
            return;
        }

        // The close button only sets GLFW's should-close flag; the window has to
        // be destroyed explicitly. Do it here and turn the feature off, otherwise
        // the block below would immediately reopen it.
        if (ThirdEyeWindow.closeRequested()) {
            shutdown(mc);
            Feature.THIRD_EYE.setEnabled(false);
            return;
        }

        // (Re-)open window if needed
        if (!ThirdEyeWindow.isOpen()) {
            ThirdEyeWindow.open(mc.getWindow().getHandle());
            if (!ThirdEyeWindow.isOpen()) return; // window creation failed
        }

        // Create or resize the ThirdEye FBO to match the main framebuffer
        int w = mc.getFramebuffer().textureWidth;
        int h = mc.getFramebuffer().textureHeight;
        if (thirdEyeFbo == null || thirdEyeFbo.textureWidth != w || thirdEyeFbo.textureHeight != h) {
            if (thirdEyeFbo != null) thirdEyeFbo.delete();
//#if MC < 12111
            thirdEyeFbo = new ThirdEyeFbo(w, h, true, MinecraftClient.IS_SYSTEM_MAC);
            // Framebuffer's default clear colour is opaque white. MinecraftClient
            // overrides its own framebuffer to transparent black; match that so a
            // region the world render does not cover reads as black, not white.
            thirdEyeFbo.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
//#else
//$$         thirdEyeFbo = new ThirdEyeFbo(w, h, true);
//#endif
        }

        // Initialise camera from player on first activation
        if (!ThirdEyeCamera.initialized) {
            ThirdEyeCamera.initFromPlayer();
        }
    }
//#else
//$$ /**
//$$  * The render target for the ThirdEye pass (MC 26.1).
//$$  * Null until the feature is first enabled in-world.
//$$  */
//$$ public static ThirdEyeFbo thirdEyeFbo = null;
//$$
//$$ public static void register() {
//$$     ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
//$$     // DISCONNECT fires on the Netty IO thread; schedule GL teardown on the render thread.
//$$     ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> shutdown(client)));
//$$ }
//$$
//$$ /** Tear down all ThirdEye resources. Safe to call repeatedly. */
//$$ private static void shutdown(Minecraft mc) {
//$$     isRenderingThirdEye = false;
//$$     if (mc != null && ThirdEyeWindow.exists()) {
//$$         ThirdEyeWindow.close(mc.getWindow().handle());
//$$     }
//$$     if (thirdEyeFbo != null) {
//$$         thirdEyeFbo.destroyBuffers();
//$$         thirdEyeFbo = null;
//$$     }
//$$     ThirdEyeCamera.initialized = false;
//$$ }
//$$
//$$ private static void tick(Minecraft mc) {
//$$     // Defensive: rendering should never be in progress at tick time.
//$$     isRenderingThirdEye = false;
//$$
//$$     if (!Feature.THIRD_EYE.isEnabled()) {
//$$         shutdown(mc);
//$$         return;
//$$     }
//$$     if (mc.level == null || mc.player == null) {
//$$         shutdown(mc);
//$$         return;
//$$     }
//$$
//$$     // The close button only sets GLFW's should-close flag; the window has to
//$$     // be destroyed explicitly. Do it here and turn the feature off, otherwise
//$$     // the block below would immediately reopen it.
//$$     if (ThirdEyeWindow.closeRequested()) {
//$$         shutdown(mc);
//$$         Feature.THIRD_EYE.setEnabled(false);
//$$         return;
//$$     }
//$$
//$$     if (!ThirdEyeWindow.isOpen()) {
//$$         ThirdEyeWindow.open(mc.getWindow().handle());
//$$         if (!ThirdEyeWindow.isOpen()) return; // window creation failed
//$$     }
//$$
//$$     // Create or resize the ThirdEye target to match the main render target
//#if MC < 260200
//$$     int w = mc.getMainRenderTarget().width;
//$$     int h = mc.getMainRenderTarget().height;
//#else
//$$     // MC 26.2 moved the main render target onto GameRenderer.
//$$     int w = mc.gameRenderer.mainRenderTarget().width;
//$$     int h = mc.gameRenderer.mainRenderTarget().height;
//#endif
//$$     if (thirdEyeFbo == null || thirdEyeFbo.width != w || thirdEyeFbo.height != h) {
//$$         if (thirdEyeFbo != null) thirdEyeFbo.destroyBuffers();
//$$         thirdEyeFbo = new ThirdEyeFbo(w, h, true);
//$$     }
//$$
//$$     if (!ThirdEyeCamera.initialized) {
//$$         ThirdEyeCamera.initFromPlayer();
//$$     }
//$$ }
//#endif
}
