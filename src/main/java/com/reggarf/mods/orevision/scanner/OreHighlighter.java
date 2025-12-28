package com.reggarf.mods.orevision.scanner;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.reggarf.mods.orevision.config.OreConfig;
import com.reggarf.mods.orevision.keybinds.Keybinds;
import com.reggarf.mods.orevision.util.OreUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(value = Dist.CLIENT)
public class OreHighlighter {

    private static final int RADIUS = 16;
    private static boolean enabled = false;

    private static final RenderType XRAY_VANILLA = RenderType.create(
            "orevision_xray",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.DEBUG_LINES,
            512,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            GameRenderer::getRendertypeLinesShader))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard(
                            "always", GL11.GL_ALWAYS))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "xray",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.blendFunc(
                                        GlStateManager.SourceFactor.SRC_ALPHA,
                                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                                );
                            },
                            RenderSystem::disableBlend
                    ))
                    .createCompositeState(false)
    );


    private static final RenderType XRAY_CUSTOM = RenderType.create(
            "orevision_wireframe",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            GameRenderer::getPositionColorShader))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    @SubscribeEvent
    public static void onWorldRender(RenderLevelStageEvent event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        if (Keybinds.TOGGLE.consumeClick())
            enabled = !enabled;

        if (!enabled)
            return;

        if (OreConfig.getBoxRenderMode() == BoxRenderMode.VANILLA) {
            renderVanilla(event, mc);
        } else {
            renderCustom(event, mc);
        }
    }

    private static void renderVanilla(RenderLevelStageEvent event, Minecraft mc) {

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        Level level = mc.level;
        BlockPos center = mc.player.blockPosition();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        RenderSystem.enableBlend();
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0F, -10.0F);

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        RenderSystem.lineWidth(6.0F);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        VertexConsumer vc = buffer.getBuffer(XRAY_VANILLA);

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-RADIUS, -RADIUS, -RADIUS),
                center.offset(RADIUS, RADIUS, RADIUS))) {

            Block block = level.getBlockState(pos).getBlock();
            if (!OreUtils.isOre(block)) continue;

            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !OreConfig.isEnabled(id)) continue;

            int argb = OreConfig.getColor(id);
            float a = ((argb >> 24) & 0xFF) / 255f;
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8) & 0xFF) / 255f;
            float b = (argb & 0xFF) / 255f;

            AABB box = new AABB(pos).inflate(0.002);

            LevelRenderer.renderLineBox(poseStack, vc, box, r, g, b, a);
        }

        buffer.endBatch(XRAY_VANILLA);
        poseStack.popPose();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
    }

    private static void renderCustom(RenderLevelStageEvent event, Minecraft mc) {

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

        Level level = mc.level;
        BlockPos center = mc.player.blockPosition();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffer.getBuffer(XRAY_CUSTOM);

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-RADIUS, -RADIUS, -RADIUS),
                center.offset(RADIUS, RADIUS, RADIUS))) {

            Block block = level.getBlockState(pos).getBlock();
            if (!OreUtils.isOre(block)) continue;

            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !OreConfig.isEnabled(id)) continue;

            int argb = OreConfig.getColor(id);
            float a = ((argb >> 24) & 0xFF) / 255f;
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8) & 0xFF) / 255f;
            float b = (argb & 0xFF) / 255f;

            AABB box = new AABB(pos).inflate(0.02f);

            RenderOutline.renderOutlineBox(poseStack, vc, box, r, g, b, a);
        }

        poseStack.popPose();
        buffer.endBatch(XRAY_CUSTOM);
    }
}
