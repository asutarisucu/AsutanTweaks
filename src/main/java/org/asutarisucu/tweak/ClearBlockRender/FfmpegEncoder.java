package org.asutarisucu.tweak.ClearBlockRender;

import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.lib.config.VideoFormat;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Pipes raw RGBA frames into an ffmpeg process.
 *
 * ffmpeg is required rather than optional: the alpha channel is the whole point
 * of this feature, and the only widely available codecs that keep it are VP9 in
 * WebM and ProRes 4444 in MOV — neither is something to hand-roll in Java.
 *
 * Frames are handed to a writer thread rather than written inline. ffmpeg
 * consumes a frame slower than the game produces one, so writing on the render
 * thread stalls it as soon as the pipe fills — a few seconds in, the game became
 * unusable. The queue makes that visible instead: {@link #hasCapacity()} reports
 * when ffmpeg is behind so the caller can skip a frame rather than block.
 *
 * Frames come off the GPU bottom-up, so {@code vflip} is applied in the filter
 * chain rather than by shuffling rows on the CPU.
 */
public final class FfmpegEncoder implements AutoCloseable {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** One queued frame plus how many times it should be repeated in the file. */
    private static final class Frame {
        final byte[] data;
        int repeats;

        Frame(int size) {
            this.data = new byte[size];
        }
    }

    private final Process process;
    private final OutputStream stdin;
    private final Path outputFile;
    private final ArrayBlockingQueue<Frame> free;
    private final ArrayBlockingQueue<Frame> filled;
    private final Thread writer;
    private volatile boolean broken;
    private volatile boolean closing;

    private FfmpegEncoder(Process process, Path outputFile, int frameBytes) {
        this.process = process;
        this.stdin = new BufferedOutputStream(process.getOutputStream(), 1 << 20);
        this.outputFile = outputFile;

        // Deep enough to ride out ffmpeg's jitter, capped by total bytes so a 4K
        // capture does not park hundreds of megabytes in the queue.
        int depth = (int) Math.max(2, Math.min(4, 48_000_000L / Math.max(1, frameBytes)));
        this.free = new ArrayBlockingQueue<>(depth);
        this.filled = new ArrayBlockingQueue<>(depth);
        for (int i = 0; i < depth; i++) this.free.add(new Frame(frameBytes));

        this.writer = new Thread(this::pump, "AsutanTweaks CBR encoder");
        this.writer.setDaemon(true);
        this.writer.start();
    }

    public Path outputFile() { return outputFile; }
    public boolean isBroken()  { return broken || !process.isAlive(); }

    /** Whether a frame can be queued right now without waiting for ffmpeg. */
    public boolean hasCapacity() { return !broken && !free.isEmpty(); }

    /**
     * Starts ffmpeg writing into {@code outputDir}.
     *
     * @throws IOException when ffmpeg cannot be launched — the caller should
     *                     surface that to the player rather than record nothing.
     */
    public static FfmpegEncoder start(Path outputDir, String ffmpegPath, VideoFormat format,
                                      int width, int height, int fps, double speed) throws IOException {
        Files.createDirectories(outputDir);
        Path out = outputDir.resolve(LocalDateTime.now().format(STAMP) + "." + format.extension);

        // Frames are captured at fps per real second. Declaring the input at
        // fps*speed makes the same frames span less (or more) time, which is the
        // playback rate: 2.0 plays twice as fast, 0.5 is slow motion.
        double inputRate = Math.max(1.0, fps * speed);

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath == null || ffmpegPath.isBlank() ? "ffmpeg" : ffmpegPath);
        cmd.add("-y");
        cmd.add("-f");            cmd.add("rawvideo");
        cmd.add("-pixel_format"); cmd.add("rgba");
        cmd.add("-video_size");   cmd.add(width + "x" + height);
        cmd.add("-framerate");    cmd.add(String.format(java.util.Locale.ROOT, "%.4f", inputRate));
        cmd.add("-i");            cmd.add("-");
        cmd.add("-vf");           cmd.add("vflip");
        for (String arg : format.encoderArgs) cmd.add(arg);
        cmd.add(out.toAbsolutePath().toString());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        Process p = pb.start();
        AsutanTweaks.LOGGER.info("[ClearBlockRender] recording to {}", out);
        return new FfmpegEncoder(p, out, width * height * 4);
    }

    /**
     * Queues one RGBA frame to be written {@code repeats} times.
     *
     * Returns without queueing anything when the writer is behind; check
     * {@link #hasCapacity()} first and skip rendering the frame at all.
     * The buffer's position is left at its limit.
     */
    public void writeFrame(ByteBuffer rgba, int repeats) {
        if (broken || closing) return;
        Frame frame = free.poll();
        if (frame == null) return;
        int n = Math.min(rgba.remaining(), frame.data.length);
        rgba.get(frame.data, 0, n);
        frame.repeats = Math.max(1, repeats);
        if (!filled.offer(frame)) free.offer(frame);
    }

    private void pump() {
        try {
            while (true) {
                Frame frame = filled.poll(100, TimeUnit.MILLISECONDS);
                if (frame == null) {
                    if (closing) break;
                    continue;
                }
                try {
                    for (int i = 0; i < frame.repeats; i++) stdin.write(frame.data);
                } finally {
                    free.offer(frame);
                }
            }
        } catch (IOException e) {
            broken = true;
            AsutanTweaks.LOGGER.warn("[ClearBlockRender] ffmpeg stopped accepting frames", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        closing = true;
        try {
            writer.join(TimeUnit.SECONDS.toMillis(30));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            stdin.flush();
            stdin.close();
        } catch (IOException ignored) {
        }
        try {
            if (!process.waitFor(30, TimeUnit.SECONDS)) process.destroyForcibly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }
}
