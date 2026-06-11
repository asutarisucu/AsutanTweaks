package org.asutarisucu.tweak.ThirdEye;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Manages the secondary OS window that displays the ThirdEye view.
 *
 * GL resource ownership model:
 *   colorTexId  – created in the MAIN GL context (part of ThirdEye's SimpleFramebuffer).
 *                  Textures are shared across contexts created with a shared handle, so
 *                  the secondary context can read it.
 *   displayFboId – a READ-ONLY FBO created in the SECONDARY context that attaches
 *                  colorTexId; used as the source for glBlitFramebuffer.
 */
public class ThirdEyeWindow {

    public static long handle = 0L;
    private static int displayFboId = 0;

    /** Open the secondary GLFW window. Must be called on the main GL thread. */
    public static void open(long mainContextHandle) {
        if (handle != 0L) return;

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,               GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE,             GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,        GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED,               GLFW.GLFW_FALSE);

        // Share the main context so texture IDs are accessible from this window.
        handle = GLFW.glfwCreateWindow(640, 360, "ThirdEye", 0L, mainContextHandle);
        if (handle == 0L) return;

        // Create the display FBO inside the secondary context.
        GLFW.glfwMakeContextCurrent(handle);
        displayFboId = GL30.glGenFramebuffers();
        GLFW.glfwMakeContextCurrent(mainContextHandle);
    }

    public static boolean isOpen() {
        return handle != 0L && !GLFW.glfwWindowShouldClose(handle);
    }

    /**
     * Blit the ThirdEye color texture to the secondary window.
     *
     * @param colorTexId   the GL texture ID from ThirdEye's SimpleFramebuffer
     * @param texW / texH  dimensions of that texture
     * @param mainContext  the main window's GL context handle (to restore after blit)
     */
    public static void blit(int colorTexId, int texW, int texH, long mainContext) {
        if (handle == 0L || colorTexId == 0) return;

        int[] wArr = new int[1], hArr = new int[1];
        GLFW.glfwGetFramebufferSize(handle, wArr, hArr);
        int ww = wArr[0], wh = hArr[0];
        if (ww == 0 || wh == 0) return;

        GLFW.glfwMakeContextCurrent(handle);

        // Attach the shared texture to the secondary FBO as READ source.
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, displayFboId);
        GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D, colorTexId, 0);

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        GL11.glClearColor(0f, 0f, 0f, 1f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GL30.glBlitFramebuffer(
                0, 0, texW, texH,
                0, 0, ww,   wh,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GLFW.glfwSwapBuffers(handle);
        GLFW.glfwMakeContextCurrent(mainContext);
    }

    /** Destroy the secondary window and free its GL resources. */
    public static void close(long mainContext) {
        if (handle == 0L) return;

        GLFW.glfwMakeContextCurrent(handle);
        if (displayFboId != 0) { GL30.glDeleteFramebuffers(displayFboId); displayFboId = 0; }
        GLFW.glfwMakeContextCurrent(mainContext);

        GLFW.glfwDestroyWindow(handle);
        handle = 0L;
    }
}
