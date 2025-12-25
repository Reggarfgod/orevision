package com.reggarf.mods.orevision.screen;

import com.reggarf.mods.orevision.config.OreConfig;
import com.reggarf.mods.orevision.config.OreConfigIO;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ColorScreen extends Screen {

    private final Screen parent;
    private final ResourceLocation ore;

    private int r, g, b;

    public ColorScreen(Screen parent, ResourceLocation ore) {
        super(Component.literal("Pick Color"));
        this.parent = parent;
        this.ore = ore;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int color = OreConfig.getColor(ore);
        r = (color >> 16) & 0xFF;
        g = (color >> 8) & 0xFF;
        b = color & 0xFF;

        int cx = width / 2;
        int y = height / 2 - 40;

        int buttonY = height - 40;
        int buttonWidth = 80;
        int gap = 10;

        addRenderableWidget(slider("Red", r, v -> r = v, cx - 100, y));
        addRenderableWidget(slider("Green", g, v -> g = v, cx - 100, y + 25));
        addRenderableWidget(slider("Blue", b, v -> b = v, cx - 100, y + 50));

        addRenderableWidget(Button.builder(
                        Component.literal("Save"),
                        btn -> {
                            int newColor =
                                    0xFF000000
                                            | ((r & 0xFF) << 16)
                                            | ((g & 0xFF) << 8)
                                            | (b & 0xFF);

                            OreConfig.setColor(ore, newColor);
                            OreConfigIO.save();
                            minecraft.setScreen(parent);
                        }
                ).pos(centerX - gap - buttonWidth, buttonY)
                .size(buttonWidth, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Reset"),
                        btn -> {
                            OreConfig.clearColor(ore);
                            OreConfigIO.save();
                            minecraft.setScreen(parent);
                        }
                ).pos(centerX + gap, buttonY)
                .size(buttonWidth, 20)
                .build());

    }

    private AbstractSliderButton slider(
            String label, int start,
            java.util.function.IntConsumer setter,
            int x, int y
    ) {
        return new AbstractSliderButton(
                x, y, 200, 20,
                Component.literal(label + ": " + start),
                start / 255.0
        ) {
            @Override
            protected void updateMessage() {
                int v = Mth.clamp((int)(value * 255), 0, 255);
                setter.accept(v);
                setMessage(Component.literal(label + ": " + v));
            }
            @Override protected void applyValue() {}
        };
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.render(gfx, mouseX, mouseY, delta);

        gfx.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);

        int preview = 0xFF000000 | (r << 16) | (g << 8) | b;
        gfx.fill(width / 2 - 32, 58, width / 2 + 32, 92, 0xFFFFFFFF);
        gfx.fill(width / 2 - 30, 60, width / 2 + 30, 90, preview);
    }
}
