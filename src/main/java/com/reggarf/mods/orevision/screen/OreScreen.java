package com.reggarf.mods.orevision.screen;

import com.reggarf.mods.better_lib.util.common.GuiUtils;
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

    private static final int BORDER = 0xFFFFA800;
    private static final int PANEL_TOP = 0xFF3A3A3A;
    private static final int PANEL_BOTTOM = 0xFF2B2B2B;

    private static final int ROW_HEIGHT = 24;

    public OreScreen(Screen parent) {
        super(Component.literal("Ore Vision"));
        this.parent = parent;
    }

    private int panelWidth() {
        return Math.min(320, width - 40);
    }

    private int panelHeight() {
        return Math.min(340, height - 40);
    }

    @Override
    protected void init() {

        scrollOffset = 0;

        int panelW = panelWidth();
        int panelH = panelHeight();

        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        searchBox = new EditBox(
                font,
                x + 12,
                y + 25,
                panelW - 24,
                18,
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
                        .pos(width / 2 - 40, y + panelH - 28)
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

        int panelW = panelWidth();
        int panelH = panelHeight();

        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        int listTop = y + 70;
        int listBottom = y + panelH - 50;

        int listY = listTop - scrollOffset;

        for (ResourceLocation ore : allOres) {

            if (!filter.isEmpty() && !ore.toString().contains(filter))
                continue;

            if (listY + ROW_HEIGHT < listTop || listY > listBottom) {
                listY += ROW_HEIGHT;
                continue;
            }

            ItemStack icon = BuiltInRegistries.BLOCK.getOptional(ore)
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);

            oreIcons.add(icon);

            Checkbox box = Checkbox.builder(
                            Component.literal("  " + ore.getPath()),
                            font)
                    .selected(OreConfig.isEnabled(ore))
                    .onValueChange((b, v) -> OreConfig.setEnabled(ore, v))
                    .build();

            box.setX(x + 32);
            box.setY(listY);

            addRenderableWidget(box);
            checkboxes.add(box);

            Button colorBtn = Button.builder(
                            Component.literal("Color"),
                            b -> minecraft.setScreen(new ColorScreen(this, ore))
                    )
                    .pos(x + panelW - 60, listY)
                    .size(50, 20)
                    .build();

            addRenderableWidget(colorBtn);
            colorButtons.add(colorBtn);

            listY += ROW_HEIGHT;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {

        int totalRows = (int) allOres.stream()
                .filter(id -> searchBox.getValue().isEmpty()
                        || id.toString().contains(searchBox.getValue().toLowerCase()))
                .count();

        int panelH = panelHeight();

        int viewHeight = panelH - 120;

        int contentHeight = totalRows * ROW_HEIGHT;

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

        gfx.drawCenteredString(font, "Ore Vision", width / 2, y + 15, BORDER);

        int listTop = y + 70;
        int listBottom = y + panelH - 50;

        int listY = listTop - scrollOffset;
        int index = 0;

        for (ResourceLocation ore : allOres) {

            if (!searchBox.getValue().isEmpty()
                    && !ore.toString().contains(searchBox.getValue().toLowerCase()))
                continue;

            if (listY + ROW_HEIGHT < listTop || listY > listBottom) {
                listY += ROW_HEIGHT;
                continue;
            }

            if (index < oreIcons.size()) {
                gfx.renderItem(oreIcons.get(index), x + 16, listY + 2);
            }

            int colorButtonX = x + panelW - 60;
            int colorBoxX = colorButtonX - 14;
            int colorBoxY = listY + 4;

            int argb = OreConfig.getColor(ore);

            gfx.fill(colorBoxX, colorBoxY,
                    colorBoxX + 12, colorBoxY + 12, argb);

            index++;
            listY += ROW_HEIGHT;
        }
    }
}