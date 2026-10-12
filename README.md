# Rhyme to Mine — Fabric 26.2 (prototype source)

**This is source code, NOT a tested or compiled mod JAR.** It has not been run against Minecraft 26.2. Expect possible source/API adjustments before it builds.

## What it does
- Prevents players from breaking blocks until the block is unlocked.
- `/rhyme <sentence>` tests the strict rhyme detector manually. Example: `/rhyme I want stone before I break a bone`.
- On Windows, starts a local background Windows desktop speech-recognition process with voice activation, recognizes sentences, and automatically submits `/rhyme ...` when a known rhyme is heard.
- Unlocks some block families (logs, doors, stone, sand, diamond ores, and more).
- Saves each player's progress in `config/rhyme-to-mine-progress/<uuid>.txt` on the world/server instance.

## IMPORTANT limitations
- Speech uses **Windows System.Speech recognition**, NOT Simple Voice Chat audio. No Simple Voice Chat integration is implemented in this prototype. Windows Desktop Speech Recognition and a compatible language recognizer must be installed and functional. Newer Windows systems may not have this speech component available. If unavailable, use `/rhyme` manually for testing.
- This is a **limited built-in rhyme dictionary**; it does not recognize every English rhyme, nor every block. `stone/bones`, `log/frog`, `ore/floor/door/four`, `sand/hand`, etc. are explicitly recognized.
- Doesn't prevent block collection from explosions, pistons, commands, loot, or other gameplay routes; prevents *player mining* only.
- All Minecraft players need the server-side mod for enforcement. For speech input, the Windows player needs the client-side component. Singleplayer loads both automatically.
- Mic audio processing happens locally via Windows speech recognizer; no intentional network uploads in this project.

## Build on Windows
1. Install **Java JDK 25** and **Gradle 9.5.1 or later compatible with Loom 1.17**.
2. Open a PowerShell terminal in this project directory.
3. Run `gradle build` (first build downloads Minecraft/Fabric dependencies).
4. If it succeeds, copy `build/libs/rhyme-to-mine-0.1.0.jar` into `%appdata%\.minecraft\mods`. Not the `-sources` JAR.
5. Keep your Fabric API installed. Simple Voice Chat, Mod Menu and Placeholder API can stay, but **aren't dependencies** of this prototype.
6. Open a disposable test world (back up existing saves), and say `frog on a log` or try `/rhyme frog on a log`.

If Gradle reports API compilation errors, send the **complete error output** so the code can be adjusted for your Fabric 26.2 version.
