package com.reggarf.mods.orevision.scanner;

import com.mojang.blaze3d.vertex.*;
import com.reggarf.mods.orevision.config.OreConfig;
import com.reggarf.mods.orevision.util.OreUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class OreBufferBuilder {

    public static VertexBuffer vertexBuffer;

    public static boolean needsRebuild(BlockPos currentCenter) {
        return vertexBuffer == null
                || OreHighlighter.forceRefresh
                || !currentCenter.equals(OreHighlighter.lastCenter);
    }

    public static void rebuild(Level level, BlockPos center, Player player) {

        destroy();

        ListTag allowedOres =
                player.getPersistentData().getList("orevision_ores", 8);

        if (allowedOres.isEmpty())
            return;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.DEBUG_LINES,
                DefaultVertexFormat.POSITION_COLOR
        );

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-OreHighlighter.RADIUS, -OreHighlighter.RADIUS, -OreHighlighter.RADIUS),
                center.offset(OreHighlighter.RADIUS, OreHighlighter.RADIUS, OreHighlighter.RADIUS))) {

            Block block = level.getBlockState(pos).getBlock();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);

            if (id == null)
                continue;

            // iron_ore -> iron
            String oreName = id.getPath();
            if (oreName.startsWith("deepslate_"))
                oreName = oreName.substring("deepslate_".length());

            if (!oreName.endsWith("_ore"))
                continue;

            oreName = oreName.replace("_ore", "");

            boolean allowed = false;
            for (int i = 0; i < allowedOres.size(); i++) {
                if (allowedOres.getString(i).equals(oreName)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed)
                continue;

            int argb = OreConfig.getColor(id);

            float a = ((argb >> 24) & 0xFF) / 255f;
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8) & 0xFF) / 255f;
            float b = (argb & 0xFF) / 255f;

            drawBox(buffer, pos.getX(), pos.getY(), pos.getZ(), r, g, b, a);
        }

        MeshData mesh = buffer.build();
        if (mesh == null)
            return;

        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vertexBuffer.bind();
        vertexBuffer.upload(mesh);
        VertexBuffer.unbind();
    }

    public static void destroy() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }

    /* ===================== GEOMETRY ===================== */

    private static void drawBox(
            BufferBuilder buf,
            float x, float y, float z,
            float r, float g, float b, float a
    ) {

        float s = 1.002f;

        // Top
        line(buf, x, y + s, z, x + s, y + s, z, r, g, b, a);
        line(buf, x + s, y + s, z, x + s, y + s, z + s, r, g, b, a);
        line(buf, x + s, y + s, z + s, x, y + s, z + s, r, g, b, a);
        line(buf, x, y + s, z + s, x, y + s, z, r, g, b, a);

        // Bottom
        line(buf, x, y, z, x + s, y, z, r, g, b, a);
        line(buf, x + s, y, z, x + s, y, z + s, r, g, b, a);
        line(buf, x + s, y, z + s, x, y, z + s, r, g, b, a);
        line(buf, x, y, z + s, x, y, z, r, g, b, a);

        // Verticals
        line(buf, x, y, z, x, y + s, z, r, g, b, a);
        line(buf, x + s, y, z, x + s, y + s, z, r, g, b, a);
        line(buf, x, y, z + s, x, y + s, z + s, r, g, b, a);
        line(buf, x + s, y, z + s, x + s, y + s, z + s, r, g, b, a);
    }

    private static void line(
            BufferBuilder buf,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        buf.addVertex(x1, y1, z1).setColor(r, g, b, a);
        buf.addVertex(x2, y2, z2).setColor(r, g, b, a);
    }
}
