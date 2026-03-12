    package com.reggarf.mods.orevision.register;

    import net.minecraft.core.Holder;
    import net.minecraft.core.registries.BuiltInRegistries;
    import net.minecraft.core.registries.Registries;
    import net.minecraft.resources.ResourceLocation;
    import net.minecraft.world.effect.MobEffect;
    import net.minecraft.world.effect.MobEffectInstance;
    import net.minecraft.world.item.Item;
    import net.minecraft.world.item.PotionItem;
    import net.minecraft.world.item.alchemy.Potion;
    import net.neoforged.fml.loading.FMLPaths;
    import net.neoforged.neoforge.registries.RegisterEvent;

    import java.nio.file.Path;
    import java.util.List;

    public class JsonPotionRegistrar {

        public static void register(RegisterEvent event) {


            DefaultOreVisionPotions.generateDefaultsIfMissing();
            DefaultOreVisionRecipes.generateDefaultsIfMissing();

            Path dir = FMLPaths.CONFIGDIR.get().resolve("orevision/potions");


            List<JsonPotionData> all = PotionJsonLoader.loadPotions(dir);

            if (event.getRegistryKey().equals(Registries.MOB_EFFECT)) {
                for (JsonPotionData data : all) {
                    ResourceLocation id =
                            ResourceLocation.fromNamespaceAndPath("orevision", data.id());

                    event.register(
                            Registries.MOB_EFFECT,
                            id,
                            () -> new OreVisionEffect(id, data.color())
                    );
                }
            }


            if (event.getRegistryKey().equals(Registries.POTION)) {
                for (JsonPotionData data : all) {

                    ResourceLocation id =
                            ResourceLocation.fromNamespaceAndPath("orevision", data.id());

                    Holder<MobEffect> effectHolder =
                            BuiltInRegistries.MOB_EFFECT.getHolder(id)
                                    .orElseThrow(() ->
                                            new IllegalStateException("Effect not registered: " + id));

                    event.register(
                            Registries.POTION,
                            id,
                            () -> new Potion(
                                    new MobEffectInstance(
                                            effectHolder,
                                            data.duration(),
                                            data.amplifier()
                                    )
                            )
                    );
                }
            }

            if (event.getRegistryKey().equals(Registries.ITEM)) {
                for (JsonPotionData data : all) {

                    ResourceLocation id =
                            ResourceLocation.fromNamespaceAndPath("orevision", data.id());

                    event.register(
                            Registries.ITEM,
                            id,
                            () -> new PotionItem(new Item.Properties().stacksTo(1))
                    );
                }
            }

        }
    }
