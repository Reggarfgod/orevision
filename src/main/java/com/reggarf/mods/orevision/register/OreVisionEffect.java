package com.reggarf.mods.orevision.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class OreVisionEffect extends MobEffect {

    private final ResourceLocation oreId;

    public OreVisionEffect(ResourceLocation oreId, int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.oreId = oreId;
    }

    public ResourceLocation getOreId() {
        return oreId;
    }
}
