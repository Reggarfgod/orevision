package com.reggarf.mods.orevision.screen;

import com.reggarf.mods.orevision.config.OreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

public class OreScreen extends Screen {

    private final Screen parent;

    private EditBox searchBox;

    private final List<Checkbox> checkboxes = new ArrayList<>();
    private final List<Button> colorButtons = new ArrayList<>();
    private final List<ItemStack> oreIcons = new ArrayList<>();

    private List<ResourceLocation> allOres = List.of();

    private int scrollOffset = 0;

    private static final int SEARCH_Y = 30;
    private static final int SEARCH_HEIGHT = 20;

    private static final int LIST_TOP = SEARCH_Y + SEARCH_HEIGHT + 20;
    private static final int LIST_BOTTOM_PADDING = 60;
    private static final int ROW_HEIGHT = 24;

    private static final int ICON_X = 30;

    private static final int COLOR_BOX_SIZE = 12;
    private static final int COLOR_BUTTON_WIDTH = 60;
    private static final int COLOR_BUTTON_HEIGHT = 20;

    public OreScreen(Screen parent) {
        super(Component.literal("Ore ESP"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        scrollOffset = 0;

        searchBox = new EditBox(
                font,
                width / 2 - 100,
                SEARCH_Y,
                200,
                SEARCH_HEIGHT,
                Component.literal("Search ores...")
        );

        searchBox.setResponder(s -> {
            scrollOffset = 0;
            refreshList();
        });

        addRenderableWidget(searchBox);

        allOres = BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(e -> e.getValue().defaultBlockState().is(Tags.Blocks.ORES))
                .map(e -> BuiltInRegistries.BLOCK.getKey(e.getValue()))
                .filter(id -> id != null)
                .sorted((a, b) -> a.getPath().compareToIgnoreCase(b.getPath()))
                .toList();

        refreshList();

        addRenderableWidget(
                Button.builder(Component.literal("Done"),
                                b -> minecraft.setScreen(parent))
                        .pos(width / 2 - 40, height - 30)
                        .size(80, 20)
                        .build()
        );
    }

    private void refreshList() {

        checkboxes.forEach(this::removeWidget);
        colorButtons.forEach(this::removeWidget);

        checkboxes.clear();
        colorButtons.clear();
        oreIcons.clear();

        String filter = searchBox.getValue().toLowerCase().trim();
        int y = LIST_TOP - scrollOffset;

        for (ResourceLocation ore : allOres) {

            if (!filter.isEmpty() && !ore.toString().contains(filter))
                continue;

            if (y + ROW_HEIGHT < LIST_TOP || y > height - LIST_BOTTOM_PADDING) {
                y += ROW_HEIGHT;
                continue;
            }

            ItemStack icon = BuiltInRegistries.BLOCK.getOptional(ore)
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
            oreIcons.add(icon);

            // Checkbox (LEFT)
            Checkbox box = Checkbox.builder(
                            Component.literal("  " + ore.getPath()),
                            font
                    )
                    .selected(OreConfig.isEnabled(ore))
                    .onValueChange((b, v) -> OreConfig.setEnabled(ore, v))
                    .build();

            box.setX(ICON_X + 20);
            box.setY(y);
            addRenderableWidget(box);
            checkboxes.add(box);

            // Color button (RIGHT)
            Button colorBtn = Button.builder(
                            Component.literal("Color"),
                            b -> minecraft.setScreen(new ColorScreen(this, ore))
                    )
                    .pos(width - 90, y)
                    .size(COLOR_BUTTON_WIDTH, COLOR_BUTTON_HEIGHT)
                    .build();

            addRenderableWidget(colorBtn);
            colorButtons.add(colorBtn);

            y += ROW_HEIGHT;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {

        int totalRows = (int) allOres.stream()
                .filter(id -> searchBox.getValue().isEmpty()
                        || id.toString().contains(searchBox.getValue().toLowerCase()))
                .count();

        int contentHeight = totalRows * ROW_HEIGHT;
        int viewHeight = height - LIST_TOP - LIST_BOTTOM_PADDING;

        int maxScroll = Math.max(0, contentHeight - viewHeight);

        scrollOffset = Mth.clamp(
                scrollOffset - (int) (deltaY * 20),
                0,
                maxScroll
        );

        refreshList();
        return true;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {

        super.render(gfx, mouseX, mouseY, delta);

        gfx.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        gfx.fill(20, LIST_TOP - 6, width - 20, LIST_TOP - 8, 0x66FFFFFF);

        int y = LIST_TOP - scrollOffset;
        int index = 0;

        for (ResourceLocation ore : allOres) {

            if (!searchBox.getValue().isEmpty()
                    && !ore.toString().contains(searchBox.getValue().toLowerCase()))
                continue;

            if (y + ROW_HEIGHT < LIST_TOP || y > height - LIST_BOTTOM_PADDING) {
                y += ROW_HEIGHT;
                continue;
            }

            // Ore icon
            if (index < oreIcons.size()) {
                gfx.renderItem(oreIcons.get(index), ICON_X, y + 2);
            }

            // ===== COLOR BOX (ANCHOR TO COLOR BUTTON) =====
            int colorButtonX = width - 90;
            int colorButtonY = y;

            int colorBoxX = colorButtonX - COLOR_BOX_SIZE - 6;
            int colorBoxY = colorButtonY + (COLOR_BUTTON_HEIGHT - COLOR_BOX_SIZE) / 2;

            int argb = OreConfig.getColor(ore);
            int border = darkenColor(argb, 0.65f);

            gfx.fill(colorBoxX, colorBoxY,
                    colorBoxX + COLOR_BOX_SIZE, colorBoxY + COLOR_BOX_SIZE, argb);

            gfx.fill(colorBoxX - 1, colorBoxY - 1,
                    colorBoxX + COLOR_BOX_SIZE + 1, colorBoxY, border);
            gfx.fill(colorBoxX - 1, colorBoxY + COLOR_BOX_SIZE,
                    colorBoxX + COLOR_BOX_SIZE + 1, colorBoxY + COLOR_BOX_SIZE + 1, border);
            gfx.fill(colorBoxX - 1, colorBoxY,
                    colorBoxX, colorBoxY + COLOR_BOX_SIZE, border);
            gfx.fill(colorBoxX + COLOR_BOX_SIZE, colorBoxY,
                    colorBoxX + COLOR_BOX_SIZE + 1, colorBoxY + COLOR_BOX_SIZE, border);

            index++;
            y += ROW_HEIGHT;
        }
    }

    private static int darkenColor(int argb, float factor) {
        factor = Mth.clamp(factor, 0.0f, 1.0f);

        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        r = (int) (r * factor);
        g = (int) (g * factor);
        b = (int) (b * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
