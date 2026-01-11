package com.reggarf.mods.orevision.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class OreConfigIO {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Type TYPE =
            new TypeToken<Map<String, OreEntry>>() {}.getType();

    private static Path getFile() {
        return Minecraft.getInstance().gameDirectory
                .toPath()
                .resolve("config")
                .resolve("orevision")
                .resolve("orevision_ores.json");
    }

    public static void load() {
        Path file = getFile();
        if (!Files.exists(file)) return;

        try {
            String json = Files.readString(file);
            Map<String, OreEntry> data = GSON.fromJson(json, TYPE);
            if (data == null) return;

            data.forEach((k, v) -> {
                ResourceLocation id = ResourceLocation.tryParse(k);
                if (id != null) {
                    OreConfig.getAll().put(id, v);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Path file = getFile();
            Files.createDirectories(file.getParent());

            Map<String, OreEntry> out = new HashMap<>();
            OreConfig.getAll().forEach(
                    (id, entry) -> out.put(id.toString(), entry)
            );

            Files.writeString(file, GSON.toJson(out));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
