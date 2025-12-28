package com.reggarf.mods.orevision.scanner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.reggarf.mods.orevision.config.OreConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class RenderOutline {

    public static void renderOutlineBox(
            PoseStack stack,
            VertexConsumer vc,
            AABB box,
            float r, float g, float b, float a
    ) {
        switch (OreConfig.getBoxRenderMode()) {
            case LINES -> renderLines(stack, vc, box, r, g, b, a);     // exact vanilla
            case VANILLA -> renderVanilla(stack, vc, box, r, g, b, a); // Mojang call
            case QUADS -> renderQuads(stack, vc, box, r, g, b, a);     // thick ESP
        }
    }

    private static void renderVanilla(
            PoseStack stack,
            VertexConsumer vc,
            AABB box,
            float r, float g, float b, float a
    ) {
        LevelRenderer.renderLineBox(stack, vc, box, r, g, b, a);
    }

    private static void renderLines(
            PoseStack stack,
            VertexConsumer vc,
            AABB box,
            float r, float g, float b, float a
    ) {
        Matrix4f m = stack.last().pose();

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Bottom
        line(vc, m, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        line(vc, m, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        line(vc, m, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        line(vc, m, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Top
        line(vc, m, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(vc, m, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        line(vc, m, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(vc, m, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // Vertical
        line(vc, m, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        line(vc, m, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(vc, m, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(vc, m, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
    }

    private static void line(
            VertexConsumer vc,
            Matrix4f m,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a);
    }

    private static void renderQuads(
            PoseStack stack,
            VertexConsumer vc,
            AABB box,
            float r, float g, float b, float a
    ) {
        Matrix4f m = stack.last().pose();

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        double camDist = box.getCenter().distanceTo(
                Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
        );

        float t = (float) Math.max(0.002F, camDist * 0.002F);

        // Bottom
        edgeX(vc, m, minX, minY, minZ, maxX, minY, minZ, t, r, g, b, a);
        edgeZ(vc, m, maxX, minY, minZ, maxX, minY, maxZ, t, r, g, b, a);
        edgeX(vc, m, minX, minY, maxZ, maxX, minY, maxZ, t, r, g, b, a);
        edgeZ(vc, m, minX, minY, minZ, minX, minY, maxZ, t, r, g, b, a);

        // Top
        edgeX(vc, m, minX, maxY, minZ, maxX, maxY, minZ, t, r, g, b, a);
        edgeZ(vc, m, maxX, maxY, minZ, maxX, maxY, maxZ, t, r, g, b, a);
        edgeX(vc, m, minX, maxY, maxZ, maxX, maxY, maxZ, t, r, g, b, a);
        edgeZ(vc, m, minX, maxY, minZ, minX, maxY, maxZ, t, r, g, b, a);

        // Vertical
        edgeY(vc, m, minX, minY, minZ, minX, maxY, minZ, t, r, g, b, a);
        edgeY(vc, m, maxX, minY, minZ, maxX, maxY, minZ, t, r, g, b, a);
        edgeY(vc, m, minX, minY, maxZ, minX, maxY, maxZ, t, r, g, b, a);
        edgeY(vc, m, maxX, minY, maxZ, maxX, maxY, maxZ, t, r, g, b, a);
    }

    private static void edgeX(VertexConsumer vc, Matrix4f m,
                              float x1, float y, float z,
                              float x2, float y2, float z2,
                              float t, float r, float g, float b, float a) {
        quad(vc, m,
                x1 - t, y - t, z - t,
                x2 + t, y + t, z + t,
                r, g, b, a);
    }

    private static void edgeY(VertexConsumer vc, Matrix4f m,
                              float x, float y1, float z,
                              float x2, float y2, float z2,
                              float t, float r, float g, float b, float a) {
        quad(vc, m,
                x - t, y1 - t, z - t,
                x + t, y2 + t, z + t,
                r, g, b, a);
    }

    private static void edgeZ(VertexConsumer vc, Matrix4f m,
                              float x, float y, float z1,
                              float x2, float y2, float z2,
                              float t, float r, float g, float b, float a) {
        quad(vc, m,
                x - t, y - t, z1 - t,
                x + t, y + t, z2 + t,
                r, g, b, a);
    }

    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(m, x2, y1, z1).setColor(r, g, b, a);
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a);
        vc.addVertex(m, x1, y2, z2).setColor(r, g, b, a);
    }
}
