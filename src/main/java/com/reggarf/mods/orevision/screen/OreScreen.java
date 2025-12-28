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
import net.minecraft.world.level.block.Block;
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

        addRenderableWidget(Button.builder(
                        Component.literal("Done"),
                        b -> minecraft.setScreen(parent)
                )
                .pos(width / 2 - 40, height - 30)
                .size(80, 20)
                .build());
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

            Block block = BuiltInRegistries.BLOCK.get(ore);
            ItemStack icon = new ItemStack(block);
            oreIcons.add(icon);

            Checkbox box = Checkbox.builder(
                            Component.literal("  " + ore.getPath()), // space for icon
                            font
                    )
                    .selected(OreConfig.isEnabled(ore))
                    .onValueChange((b, v) -> OreConfig.setEnabled(ore, v))
                    .build();

            box.setX(50); // shifted for icon
            box.setY(y);
            addRenderableWidget(box);
            checkboxes.add(box);

            Button colorBtn = Button.builder(
                            Component.literal("Color"),
                            b -> minecraft.setScreen(new ColorScreen(this, ore))
                    )
                    .pos(width - 90, y)
                    .size(60, 20)
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

        // ===== Render ore icons =====
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

            if (index < oreIcons.size()) {
                gfx.renderItem(
                        oreIcons.get(index),
                        30,
                        y + 2
                );
            }

            index++;
            y += ROW_HEIGHT;
        }
    }
}
