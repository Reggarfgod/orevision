package com.reggarf.mods.orevision.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.*;


public class OreColorUtil {

    private static final Map<ResourceLocation, float[]> BASE_ORE_COLORS = new HashMap<>();

    private static final Set<Integer> USED_PRESETS = new HashSet<>();

    public static float[] getColor(Block oreBlock) {
        ResourceLocation blockId = oreBlock
                .builtInRegistryHolder()
                .key()
                .location();

        ResourceLocation baseOreId = normalizeOreId(blockId);
        return BASE_ORE_COLORS.computeIfAbsent(baseOreId, OreColorUtil::assignColorForBaseOre);
    }

    private static float[] assignColorForBaseOre(ResourceLocation baseOreId) {
        int[] presets = PresetColors.PRESET_COLORS;

        // Use preset colors first (unique)
        if (USED_PRESETS.size() < presets.length) {
            int index = selectUnusedPresetIndex(baseOreId, presets.length);
            USED_PRESETS.add(index);
            return argbToFloat(presets[index], 0.85f);
        }

        // Fallback (deterministic)
        return generateDarkColorFromId(baseOreId);
    }

    private static int selectUnusedPresetIndex(ResourceLocation id, int max) {
        int hash = Math.abs(id.toString().hashCode());

        for (int i = 0; i < max; i++) {
            int idx = (hash + i) % max;
            if (!USED_PRESETS.contains(idx)) {
                return idx;
            }
        }

        // Should never happen
        return hash % max;
    }

    private static ResourceLocation normalizeOreId(ResourceLocation id) {
        String namespace = id.getNamespace();
        String path = id.getPath();

        path = path.replace("deepslate_", "");
        path = path.replace("nether_", "");
        path = path.replace("end_", "");

        if (path.endsWith("_ore")) {
            path = path.substring(0, path.length() - 4);
        }

        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static float[] generateDarkColorFromId(ResourceLocation id) {
        Random rand = new Random(id.toString().hashCode());
        float min = 0.20f;
        float max = 0.55f;

        float r = min + rand.nextFloat() * (max - min);
        float g = min + rand.nextFloat() * (max - min);
        float b = min + rand.nextFloat() * (max - min);

        return new float[]{r, g, b, 0.85f};
    }

    private static float[] argbToFloat(int argb, float alpha) {
        return new float[]{
                ((argb >> 16) & 0xFF) / 255f,
                ((argb >> 8) & 0xFF) / 255f,
                (argb & 0xFF) / 255f,
                alpha
        };
    }
}
