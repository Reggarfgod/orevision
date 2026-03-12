package com.reggarf.mods.orevision.register;

import com.reggarf.mods.orevision.config.OreConfig;
import com.reggarf.mods.orevision.scanner.OreHighlighter;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class OreVisionEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();

        ListTag ores = new ListTag();

        for (MobEffectInstance inst : player.getActiveEffects()) {

            if (inst.getEffect().value() instanceof OreVisionEffect e) {

                String oreName = e.getOreId().getPath();

                ResourceLocation normalOre =
                        ResourceLocation.fromNamespaceAndPath("minecraft", oreName + "_ore");

                ResourceLocation deepslateOre =
                        ResourceLocation.fromNamespaceAndPath("minecraft", "deepslate_" + oreName + "_ore");

                boolean enabled =
                        OreConfig.isEnabled(normalOre) ||
                                OreConfig.isEnabled(deepslateOre);

                if (!enabled) {

                    // Freeze potion timer
                    MobEffectInstance copy = new MobEffectInstance(
                            inst.getEffect(),
                            inst.getDuration() + 1,
                            inst.getAmplifier(),
                            inst.isAmbient(),
                            inst.isVisible(),
                            inst.showIcon()
                    );

                    player.addEffect(copy);

                    continue;
                }

                ores.add(StringTag.valueOf(oreName));
            }
        }

        if (!ores.isEmpty()) {

            player.getPersistentData().putBoolean("orevision_active", true);
            player.getPersistentData().put("orevision_ores", ores);

            OreHighlighter.forceRefresh = true;

        } else {

            player.getPersistentData().remove("orevision_active");
            player.getPersistentData().remove("orevision_ores");

            OreHighlighter.forceRefresh = true;
        }
    }
}