package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import net.caffeinemc.mods.sodium.client.gl.GlObject;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import net.caffeinemc.mods.sodium.client.gl.shader.GlShader;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderType;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.programs.ProgramFallbackResolver;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.sodium.terrain.IrisModelVertexFormats;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class SodiumPrograms {
	private final EnumMap<Pass, GlProgram<ChunkShaderInterface>> shaders = new EnumMap<>(Pass.class);

	public SodiumPrograms(IrisRenderingPipeline pipeline, ProgramSet programSet, ProgramFallbackResolver resolver,
						  RenderTargets renderTargets, Supplier<ShadowRenderTargets> shadowRenderTargets,
						  CustomUniforms customUniforms) {
		for (Pass pass : Pass.values()) {
			ProgramSource source = resolver.resolveNullable(pass.getOriginalId());

			if (source == null) {
				Iris.logger.fatal("TODO: Implement default shaders");
				continue;
			}

			AlphaTest alphaTest = getAlphaTest(pass, source);
			Map<PatchShaderType, String> transformed = transformShaders(source, alphaTest, programSet);
			GlProgram<ChunkShaderInterface> shader = createShader(pipeline, pass, source, alphaTest, renderTargets,
				shadowRenderTargets, customUniforms, createGlShaders(pass.name().toLowerCase(Locale.ROOT), transformed));
			shaders.put(pass, shader);
		}
	}

	private AlphaTest getAlphaTest(Pass pass, ProgramSource source) {
		return source.getDirectives().getAlphaTestOverride().orElse(
			pass == Pass.TERRAIN_CUTOUT || pass == Pass.SHADOW_CUTOUT ? AlphaTests.ONE_TENTH_ALPHA : AlphaTest.ALWAYS);
	}

	private Map<PatchShaderType, String> transformShaders(ProgramSource source, AlphaTest alphaTest, ProgramSet programSet) {
		Map<PatchShaderType, String> transformed = TransformPatcher.patchSodium(
			source.getName(),
			source.getVertexSource().orElse(null),
			source.getGeometrySource().orElse(null),
			source.getTessControlSource().orElse(null),
			source.getTessEvalSource().orElse(null),
			source.getFragmentSource().orElse(null),
			alphaTest, IrisModelVertexFormats.MODEL_VERTEX_XHFP,
			programSet.getPackDirectives().getTextureMap());

		ShaderPrinter.printProgram("sodium_" + source.getName()).addSources(transformed).print();

		return transformed;
	}

	private Map<PatchShaderType, GlShader> createGlShaders(String passName, Map<PatchShaderType, String> transformed) {
		Map<PatchShaderType, GlShader> newMap = new EnumMap<>(PatchShaderType.class);
		for (Map.Entry<PatchShaderType, String> entry : transformed.entrySet()) {
			if (entry.getValue() == null) continue;
			newMap.put(entry.getKey(), new GlShader(ShaderType.fromGlShaderType(entry.getKey().glShaderType.id),
				ResourceLocation.fromNamespaceAndPath("iris", "sodium-shader-" + passName), entry.getValue()));
		}
		return newMap;
	}

	private Supplier<ImmutableSet<Integer>> getFlipState(IrisRenderingPipeline pipeline, Pass pass, boolean isShadowPass) {
		if (isShadowPass) {
			return pipeline::getFlippedBeforeShadow;
		}
		return () -> pass == Pass.TRANSLUCENT ? pipeline.getFlippedAfterTranslucent() : pipeline.getFlippedAfterPrepare();
	}

	private GlProgram<ChunkShaderInterface> createShader(IrisRenderingPipeline pipeline, Pass pass, ProgramSource source,
														 AlphaTest alphaTest, RenderTargets renderTargets,
														 Supplier<ShadowRenderTargets> shadowRenderTargets,
														 CustomUniforms customUniforms,
														 Map<PatchShaderType, GlShader> transformed) {
		GlProgram.Builder builder = GlProgram.builder(ResourceLocation.fromNamespaceAndPath("sodium", "chunk_shader_for_" + pass.name().toLowerCase(Locale.ROOT)));

		for (GlShader shader : transformed.values()) {
			builder.attachShader(shader);
		}

		Supplier<ImmutableSet<Integer>> flipState = getFlipState(pipeline, pass, pass == Pass.SHADOW || pass == Pass.SHADOW_CUTOUT);
		GlFramebuffer framebuffer = createFramebuffer(pass, source, shadowRenderTargets, renderTargets, flipState);
		boolean containsTessellation = source.getTessEvalSource().isPresent();

		try {
			return buildProgram(builder, pipeline, pass, source, alphaTest, framebuffer, customUniforms, flipState, containsTessellation);
		} finally {
			transformed.values().forEach(GlShader::delete);
		}
	}

	private GlFramebuffer createFramebuffer(Pass pass, ProgramSource source,
											Supplier<ShadowRenderTargets> shadowRenderTargets,
											RenderTargets renderTargets,
											Supplier<ImmutableSet<Integer>> flipState) {
		if (pass == Pass.SHADOW || pass == Pass.SHADOW_CUTOUT) {
			return shadowRenderTargets.get().createShadowFramebuffer(ImmutableSet.of(),
				source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0, 1} : source.getDirectives().getDrawBuffers());
		} else {
			return renderTargets.createGbufferFramebuffer(flipState.get(), source.getDirectives().getDrawBuffers());
		}
	}

	private List<BufferBlendOverride> createBufferBlendOverrides(ProgramSource source) {
		List<BufferBlendOverride> overrides = new ArrayList<>();
		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
			if (index > -1) {
				overrides.add(new BufferBlendOverride(index, information.blendMode()));
			}
		});
		return overrides;
	}

	private GlProgram<ChunkShaderInterface> buildProgram(GlProgram.Builder builder, IrisRenderingPipeline pipeline,
														 Pass pass, ProgramSource source, AlphaTest alphaTest,
														 GlFramebuffer framebuffer, CustomUniforms customUniforms,
														 Supplier<ImmutableSet<Integer>> flipState,
														 boolean containsTessellation) {
		return builder
			.bindAttribute("a_PositionHi", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_HI)
			.bindAttribute("a_PositionLo", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_LO)
			.bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR)
			.bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_TEXTURE)
			.bindAttribute("a_LightAndData", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_MATERIAL_INDEX)
			.bindAttribute("mc_Entity", 11)
			.bindAttribute("mc_midTexCoord", 12)
			.bindAttribute("at_tangent", 13)
			.bindAttribute("iris_Normal", 10)
			.bindAttribute("at_midBlock", 14)
			.link((shader) -> {
				int handle = ((GlObject) shader).handle();
				GLDebug.nameObject(GL43C.GL_PROGRAM, handle, "sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT));
				return new SodiumShader(pipeline, pass, shader, handle, source.getDirectives().getBlendModeOverride(),
					createBufferBlendOverrides(source), framebuffer, customUniforms, flipState,
					alphaTest.reference(), containsTessellation);
			});
	}

	public GlProgram<ChunkShaderInterface> getProgram(TerrainRenderPass pass) {
		Pass pass2 = mapTerrainRenderPass(pass);
		return this.shaders.get(pass2);
	}

	private Pass mapTerrainRenderPass(TerrainRenderPass pass) {
		if (pass == DefaultTerrainRenderPasses.SOLID) {
			return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? Pass.SHADOW : Pass.TERRAIN;
		} else if (pass == DefaultTerrainRenderPasses.CUTOUT) {
			return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? Pass.SHADOW_CUTOUT : Pass.TERRAIN_CUTOUT;
		} else if (pass == DefaultTerrainRenderPasses.TRANSLUCENT) {
			return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? Pass.SHADOW : Pass.TRANSLUCENT;
		} else {
			throw new IllegalArgumentException("Unknown pass: " + pass);
		}
	}

	public enum Pass {
		SHADOW(ProgramId.ShadowSolid),
		SHADOW_CUTOUT(ProgramId.ShadowCutout),
		TERRAIN(ProgramId.TerrainSolid),
		TERRAIN_CUTOUT(ProgramId.TerrainCutout),
		TRANSLUCENT(ProgramId.Water);

		private final ProgramId originalId;

		Pass(ProgramId originalId) {
			this.originalId = originalId;
		}

		public ProgramId getOriginalId() {
			return originalId;
		}
	}
}
