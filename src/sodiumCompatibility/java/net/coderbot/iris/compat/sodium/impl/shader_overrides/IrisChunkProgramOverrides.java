package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.gfx.api.shader.Program;
import net.caffeinemc.gfx.api.shader.ShaderDescription;
import net.caffeinemc.gfx.api.shader.ShaderType;
import net.caffeinemc.gfx.opengl.shader.GlProgram;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.DefaultRenderPasses;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.sodium.render.shader.ShaderConstants;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Optional;

public class IrisChunkProgramOverrides {
	private boolean shadersCreated = false;
    private final EnumMap<IrisTerrainPass, Program<ChunkShaderInterface>> programs = new EnumMap<>(IrisTerrainPass.class);

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
		constants.add("VERT_POS_SCALE", String.valueOf(vertexType.getPositionScale()));
		constants.add("VERT_POS_OFFSET", String.valueOf(vertexType.getPositionOffset()));
		constants.add("VERT_TEX_SCALE", String.valueOf(vertexType.getTextureScale()));
		return constants.build();
	}

    @Nullable
    private Program<ChunkShaderInterface> createShader(RenderDevice device, IrisTerrainPass pass, TerrainVertexType type, SodiumTerrainPipeline pipeline) {
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

		ShaderDescription desc = builder.addShaderSource(ShaderType.FRAGMENT, fragShader).addAttributeBinding("a_Position", 1)
			.addAttributeBinding("a_Color", 2)
			.addAttributeBinding("a_TexCoord", 3)
			.addAttributeBinding("a_LightCoord", 4)
			.addAttributeBinding("mc_Entity", IrisChunkShaderBindingPoints.BLOCK_ID)
			.addAttributeBinding("mc_midTexCoord", IrisChunkShaderBindingPoints.MID_TEX_COORD)
			.addAttributeBinding("at_tangent", IrisChunkShaderBindingPoints.TANGENT)
			.addAttributeBinding("a_Normal", IrisChunkShaderBindingPoints.NORMAL)
			.addFragmentBinding("iris_FragData", 0)
			.build();

		Program<ChunkShaderInterface> interfaces = device.createProgram(desc, IrisChunkShaderInterface::new);

		((IrisChunkShaderInterface) interfaces.getInterface()).setInfo(pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT, pipeline, GlProgram.getHandle(interfaces), pass, getBlendOverride(pass, pipeline));

		return interfaces;
    }

    private SodiumTerrainPipeline getSodiumTerrainPipeline() {
		WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();

		if (worldRenderingPipeline != null) {
			return worldRenderingPipeline.getSodiumTerrainPipeline();
		} else {
			return null;
		}
	}

    private void createShaders(RenderDevice device, TerrainVertexType vertexType) {
    	SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();
        Iris.getPipelineManager().clearSodiumShaderReloadNeeded();

        if (pipeline != null) {
			pipeline.patchShaders(vertexType);
			for (IrisTerrainPass pass : IrisTerrainPass.values()) {
                this.programs.put(pass, createShader(device, pass, vertexType, pipeline));
            }
        } else {
            this.programs.clear();
        }

        shadersCreated = true;
    }

    @Nullable
    public Program<ChunkShaderInterface> getProgramOverride(RenderDevice device, ChunkRenderPass pass, TerrainVertexType vertexType) {
        if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
            deleteShaders(device);
        }

        if (!shadersCreated) {
			createShaders(device, vertexType);
		}

        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
        	if (pass == DefaultRenderPasses.CUTOUT || pass == DefaultRenderPasses.CUTOUT_MIPPED) {
				return this.programs.get(IrisTerrainPass.SHADOW_CUTOUT);
			} else {
				return this.programs.get(IrisTerrainPass.SHADOW);
			}
        } else {
			if (pass == DefaultRenderPasses.CUTOUT || pass == DefaultRenderPasses.CUTOUT_MIPPED) {
				return this.programs.get(IrisTerrainPass.GBUFFER_CUTOUT);
			} else if (pass == DefaultRenderPasses.TRANSLUCENT || pass == DefaultRenderPasses.TRIPWIRE) {
				return this.programs.get(IrisTerrainPass.GBUFFER_TRANSLUCENT);
			} else {
				return this.programs.get(IrisTerrainPass.GBUFFER_SOLID);
			}
        }
    }



    public void deleteShaders(RenderDevice device) {
        for (Program<?> program : this.programs.values()) {
            if (program != null && ((GlObjectExt) program).getHandle() != -2147483648) {
                device.deleteProgram(program);
            }
        }

        this.programs.clear();
        shadersCreated = false;
    }
}
