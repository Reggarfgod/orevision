package com.reggarf.mods.orevision.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class OreConfigIO {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE =
            new TypeToken<Map<String, Integer>>() {}.getType();

    private static Path getFile() {
        return Minecraft.getInstance().gameDirectory
                .toPath()
                .resolve("config")
                .resolve("orevision")
                .resolve("orevision_colors.json");
    }

    /* ---------------- LOAD ---------------- */

    public static void load() {
        Path file = getFile();
        if (!Files.exists(file)) return;

        try {
            String json = Files.readString(file);
            Map<String, Integer> data = GSON.fromJson(json, MAP_TYPE);

            if (data == null) return;

            for (var e : data.entrySet()) {
                ResourceLocation id = ResourceLocation.tryParse(e.getKey());
                if (id != null) {
                    OreConfig.setColor(id, e.getValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ---------------- SAVE ---------------- */

    public static void save() {
        Path file = getFile();

        try {
            Files.createDirectories(file.getParent());

            Map<String, Integer> out = new HashMap<>();
            OreConfig.getAllColors().forEach(
                    (id, color) -> out.put(id.toString(), color)
            );

            Files.writeString(file, GSON.toJson(out));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
