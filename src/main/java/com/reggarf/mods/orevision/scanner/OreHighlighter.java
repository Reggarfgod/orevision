package com.reggarf.mods.orevision.scanner;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.reggarf.mods.orevision.util.OreUtils;
import com.reggarf.mods.orevision.config.OreConfig;
import com.reggarf.mods.orevision.keybinds.Keybinds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
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

import org.joml.Matrix4f;

import static com.reggarf.mods.orevision.scanner.RenderOutline.renderOutlineBox;

@EventBusSubscriber(value = Dist.CLIENT)
public class OreHighlighter {

    private static final int RADIUS = 16;
    private static boolean enabled = false;

    private static final RenderType XRAY_TYPE = RenderType.create(
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

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        if (Keybinds.TOGGLE.consumeClick())
            enabled = !enabled;

        if (!enabled)
            return;

        Level level = mc.level;
        BlockPos center = mc.player.blockPosition();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffer.getBuffer(XRAY_TYPE);

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-RADIUS, -RADIUS, -RADIUS),
                center.offset(RADIUS, RADIUS, RADIUS))) {

            Block block = level.getBlockState(pos).getBlock();
            if (!OreUtils.isOre(block))
                continue;

            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !OreConfig.isEnabled(id))
                continue;

            int argb = OreConfig.getColor(id);

            float a = ((argb >> 24) & 0xFF) / 255f;
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8) & 0xFF) / 255f;
            float b = (argb & 0xFF) / 255f;

            AABB box = new AABB(pos).inflate(0.02f);

            renderOutlineBox(poseStack, vc, box, r, g, b, a);
        }

        poseStack.popPose();
        buffer.endBatch(XRAY_TYPE);
    }

}
