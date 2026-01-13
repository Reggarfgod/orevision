package com.reggarf.mods.orevision.scanner;

import com.reggarf.mods.orevision.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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

        if (Keybinds.TOGGLE.consumeClick()) {
            enabled = !enabled;
            forceRefresh = true;

            if (!enabled) {
                OreBufferBuilder.destroy();
                return;
            }
        }

        if (!enabled)
            return;

        BlockPos currentCenter = mc.player.blockPosition();

        if (OreBufferBuilder.needsRebuild(currentCenter)) {
            OreBufferBuilder.rebuild(mc.level, currentCenter);
            lastCenter = currentCenter;
            forceRefresh = false;
        }

        OreRender.render(event, mc);
    }
}
