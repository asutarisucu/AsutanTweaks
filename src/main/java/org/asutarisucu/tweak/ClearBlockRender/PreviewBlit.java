package org.asutarisucu.tweak.ClearBlockRender;

//#if MC < 12111
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

/**
 * Draws the capture preview's colour texture into the config screen.
 *
 * Before MC 1.21.11 the GUI has no way to draw a bare OpenGL texture — every
 * public path wants an Identifier from the texture manager, and the preview is
 * an off-screen render target. So the quad is written by hand with the same
 * position/texture shader the GUI uses.
 */
public final class PreviewBlit {

    private PreviewBlit() {}

    /**
     * @param positionMatrix the GUI transform in force, so the pane lands where the
     *                       rest of the screen is drawn
     * @param textureId      OpenGL name of the colour attachment
     */
    public static void draw(Matrix4f positionMatrix, int textureId, int x0, int y0, int x1, int y1) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // v runs the other way: the render target's first row is the bottom of the image.
//#if MC < 12101
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(positionMatrix, x0, y1, 0.0f).texture(0.0f, 0.0f).next();
        buffer.vertex(positionMatrix, x1, y1, 0.0f).texture(1.0f, 0.0f).next();
        buffer.vertex(positionMatrix, x1, y0, 0.0f).texture(1.0f, 1.0f).next();
        buffer.vertex(positionMatrix, x0, y0, 0.0f).texture(0.0f, 1.0f).next();
//#else
        //$$ BufferBuilder buffer = Tessellator.getInstance()
        //$$         .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        //$$ buffer.vertex(positionMatrix, x0, y1, 0.0f).texture(0.0f, 0.0f);
        //$$ buffer.vertex(positionMatrix, x1, y1, 0.0f).texture(1.0f, 0.0f);
        //$$ buffer.vertex(positionMatrix, x1, y0, 0.0f).texture(1.0f, 1.0f);
        //$$ buffer.vertex(positionMatrix, x0, y0, 0.0f).texture(0.0f, 1.0f);
//#endif
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
    }
}
//#else
//$$ /**
//$$  * Not used from MC 1.21.11 on, where the GUI can draw a GpuTextureView directly.
//$$  */
//$$ public final class PreviewBlit {
//$$     private PreviewBlit() {}
//$$ }
//#endif
