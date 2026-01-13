package com.reggarf.mods.orevision.scanner;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;

public class OreRender {

    public static void render(RenderLevelStageEvent event, Minecraft mc) {

        if (OreBufferBuilder.vertexBuffer == null)
            return;

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.applyModelViewMatrix();

        poseStack.mulPose(event.getModelViewMatrix());
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        OreBufferBuilder.vertexBuffer.bind();
        OreBufferBuilder.vertexBuffer.drawWithShader(
                poseStack.last().pose(),
                event.getProjectionMatrix(),
                RenderSystem.getShader()
        );
        OreBufferBuilder.vertexBuffer.unbind();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
