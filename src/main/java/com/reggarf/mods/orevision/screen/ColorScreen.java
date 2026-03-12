package com.reggarf.mods.orevision.screen;

import com.reggarf.mods.better_lib.util.common.GuiUtils;
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

    private int presetStartY;

    private static final int BORDER = 0xFFFFA800;
    private static final int PANEL_TOP = 0xFF3A3A3A;
    private static final int PANEL_BOTTOM = 0xFF2B2B2B;

    public ColorScreen(Screen parent, ResourceLocation ore) {
        super(Component.literal("Pick Color"));
        this.parent = parent;
        this.ore = ore;
    }

    private int panelWidth() {
        return Math.min(320, width - 40);
    }

    private int panelHeight() {
        return Math.min(360, height - 40);
    }

    @Override
    protected void init() {

        clearWidgets();

        int color = OreConfig.getColor(ore);
        r = (color >> 16) & 0xFF;
        g = (color >> 8) & 0xFF;
        b = color & 0xFF;

        int panelW = panelWidth();
        int panelH = panelHeight();

        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        int centerX = width / 2;

        // ---------- SLIDERS ----------
        int sliderY = y + 90;
        int spacing = 24;

        addRenderableWidget(slider("Red", r, v -> r = v, centerX - 100, sliderY));
        addRenderableWidget(slider("Green", g, v -> g = v, centerX - 100, sliderY + spacing));
        addRenderableWidget(slider("Blue", b, v -> b = v, centerX - 100, sliderY + spacing * 2));

        int slidersBottom = sliderY + spacing * 3;

        // ---------- BOTTOM BUTTONS ----------
        int bottomY = y + panelH - 28;

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

        // ---------- PRESET COLORS ----------
        int boxSize = 14;
        int boxSpacing = 6;
        int boxesPerRow = 10;

        int rows = (int) Math.ceil(PRESET_COLORS.length / (double) boxesPerRow);
        int gridHeight = rows * (boxSize + boxSpacing) - boxSpacing;

        int availableSpace = bottomY - slidersBottom;

        presetStartY = slidersBottom + (availableSpace - gridHeight) / 2;

        int startX = centerX - ((boxesPerRow * (boxSize + boxSpacing)) / 2);

        for (int i = 0; i < PRESET_COLORS.length; i++) {

            int col = PRESET_COLORS[i];

            int bx = startX + (i % boxesPerRow) * (boxSize + boxSpacing);
            int by = presetStartY + (i / boxesPerRow) * (boxSize + boxSpacing);

            addRenderableWidget(Button.builder(Component.empty(), btn -> {

                r = (col >> 16) & 0xFF;
                g = (col >> 8) & 0xFF;
                b = col & 0xFF;

            }).pos(bx, by).size(boxSize, boxSize).build());
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

                int v = Mth.clamp((int)(value * 255), 0, 255);

                setter.accept(v);

                setMessage(Component.literal(label + ": " + v));
            }

            @Override
            protected void applyValue() {}
        };
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {

        GuiUtils.drawDimBackground(gfx, width, height);

        int panelW = panelWidth();
        int panelH = panelHeight();

        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        gfx.fill(x - 3, y - 3, x + panelW + 3, y + panelH + 3, BORDER);
        gfx.fillGradient(x, y, x + panelW, y + panelH, PANEL_TOP, PANEL_BOTTOM);

        super.render(gfx, mouseX, mouseY, delta);

        gfx.drawCenteredString(font, title, width / 2, y + 15, BORDER);

        // ---------- COLOR PREVIEW ----------
        int preview = 0xFF000000 | (r << 16) | (g << 8) | b;

        int px = width / 2;
        int py = y + 40;

        gfx.fill(px - 34, py - 2, px + 34, py + 34, 0xFFFFFFFF);
        gfx.fill(px - 32, py, px + 32, py + 32, preview);

        // ---------- PRESET COLORS ----------
        int boxSize = 14;
        int boxSpacing = 6;
        int boxesPerRow = 10;

        int startX = px - ((boxesPerRow * (boxSize + boxSpacing)) / 2);

        for (int i = 0; i < PRESET_COLORS.length; i++) {

            int col = PRESET_COLORS[i];

            int bx = startX + (i % boxesPerRow) * (boxSize + boxSpacing);
            int by = presetStartY + (i / boxesPerRow) * (boxSize + boxSpacing);

            gfx.fill(bx, by, bx + boxSize, by + boxSize, col);

            gfx.fill(bx - 1, by - 1, bx + boxSize + 1, by, 0xFF000000);
            gfx.fill(bx - 1, by + boxSize, bx + boxSize + 1, by + boxSize + 1, 0xFF000000);
            gfx.fill(bx - 1, by, bx, by + boxSize, 0xFF000000);
            gfx.fill(bx + boxSize, by, bx + boxSize + 1, by + boxSize, 0xFF000000);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}