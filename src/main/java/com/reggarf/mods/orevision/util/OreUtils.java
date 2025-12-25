package com.reggarf.mods.orevision.util;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

public class OreUtils {

    public static boolean isOre(Block block) {
        return block.defaultBlockState().is(Tags.Blocks.ORES);
    }
}
