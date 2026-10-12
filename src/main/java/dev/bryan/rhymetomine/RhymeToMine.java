package dev.bryan.rhymetomine;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.nio.file.*;
import java.io.*;
import java.util.*;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RhymeToMine implements ModInitializer {
    private static final Path SAVE_DIR = FabricLoader.getInstance().getConfigDir().resolve("rhyme-to-mine-progress");
    private static final Map<UUID,Set<String>> UNLOCKS = new HashMap<>();

    @Override public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) return true;
            if (!(player instanceof ServerPlayer sp)) return true;
            String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            if (allowed(sp.getUUID(), id)) return true;
            sp.displayClientMessage(Component.literal("§c🔒 Locked: " + id + " — SAY A RHYME!"), true);
            return false;
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> {
            dispatcher.register(literal("rhyme").then(argument("sentence", StringArgumentType.greedyString())
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    String sentence = StringArgumentType.getString(ctx, "sentence");
                    Set<String> terms = Rhymes.detect(sentence);
                    if (terms.isEmpty()) {
                        player.displayClientMessage(Component.literal("§cNo strict block rhyme detected!"), true);
                        return 0;
                    }
                    Set<String> mine = get(player.getUUID());
                    int before=mine.size();
                    for(String term:terms) unlockFamily(mine,term);
                    save(player.getUUID());
                    player.displayClientMessage(Component.literal("§a🎤 RHYME! §e"+terms+" §a("+(mine.size()-before)+" new blocks)"),true);
                    return 1;
                })));
            dispatcher.register(literal("rhymeprogress").executes(ctx -> {
                ServerPlayer p=ctx.getSource().getPlayerOrException();
                p.sendSystemMessage(Component.literal("§eRhyme to Mine: §a"+get(p.getUUID()).size()+"§e block types unlocked"));
                return 1;
            }));
        });
    }
    private static boolean allowed(UUID uuid, String blockId) { return get(uuid).contains(blockId) || blockId.equals("minecraft:air") || blockId.equals("minecraft:cave_air") || blockId.equals("minecraft:void_air"); }
    private static Set<String> get(UUID uuid) {
        return UNLOCKS.computeIfAbsent(uuid,id->{
            Set<String> data=new HashSet<>();
            Path file=SAVE_DIR.resolve(id+".txt");
            try { if(Files.exists(file)) data.addAll(Files.readAllLines(file)); } catch(IOException e){e.printStackTrace();}
            return data;
        });
    }
    private static void save(UUID uuid) {
        try { Files.createDirectories(SAVE_DIR); Files.write(SAVE_DIR.resolve(uuid+".txt"),get(uuid)); }
        catch(IOException e){e.printStackTrace();}
    }
    private static void unlockFamily(Set<String> out, String term) {
        String[] names = switch(term) {
            case "stone" -> new String[]{"stone", "cobblestone", "smooth_stone", "stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks"};
            case "bone", "bones" -> new String[]{"bone_block"};
            case "ore" -> new String[]{}; // Ore alone is ambiguous, not every ore in the game!
            case "diamond_ore" -> new String[]{"diamond_ore", "deepslate_diamond_ore"};
            case "iron_ore" -> new String[]{"iron_ore", "deepslate_iron_ore"};
            case "gold_ore" -> new String[]{"gold_ore", "deepslate_gold_ore", "nether_gold_ore"};
            case "sand" -> new String[]{"sand", "red_sand", "sandstone", "red_sandstone"};
            case "wood", "log" -> new String[]{"_log", "_wood", "_stem", "_hyphae", "stripped_"};
            case "door" -> new String[]{"_door"};
            case "brick" -> new String[]{"bricks", "brick_slab", "brick_stairs", "brick_wall"};
            case "glass" -> new String[]{"glass", "glass_pane"};
            case "grass" -> new String[]{"grass_block", "short_grass", "tall_grass"};
            case "snow" -> new String[]{"snow", "snow_block", "powder_snow"};
            case "ice" -> new String[]{"ice", "packed_ice", "blue_ice"};
            case "gold" -> new String[]{"gold_block"};
            case "wool" -> new String[]{"_wool"};
            case "bed" -> new String[]{"_bed"};
            case "gate" -> new String[]{"_fence_gate"};
            case "stair" -> new String[]{"_stairs"};
            case "red" -> new String[]{"red_"};
            case "white" -> new String[]{"white_"};
            case "light" -> new String[]{"sea_lantern", "glowstone", "light"};
            default -> new String[]{term};
        };
        for (var block:BuiltInRegistries.BLOCK) {
            Identifier id=BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals("minecraft")) continue;
            String name=id.getPath();
            for(String pattern:names) {
                if(pattern.startsWith("_") ? name.endsWith(pattern) : pattern.endsWith("_") ? name.startsWith(pattern) : name.equals(pattern)) {
                    out.add(id.toString()); break;
                }
            }
        }
    }
}
