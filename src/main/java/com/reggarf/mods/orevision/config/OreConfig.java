package com.reggarf.mods.orevision.config;


import com.reggarf.mods.orevision.util.OreColorUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class OreConfig {

    // enabled ores
    private static final Map<ResourceLocation, Boolean> ENABLED = new HashMap<>();
    private static final Map<ResourceLocation, Integer> COLORS = new HashMap<>();

    public static boolean isEnabled(ResourceLocation ore) {
        return ENABLED.getOrDefault(ore, true);
    }

    public static void setEnabled(ResourceLocation ore, boolean enabled) {
        ENABLED.put(ore, enabled);
    }

    public static int getColor(ResourceLocation ore) {

        if (COLORS.containsKey(ore)) {
            return COLORS.get(ore);
        }

        float[] c = OreColorUtil.getColor(
                net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(ore)
        );
        int r = (int) (c[0] * 255f);
        int g = (int) (c[1] * 255f);
        int b = (int) (c[2] * 255f);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static void setColor(ResourceLocation ore, int argb) {
        COLORS.put(ore, argb);
    }

    public static void clearColor(ResourceLocation ore) {
        COLORS.remove(ore);
    }

    public static Map<ResourceLocation, Integer> getAllColors() {
        return COLORS;
    }

}
