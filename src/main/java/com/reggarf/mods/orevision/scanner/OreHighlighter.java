package com.reggarf.mods.orevision.scanner;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.reggarf.mods.orevision.config.OreConfig;
import com.reggarf.mods.orevision.keybinds.Keybinds;
import com.reggarf.mods.orevision.util.OreUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

@EventBusSubscriber(value = Dist.CLIENT)
public class OreHighlighter {

    private static final int RADIUS = 16;
    private static boolean enabled = false;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterParticles event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (Keybinds.TOGGLE.consumeClick())
            enabled = !enabled;

        if (!enabled) return;

        render(event.getPoseStack(), mc);
    }

    private static void render(PoseStack poseStack, Minecraft mc) {

        Level level = mc.level;
        BlockPos center = mc.player.blockPosition();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        RenderSystem.AutoStorageIndexBuffer indices =
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);

        BufferBuilder builder = Tesselator.getInstance()
                .begin(
                        OrePipelines.LINES_NO_DEPTH.getVertexFormatMode(),
                        OrePipelines.LINES_NO_DEPTH.getVertexFormat()
                );

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-RADIUS, -RADIUS, -RADIUS),
                center.offset(RADIUS, RADIUS, RADIUS))) {

            Block block = level.getBlockState(pos).getBlock();
            if (!OreUtils.isOre(block)) continue;

            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !OreConfig.isEnabled(id)) continue;

            int color = OreConfig.getColor(id);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            r *= 0.55f;
            g *= 0.55f;
            b *= 0.55f;


            ShapeRenderer.renderLineBox(
                    poseStack,
                    builder,
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                    r, g, b, 0.85f
            );
        }

        poseStack.popPose();

        try (MeshData mesh = builder.buildOrThrow()) {

            GpuBuffer vertexBuffer = RenderSystem.getDevice()
                    .createBuffer(
                            () -> "orevision_xray",
                            GpuBuffer.USAGE_VERTEX,
                            mesh.vertexBuffer()
                    );

            GpuBuffer indexBuffer = indices.getBuffer(mesh.drawState().indexCount());

            GpuTextureView colorTex = mc.getMainRenderTarget().getColorTextureView();
            GpuTextureView depthTex = mc.getMainRenderTarget().getDepthTextureView();

            GpuBufferSlice[] transforms =
                    RenderSystem.getDynamicUniforms().writeTransforms(
                            new DynamicUniforms.Transform(
                                    RenderSystem.getModelViewMatrix(),
                                    new Vector4f(1, 1, 1, 1),
                                    new Vector3f(),
                                    new Matrix4f(),
                                    2.0f
                            )
                    );

            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(
                            () -> "orevision_xray",
                            colorTex,
                            OptionalInt.empty(),
                            depthTex,
                            OptionalDouble.empty()
                    )) {

                pass.setPipeline(OrePipelines.LINES_NO_DEPTH);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.setUniform("DynamicTransforms", transforms[0]);
                pass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
            }

            vertexBuffer.close();
        }
    }
}
