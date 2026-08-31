package org.asutarisucu.tweak.ClearBlockRender;

import net.fabricmc.loader.api.FabricLoader;
import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.GUI.HudLogger;
import org.asutarisucu.lib.util.MessageUtils;
import org.asutarisucu.tweak.WorldEditGUI.WorldEditSelection;

import java.nio.ByteBuffer;

//#if MC < 12111
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeFbo;
import org.lwjgl.system.MemoryUtil;
//#elseif MC < 260100
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import com.mojang.blaze3d.textures.GpuTextureView;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.world.ClientWorld;
//$$ import org.asutarisucu.tweak.ThirdEye.ThirdEyeFbo;
//$$ import java.nio.file.Path;
//#else
//$$ import com.mojang.blaze3d.pipeline.RenderTarget;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import com.mojang.blaze3d.textures.GpuTextureView;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.multiplayer.ClientLevel;
//$$ import org.asutarisucu.tweak.ThirdEye.ThirdEyeFbo;
//#if MC >= 260200
//$$ import org.joml.Vector4f;
//#endif
//$$ import java.nio.file.Path;
//#endif

/**
 * Records the WorldEdit selection as a transparent video.
 *
 * Each captured frame renders only the selected blocks — see
 * {@link RegionRenderer} — into an off-screen buffer cleared to a fully
 * transparent black, then pipes the raw RGBA back out to ffmpeg. The world
 * keeps ticking while recording, so a running machine is captured in motion;
 * the camera can additionally orbit the selection.
 *
 * Capture resolution is independent of the game window, so a 4K clip can be
 * recorded from a windowed game.
 */
public final class ClearBlockRender {

    private ClearBlockRender() {}

//#if MC < 12111
    private static boolean recording;
    private static ThirdEyeFbo fbo;
    private static FfmpegEncoder encoder;
    private static ByteBuffer pixels;
    private static int frameW, frameH, fps;
    private static int[] bounds;
    private static Object recordingWorld;
    private static long startNanos;
    private static long framesWritten;
    private static double orbitYaw;

    public static boolean isRecording() { return recording; }

    /** Hotkey entry point. */
    public static void toggle() {
        if (recording) stop("stopped");
        else start();
    }

