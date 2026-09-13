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
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
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
		ProgramId programId, AlphaTest fallbackAlpha, VertexFormat vertexFormat,
		ShaderAttributeInputs inputs, FrameUpdateNotifier updateNotifier, IrisRenderingPipeline parent,
		Supplier<ImmutableSet<Integer>> flipped, FogMode fogMode, boolean isIntensity, boolean isFullbright,
		boolean isShadowPass, boolean isLines, CustomUniforms customUniforms, Patch patch)
		throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
		BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());

		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}

		Map<PatchShaderType, String> transformed;

		if (patch == Patch.SODIUM) {
			transformed = TransformPatcher.patchSodium(
				name, source.getVertexSource().orElseThrow(RuntimeException::new), source.getGeometrySource().orElse(null),
				source.getTessControlSource().orElse(null), source.getTessEvalSource().orElse(null),
				source.getFragmentSource().orElseThrow(RuntimeException::new), alpha, pipeline.getTextureMap(), pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW), false);
		} else {
			transformed = TransformPatcher.patchVanilla(name, source.getVertexSource().orElseThrow(RuntimeException::new),
				source.getGeometrySource().orElse(null), source.getTessControlSource().orElse(null),
				source.getTessEvalSource().orElse(null),
				source.getFragmentSource().orElseThrow(RuntimeException::new), alpha, isLines,
				shaderKey == ShaderKey.CLOUDS, true, inputs, pipeline.getTextureMap(), pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW));
		}

		ShaderPrinter.printProgram(name).addSources(transformed).print();

		return new ShaderSupplier(shaderKey,
			()
				-> new WorldProgram(parent, name, transformed, source.getDirectives(),
				source.getDirectives().getDrawBuffers(), blendModeOverride, alpha, inputs,
				isShadowPass));
	}

	public static ShaderSupplier createFallback(String name, ShaderKey shaderKey, AlphaTest alpha, VertexFormat vertexFormat,
		BlendModeOverride blendModeOverride, IrisRenderingPipeline parent, FogMode fogMode,
		boolean entityLighting, boolean isGlint, boolean isText, boolean intensityTex,
		boolean isFullbright) throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}

		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);
		ShaderPrinter.printProgram(name).addSource(PatchShaderType.VERTEX, vertex).addSource(PatchShaderType.FRAGMENT, fragment).print();

		return new ShaderSupplier(shaderKey,
			()
				-> new WorldProgram(parent, name,
				Map.of(PatchShaderType.VERTEX, vertex, PatchShaderType.FRAGMENT, fragment), null,
				new int[]{0}, blendModeOverride, alpha, inputs, false));
	}

	public static ShaderSupplier createFallbackShadow(String name, ShaderKey shaderKey, Supplier<ShadowRenderTargets> shadowSupplier,
		AlphaTest alpha, VertexFormat vertexFormat, BlendModeOverride blendModeOverride,
		IrisRenderingPipeline parent, FogMode fogMode, boolean entityLighting,
		boolean isGlint, boolean isText, boolean intensityTex, boolean isFullbright)
		throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}

		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);
		ShaderPrinter.printProgram(name).addSource(PatchShaderType.VERTEX, vertex).addSource(PatchShaderType.FRAGMENT, fragment).print();

		return new ShaderSupplier(shaderKey,
			()
				-> new WorldProgram(parent, name,
				Map.of(PatchShaderType.VERTEX, vertex, PatchShaderType.FRAGMENT, fragment), null,
				new int[]{0}, blendModeOverride, alpha, inputs, true));
	}

	public static ShaderSupplier createShadow(WorldRenderingPipeline pipeline, String name, ShaderKey shaderKey, ProgramSource source,
		ProgramId programId, Supplier<ShadowRenderTargets> shadowSupplier, AlphaTest fallbackAlpha,
		VertexFormat vertexFormat, ShaderAttributeInputs inputs, FrameUpdateNotifier updateNotifier,
		IrisRenderingPipeline parent, Supplier<ImmutableSet<Integer>> flipped, FogMode fogMode,
		boolean isIntensity, boolean isFullbright, boolean isShadowPass, boolean isLines,
		CustomUniforms customUniforms, Patch patchType) throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
		BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());

		if (vertexFormat == null) {

			vertexFormat = WorldRenderingSettings.INSTANCE.getVertexFormat().getVertexFormat();
		}

		Map<PatchShaderType, String> transformed;

		if (patchType == Patch.SODIUM) {
			transformed = TransformPatcher.patchSodium(
				name, source.getVertexSource().orElseThrow(RuntimeException::new), source.getGeometrySource().orElse(null),
				source.getTessControlSource().orElse(null), source.getTessEvalSource().orElse(null),
				source.getFragmentSource().orElseThrow(RuntimeException::new), alpha, pipeline.getTextureMap(), pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW), true);
		} else {
			transformed = TransformPatcher.patchVanilla(name, source.getVertexSource().orElseThrow(RuntimeException::new),
				source.getGeometrySource().orElse(null), source.getTessControlSource().orElse(null),
				source.getTessEvalSource().orElse(null),
				source.getFragmentSource().orElseThrow(RuntimeException::new), alpha, isLines,
				shaderKey == ShaderKey.CLOUDS, true, inputs, pipeline.getTextureMap(), pipeline.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW));
		}

		ShaderPrinter.printProgram(name).addSources(transformed).print();

		return new ShaderSupplier(shaderKey,
			()
				-> new WorldProgram(parent, name, transformed, source.getDirectives(),
				source.getDirectives().getDrawBuffers(), blendModeOverride, alpha, inputs, true));
	}
}
