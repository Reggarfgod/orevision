package com.reggarf.mods.orevision.config;

import com.reggarf.mods.orevision.util.OreColorUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OreConfig {

    private static final Map<ResourceLocation, OreEntry> ORES = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static OreEntry getOrCreate(ResourceLocation ore) {
        return ORES.computeIfAbsent(ore, id -> {
            int color = generateInitialColor(id);
            OreEntry entry = new OreEntry(true, color);
            OreConfigIO.save();
            return entry;
        });
    }

    public static boolean isEnabled(ResourceLocation ore) {
        return getOrCreate(ore).enabled;
    }

    public static void setEnabled(ResourceLocation ore, boolean enabled) {
        getOrCreate(ore).enabled = enabled;
        OreConfigIO.save();
    }

    public static int getColor(ResourceLocation ore) {
        return getOrCreate(ore).color;
    }

    public static void setColor(ResourceLocation ore, int argb) {
        getOrCreate(ore).color = argb;
        OreConfigIO.save();
    }

    public static Map<ResourceLocation, OreEntry> getAll() {
        return ORES;
    }


    private static int generateInitialColor(ResourceLocation ore) {
        return BuiltInRegistries.BLOCK.getOptional(ore)
                .map(block -> {
                    float[] c = OreColorUtil.getColor(block);
                    if (c != null && c.length >= 3) {
                        int r = (int) (c[0] * 255f);
                        int g = (int) (c[1] * 255f);
                        int b = (int) (c[2] * 255f);
                        return 0xFF000000 | (r << 16) | (g << 8) | b;
                    }
                    return randomBrightColor();
                })
                .orElseGet(OreConfig::randomBrightColor);
    }

    private static int randomBrightColor() {
        int r = 100 + RANDOM.nextInt(156);
        int g = 100 + RANDOM.nextInt(156);
        int b = 100 + RANDOM.nextInt(156);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }


//    private static BoxRenderMode boxRenderMode;
//
//    static {
//        boxRenderMode = BoxRenderMode.VANILLA;
//
//        if (ModList.get().isLoaded("sodium")) {
//            boxRenderMode = BoxRenderMode.LINES;
//        }
//        if (ModList.get().isLoaded("embeddium")) {
//            boxRenderMode = BoxRenderMode.VANILLA;
//        }
//    }
//
//    public static BoxRenderMode getBoxRenderMode() {
//        return boxRenderMode;
//    }
//
//    public static void setBoxRenderMode(BoxRenderMode mode) {
//        boxRenderMode = mode;
//    }
}
