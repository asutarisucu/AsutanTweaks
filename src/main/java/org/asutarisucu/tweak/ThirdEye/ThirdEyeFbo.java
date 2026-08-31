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
//$$ import com.mojang.blaze3d.opengl.GlTexture;
//$$ import com.mojang.blaze3d.pipeline.TextureTarget;
//$$
//$$ /**
//$$  * TextureTarget subclass for MC 26.1 (Mojang mappings). The color attachment is
//$$  * a GpuTexture; getColorTexId() unwraps the OpenGL backend handle (GlTexture)
//$$  * so the ThirdEye blit can read it via raw GL in the secondary window.
//$$  */
//$$ public class ThirdEyeFbo extends TextureTarget {
//$$     public ThirdEyeFbo(int width, int height, boolean useDepth) {
//#if MC < 260200
//$$         super("ThirdEye", width, height, useDepth);
//#else
//$$         // MC 26.2 requires the colour format to be stated explicitly.
//$$         super("ThirdEye", width, height, useDepth, com.mojang.blaze3d.GpuFormat.RGBA8_UNORM);
//#endif
//$$     }
//$$
//$$     public int getColorTexId() {
//$$         return getColorTexture() instanceof GlTexture gl ? gl.glId() : 0;
//$$     }
//$$ }
//#endif
