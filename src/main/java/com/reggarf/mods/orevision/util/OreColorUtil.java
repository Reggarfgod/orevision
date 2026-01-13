package com.reggarf.mods.orevision.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OreColorUtil {

    private static final Map<ResourceLocation, float[]> CACHE = new HashMap<>();

    public static float[] getColor(Block oreBlock) {
        ResourceLocation id = oreBlock
                .builtInRegistryHolder()
                .key()
                .location();

        return CACHE.computeIfAbsent(id, OreColorUtil::generateDarkColorFromId);
    }

    private static float[] generateDarkColorFromId(ResourceLocation id) {
        if (id == null) {
            return new float[]{0.4f, 0.4f, 0.4f, 1.0f};
        }

        Random rand = new Random(id.toString().hashCode());
        float min = 0.20f;
        float max = 0.55f;

        float r = min + rand.nextFloat() * (max - min);
        float g = min + rand.nextFloat() * (max - min);
        float b = min + rand.nextFloat() * (max - min);

        return new float[]{r, g, b, 0.85f};
    }
}
