package com.reggarf.mods.orevision;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;


@Mod(Orevision.MODID)
public class Orevision {
    public static final String MODID = "orevision";
    private static final Logger LOGGER = LogUtils.getLogger();
    public Orevision() {

    }
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
    public static ResourceLocation assetLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/" + path);
    }
}
