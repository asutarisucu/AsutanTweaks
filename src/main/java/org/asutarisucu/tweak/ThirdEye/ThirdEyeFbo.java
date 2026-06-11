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
//#elseif MC < 260100
//$$ import net.minecraft.client.gl.SimpleFramebuffer;
//$$ import net.minecraft.client.texture.GlTexture;
//$$
//$$ /**
//$$  * SimpleFramebuffer subclass for MC 1.21.11. The color attachment is now a
//$$  * GpuTexture; getColorTexId() unwraps the OpenGL backend handle (GlTexture)
//$$  * so the ThirdEye blit can read it via raw GL in the secondary window.
//$$  */
//$$ public class ThirdEyeFbo extends SimpleFramebuffer {
//$$     public ThirdEyeFbo(int width, int height, boolean useDepth) {
//$$         super("ThirdEye", width, height, useDepth);
//$$     }
//$$
//$$     public int getColorTexId() {
//$$         return ((GlTexture) this.getColorAttachment()).getGlId();
//$$     }
//$$ }
//#else
//$$ // Stub for MC 260100+ (Mojang mappings): ThirdEye not yet implemented.
//$$ public class ThirdEyeFbo {}
//#endif
