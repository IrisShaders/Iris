package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.fallback.ShaderSynthesizer;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class ShaderCreator {
	public static ShaderSupplier create(WorldRenderingPipeline pipeline, String name, ShaderKey shaderKey, ProgramSource source,
		ProgramId programId, AlphaTest fallbackAlpha, VertexFormat vertexFormat, ShaderAttributeInputs inputs,
		FrameUpdateNotifier updateNotifier, IrisRenderingPipeline parent, Supplier<ImmutableSet<Integer>> flipped, FogMode fogMode,
		boolean isIntensity, boolean isFullbright, boolean isShadowPass, boolean isLines, CustomUniforms customUniforms,
		Patch patch) throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
		BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());


		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}
		Map<PatchShaderType, String> transformed;

		if (patch == Patch.SODIUM) {
			transformed = TransformPatcher.patchSodium(name, source.getVertexSource().orElseThrow(RuntimeException::new),
				source.getGeometrySource().orElse(null), source.getTessControlSource().orElse(null),
				source.getTessEvalSource().orElse(null), source.getFragmentSource().orElseThrow(RuntimeException::new), alpha,
				pipeline.getTextureMap(), pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW), false);
		} else {
			transformed = TransformPatcher.patchVanilla(name, source.getVertexSource().orElseThrow(RuntimeException::new),
				source.getGeometrySource().orElse(null), source.getTessControlSource().orElse(null),
				source.getTessEvalSource().orElse(null), source.getFragmentSource().orElseThrow(RuntimeException::new), alpha, isLines,
				shaderKey == ShaderKey.CLOUDS, true, inputs, pipeline.getTextureMap(),
				pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW));
		}


		var shaderVertexFormat = vertexFormat;
		return new ShaderSupplier(shaderKey,
			() -> new ExtendedShader(parent, name, transformed, source.getDirectives(), source.getDirectives().getDrawBuffers(),
				blendModeOverride, alpha, inputs, shaderVertexFormat, shaderKey));
	}

	public static ShaderSupplier createFallback(String name, ShaderKey shaderKey, AlphaTest alpha, VertexFormat vertexFormat,
		BlendModeOverride blendModeOverride, IrisRenderingPipeline parent, FogMode fogMode, boolean entityLighting, boolean isGlint,
		boolean isText, boolean intensityTex, boolean isFullbright) throws IOException {
		if (shaderKey.patch == Patch.SODIUM) {
			return createSodiumFallback(name, shaderKey, alpha, blendModeOverride, parent, false);
		}

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}

		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);


		var shaderVertexFormat = vertexFormat;
		return new ShaderSupplier(shaderKey,
			() -> new ExtendedShader(parent, name, Map.of(PatchShaderType.VERTEX, vertex, PatchShaderType.FRAGMENT, fragment), null,
				new int[]{0}, blendModeOverride, alpha, inputs, shaderVertexFormat, shaderKey));
	}

	public static ShaderSupplier createFallbackShadow(String name, ShaderKey shaderKey, Supplier<ShadowRenderTargets> shadowSupplier,
		AlphaTest alpha, VertexFormat vertexFormat, BlendModeOverride blendModeOverride, IrisRenderingPipeline parent, FogMode fogMode,
		boolean entityLighting, boolean isGlint, boolean isText, boolean intensityTex, boolean isFullbright) throws IOException {
		if (shaderKey.patch == Patch.SODIUM) {
			return createSodiumFallback(name, shaderKey, alpha, blendModeOverride, parent, true);
		}

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}

		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);


		var shaderVertexFormat = vertexFormat;
		return new ShaderSupplier(shaderKey,
			() -> new ExtendedShader(parent, name, Map.of(PatchShaderType.VERTEX, vertex, PatchShaderType.FRAGMENT, fragment), null,
				new int[]{0}, blendModeOverride, alpha, inputs, shaderVertexFormat, shaderKey));
	}

	private static ShaderSupplier createSodiumFallback(String name, ShaderKey shaderKey, AlphaTest alpha,
		BlendModeOverride blendModeOverride, IrisRenderingPipeline parent, boolean shadow) {
		VertexFormat vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, false, false, false, false, false);
		Map<PatchShaderType, String> transformed = TransformPatcher.patchSodium(name, ShaderSynthesizer.sodiumVsh(), null, null, null,
			ShaderSynthesizer.sodiumFsh(shadow), alpha, parent.getTextureMap(),
			parent.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW), shadow);

		return new ShaderSupplier(shaderKey,
			() -> new ExtendedShader(parent, name, transformed, null, new int[]{0}, blendModeOverride, alpha, inputs, vertexFormat,
				shaderKey));
	}

	public static ShaderSupplier createShadow(WorldRenderingPipeline pipeline, String name, ShaderKey shaderKey, ProgramSource source,
		ProgramId programId, Supplier<ShadowRenderTargets> shadowSupplier, AlphaTest fallbackAlpha, VertexFormat vertexFormat,
		ShaderAttributeInputs inputs, FrameUpdateNotifier updateNotifier, IrisRenderingPipeline parent,
		Supplier<ImmutableSet<Integer>> flipped, FogMode fogMode, boolean isIntensity, boolean isFullbright, boolean isShadowPass,
		boolean isLines, CustomUniforms customUniforms, Patch patchType) throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
		BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());
		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}
		Map<PatchShaderType, String> transformed;
		if (patchType == Patch.SODIUM) {
			transformed = TransformPatcher.patchSodium(name, source.getVertexSource().orElseThrow(RuntimeException::new),
				source.getGeometrySource().orElse(null), source.getTessControlSource().orElse(null),
				source.getTessEvalSource().orElse(null), source.getFragmentSource().orElseThrow(RuntimeException::new), alpha,
				pipeline.getTextureMap(), pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW), true);
		} else {
			transformed = TransformPatcher.patchVanilla(name, source.getVertexSource().orElseThrow(RuntimeException::new),
				source.getGeometrySource().orElse(null), source.getTessControlSource().orElse(null),
				source.getTessEvalSource().orElse(null), source.getFragmentSource().orElseThrow(RuntimeException::new), alpha, isLines,
				shaderKey == ShaderKey.CLOUDS, true, inputs, pipeline.getTextureMap(),
				pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW));
		}


		var shaderVertexFormat = vertexFormat;
		return new ShaderSupplier(shaderKey, () -> {
			int[] drawBuffers = shadowSupplier.get()
				.getDrawBuffers(source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0, 1} : source.getDirectives().getDrawBuffers());
			return new ExtendedShader(parent, name, transformed, source.getDirectives().withOverriddenDrawBuffers(drawBuffers), drawBuffers,
				blendModeOverride, alpha, inputs, shaderVertexFormat, shaderKey);
		});
	}
}
