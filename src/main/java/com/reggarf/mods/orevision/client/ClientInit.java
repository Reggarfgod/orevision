package com.reggarf.mods.orevision.client;

import com.reggarf.mods.orevision.config.OreConfigIO;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        OreConfigIO.load();
    }
}
