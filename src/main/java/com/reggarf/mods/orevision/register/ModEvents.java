package com.reggarf.mods.orevision.register;

import com.reggarf.mods.orevision.Orevision;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Path;

@EventBusSubscriber(modid = Orevision.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents {

    /* -------- Register Effects, Potions, Items -------- */
    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        JsonPotionRegistrar.register(event);
    }

//    /* -------- Ore Vision Creative Tab -------- */
//    @SubscribeEvent
//    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
//
//        if (!event.getTabKey().equals(ModCreativeTabs.OREVISION_TAB.getKey()))
//            return;
//
//        Path dir = FMLPaths.CONFIGDIR.get().resolve("orevision/potions");
//
//        for (JsonPotionData data : PotionJsonLoader.load(dir)) {
//            event.accept(
//                    BuiltInRegistries.ITEM.get(
//                            ResourceLocation.fromNamespaceAndPath(Orevision.MODID, data.id())
//                    )
//            );
//        }
//    }
}
