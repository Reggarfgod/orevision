package com.reggarf.mods.orevision.scanner;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class OrePipelines {

    public static final RenderPipeline LINES_NO_DEPTH =
            RenderPipeline.builder(
                            RenderPipelines.MATRICES_FOG_SNIPPET,
                            RenderPipelines.GLOBALS_SNIPPET
                    )
                    .withLocation(ResourceLocation.fromNamespaceAndPath("orevision", "pipeline/ore_lines"))
                    .withVertexShader("core/rendertype_lines")
                    .withFragmentShader(ResourceLocation.fromNamespaceAndPath("orevision", "frag/ore_color"))
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(false)
                    .withVertexFormat(
                            DefaultVertexFormat.POSITION_COLOR_NORMAL,
                            VertexFormat.Mode.LINES
                    )
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withColorLogic(LogicOp.NONE)
                    .build();
}
