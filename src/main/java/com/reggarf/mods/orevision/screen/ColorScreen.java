package com.reggarf.mods.orevision.screen;

import com.reggarf.mods.orevision.config.OreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static com.reggarf.mods.orevision.util.PresetColors.PRESET_COLORS;

public class ColorScreen extends Screen {

    private final Screen parent;
    private final ResourceLocation ore;

    private int r, g, b;

//    // ---------- PRESET COLORS ----------
//    private static final int[] PRESET_COLORS = {
//            0xFFFF0000, 0xFF00FF00, 0xFF0000FF,
//            0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF,
//            0xFFFF8000, 0xFFFFA500, 0xFF8000FF, 0xFF9932CC,
//            0xFFFFFFFF, 0xFFDDDDDD, 0xFFAAAAAA, 0xFF555555, 0xFF000000,
//            0xFF8B0000, 0xFFB22222, 0xFFFF69B4,
//            0xFF006400, 0xFF228B22, 0xFF7CFC00,
//            0xFF00008B, 0xFF1E90FF, 0xFF87CEEB,
//            0xFF8B4513, 0xFFA0522D, 0xFFDEB887,
//            0xFF55FF55, 0xFF5555FF, 0xFFFF5555,
//            0xFFFFFF55, 0xFFAA00AA, 0xFF00AAAA
//    };

    // cached for render()
    private int presetStartY;

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

        // ---------- SLIDERS ----------
        int sliderY = height / 2 - 40;
        int spacing = 24;

        addRenderableWidget(slider("Red", r, v -> r = v, centerX - 100, sliderY));
        addRenderableWidget(slider("Green", g, v -> g = v, centerX - 100, sliderY + spacing));
        addRenderableWidget(slider("Blue", b, v -> b = v, centerX - 100, sliderY + spacing * 2));

        int slidersBottom = sliderY + spacing * 3;

        // ---------- BOTTOM BUTTONS ----------
        int bottomY = height - 28;
        int buttonsTop = bottomY;

        addRenderableWidget(Button.builder(
                        Component.literal("Save"),
                        btn -> {
                            int newColor = 0xFF000000 | (r << 16) | (g << 8) | b;
                            OreConfig.setColor(ore, newColor);
                            minecraft.setScreen(parent);
                        })
                .pos(centerX - 90, bottomY)
                .size(80, 20)
                .build()
        );

        addRenderableWidget(Button.builder(
                        Component.literal("Reset"),
                        btn -> {
                            int def = OreConfig.getColor(ore);
                            r = (def >> 16) & 0xFF;
                            g = (def >> 8) & 0xFF;
                            b = def & 0xFF;
                        })
                .pos(centerX + 10, bottomY)
                .size(80, 20)
                .build()
        );

        // ---------- PRESET COLOR BOXES (CENTERED BETWEEN) ----------
        int boxSize = 14;
        int boxSpacing = 6;
        int boxesPerRow = 20;

        int rows = (int) Math.ceil(PRESET_COLORS.length / (double) boxesPerRow);
        int gridHeight = rows * (boxSize + boxSpacing) - boxSpacing;

        int availableSpace = buttonsTop - slidersBottom;
        presetStartY = slidersBottom + (availableSpace - gridHeight) / 2;

        int startX = centerX - ((boxesPerRow * (boxSize + boxSpacing)) / 2);

        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int col = PRESET_COLORS[i];

            int x = startX + (i % boxesPerRow) * (boxSize + boxSpacing);
            int y = presetStartY + (i / boxesPerRow) * (boxSize + boxSpacing);

            addRenderableWidget(Button.builder(Component.empty(), btn -> {
                        r = (col >> 16) & 0xFF;
                        g = (col >> 8) & 0xFF;
                        b = col & 0xFF;
                    })
                    .pos(x, y)
                    .size(boxSize, boxSize)
                    .build());
        }
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
        super.render(gfx, mouseX, mouseY, delta);

        gfx.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);

        // ---------- COLOR PREVIEW ----------
        int preview = 0xFF000000 | (r << 16) | (g << 8) | b;
        int px = width / 2;
        int py = height / 2 - 110;

        gfx.fill(px - 34, py - 2, px + 34, py + 34, 0xFFFFFFFF);
        gfx.fill(px - 32, py, px + 32, py + 32, preview);

        // ---------- DRAW PRESET BOX COLORS ----------
        int boxSize = 14;
        int boxSpacing = 6;
        int boxesPerRow = 20;
        int startX = px - ((boxesPerRow * (boxSize + boxSpacing)) / 2);

        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int col = PRESET_COLORS[i];
            int x = startX + (i % boxesPerRow) * (boxSize + boxSpacing);
            int y = presetStartY + (i / boxesPerRow) * (boxSize + boxSpacing);

            gfx.fill(x, y, x + boxSize, y + boxSize, col);
            gfx.fill(x - 1, y - 1, x + boxSize + 1, y, 0xFF000000);
            gfx.fill(x - 1, y + boxSize, x + boxSize + 1, y + boxSize + 1, 0xFF000000);
            gfx.fill(x - 1, y, x, y + boxSize, 0xFF000000);
            gfx.fill(x + boxSize, y, x + boxSize + 1, y + boxSize, 0xFF000000);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
