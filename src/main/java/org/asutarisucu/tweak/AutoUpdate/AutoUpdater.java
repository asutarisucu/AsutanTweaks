package org.asutarisucu.tweak.AutoUpdate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import org.asutarisucu.AsutanTweaks;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks for a newer AsutanTweaks on Modrinth at startup, and on request
 * downloads it and puts it in place of the running jar.
 *
 * The newer version is the one with a file for the same Minecraft version as
 * the running jar, told by the "-mc<version>.jar" end of the file name. The
 * running jar cannot be deleted on Windows while the game is open, so there the
 * swap is left to a small script started when the game exits. Until then the
 * new jar keeps a name Fabric does not load, so the two are never loaded
 * together.
 */
public final class AutoUpdater {

    private AutoUpdater() {}

    public enum State { CHECKING, LATEST, AVAILABLE, DOWNLOADING, READY, FAILED, UNSUPPORTED }

    private static final String PROJECT = "NE0sQsvt";
    /** Release jars are asutantweaks-v1.5.2-mc1.21.1.jar; local builds add -SNAPSHOT or +build.N. */
    private static final Pattern JAR_NAME =
            Pattern.compile("^asutantweaks-v[^-]+-mc(.+?)(?:-SNAPSHOT|\\+build\\.\\d+)?\\.jar$");

    private static volatile State state = State.CHECKING;
    private static volatile String latestVersion;
    private static String fileUrl, fileName, fileSha512;
    private static Path runningJar;
    private static boolean notified;

    public static State state() { return state; }

    public static String latestVersion() { return latestVersion; }

    public static void start() {
        Thread t = new Thread(AutoUpdater::check, "AsutanTweaks update check");
        t.setDaemon(true);
        t.start();
    }

    /** True the first time it is asked after an update was found. */
    public static synchronized boolean takeNotification() {
        if (state != State.AVAILABLE || notified) return false;
        notified = true;
        return true;
    }

    private static String userAgent() {
        return "asutarisucu/AsutanTweaks/" + currentVersion().getFriendlyString();
    }

    private static Version currentVersion() {
        return FabricLoader.getInstance().getModContainer("asutantweaks")
                .map(c -> c.getMetadata().getVersion()).orElseThrow();
    }

    private static void check() {
        try {
            ModContainer self = FabricLoader.getInstance().getModContainer("asutantweaks").orElseThrow();
            List<Path> paths = self.getOrigin().getKind() == net.fabricmc.loader.api.metadata.ModOrigin.Kind.PATH
                    ? self.getOrigin().getPaths() : List.of();
            Matcher m = paths.size() == 1 && Files.isRegularFile(paths.get(0))
                    ? JAR_NAME.matcher(paths.get(0).getFileName().toString()) : null;
            if (m == null || !m.matches()) {
                // Run from the development environment or a renamed jar.
                state = State.UNSUPPORTED;
                return;
            }
            runningJar = paths.get(0);
            String suffix = "-mc" + m.group(1) + ".jar";

            HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(
                            "https://api.modrinth.com/v2/project/" + PROJECT + "/version?loaders=%5B%22fabric%22%5D"))
                    .header("User-Agent", userAgent())
                    .timeout(Duration.ofSeconds(20))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new IllegalStateException("HTTP " + res.statusCode());

            Version current = currentVersion();
            Version best = null;
            for (JsonElement e : JsonParser.parseString(res.body()).getAsJsonArray()) {
                JsonObject v = e.getAsJsonObject();
                SemanticVersion ver;
                try {
                    ver = SemanticVersion.parse(v.get("version_number").getAsString());
                } catch (Exception ignored) {
                    continue;
                }
                if (ver.compareTo(current) <= 0 || (best != null && ver.compareTo(best) <= 0)) continue;
                JsonArray files = v.getAsJsonArray("files");
                for (JsonElement f : files) {
                    JsonObject file = f.getAsJsonObject();
                    String name = file.get("filename").getAsString();
                    if (!name.startsWith("asutantweaks-v") || !name.endsWith(suffix)) continue;
                    best = ver;
                    fileUrl = file.get("url").getAsString();
                    fileName = name;
                    fileSha512 = file.getAsJsonObject("hashes").get("sha512").getAsString();
                    break;
                }
            }
            if (best == null) {
                state = State.LATEST;
            } else {
                latestVersion = best.getFriendlyString();
                state = State.AVAILABLE;
                AsutanTweaks.LOGGER.info("AsutanTweaks {} is available", latestVersion);
            }
        } catch (Throwable e) {
            // Also covers an old Fabric Loader without the ModOrigin API.
            AsutanTweaks.LOGGER.warn("Update check failed", e);
            state = State.FAILED;
        }
    }

    /** Starts downloading the newer version. Does nothing unless one was found. */
    public static synchronized void download() {
        if (state != State.AVAILABLE) return;
        state = State.DOWNLOADING;
        Thread t = new Thread(AutoUpdater::downloadNow, "AsutanTweaks update download");
        t.setDaemon(true);
        t.start();
    }

    private static void downloadNow() {
        Path dir = runningJar.getParent();
        Path target = dir.resolve(fileName);
        // Not a .jar, so Fabric skips it if the game starts before the swap.
        Path staged = dir.resolve(fileName + ".update");
        try {
            HttpClient http = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(fileUrl))
                    .header("User-Agent", userAgent())
                    .build();
            HttpResponse<InputStream> res = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() != 200) throw new IllegalStateException("HTTP " + res.statusCode());
            try (InputStream in = res.body()) {
                Files.copy(in, staged, StandardCopyOption.REPLACE_EXISTING);
            }
            byte[] digest = MessageDigest.getInstance("SHA-512").digest(Files.readAllBytes(staged));
            if (!HexFormat.of().formatHex(digest).equalsIgnoreCase(fileSha512)) {
                Files.deleteIfExists(staged);
                throw new IllegalStateException("SHA-512 mismatch");
            }

            try {
                Files.delete(runningJar);
                Files.move(staged, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception locked) {
                // Windows keeps the running jar open; swap them after the game exits.
                swapOnExit(runningJar, staged, target);
            }
            state = State.READY;
            AsutanTweaks.LOGGER.info("Downloaded {}; it is used from the next start", fileName);
        } catch (Exception e) {
            AsutanTweaks.LOGGER.warn("Update download failed", e);
            state = State.FAILED;
        }
    }

    private static void swapOnExit(Path oldJar, Path staged, Path target) {
        String script = "$old=" + quote(oldJar) + ";$tmp=" + quote(staged) + ";$new=" + quote(target) + ";"
                + "for($i=0;$i -lt 120;$i++){try{Remove-Item -LiteralPath $old -Force -ErrorAction Stop;break}"
                + "catch{Start-Sleep -Milliseconds 500}};"
                + "if(-not(Test-Path -LiteralPath $old)){Move-Item -LiteralPath $tmp -Destination $new -Force}";
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden",
                        "-Command", script).start();
            } catch (Exception e) {
                AsutanTweaks.LOGGER.warn("Could not start the update swap", e);
            }
        }, "AsutanTweaks update swap"));
    }

    /** A PowerShell single-quoted string, in which only ' needs escaping. */
    private static String quote(Path p) {
        return "'" + p.toAbsolutePath().toString().replace("'", "''") + "'";
    }
}
