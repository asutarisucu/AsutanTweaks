package org.asutarisucu.tweak.ClearBlockRender;

//#if MC < 12111
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

/**
 * Reads a rendered frame back off the GPU as RGBA bytes.
 *
 * Reads the colour texture rather than the framebuffer so the same code works
 * whatever is currently bound, and so the alpha channel comes back untouched.
 */
public final class FrameGrabber {

    private FrameGrabber() {}

    /** Fills {@code dst} (capacity must be width*height*4) with the texture's RGBA pixels, bottom row first. */
    public static void read(int textureId, int width, int height, ByteBuffer dst) {
        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);
        dst.clear();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dst);
        dst.position(0).limit(width * height * 4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
    }
}
//#else
//$$ import com.mojang.blaze3d.buffers.GpuBuffer;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import com.mojang.blaze3d.textures.GpuTexture;
//$$
//$$ import java.nio.ByteBuffer;
//$$ import java.util.function.Consumer;
//$$
//$$ /**
//$$  * Reads a rendered frame back off the GPU as RGBA bytes.
//$$  *
//$$  * From MC 1.21.11 on, GPU work is recorded into a command stream instead of
//$$  * running as the calls are made, so reading the texture with raw GL returns
//$$  * whatever was there before the pass — an untouched, fully transparent buffer.
//$$  * The copy has to go through the command encoder and the result is handed back
//$$  * in its callback, which is the same path Screenshot uses.
//$$  */
//$$ public final class FrameGrabber {
//$$
//$$     private FrameGrabber() {}
//$$
//$$     /**
//$$      * Kept between frames. Allocating a frame-sized GPU buffer per capture and
//$$      * closing it in the callback churned tens of megabytes a second at 1080p.
//$$      */
//$$     private static GpuBuffer buffer;
//$$     private static long bufferSize;
//$$     private static boolean busy;
//$$
//$$     /** Whether a readback started earlier has not delivered its data yet. */
//$$     public static boolean isBusy() { return busy; }
//$$
//$$     /**
//$$      * Copies the texture into host memory and hands it to {@code consumer} as RGBA
//$$      * bytes, bottom row first. The buffer is only valid inside the callback.
//$$      *
//$$      * Returns false without reading anything while an earlier readback is still
//$$      * in flight — the single buffer would be overwritten under it.
//$$      */
//$$     public static boolean read(GpuTexture texture, int width, int height, Consumer<ByteBuffer> consumer) {
//$$         if (busy) return false;
//$$         long size = (long) width * height * 4;
//$$         if (buffer == null || bufferSize != size) {
//$$             if (buffer != null) buffer.close();
//$$             buffer = RenderSystem.getDevice()
//$$                     .createBuffer(() -> "AsutanTweaks capture readback", 9, size);
//$$             bufferSize = size;
//$$         }
//$$         GpuBuffer target = buffer;
//$$         busy = true;
//$$         RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, target, 0L, () -> {
//$$             try {
//#if MC < 260200
//$$                 var view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(target, true, false);
//#else
//$$                 var view = target.map(true, false);
//#endif
//$$                 try {
//$$                     ByteBuffer data = view.data().duplicate();
//$$                     data.position(0).limit(width * height * 4);
//$$                     consumer.accept(data);
//$$                 } finally {
//$$                     view.close();
//$$                 }
//$$             } finally {
//$$                 busy = false;
//$$             }
//$$         }, 0);
//$$         return true;
//$$     }
//$$ }
//#endif
