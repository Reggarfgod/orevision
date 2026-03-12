package com.reggarf.mods.orevision.register;

import com.reggarf.mods.orevision.Orevision;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {

    public static final ResourceKey<CreativeModeTab> OREVISION_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    ResourceLocation.fromNamespaceAndPath(Orevision.MODID, "orevision")
            );

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Orevision.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OREVISION_TAB =
            TABS.register("orevision", () ->
                    CreativeModeTab.builder()
                            .title(Component.literal("Ore Vision"))
                            .icon(() -> new ItemStack(Items.POTION))
                            .displayItems((params, output) -> {

                                BuiltInRegistries.POTION.holders().forEach(holder -> {

                                    ResourceLocation id = holder.key().location();

                                    if (!id.getNamespace().equals(Orevision.MODID))
                                        return;

                                    var contents = new net.minecraft.world.item.alchemy.PotionContents(holder);

                                    // Normal Potion
                                    ItemStack normal = new ItemStack(Items.POTION);
                                    normal.set(DataComponents.POTION_CONTENTS, contents);
                                    output.accept(normal);

                                    // Splash Potion
                                    ItemStack splash = new ItemStack(Items.SPLASH_POTION);
                                    splash.set(DataComponents.POTION_CONTENTS, contents);
                                    output.accept(splash);

                                    // Lingering Potion
                                    ItemStack lingering = new ItemStack(Items.LINGERING_POTION);
                                    lingering.set(DataComponents.POTION_CONTENTS, contents);
                                    output.accept(lingering);
                                });

                            })
                            .build()
            );
}