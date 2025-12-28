package com.reggarf.mods.orevision.screen;

import com.reggarf.mods.orevision.config.OreConfig;
import com.reggarf.mods.orevision.config.OreConfigIO;
import com.reggarf.mods.orevision.scanner.BoxRenderMode;
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
        clearWidgets();

        int color = OreConfig.getColor(ore);
        r = (color >> 16) & 0xFF;
        g = (color >> 8) & 0xFF;
        b = color & 0xFF;

        int centerX = width / 2;

        // 🔹 Vertical layout anchor (scales perfectly)
        int startY = height / 2 - 70;
        int spacing = 24;

        addRenderableWidget(slider("Red", r, v -> r = v, centerX - 100, startY));
        addRenderableWidget(slider("Green", g, v -> g = v, centerX - 100, startY + spacing));
        addRenderableWidget(slider("Blue", b, v -> b = v, centerX - 100, startY + spacing * 2));

        // 🔹 Box render mode button
        addRenderableWidget(Button.builder(
                        Component.literal("Box: " + OreConfig.getBoxRenderMode()),
                        btn -> {
                            BoxRenderMode next = switch (OreConfig.getBoxRenderMode()) {
                                case LINES -> BoxRenderMode.QUADS;
                                case QUADS -> BoxRenderMode.VANILLA;
                                case VANILLA -> BoxRenderMode.LINES;
                            };
                            OreConfig.setBoxRenderMode(next);
                            OreConfigIO.save();
                            btn.setMessage(Component.literal("Box: " + next));
                        }
                ).pos(centerX - 60, startY + spacing * 3 + 6)
                .size(120, 20)
                .build());

        // 🔹 Bottom buttons (always visible)
        int bottomY = height - 28;

        addRenderableWidget(Button.builder(
                        Component.literal("Save"),
                        btn -> {
                            int newColor = 0xFF000000 | (r << 16) | (g << 8) | b;
                            OreConfig.setColor(ore, newColor);
                            OreConfigIO.save();
                            minecraft.setScreen(parent);
                        }
                ).pos(centerX - 90, bottomY)
                .size(80, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Reset"),
                        btn -> {
                            OreConfig.clearColor(ore);
                            OreConfigIO.save();
                            minecraft.setScreen(parent);
                        }
                ).pos(centerX + 10, bottomY)
                .size(80, 20)
                .build());
    }

    private AbstractSliderButton slider(
            String label,
            int start,
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
                int v = Mth.clamp((int) (value * 255), 0, 255);
                setter.accept(v);
                setMessage(Component.literal(label + ": " + v));
            }

            @Override
            protected void applyValue() {}
        };
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
      //  renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, delta);

        // 🔹 Title
        gfx.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);

        // 🔹 Color preview (scales & centers)
        int preview = 0xFF000000 | (r << 16) | (g << 8) | b;
        int px = width / 2;
        int py = height / 2 - 110;

        gfx.fill(px - 34, py - 2, px + 34, py + 34, 0xFFFFFFFF);
        gfx.fill(px - 32, py, px + 32, py + 32, preview);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
