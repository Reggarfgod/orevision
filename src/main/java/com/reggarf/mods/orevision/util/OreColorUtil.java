package com.reggarf.mods.orevision.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class OreColorUtil {

    private static final Map<ResourceLocation, float[]> CACHE = new HashMap<>();

    public static float[] getColor(Block oreBlock) {
        ResourceLocation id = oreBlock
                .builtInRegistryHolder()
                .key()
                .location();

        return CACHE.computeIfAbsent(id, _id -> extractBlockColor(oreBlock));
    }

    private static float[] extractBlockColor(Block block) {
        try {
            Minecraft mc = Minecraft.getInstance();

            TextureAtlasSprite sprite = mc.getBlockRenderer()
                    .getBlockModelShaper()
                    .getParticleIcon(block.defaultBlockState());

            NativeImage img = sprite.contents().getOriginalImage();
            if (img == null) {
                return stableColorFromId(
                        block.builtInRegistryHolder().key().location()
                );
            }

            int rSum = 0, gSum = 0, bSum = 0;
            int count = 0;

            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {

                    // ABGR format (1.21+)
                    int abgr = img.getPixel(x, y);

                    int a = (abgr >>> 24) & 0xFF;
                    if (a < 40) continue;

                    int b = (abgr >>> 16) & 0xFF;
                    int g = (abgr >>> 8) & 0xFF;
                    int r = abgr & 0xFF;

                    // Ignore very dark pixels
                    if (r + g + b < 120) continue;

                    rSum += r;
                    gSum += g;
                    bSum += b;
                    count++;
                }
            }

            if (count == 0) {
                return stableColorFromId(
                        block.builtInRegistryHolder().key().location()
                );
            }

            float r = rSum / (count * 255f);
            float g = gSum / (count * 255f);
            float b = bSum / (count * 255f);

            // ESP visibility boost
            r = 0.35f + r * 0.65f;
            g = 0.35f + g * 0.65f;
            b = 0.35f + b * 0.65f;

            return new float[]{r, g, b, 1.0f};

        } catch (Throwable t) {
            return stableColorFromId(
                    block.builtInRegistryHolder().key().location()
            );
        }
    }

    /**
     * Stable fallback color (never crashes)
     */
    private static float[] stableColorFromId(ResourceLocation id) {
        if (id == null)
            return new float[]{0.8f, 0.8f, 0.8f, 1f};

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
