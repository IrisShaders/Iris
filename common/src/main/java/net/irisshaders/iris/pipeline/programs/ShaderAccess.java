package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;

public class ShaderAccess {
	public static final VertexFormat IE_FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV0", VertexFormatElement.UV0)
		.add("Normal", VertexFormatElement.NORMAL)
		.padding(1)
		.build();

	public static CompiledShaderProgram getParticleTranslucentShader() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof ShaderRenderingPipeline) {
			CompiledShaderProgram override = ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.PARTICLES_TRANS);

			if (override != null) {
				return override;
			}
		}

		return Minecraft.getInstance().getShaderManager().getProgram(CoreShaders.PARTICLE);
	}

	public static CompiledShaderProgram getIEVBOShader() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof ShaderRenderingPipeline) {

			return ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? ShaderKey.IE_COMPAT_SHADOW : ShaderKey.IE_COMPAT);
		}

		return null;
	}

	public static ShaderProgram MEKANISM_FLAME = new ShaderProgram(ResourceLocation.fromNamespaceAndPath("iris", "mekanism_flame"), DefaultVertexFormat.POSITION_TEX_COLOR, ShaderDefines.EMPTY);
	public static ShaderProgram MEKASUIT = new ShaderProgram(ResourceLocation.fromNamespaceAndPath("iris", "mekasuit"), DefaultVertexFormat.NEW_ENTITY, ShaderDefines.EMPTY);
	public static ShaderProgram IE_COMPAT = new ShaderProgram(ResourceLocation.fromNamespaceAndPath("iris", "ie_vbo"), IE_FORMAT, ShaderDefines.EMPTY);
}
