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
import java.util.HashSet;
import java.util.Set;

public class DefaultOreVisionRecipes {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void generateDefaultsIfMissing() {

        Path dir = FMLPaths.CONFIGDIR.get().resolve("orevision/recipes");

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Set<String> generated = new HashSet<>();

        for (Block block : BuiltInRegistries.BLOCK) {

            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null) continue;

            String baseName = simplifyOreName(id.getPath());
            if (baseName == null) continue;

            // avoid duplicate iron/deepslate_iron
            if (!generated.add(baseName)) continue;

            Path file = dir.resolve(baseName + ".json");

            if (Files.exists(file)) continue;

            JsonObject json = new JsonObject();

            JsonObject ingredient = new JsonObject();
            ingredient.addProperty("item", id.toString());

            json.add("ingredient", ingredient);
            json.addProperty("input", "minecraft:awkward");
            json.addProperty("output", "orevision:" + baseName);

            try {
                Files.writeString(file, GSON.toJson(json));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String simplifyOreName(String path) {

        if (!path.endsWith("_ore"))
            return null;

        if (path.startsWith("deepslate_"))
            path = path.substring("deepslate_".length());

        return path.replace("_ore", "");
    }
}