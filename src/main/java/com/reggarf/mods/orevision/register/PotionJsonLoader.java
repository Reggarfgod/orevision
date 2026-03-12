package com.reggarf.mods.orevision.register;

import com.google.gson.*;
import net.neoforged.fml.loading.FMLPaths;

import java.io.Reader;
import java.nio.file.*;
import java.util.*;

public class PotionJsonLoader {

    private static final Gson GSON = new Gson();


    public static List<JsonPotionData> loadPotions(Path dir) {

        List<JsonPotionData> list = new ArrayList<>();

        try {

            if (!Files.exists(dir))
                Files.createDirectories(dir);

            for (Path p : Files.list(dir).toList()) {

                if (!p.toString().endsWith(".json"))
                    continue;

                try (Reader r = Files.newBufferedReader(p)) {

                    JsonObject o = GSON.fromJson(r, JsonObject.class);

                    list.add(new JsonPotionData(
                            o.get("id").getAsString(),
                            Integer.decode(o.get("color").getAsString()),
                            o.get("duration").getAsInt(),
                            o.get("amplifier").getAsInt()
                    ));
                }
            }

        } catch (Exception ignored) {}

        return list;
    }


    public static List<JsonRecipeData> loadRecipes(Path dir) {

        List<JsonRecipeData> list = new ArrayList<>();

        try {

            if (!Files.exists(dir))
                Files.createDirectories(dir);

            for (Path p : Files.list(dir).toList()) {

                if (!p.toString().endsWith(".json"))
                    continue;

                try (Reader r = Files.newBufferedReader(p)) {

                    JsonObject o = GSON.fromJson(r, JsonObject.class);

                    JsonObject ingredient = o.getAsJsonObject("ingredient");

                    list.add(new JsonRecipeData(
                            ingredient.get("item").getAsString(),
                            o.get("input").getAsString(),
                            o.get("output").getAsString()
                    ));
                }
            }

        } catch (Exception ignored) {}

        return list;
    }


    public static Path potionDir() {
        return FMLPaths.CONFIGDIR.get().resolve("orevision/potions");
    }

    public static Path recipeDir() {
        return FMLPaths.CONFIGDIR.get().resolve("orevision/recipes");
    }
}