    /** Renders one frame and writes it out as a transparent PNG. */
    public static void saveStill() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!checkReady(mc)) return;
        int[] sel = WorldEditSelection.getBounds();
        int w = Configs.Generic.CBR_WIDTH.getIntegerValue();
        int h = Configs.Generic.CBR_HEIGHT.getIntegerValue();
        ThirdEyeFbo target = new ThirdEyeFbo(w, h, true, MinecraftClient.IS_SYSTEM_MAC);
        target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        ByteBuffer buffer = MemoryUtil.memAlloc(w * h * 4);
        try {
            renderInto(mc, mc.world, target, sel, w, h, 0.0, 0.0f, buffer);
            java.nio.file.Path out = PngWriter.write(
                    FabricLoader.getInstance().getGameDir().resolve("clear_block_render"), buffer, w, h);
            HudLogger.INSTANCE.log(MessageUtils.colorGreen("Saved " + out.getFileName()));
        } catch (Throwable t) {
            AsutanTweaks.LOGGER.error("[ClearBlockRender] still capture failed", t);
            HudLogger.INSTANCE.log(MessageUtils.colorRed("Image export failed — see the log"));
        } finally {
            MemoryUtil.memFree(buffer);
            target.delete();
        }
    }

    private static boolean checkReady(MinecraftClient mc) {
        if (mc.world == null) return false;
        if (!Feature.CLEAR_BLOCK_RENDER.isEnabled()) {
            HudLogger.INSTANCE.log(MessageUtils.colorRed("Clear Block Render is disabled"));
            return false;
        }
        if (WorldEditSelection.getBounds() == null) {
            HudLogger.INSTANCE.log(MessageUtils.colorRed("No WorldEdit cuboid selection"));
            return false;
        }
        return true;
    }

    private static ThirdEyeFbo previewFbo;
    private static int previewW, previewH;
    private static boolean previewWanted;

    /**
     * Asks for a preview frame at this size on the next rendered frame.
     *
     * The screen cannot render the region itself: its draw phase runs with the
     * main framebuffer bound and the GUI projection in force, while the region
     * pass needs its own target and camera. So the pass runs on the frame hook
     * and the screen draws the result.
     */
    public static void requestPreview(int width, int height) {
        previewWanted = true;
        if (previewFbo == null || previewW != width || previewH != height) {
            if (previewFbo != null) previewFbo.delete();
            previewFbo = new ThirdEyeFbo(Math.max(1, width), Math.max(1, height), true,
                    MinecraftClient.IS_SYSTEM_MAC);
            previewFbo.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            previewW = width;
            previewH = height;
        }
    }

    /** The preview colour texture, or 0 when no preview has been rendered. */
    public static int previewTexture() {
        return previewFbo == null ? 0 : previewFbo.getColorTexId();
    }

    public static void releasePreview() {
        previewWanted = false;
        if (previewFbo != null) { previewFbo.delete(); previewFbo = null; }
    }

    private static void renderPreview(MinecraftClient mc, float tickDelta) {
        if (!previewWanted || previewFbo == null) return;
        previewWanted = false;
        if (mc.world == null || !Feature.CLEAR_BLOCK_RENDER.isEnabled()) return;
        int[] sel = WorldEditSelection.getBounds();
        if (sel == null) return;
        CaptureCamera cam = CaptureCamera.frame(sel, recording ? orbitYaw : 0.0);
        previewFbo.clear(MinecraftClient.IS_SYSTEM_MAC);
        previewFbo.beginWrite(true);
        try {
            RegionRenderer.render(mc.world, sel, cam, previewW, previewH, tickDelta);
        } catch (Throwable t) {
            AsutanTweaks.LOGGER.error("[ClearBlockRender] preview render failed", t);
            releasePreview();
        } finally {
            mc.getFramebuffer().beginWrite(true);
        }
    }

    /** Draws the region into {@code target} and reads the result back into {@code out}. */
    private static void renderInto(MinecraftClient mc, ClientWorld world, ThirdEyeFbo target, int[] region,
                                   int w, int h, double yawOffset, float tickDelta, ByteBuffer out) {
        CaptureCamera cam = CaptureCamera.frame(region, yawOffset);
        target.clear(MinecraftClient.IS_SYSTEM_MAC);
        target.beginWrite(true);
        try {
            RegionRenderer.render(world, region, cam, w, h, tickDelta);
        } finally {
            mc.getFramebuffer().beginWrite(true);
        }
        FrameGrabber.read(target.getColorTexId(), w, h, out);
    }

    private static void start() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!checkReady(mc)) return;
        int[] sel = WorldEditSelection.getBounds();

        frameW = Configs.Generic.CBR_WIDTH.getIntegerValue();
        frameH = Configs.Generic.CBR_HEIGHT.getIntegerValue();
        fps = Configs.Generic.CBR_FPS.getIntegerValue();

        try {
            encoder = FfmpegEncoder.start(
                    FabricLoader.getInstance().getGameDir().resolve("clear_block_render"),
                    Configs.Generic.CBR_FFMPEG_PATH.getValue(),
                    Configs.Generic.CBR_FORMAT.getValue(),
                    frameW, frameH, fps, Configs.Generic.CBR_SPEED.getDoubleValue());
        } catch (Exception e) {
            AsutanTweaks.LOGGER.warn("[ClearBlockRender] failed to start ffmpeg", e);
            HudLogger.INSTANCE.log(MessageUtils.colorRed("ffmpeg could not be started — check CBR FFmpeg Path"));
            encoder = null;
            return;
        }

        fbo = new ThirdEyeFbo(frameW, frameH, true, MinecraftClient.IS_SYSTEM_MAC);
        fbo.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        pixels = MemoryUtil.memAlloc(frameW * frameH * 4);

        bounds = sel;
        recordingWorld = mc.world;
        startNanos = System.nanoTime();
        framesWritten = 0;
        orbitYaw = 0.0;
        recording = true;
        HudLogger.INSTANCE.log(MessageUtils.colorGreen("Recording " + frameW + "x" + frameH + " @" + fps + "fps"));
    }

    private static void stop(String why) {
        recording = false;
        if (encoder != null) {
            encoder.close();
            HudLogger.INSTANCE.log(MessageUtils.colorGreen("Saved " + encoder.outputFile().getFileName()));
            encoder = null;
        } else if (why != null) {
            HudLogger.INSTANCE.log(why);
        }
        if (fbo != null) { fbo.delete(); fbo = null; }
        if (pixels != null) { MemoryUtil.memFree(pixels); pixels = null; }
        bounds = null;
        recordingWorld = null;
    }

    /** Called once per rendered frame from the game renderer mixin. */
    public static void onFrame(float tickDelta) {
        renderPreview(MinecraftClient.getInstance(), tickDelta);
        if (!recording) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.world != recordingWorld || !Feature.CLEAR_BLOCK_RENDER.isEnabled()) {
            stop("recording stopped");
            return;
        }
        if (encoder == null || encoder.isBroken()) {
            HudLogger.INSTANCE.log(MessageUtils.colorRed("ffmpeg exited — recording stopped"));
            stop(null);
            return;
        }

        double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
        long due = (long) (elapsed * fps);
        if (due <= framesWritten) return;

        // ffmpeg is behind. Skip the whole frame — render, readback and all —
        // rather than queue up work the encoder cannot drain. framesWritten is
        // left alone so the repeat below fills the gap once it catches up.
        if (!encoder.hasCapacity()) return;

        // A frame that arrives late is repeated so the clip keeps real-time pacing,
        // but only up to a few repeats — a long stall should not fill the file.
        int repeats = (int) Math.min(due - framesWritten, 4);

        orbitYaw += Configs.Generic.CBR_ORBIT_SPEED.getDoubleValue() * repeats / fps;
        captureFrame(mc, (ClientWorld) recordingWorld, tickDelta);

        pixels.position(0).limit(frameW * frameH * 4);
        encoder.writeFrame(pixels, repeats);
        framesWritten = due;
    }

    private static void captureFrame(MinecraftClient mc, ClientWorld world, float tickDelta) {
        CaptureCamera cam = CaptureCamera.frame(bounds, orbitYaw);
        fbo.clear(MinecraftClient.IS_SYSTEM_MAC);
        fbo.beginWrite(true);
        try {
            RegionRenderer.render(world, bounds, cam, frameW, frameH, tickDelta);
        } catch (Throwable t) {
            AsutanTweaks.LOGGER.error("[ClearBlockRender] region render failed", t);
            HudLogger.INSTANCE.log(MessageUtils.colorRed("Render failed — recording stopped"));
            mc.getFramebuffer().beginWrite(true);
            stop(null);
            return;
        }
        FrameGrabber.read(fbo.getColorTexId(), frameW, frameH, pixels);
        mc.getFramebuffer().beginWrite(true);
    }
