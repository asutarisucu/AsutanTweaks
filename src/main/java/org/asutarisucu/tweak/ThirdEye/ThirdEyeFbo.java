package org.asutarisucu.tweak.ThirdEye;

//#if MC < 12111
import net.minecraft.client.gl.SimpleFramebuffer;

/**
 * Thin subclass of SimpleFramebuffer that exposes the protected colorAttachment
 * field as a public accessor for use in the ThirdEye blit pipeline.
 */
public class ThirdEyeFbo extends SimpleFramebuffer {
    public ThirdEyeFbo(int width, int height, boolean useDepth, boolean isMac) {
        super(width, height, useDepth, isMac);
    }

    public int getColorTexId() {
        return this.colorAttachment;
    }
}
//#else
//$$ // Stub for MC 1.21.11+: SimpleFramebuffer API changed (GpuTexture, name param),
//$$ // ThirdEye rendering is not yet implemented for these versions.
//$$ public class ThirdEyeFbo {}
//#endif
