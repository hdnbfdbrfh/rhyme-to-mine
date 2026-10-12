package dev.bryan.rhymetomine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;

/** Experimental voice-activated singleplayer mode using Windows desktop speech recognition. */
public class RhymeToMineClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            System.out.println("[Rhyme to Mine] Automatic speech requires Windows in this version.");
            return;
        }
        Thread t = new Thread(this::listen, "rhyme-to-mine-voice");
        t.setDaemon(true);
        t.start();
    }

    private void listen() {
        try {
            Path script = FabricLoader.getInstance().getConfigDir().resolve("rhyme-to-mine-speech.ps1");
            try (InputStream resource = getClass().getResourceAsStream("/rhyme-to-mine-speech.ps1")) {
                if (resource == null) throw new IOException("Speech script is missing from mod JAR");
                // Essential: always replace the old voice script when upgrading the mod.
                Files.copy(resource, script, StandardCopyOption.REPLACE_EXISTING);
            }
            Process process = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
                "-File", script.toAbsolutePath().toString()
            ).redirectErrorStream(true).start();
            Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("STATUS:")) {
                        System.out.println("[Rhyme to Mine] " + line.substring(7));
                        continue;
                    }
                    if (line.startsWith("ERROR:")) {
                        System.err.println("[Rhyme to Mine] Speech recognition: " + line.substring(6));
                        continue;
                    }
                    if (!line.startsWith("RHYME:")) continue;
                    String spoken = line.substring(6).trim().replaceAll("[\\r\\n]", " ");
                    if (spoken.isBlank() || Rhymes.detect(spoken).isEmpty()) continue;
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> {
                        // Voice unlocks only for singleplayer in this prototype.
                        if (mc.hasSingleplayerServer() && mc.player != null && mc.player.connection != null) {
                            mc.player.connection.sendCommand("rhyme " + spoken);
                        }
                    });
                }
            }
            System.err.println("[Rhyme to Mine] Speech process exited with code " + process.waitFor());
        } catch (Exception e) {
            System.err.println("[Rhyme to Mine] Failed to start speech recognition: " + e);
        }
    }
}
