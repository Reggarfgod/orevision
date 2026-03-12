package com.reggarf.mods.orevision.scanner;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class OreHighlighter {

    public static final int RADIUS = 16;

    public static boolean enabled = false;
    public static boolean forceRefresh = true;
    public static BlockPos lastCenter = BlockPos.ZERO;

    @SubscribeEvent
    public static void onWorldRender(RenderLevelStageEvent event) {

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        boolean active =
                mc.player.getPersistentData().getBoolean("orevision_active");

        // Potion just ended
        if (!active && enabled) {
            enabled = false;
            OreBufferBuilder.destroy();
            return;
        }

        // Potion just started
        if (active && !enabled) {
            enabled = true;
            forceRefresh = true;
        }

        if (!enabled)
            return;

        BlockPos center = mc.player.blockPosition();

        if (OreBufferBuilder.needsRebuild(center)) {
            OreBufferBuilder.rebuild(mc.level, center, mc.player);
            lastCenter = center;
            forceRefresh = false;
        }

        OreRender.render(event, mc);
    }
}
