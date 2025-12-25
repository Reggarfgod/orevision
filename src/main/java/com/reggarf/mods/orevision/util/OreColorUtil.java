package com.reggarf.mods.orevision.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class OreColorUtil {

    private static final Map<ResourceLocation, float[]> CACHE = new HashMap<>();

    public static float[] getColor(Block oreBlock) {
        ResourceLocation blockId = oreBlock.builtInRegistryHolder().key().location();

        if (CACHE.containsKey(blockId)) {
            return CACHE.get(blockId);
        }

        Item inferred = inferDropItem(blockId);
        if (inferred != null && inferred != Items.AIR) {
            float[] color = extractItemColor(inferred);
            CACHE.put(blockId, color);
            return color;
        }

        Item blockItem = oreBlock.asItem();
        if (blockItem != Items.AIR) {
            float[] color = extractItemColor(blockItem);
            CACHE.put(blockId, color);
            return color;
        }

        float[] color = stableColorFromId(blockId);
        CACHE.put(blockId, color);
        return color;
    }


    private static Item inferDropItem(ResourceLocation blockId) {
        String path = blockId.getPath();

        String base = path
                .replace("deepslate_", "")
                .replace("netherrack_", "")
                .replace("_ore", "")
                .replace("_ores", "");

        // raw_<name>
        Item raw = getItem("raw_" + base);
        if (raw != Items.AIR) return raw;

        // <name>_ingot
        Item ingot = getItem(base + "_ingot");
        if (ingot != Items.AIR) return ingot;

        // gem / dust / direct
        Item direct = getItem(base);
        if (direct != Items.AIR) return direct;

        return Items.AIR;
    }

    private static Item getItem(String name) {
        ResourceLocation id = ResourceLocation.tryParse("minecraft:" + name);
        if (id == null) return Items.AIR;
        return BuiltInRegistries.ITEM.get(id);
    }

    private static float[] extractItemColor(Item item) {
        try {
            Minecraft mc = Minecraft.getInstance();

            TextureAtlasSprite sprite = mc.getItemRenderer()
                    .getModel(new ItemStack(item), null, null, 0)
                    .getParticleIcon();

            NativeImage img = sprite.contents().getOriginalImage();
            if (img == null) {
                return stableColorFromId(BuiltInRegistries.ITEM.getKey(item));
            }

            int[] hueCount = new int[360];
            int[][] rgbSum = new int[360][3];

            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    int argb = img.getPixelRGBA(x, y);
                    int a = (argb >>> 24) & 0xFF;
                    if (a < 40) continue;

                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;

                    // Ignore dark pixels
                    if (r + g + b < 150) continue;

                    // Ignore grayscale
                    if (Math.abs(r - g) < 12 &&
                            Math.abs(r - b) < 12 &&
                            Math.abs(g - b) < 12) continue;

                    float[] hsv = java.awt.Color.RGBtoHSB(r, g, b, null);
                    if (hsv[1] < 0.35f || hsv[2] < 0.4f) continue;

                    int hue = (int) (hsv[0] * 359);
                    hueCount[hue]++;
                    rgbSum[hue][0] += r;
                    rgbSum[hue][1] += g;
                    rgbSum[hue][2] += b;
                }
            }

            int bestHue = -1;
            int bestCount = 0;

            for (int i = 0; i < 360; i++) {
                if (hueCount[i] > bestCount) {
                    bestCount = hueCount[i];
                    bestHue = i;
                }
            }

            if (bestHue == -1) {
                return stableColorFromId(BuiltInRegistries.ITEM.getKey(item));
            }

            float r = rgbSum[bestHue][0] / (hueCount[bestHue] * 255f);
            float g = rgbSum[bestHue][1] / (hueCount[bestHue] * 255f);
            float b = rgbSum[bestHue][2] / (hueCount[bestHue] * 255f);

            // visibility boost
            r = 0.35f + r * 0.65f;
            g = 0.35f + g * 0.65f;
            b = 0.35f + b * 0.65f;

            return new float[]{r, g, b, 1.0f};

        } catch (Throwable t) {
            return stableColorFromId(BuiltInRegistries.ITEM.getKey(item));
        }
    }

    private static float[] stableColorFromId(ResourceLocation id) {
        if (id == null) return new float[]{0.8f, 0.8f, 0.8f, 1f};

        int h = id.toString().hashCode();

        float r = ((h >> 16) & 0xFF) / 255f;
        float g = ((h >> 8) & 0xFF) / 255f;
        float b = (h & 0xFF) / 255f;

        r = 0.35f + r * 0.65f;
        g = 0.35f + g * 0.65f;
        b = 0.35f + b * 0.65f;

        return new float[]{r, g, b, 1.0f};
    }
}
