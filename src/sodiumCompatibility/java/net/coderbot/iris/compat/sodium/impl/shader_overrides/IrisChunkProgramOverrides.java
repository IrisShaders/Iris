package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.gfx.api.shader.Program;
import net.caffeinemc.gfx.api.shader.ShaderDescription;
import net.caffeinemc.gfx.api.shader.ShaderType;
import net.caffeinemc.gfx.opengl.shader.GlProgram;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.sodium.render.shader.ShaderConstants;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.AlphaTests;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Optional;

public class IrisChunkProgramOverrides {
	private boolean shadersCreated = false;
	private final EnumMap<IrisTerrainPass, Program<IrisChunkShaderInterface>> programs = new EnumMap<>(IrisTerrainPass.class);

	private String createVertexShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
		Optional<String> irisVertexShader;

		if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
			irisVertexShader = pipeline.getShadowVertexShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
			irisVertexShader = pipeline.getTerrainVertexShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			irisVertexShader = pipeline.getTranslucentVertexShaderSource();
		} else {
			throw new IllegalArgumentException("Unknown pass type " + pass);
		}

		return irisVertexShader.orElse(null);
	}

	private String createGeometryShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
		Optional<String> irisGeometryShader;

		if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
			irisGeometryShader = pipeline.getShadowGeometryShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
			irisGeometryShader = pipeline.getTerrainGeometryShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			irisGeometryShader = pipeline.getTranslucentGeometryShaderSource();
		} else {
			throw new IllegalArgumentException("Unknown pass type " + pass);
		}

		return irisGeometryShader.orElse(null);
	}

	private String createFragmentShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
		Optional<String> irisFragmentShader;

		if (pass == IrisTerrainPass.SHADOW) {
			irisFragmentShader = pipeline.getShadowFragmentShaderSource();
		} else if (pass == IrisTerrainPass.SHADOW_CUTOUT) {
			irisFragmentShader = pipeline.getShadowCutoutFragmentShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
			irisFragmentShader = pipeline.getTerrainFragmentShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_CUTOUT) {
			irisFragmentShader = pipeline.getTerrainCutoutFragmentShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			irisFragmentShader = pipeline.getTranslucentFragmentShaderSource();
		} else {
			throw new IllegalArgumentException("Unknown pass type " + pass);
		}

		return irisFragmentShader.orElse(null);
	}

	private BlendModeOverride getBlendOverride(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
		if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
			return pipeline.getShadowBlendOverride();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
			return pipeline.getTerrainBlendOverride();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			return pipeline.getTranslucentBlendOverride();
		} else {
			throw new IllegalArgumentException("Unknown pass type " + pass);
		}
	}

	private static ShaderConstants getShaderConstants(IrisTerrainPass pass, TerrainVertexType vertexType) {
		ShaderConstants.Builder constants = ShaderConstants.builder();
		if (pass == IrisTerrainPass.GBUFFER_CUTOUT || pass == IrisTerrainPass.SHADOW_CUTOUT) {
			constants.add("ALPHA_CUTOFF", String.valueOf(0.1f));
		}

		constants.add("USE_VERTEX_COMPRESSION");
		constants.add("VERT_SCALE", String.valueOf(vertexType.getVertexRange()));
		return constants.build();
	}

	@Nullable
	private Program<IrisChunkShaderInterface> createShader(RenderDevice device, IrisTerrainPass pass, TerrainVertexType type, SodiumTerrainPipeline pipeline) {
		ShaderConstants constants = getShaderConstants(pass, type);
		String vertShader = createVertexShader(pass, pipeline);
		String geomShader = createGeometryShader(pass, pipeline);
		String fragShader = createFragmentShader(pass, pipeline);
		ShaderDescription.Builder builder = ShaderDescription.builder().addShaderSource(ShaderType.VERTEX, vertShader);

		if (fragShader == null || vertShader == null) {
			if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
				return null;
			} else {
				throw new RuntimeException("Shader source is null for pass " + pass.getName());
			}
		}

		if (geomShader != null) {
			builder = builder.addShaderSource(ShaderType.GEOMETRY, geomShader);
		}

		ShaderDescription desc = builder.addShaderSource(ShaderType.FRAGMENT, fragShader).build();
		Program<IrisChunkShaderInterface> interfaces;

		try {
			interfaces = device.createProgram(desc, IrisChunkShaderInterface::new);
		} catch (RuntimeException e) {
			Iris.logger.error("Shader " + pass.getName() + " failed to compile!", e);
			throw e;
		}

		int handle = GlProgram.getHandle(interfaces);

		interfaces.getInterface().setInfo(pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT, pipeline, handle, pass, getBlendOverride(pass, pipeline), getAlphaReference(pass, pipeline));

		return interfaces;
	}

	private float getAlphaReference(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
		if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
			return pipeline.getShadowAlpha().orElse(AlphaTests.ONE_TENTH_ALPHA).getReference();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
			return pipeline.getTerrainCutoutAlpha().orElse(AlphaTests.ONE_TENTH_ALPHA).getReference();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			return pipeline.getTranslucentAlpha().orElse(AlphaTest.ALWAYS).getReference();
		} else {
			throw new IllegalArgumentException("Unknown pass type " + pass);
		}
	}

	public SodiumTerrainPipeline getSodiumTerrainPipeline() {
		WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();

		if (worldRenderingPipeline != null) {
			return worldRenderingPipeline.getSodiumTerrainPipeline();
		} else {
			return null;
		}
	}

	private void createShaders(int maxDrawCount, SodiumTerrainPipeline pipeline, RenderDevice device, TerrainVertexType vertexType, boolean baseInstanced) {
		this.programs.clear();

		if (pipeline != null) {
			pipeline.patchShaders(maxDrawCount, vertexType.getVertexRange(), baseInstanced);
			for (IrisTerrainPass pass : IrisTerrainPass.values()) {
				if (!pipeline.hasShadowPass() && pass.isShadow()) {
					continue;
				}
				this.programs.put(pass, createShader(device, pass, vertexType, pipeline));
			}
		}

		shadersCreated = true;
	}

	@Nullable
	public Program<IrisChunkShaderInterface> getProgramOverride(boolean baseInstanced, int maxDrawCount, boolean isShadowPass, RenderDevice device, ChunkRenderPass pass, TerrainVertexType vertexType) {
		SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();

		if (!shadersCreated) {
			createShaders(maxDrawCount, pipeline, device, vertexType, baseInstanced);
		}

		if (isShadowPass) {
			if (pipeline != null && !pipeline.hasShadowPass()) {
				throw new IllegalStateException("Shadow program requested, but the pack does not have a shadow pass?");
			}

			if (pass.isCutout()) {
				return this.programs.get(IrisTerrainPass.SHADOW_CUTOUT);
			} else {
				return this.programs.get(IrisTerrainPass.SHADOW);
			}
		} else {
			if (pass.isCutout()) {
				return this.programs.get(IrisTerrainPass.GBUFFER_CUTOUT);
			} else if (pass.isTranslucent()) {
				return this.programs.get(IrisTerrainPass.GBUFFER_TRANSLUCENT);
			} else {
				return this.programs.get(IrisTerrainPass.GBUFFER_SOLID);
			}
		}
	}



	public void deleteShaders(RenderDevice device) {
		for (Program<?> program : this.programs.values()) {
			if (program != null) {
				device.deleteProgram(program);
			}
		}

		this.programs.clear();
		shadersCreated = false;
	}
}
