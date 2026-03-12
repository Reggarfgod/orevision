package com.reggarf.mods.orevision.register;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultOreVisionPotions {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void generateDefaultsIfMissing() {

        Path dir = FMLPaths.CONFIGDIR.get().resolve("orevision/potions");

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (Block block : BuiltInRegistries.BLOCK) {

            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null)
                continue;

            String baseName = simplifyOreName(id.getPath());
            if (baseName == null)
                continue;

            Path file = dir.resolve(baseName + ".json");
            if (Files.exists(file))
                continue;

            JsonObject json = new JsonObject();
            json.addProperty("id", baseName);
            json.addProperty("color", defaultColor(baseName));
            json.addProperty("duration", defaultDuration(baseName));
            json.addProperty("amplifier", 0);

            try {
                Files.writeString(file, GSON.toJson(json));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * iron_ore / deepslate_iron_ore -> iron
     */
    private static String simplifyOreName(String path) {

        if (!path.endsWith("_ore"))
            return null;

        if (path.startsWith("deepslate_"))
            path = path.substring("deepslate_".length());

        return path.replace("_ore", "");
    }

    private static String defaultColor(String id) {
        return switch (id) {
            case "iron" -> "#C8C8C8";
            case "gold" -> "#FFD700";
            case "diamond" -> "#4AEDD9";
            case "emerald" -> "#17DD62";
            case "redstone" -> "#AA0000";
            case "lapis" -> "#345EC3";
            case "coal" -> "#1C1C1C";
            case "copper" -> "#B87333";
            case "zinc" -> "#9FA6A8";
            case "tin" -> "#B0C4DE";
            default -> "#FFFFFF";
        };
    }

    private static int defaultDuration(String id) {
        return switch (id) {
            case "redstone" -> 4800;
            case "coal" -> 3000;
            default -> 3600;
        };
    }
}
