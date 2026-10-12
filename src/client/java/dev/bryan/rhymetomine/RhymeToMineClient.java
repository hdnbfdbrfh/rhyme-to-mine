package dev.bryan.rhymetomine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.nio.file.*;
import java.util.Locale;

/** Windows-only voice-activity speech bridge (Windows System.Speech). */
public class RhymeToMineClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) return;
        Thread t = new Thread(this::runSpeech, "rhyme-to-mine-windows-voice");
        t.setDaemon(true); t.start();
    }
    private void runSpeech() {
        try {
            Path script=FabricLoader.getInstance().getConfigDir().resolve("rhyme-to-mine-speech.ps1");
            if (!Files.exists(script)) try(var resource=getClass().getResourceAsStream("/rhyme-to-mine-speech.ps1")) {
                if(resource==null) return;
                Files.copy(resource,script);
            }
            Process p=new ProcessBuilder("powershell.exe","-NoProfile","-ExecutionPolicy","Bypass","-File",script.toAbsolutePath().toString())
                .redirectErrorStream(true).start();
            try(var lines=new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while((line=lines.readLine())!=null) {
                    if(!line.startsWith("RHYME:")) continue;
                    String phrase=line.substring(6).trim().replaceAll("[\\r\\n]", " ");
                    if(phrase.isEmpty() || Rhymes.detect(phrase).isEmpty()) continue;
                    Minecraft mc=Minecraft.getInstance();
                    mc.execute(()->{
                        if(mc.player!=null && mc.player.connection!=null) {
                            mc.player.connection.sendCommand("rhyme " + phrase);
                        }
                    });
                }
            }
        } catch(Exception e){ System.err.println("[Rhyme to Mine] Windows speech unavailable: "+e); }
    }
}