//#elseif MC < 260100
    //$$ private static boolean recording;
    //$$ private static ThirdEyeFbo fbo;
    //$$ private static FfmpegEncoder encoder;
    //$$ private static int frameW, frameH, fps;
    //$$ private static int[] bounds;
    //$$ private static Object recordingWorld;
    //$$ private static long startNanos;
    //$$ private static long framesWritten;
    //$$ private static double orbitYaw;
    //$$
    //$$ public static boolean isRecording() { return recording; }
    //$$
    //$$ public static void toggle() {
    //$$     if (recording) stop("stopped");
    //$$     else start();
    //$$ }
    //$$
    //$$ /** Renders one frame and writes it out as a transparent PNG. */
    //$$ public static void saveStill() {
    //$$     MinecraftClient mc = MinecraftClient.getInstance();
    //$$     if (!checkReady(mc)) return;
    //$$     int[] sel = WorldEditSelection.getBounds();
    //$$     int w = Configs.Generic.CBR_WIDTH.getIntegerValue();
    //$$     int h = Configs.Generic.CBR_HEIGHT.getIntegerValue();
    //$$     ThirdEyeFbo target = new ThirdEyeFbo(w, h, true);
    //$$     try {
    //$$         // The readback runs in a callback, so the target is released there
    //$$         // rather than in a finally block that would fire first.
    //$$         boolean started = renderInto(mc, (ClientWorld) mc.world, target, sel, w, h, 0.0, 0.0f, data -> {
    //$$             try {
    //$$                 Path out = PngWriter.write(
    //$$                         FabricLoader.getInstance().getGameDir().resolve("clear_block_render"), data, w, h);
    //$$                 HudLogger.INSTANCE.log(MessageUtils.colorGreen("Saved " + out.getFileName()));
    //$$             } catch (Throwable t) {
    //$$                 AsutanTweaks.LOGGER.error("[ClearBlockRender] writing the image failed", t);
    //$$                 HudLogger.INSTANCE.log(MessageUtils.colorRed("Image export failed — see the log"));
    //$$             } finally {
    //$$                 target.delete();
    //$$             }
    //$$         });
    //$$         if (!started) {
    //$$             HudLogger.INSTANCE.log(MessageUtils.colorRed("Busy reading the last frame — try again"));
    //$$             target.delete();
    //$$         }
    //$$     } catch (Throwable t) {
    //$$         AsutanTweaks.LOGGER.error("[ClearBlockRender] still capture failed", t);
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("Image export failed — see the log"));
    //$$         target.delete();
    //$$     }
    //$$ }
    //$$
    //$$ private static boolean checkReady(MinecraftClient mc) {
    //$$     if (mc.world == null) return false;
    //$$     if (!Feature.CLEAR_BLOCK_RENDER.isEnabled()) {
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("Clear Block Render is disabled"));
    //$$         return false;
    //$$     }
    //$$     if (WorldEditSelection.getBounds() == null) {
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("No WorldEdit cuboid selection"));
    //$$         return false;
    //$$     }
    //$$     return true;
    //$$ }
    //$$
    //$$ private static void start() {
    //$$     MinecraftClient mc = MinecraftClient.getInstance();
    //$$     if (!checkReady(mc)) return;
    //$$
    //$$     frameW = Configs.Generic.CBR_WIDTH.getIntegerValue();
    //$$     frameH = Configs.Generic.CBR_HEIGHT.getIntegerValue();
    //$$     fps = Configs.Generic.CBR_FPS.getIntegerValue();
    //$$
    //$$     try {
    //$$         encoder = FfmpegEncoder.start(
    //$$                 FabricLoader.getInstance().getGameDir().resolve("clear_block_render"),
    //$$                 Configs.Generic.CBR_FFMPEG_PATH.getValue(),
    //$$                 Configs.Generic.CBR_FORMAT.getValue(),
    //$$                 frameW, frameH, fps, Configs.Generic.CBR_SPEED.getDoubleValue());
    //$$     } catch (Exception e) {
    //$$         AsutanTweaks.LOGGER.warn("[ClearBlockRender] failed to start ffmpeg", e);
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("ffmpeg could not be started — check FFmpeg Path"));
    //$$         encoder = null;
    //$$         return;
    //$$     }
    //$$
    //$$     fbo = new ThirdEyeFbo(frameW, frameH, true);
    //$$     bounds = WorldEditSelection.getBounds();
    //$$     recordingWorld = mc.world;
    //$$     startNanos = System.nanoTime();
    //$$     framesWritten = 0;
    //$$     orbitYaw = 0.0;
    //$$     recording = true;
    //$$     HudLogger.INSTANCE.log(MessageUtils.colorGreen("Recording " + frameW + "x" + frameH + " @" + fps + "fps"));
    //$$ }
    //$$
    //$$ private static void stop(String why) {
    //$$     recording = false;
    //$$     if (encoder != null) {
    //$$         encoder.close();
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorGreen("Saved " + encoder.outputFile().getFileName()));
    //$$         encoder = null;
    //$$     } else if (why != null) {
    //$$         HudLogger.INSTANCE.log(why);
    //$$     }
    //$$     if (fbo != null) { fbo.delete(); fbo = null; }
    //$$     bounds = null;
    //$$     recordingWorld = null;
    //$$ }
    //$$
    //$$ private static ThirdEyeFbo previewFbo;
    //$$ private static int previewW, previewH;
    //$$ private static boolean previewWanted;
    //$$
    //$$ /**
    //$$  * Asks for a preview frame at this size on the next rendered frame.
    //$$  *
    //$$  * The screen cannot render the region itself: its draw phase only records GUI
    //$$  * state, while the region pass needs to open real render passes. So the pass
    //$$  * runs on the frame hook and the screen blits the result.
    //$$  */
    //$$ public static void requestPreview(int width, int height) {
    //$$     previewWanted = true;
    //$$     if (previewFbo == null || previewW != width || previewH != height) {
    //$$         if (previewFbo != null) previewFbo.delete();
    //$$         previewFbo = new ThirdEyeFbo(Math.max(1, width), Math.max(1, height), true);
    //$$         previewW = width;
    //$$         previewH = height;
    //$$     }
    //$$ }
    //$$
    //$$ /** The preview colour texture, or null when no preview has been rendered. */
    //$$ public static GpuTextureView previewTexture() {
    //$$     return previewFbo == null ? null : previewFbo.getColorAttachmentView();
    //$$ }
    //$$
    //$$ public static void releasePreview() {
    //$$     previewWanted = false;
    //$$     if (previewFbo != null) { previewFbo.delete(); previewFbo = null; }
    //$$ }
    //$$
    //$$ private static void renderPreview(MinecraftClient mc, float tickDelta) {
    //$$     if (!previewWanted || previewFbo == null) return;
    //$$     previewWanted = false;
    //$$     if (mc.world == null || !Feature.CLEAR_BLOCK_RENDER.isEnabled()) return;
    //$$     int[] sel = WorldEditSelection.getBounds();
    //$$     if (sel == null) return;
    //$$     CaptureCamera cam = CaptureCamera.frame(sel, recording ? orbitYaw : 0.0);
    //$$     RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
    //$$             previewFbo.getColorAttachment(), 0, previewFbo.getDepthAttachment(), 1.0);
    //$$     GpuTextureView savedColor = RenderSystem.outputColorTextureOverride;
    //$$     GpuTextureView savedDepth = RenderSystem.outputDepthTextureOverride;
    //$$     RenderSystem.outputColorTextureOverride = previewFbo.getColorAttachmentView();
    //$$     RenderSystem.outputDepthTextureOverride = previewFbo.getDepthAttachmentView();
    //$$     try {
    //$$         RegionRenderer.render((ClientWorld) mc.world, sel, cam, previewW, previewH, tickDelta);
    //$$     } catch (Throwable t) {
    //$$         AsutanTweaks.LOGGER.error("[ClearBlockRender] preview render failed", t);
    //$$         releasePreview();
    //$$     } finally {
    //$$         RenderSystem.outputColorTextureOverride = savedColor;
    //$$         RenderSystem.outputDepthTextureOverride = savedDepth;
    //$$     }
    //$$ }
    //$$
    //$$ /** Called once per rendered frame from the game renderer mixin. */
    //$$ public static void onFrame(float tickDelta) {
    //$$     renderPreview(MinecraftClient.getInstance(), tickDelta);
    //$$     if (!recording) return;
    //$$     MinecraftClient mc = MinecraftClient.getInstance();
    //$$     if (mc.world == null || mc.world != recordingWorld || !Feature.CLEAR_BLOCK_RENDER.isEnabled()) {
    //$$         stop("recording stopped");
    //$$         return;
    //$$     }
    //$$     if (encoder == null || encoder.isBroken()) {
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("ffmpeg exited — recording stopped"));
    //$$         stop(null);
    //$$         return;
    //$$     }
    //$$
    //$$     double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
    //$$     long due = (long) (elapsed * fps);
    //$$     if (due <= framesWritten) return;
    //$$
    //$$     // ffmpeg is behind, or the previous readback has not landed yet. Skip the
    //$$     // whole frame rather than pile up work that stalls the render thread.
    //$$     if (!encoder.hasCapacity() || FrameGrabber.isBusy()) return;
    //$$
    //$$     // A frame that arrives late is repeated so the clip keeps real-time pacing,
    //$$     // but only up to a few repeats — a long stall should not fill the file.
    //$$     int repeats = (int) Math.min(due - framesWritten, 4);
    //$$     orbitYaw += Configs.Generic.CBR_ORBIT_SPEED.getDoubleValue() * repeats / fps;
    //$$
    //$$     FfmpegEncoder target = encoder;
    //$$     try {
    //$$         renderInto(mc, (ClientWorld) recordingWorld, fbo, bounds, frameW, frameH, orbitYaw, tickDelta, data -> {
    //$$             data.position(0).limit(frameW * frameH * 4);
    //$$             target.writeFrame(data, repeats);
    //$$         });
    //$$     } catch (Throwable t) {
    //$$         AsutanTweaks.LOGGER.error("[ClearBlockRender] region render failed", t);
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("Render failed — recording stopped"));
    //$$         stop(null);
    //$$         return;
    //$$     }
    //$$     framesWritten = due;
    //$$ }
    //$$
    //$$ /**
    //$$  * Draws the region into {@code target} and reads the result back.
    //$$  *
    //$$  * Every draw resolves its attachments through the output overrides, whatever
    //$$  * target its RenderLayer names, so nothing of the capture reaches the screen.
    //$$  */
    //$$ private static boolean renderInto(MinecraftClient mc, ClientWorld world, ThirdEyeFbo target, int[] region,
    //$$                                int w, int h, double yawOffset, float tickDelta,
    //$$                                java.util.function.Consumer<ByteBuffer> out) {
    //$$     CaptureCamera cam = CaptureCamera.frame(region, yawOffset);
    //$$     RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
    //$$             target.getColorAttachment(), 0, target.getDepthAttachment(), 1.0);
    //$$     GpuTextureView savedColor = RenderSystem.outputColorTextureOverride;
    //$$     GpuTextureView savedDepth = RenderSystem.outputDepthTextureOverride;
    //$$     RenderSystem.outputColorTextureOverride = target.getColorAttachmentView();
    //$$     RenderSystem.outputDepthTextureOverride = target.getDepthAttachmentView();
    //$$     try {
    //$$         RegionRenderer.render(world, region, cam, w, h, tickDelta);
    //$$     } finally {
    //$$         RenderSystem.outputColorTextureOverride = savedColor;
    //$$         RenderSystem.outputDepthTextureOverride = savedDepth;
    //$$     }
    //$$     return FrameGrabber.read(target.getColorAttachment(), w, h, out);
    //$$ }
