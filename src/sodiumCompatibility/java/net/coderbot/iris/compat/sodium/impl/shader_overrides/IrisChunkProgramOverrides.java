package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Optional;

public class IrisChunkProgramOverrides {
    private static final ShaderConstants EMPTY_CONSTANTS = ShaderConstants.builder().build();

    private final EnumMap<IrisTerrainPass, ChunkProgram> programs = new EnumMap<>(IrisTerrainPass.class);

    private GlShader createVertexShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisVertexShader;

        if (pass == IrisTerrainPass.SHADOW) {
            irisVertexShader = pipeline.getShadowVertexShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
            irisVertexShader = pipeline.getTerrainVertexShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
            irisVertexShader = pipeline.getTranslucentVertexShaderSource();
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        String source = irisVertexShader.orElse(null);

        if (source == null) {
            return null;
        }

        return new GlShader(device, ShaderType.VERTEX, new ResourceLocation("iris",
			"sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT) + ".vsh"), source, EMPTY_CONSTANTS);
    }

    private GlShader createGeometryShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisGeometryShader;

        if (pass == IrisTerrainPass.SHADOW) {
            irisGeometryShader = pipeline.getShadowGeometryShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
            irisGeometryShader = pipeline.getTerrainGeometryShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
            irisGeometryShader = pipeline.getTranslucentGeometryShaderSource();
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        String source = irisGeometryShader.orElse(null);

        if (source == null) {
            return null;
        }

        return new GlShader(device, IrisShaderTypes.GEOMETRY, new ResourceLocation("iris",
			"sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT) + ".gsh"), source, EMPTY_CONSTANTS);
    }

    private GlShader createFragmentShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisFragmentShader;

        if (pass == IrisTerrainPass.SHADOW) {
            irisFragmentShader = pipeline.getShadowFragmentShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
            irisFragmentShader = pipeline.getTerrainFragmentShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
            irisFragmentShader = pipeline.getTranslucentFragmentShaderSource();
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        String source = irisFragmentShader.orElse(null);

        if (source == null) {
            return null;
        }

        return new GlShader(device, ShaderType.FRAGMENT, new ResourceLocation("iris",
			"sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT) + ".fsh"), source, EMPTY_CONSTANTS);
    }

    @Nullable
    private ChunkProgram createShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        GlShader vertShader = createVertexShader(device, pass, pipeline);
        GlShader geomShader = createGeometryShader(device, pass, pipeline);
        GlShader fragShader = createFragmentShader(device, pass, pipeline);

        if (vertShader == null || fragShader == null) {
            if (vertShader != null) {
                vertShader.delete();
            }

            if (geomShader != null) {
                geomShader.delete();
            }

            if (fragShader != null) {
                fragShader.delete();
            }

            // TODO: Partial shader programs?
            return null;
        }

        try {
            GlProgram.Builder builder = GlProgram.builder(new ResourceLocation("sodium", "chunk_shader_for_"
                    + pass.getName()));

            if (geomShader != null) {
                builder.attachShader(geomShader);
            }

            return builder.attachShader(vertShader)
                    .attachShader(fragShader)
                    .bindAttribute("a_Pos", ChunkShaderBindingPoints.POSITION)
                    .bindAttribute("a_Color", ChunkShaderBindingPoints.COLOR)
                    .bindAttribute("a_TexCoord", ChunkShaderBindingPoints.TEX_COORD)
                    .bindAttribute("a_LightCoord", ChunkShaderBindingPoints.LIGHT_COORD)
                    .bindAttribute("mc_Entity", IrisChunkShaderBindingPoints.BLOCK_ID)
                    .bindAttribute("mc_midTexCoord", IrisChunkShaderBindingPoints.MID_TEX_COORD)
                    .bindAttribute("at_tangent", IrisChunkShaderBindingPoints.TANGENT)
                    .bindAttribute("a_Normal", IrisChunkShaderBindingPoints.NORMAL)
                    .bindAttribute("d_ModelOffset", ChunkShaderBindingPoints.MODEL_OFFSET)
                    .build((program, name) -> {
                        ProgramUniforms uniforms = pipeline.initUniforms(name);
                        ProgramSamplers samplers;
						ProgramImages images;

                        if (pass == IrisTerrainPass.SHADOW) {
                            samplers = pipeline.initShadowSamplers(name);
							images = pipeline.initShadowImages(name);
                        } else {
                            samplers = pipeline.initTerrainSamplers(name);
							images = pipeline.initTerrainImages(name);
                        }

                        return new IrisChunkProgram(device, program, name, uniforms, samplers, images);
                    });
        } finally {
            vertShader.delete();
            if (geomShader != null) {
                geomShader.delete();
            }
            fragShader.delete();
        }
    }

    public void createShaders(RenderDevice device) {
        WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();
        SodiumTerrainPipeline sodiumTerrainPipeline = null;

        if (worldRenderingPipeline != null) {
            sodiumTerrainPipeline = worldRenderingPipeline.getSodiumTerrainPipeline();
        }

        Iris.getPipelineManager().clearSodiumShaderReloadNeeded();

        if (sodiumTerrainPipeline != null) {
            for (IrisTerrainPass pass : IrisTerrainPass.values()) {
				if (pass == IrisTerrainPass.SHADOW && !sodiumTerrainPipeline.hasShadowPass()) {
					this.programs.put(pass, null);
					continue;
				}

                this.programs.put(pass, createShader(device, pass, sodiumTerrainPipeline));
            }
        } else {
            this.programs.clear();
        }
    }

    @Nullable
    public ChunkProgram getProgramOverride(RenderDevice device, BlockRenderPass pass) {
        if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
            deleteShaders();
            createShaders(device);
        }

        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ChunkProgram shadowProgram = this.programs.get(IrisTerrainPass.SHADOW);

			if (shadowProgram == null) {
				throw new IllegalStateException("Shadow program requested, but the pack does not have a shadow pass?");
			}

			return shadowProgram;
        } else {
            return this.programs.get(pass.isTranslucent() ? IrisTerrainPass.GBUFFER_TRANSLUCENT : IrisTerrainPass.GBUFFER_SOLID);
        }
    }

    public void deleteShaders() {
        for (ChunkProgram program : this.programs.values()) {
            if (program != null) {
                program.delete();
            }
        }

        this.programs.clear();
    }
}
