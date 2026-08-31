package org.asutarisucu.lib.config;

/**
 * Output container/codec for Clear Block Render. Both entries keep the alpha
 * channel — most common codecs (h264, h265) do not, so they are not offered.
 */
public enum VideoFormat {
    /**
     * VP9 in WebM (yuva420p). Plays in browsers, small files.
     *
     * libvpx-vp9 at its default deadline encodes far slower than the game
     * produces frames, which backs the capture up into the render thread.
     * {@code realtime} with {@code cpu-used 8} and row threading keeps it ahead
     * at the cost of some compression.
     */
    WEBM_VP9("webm", new String[]{"-c:v", "libvpx-vp9", "-pix_fmt", "yuva420p", "-b:v", "0", "-crf", "24",
            "-deadline", "realtime", "-cpu-used", "8", "-row-mt", "1"}),
    /** ProRes 4444 in MOV (yuva444p10le). Large files, for video editors. */
    MOV_PRORES("mov", new String[]{"-c:v", "prores_ks", "-profile:v", "4444", "-pix_fmt", "yuva444p10le"});

    public final String extension;
    public final String[] encoderArgs;

    VideoFormat(String extension, String[] encoderArgs) {
        this.extension = extension;
        this.encoderArgs = encoderArgs;
    }
}