//#else
    //$$ private static boolean recording;
    //$$ private static ThirdEyeFbo fbo;
    //$$ private static FfmpegEncoder encoder;
    //$$ private static int frameW, frameH, fps;
    //$$ private static int[] bounds;
    //$$ private static Object recordingWorld;
    //$$ private static long startNanos;
    //$$ private static long framesWritten;
    //$$ private static double orbitYaw;
    //$$ /** Non-null only while a capture pass is drawing, so the render target mixin can redirect. */
    //$$ private static ThirdEyeFbo captureTarget;
    //$$
    //$$ public static boolean isRecording() { return recording; }
    //$$
    //$$ /** The buffer the capture pass draws into, or null when no pass is running. */
    //$$ public static RenderTarget activeCaptureTarget() { return captureTarget; }
    //$$
    //$$ public static void toggle() {
    //$$     if (recording) stop("stopped");
    //$$     else start();
    //$$ }
    //$$
    //$$ /** Renders one frame and writes it out as a transparent PNG. */
    //$$ public static void saveStill() {
    //$$     Minecraft mc = Minecraft.getInstance();
    //$$     if (!checkReady(mc)) return;
    //$$     int[] sel = WorldEditSelection.getBounds();
    //$$     int w = Configs.Generic.CBR_WIDTH.getIntegerValue();
    //$$     int h = Configs.Generic.CBR_HEIGHT.getIntegerValue();
    //$$     ThirdEyeFbo target = new ThirdEyeFbo(w, h, true);
    //$$     AsutanTweaks.LOGGER.info("[ClearBlockRender] capturing {}x{} of a {}x{}x{} region",
    //$$             w, h, sel[3] - sel[0] + 1, sel[4] - sel[1] + 1, sel[5] - sel[2] + 1);
    //$$     RegionRenderer.requestGeometryLog();
    //$$     try {
    //$$         // The readback runs in a callback, so the target is released there
    //$$         // rather than in a finally block that would fire first.
    //$$         boolean started = renderInto(mc, (ClientLevel) mc.level, target, sel, w, h, 0.0, 0.0f, data -> {
    //$$             try {
    //$$                 Path out = PngWriter.write(
    //$$                         FabricLoader.getInstance().getGameDir().resolve("clear_block_render"), data, w, h);
    //$$                 HudLogger.INSTANCE.log(MessageUtils.colorGreen("Saved " + out.getFileName()));
    //$$             } catch (Throwable t) {
    //$$                 AsutanTweaks.LOGGER.error("[ClearBlockRender] writing the image failed", t);
    //$$                 HudLogger.INSTANCE.log(MessageUtils.colorRed("Image export failed — see the log"));
    //$$             } finally {
    //$$                 target.destroyBuffers();
    //$$             }
    //$$         });
    //$$         if (!started) {
    //$$             // A recording's readback is in flight and owns the shared buffer.
    //$$             HudLogger.INSTANCE.log(MessageUtils.colorRed("Busy reading the last frame — try again"));
    //$$             target.destroyBuffers();
    //$$         }
    //$$     } catch (Throwable t) {
    //$$         AsutanTweaks.LOGGER.error("[ClearBlockRender] still capture failed", t);
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("Image export failed — see the log"));
    //$$         target.destroyBuffers();
    //$$     }
    //$$ }
    //$$
    //$$ private static boolean checkReady(Minecraft mc) {
    //$$     if (mc.level == null) return false;
    //$$     if (!Feature.CLEAR_BLOCK_RENDER.isEnabled()) {
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("Clear Block Render is disabled"));
    //$$         return false;
    //$$     }
    //$$     if (WorldEditSelection.getBounds() == null) {
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("No WorldEdit cuboid selection"));
    //$$         return false;
    //$$     }
    //$$     return true;
    //$$ }
    //$$
    //$$ private static void start() {
    //$$     Minecraft mc = Minecraft.getInstance();
    //$$     if (!checkReady(mc)) return;
    //$$
    //$$     frameW = Configs.Generic.CBR_WIDTH.getIntegerValue();
    //$$     frameH = Configs.Generic.CBR_HEIGHT.getIntegerValue();
    //$$     fps = Configs.Generic.CBR_FPS.getIntegerValue();
    //$$
    //$$     try {
    //$$         encoder = FfmpegEncoder.start(
    //$$                 FabricLoader.getInstance().getGameDir().resolve("clear_block_render"),
    //$$                 Configs.Generic.CBR_FFMPEG_PATH.getValue(),
    //$$                 Configs.Generic.CBR_FORMAT.getValue(),
    //$$                 frameW, frameH, fps, Configs.Generic.CBR_SPEED.getDoubleValue());
    //$$     } catch (Exception e) {
    //$$         AsutanTweaks.LOGGER.warn("[ClearBlockRender] failed to start ffmpeg", e);
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("ffmpeg could not be started — check FFmpeg Path"));
    //$$         encoder = null;
    //$$         return;
    //$$     }
    //$$
    //$$     fbo = new ThirdEyeFbo(frameW, frameH, true);

    //$$     bounds = WorldEditSelection.getBounds();
    //$$     recordingWorld = mc.level;
    //$$     startNanos = System.nanoTime();
    //$$     framesWritten = 0;
    //$$     orbitYaw = 0.0;
    //$$     recording = true;
    //$$     RegionRenderer.requestGeometryLog();
    //$$     HudLogger.INSTANCE.log(MessageUtils.colorGreen("Recording " + frameW + "x" + frameH + " @" + fps + "fps"));
    //$$ }
    //$$
    //$$ private static void stop(String why) {
    //$$     recording = false;
    //$$     if (encoder != null) {
    //$$         encoder.close();
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorGreen("Saved " + encoder.outputFile().getFileName()));
    //$$         encoder = null;
    //$$     } else if (why != null) {
    //$$         HudLogger.INSTANCE.log(why);
    //$$     }
    //$$     if (fbo != null) { fbo.destroyBuffers(); fbo = null; }

    //$$     bounds = null;
    //$$     recordingWorld = null;
    //$$ }
    //$$
    //$$ private static ThirdEyeFbo previewFbo;
    //$$ private static int previewW, previewH;
    //$$ private static boolean previewWanted;
    //$$
    //$$ /**
    //$$  * Asks for a preview frame at this size on the next rendered frame.
    //$$  *
    //$$  * The screen cannot render the region itself: its draw phase only records GUI
    //$$  * state, while the region pass needs to open real render passes. So the pass
    //$$  * runs on the frame hook and the screen blits the result.
    //$$  */
    //$$ public static void requestPreview(int width, int height) {
    //$$     previewWanted = true;
    //$$     if (previewFbo == null || previewW != width || previewH != height) {
    //$$         if (previewFbo != null) previewFbo.destroyBuffers();
    //$$         previewFbo = new ThirdEyeFbo(Math.max(1, width), Math.max(1, height), true);
    //$$         previewW = width;
    //$$         previewH = height;
    //$$     }
    //$$ }
    //$$
    //$$ /** The preview colour texture, or null when no preview has been rendered. */
    //$$ public static GpuTextureView previewTexture() {
    //$$     return previewFbo == null ? null : previewFbo.getColorTextureView();
    //$$ }
    //$$
    //$$ public static void releasePreview() {
    //$$     previewWanted = false;
    //$$     if (previewFbo != null) { previewFbo.destroyBuffers(); previewFbo = null; }
    //$$ }
    //$$
    //$$ private static void renderPreview(Minecraft mc, float tickDelta) {
    //$$     if (!previewWanted || previewFbo == null) return;
    //$$     previewWanted = false;
    //$$     if (mc.level == null || !Feature.CLEAR_BLOCK_RENDER.isEnabled()) return;
    //$$     int[] sel = WorldEditSelection.getBounds();
    //$$     if (sel == null) return;
    //$$     CaptureCamera cam = CaptureCamera.frame(sel, recording ? orbitYaw : 0.0);
    //$$     clearTransparent(previewFbo);
    //$$     captureTarget = previewFbo;
    //$$     GpuTextureView savedColor = RenderSystem.outputColorTextureOverride;
    //$$     GpuTextureView savedDepth = RenderSystem.outputDepthTextureOverride;
    //$$     RenderSystem.outputColorTextureOverride = previewFbo.getColorTextureView();
    //$$     RenderSystem.outputDepthTextureOverride = previewFbo.getDepthTextureView();
    //$$     try {
    //$$         RegionRenderer.render((ClientLevel) mc.level, sel, cam, previewW, previewH, tickDelta);
    //$$     } catch (Throwable t) {
    //$$         AsutanTweaks.LOGGER.error("[ClearBlockRender] preview render failed", t);
    //$$         releasePreview();
    //$$     } finally {
    //$$         RenderSystem.outputColorTextureOverride = savedColor;
    //$$         RenderSystem.outputDepthTextureOverride = savedDepth;
    //$$         captureTarget = null;
    //$$     }
    //$$ }
    //$$
    //$$ /** Called once per rendered frame from the game renderer mixin. */
    //$$ public static void onFrame(float tickDelta) {
    //$$     renderPreview(Minecraft.getInstance(), tickDelta);
    //$$     if (!recording) return;
    //$$     Minecraft mc = Minecraft.getInstance();
    //$$     if (mc.level == null || mc.level != recordingWorld || !Feature.CLEAR_BLOCK_RENDER.isEnabled()) {
    //$$         stop("recording stopped");
    //$$         return;
    //$$     }
    //$$     if (encoder == null || encoder.isBroken()) {
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("ffmpeg exited — recording stopped"));
    //$$         stop(null);
    //$$         return;
    //$$     }
    //$$
    //$$     double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
    //$$     long due = (long) (elapsed * fps);
    //$$     if (due <= framesWritten) return;
    //$$
    //$$     // ffmpeg is behind, or the previous readback has not landed yet. Skip the
    //$$     // whole frame — render, readback and all — rather than pile up work that
    //$$     // stalls the render thread. framesWritten is left alone so the repeat
    //$$     // below fills the gap once things catch up.
    //$$     if (!encoder.hasCapacity() || FrameGrabber.isBusy()) return;
    //$$
    //$$     // A frame that arrives late is repeated so the clip keeps real-time pacing,
    //$$     // but only up to a few repeats — a long stall should not fill the file.
    //$$     int repeats = (int) Math.min(due - framesWritten, 4);
    //$$     orbitYaw += Configs.Generic.CBR_ORBIT_SPEED.getDoubleValue() * repeats / fps;
    //$$
    //$$     FfmpegEncoder target = encoder;
    //$$     try {
    //$$         renderInto(mc, (ClientLevel) recordingWorld, fbo, bounds, frameW, frameH, orbitYaw, tickDelta, data -> {
    //$$             data.position(0).limit(frameW * frameH * 4);
    //$$             target.writeFrame(data, repeats);
    //$$         });
    //$$     } catch (Throwable t) {
    //$$         AsutanTweaks.LOGGER.error("[ClearBlockRender] region render failed", t);
    //$$         HudLogger.INSTANCE.log(MessageUtils.colorRed("Render failed — recording stopped"));
    //$$         stop(null);
    //$$         return;
    //$$     }
    //$$     framesWritten = due;
    //$$ }
    //$$
    //$$ /**
    //$$  * Draws the region into {@code target} and reads the result back into {@code out}.
    //$$  *
    //$$  * The depth buffer is cleared to 0, not 1: MC 26.2 renders with a reversed
    //$$  * depth range, where the far plane sits at 0.
    //$$  */
    //$$ private static boolean renderInto(Minecraft mc, ClientLevel level, ThirdEyeFbo target, int[] region,
    //$$                                int w, int h, double yawOffset, float tickDelta,
    //$$                                java.util.function.Consumer<ByteBuffer> out) {
    //$$     CaptureCamera cam = CaptureCamera.frame(region, yawOffset);
    //$$     clearTransparent(target);
    //$$     captureTarget = target;
    //$$     // Every draw resolves its attachments through these overrides, whatever
    //$$     // OutputTarget its RenderType names — the translucent block layer draws to
    //$$     // the item entity target, which redirecting mainRenderTarget() alone missed.
    //$$     GpuTextureView savedColor = RenderSystem.outputColorTextureOverride;
    //$$     GpuTextureView savedDepth = RenderSystem.outputDepthTextureOverride;
    //$$     RenderSystem.outputColorTextureOverride = target.getColorTextureView();
    //$$     RenderSystem.outputDepthTextureOverride = target.getDepthTextureView();
    //$$     try {
    //$$         RegionRenderer.render(level, region, cam, w, h, tickDelta);
    //$$     } finally {
    //$$         RenderSystem.outputColorTextureOverride = savedColor;
    //$$         RenderSystem.outputDepthTextureOverride = savedDepth;
    //$$         captureTarget = null;
    //$$     }
    //$$     return FrameGrabber.read(target.getColorTexture(), w, h, out);
    //$$ }
    //$$
    //$$ /**
    //$$  * Clears a capture buffer to fully transparent black.
    //$$  *
    //$$  * MC 26.2 both takes the colour as a float vector and renders with a reversed
    //$$  * depth range, where the far plane sits at 0 rather than 1.
    //$$  */
    //$$ private static void clearTransparent(ThirdEyeFbo target) {
//#if MC >= 260200
    //$$     RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
    //$$             target.getColorTexture(), new Vector4f(0.0f, 0.0f, 0.0f, 0.0f),
    //$$             target.getDepthTexture(), 0.0);
//#else
    //$$     RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
    //$$             target.getColorTexture(), 0, target.getDepthTexture(), 1.0);
//#endif
    //$$ }
//#endif
}
