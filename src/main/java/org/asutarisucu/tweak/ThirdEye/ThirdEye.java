package org.asutarisucu.tweak.ThirdEye;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

//#if MC < 12111
    /**
     * The MC framebuffer used as the render target for the ThirdEye pass.
     * Null until the feature is first enabled in-world.
     */
    public static ThirdEyeFbo thirdEyeFbo = null;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
    }

    private static void tick(MinecraftClient mc) {
        if (!Feature.THIRD_EYE.isEnabled()) {
            // Clean up when feature is turned off
            if (ThirdEyeWindow.isOpen()) {
                ThirdEyeWindow.close(mc.getWindow().getHandle());
            }
            if (thirdEyeFbo != null) {
                thirdEyeFbo.delete();
                thirdEyeFbo = null;
            }
            ThirdEyeCamera.initialized = false;
            return;
        }

        if (mc.world == null || mc.player == null) return;

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
            thirdEyeFbo = new ThirdEyeFbo(w, h, true, MinecraftClient.IS_SYSTEM_MAC);
        }

        // Initialise camera from player on first activation
        if (!ThirdEyeCamera.initialized) {
            ThirdEyeCamera.initFromPlayer();
        }

        // Camera movement (only when THIRD_EYE_MOVEMENT is on)
        if (Feature.THIRD_EYE_MOVEMENT.isEnabled()) {
            ThirdEyeCamera.tick(mc.getWindow().getHandle());
        }
    }
//#elseif MC < 260100
//$$ // TODO: implement ThirdEye for MC 1.21.11 (GpuTexture API changed, SimpleFramebuffer constructor changed)
//$$ public static void register() {}
//#else
//$$ // TODO: implement for MC 260100+ (Mojang mappings)
//$$ public static void register() {}
//#endif
}
