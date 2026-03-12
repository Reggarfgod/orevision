package com.reggarf.mods.orevision;

import com.mojang.logging.LogUtils;
import com.reggarf.mods.orevision.register.ModCreativeTabs;
import com.reggarf.mods.orevision.register.OreVisionEvents;
import jdk.jfr.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;


@Mod(Orevision.MODID)
public class Orevision {
    public static final String MODID = "orevision";
    private static final Logger LOGGER = LogUtils.getLogger();
    public Orevision(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(OreVisionEvents.class);
        ModCreativeTabs.TABS.register(modEventBus);
    }

}
