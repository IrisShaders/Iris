package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.fallback.ShaderSynthesizer;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.VanillaUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ShaderCreator {
	public static ExtendedShader create(WorldRenderingPipeline pipeline, String name, ProgramSource source, ProgramId programId, GlFramebuffer writingToBeforeTranslucent,
										GlFramebuffer writingToAfterTranslucent, AlphaTest fallbackAlpha,
										VertexFormat vertexFormat, ShaderAttributeInputs inputs, FrameUpdateNotifier updateNotifier,
										IrisRenderingPipeline parent, Supplier<ImmutableSet<Integer>> flipped, FogMode fogMode, boolean isIntensity,
										boolean isFullbright, boolean isShadowPass, boolean isLines, CustomUniforms customUniforms) throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
		BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());

		Map<PatchShaderType, String> transformed = TransformPatcher.patchVanilla(
			name,
			source.getVertexSource().orElseThrow(RuntimeException::new),
			source.getGeometrySource().orElse(null),
			source.getTessControlSource().orElse(null),
			source.getTessEvalSource().orElse(null),
			source.getFragmentSource().orElseThrow(RuntimeException::new),
			alpha, isLines, true, inputs, pipeline.getTextureMap());
		String vertex = transformed.get(PatchShaderType.VERTEX);
		String geometry = transformed.get(PatchShaderType.GEOMETRY);
		String tessControl = transformed.get(PatchShaderType.TESS_CONTROL);
		String tessEval = transformed.get(PatchShaderType.TESS_EVAL);
		String fragment = transformed.get(PatchShaderType.FRAGMENT);

		String shaderJsonString = "{\n" +
			"    \"blend\": {\n" +
			"        \"func\": \"add\",\n" +
			"        \"srcrgb\": \"srcalpha\",\n" +
			"        \"dstrgb\": \"1-srcalpha\"\n" +
			"    },\n" +
			"    \"vertex\": \"" + name + "\",\n" +
			"    \"fragment\": \"" + name + "\",\n" +
			"    \"attributes\": [\n" +
			"        \"Position\",\n" +
			"        \"Color\",\n" +
			"        \"UV0\",\n" +
			"        \"UV1\",\n" +
			"        \"UV2\",\n" +
			"        \"Normal\"\n" +
			"    ],\n" +
			"    \"uniforms\": [\n" +
			"        { \"name\": \"iris_TextureMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"iris_ModelViewMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"iris_ModelViewMatInverse\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"iris_ProjMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"iris_ProjMatInverse\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"iris_NormalMat\", \"type\": \"matrix3x3\", \"count\": 9, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 ] },\n" +
			"        { \"name\": \"iris_ChunkOffset\", \"type\": \"float\", \"count\": 3, \"values\": [ 0.0, 0.0, 0.0 ] },\n" +
			"        { \"name\": \"iris_ColorModulator\", \"type\": \"float\", \"count\": 4, \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n" +
			"        { \"name\": \"iris_GlintAlpha\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
			"        { \"name\": \"iris_FogStart\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.0 ] },\n" +
			"        { \"name\": \"iris_FogEnd\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
			"        { \"name\": \"iris_FogColor\", \"type\": \"float\", \"count\": 4, \"values\": [ 0.0, 0.0, 0.0, 0.0 ] }\n" +
			"    ]\n" +
			"}";

		ShaderPrinter.printProgram(name).addSources(transformed).addJson(shaderJsonString).print();

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, geometry, tessControl, tessEval, fragment);

		List<BufferBlendOverride> overrides = new ArrayList<>();
		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
			if (index > -1) {
				overrides.add(new BufferBlendOverride(index, information.blendMode()));
			}
		});

		return new ExtendedShader(shaderResourceFactory, name, vertexFormat, tessControl != null || tessEval != null, writingToBeforeTranslucent, writingToAfterTranslucent, blendModeOverride, alpha, uniforms -> {
			CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
			customUniforms.assignTo(uniforms);
			//SamplerUniforms.addWorldSamplerUniforms(uniforms);
			//SamplerUniforms.addDepthSamplerUniforms(uniforms);
			BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
			VanillaUniforms.addVanillaUniforms(uniforms);
		}, (samplerHolder, imageHolder) -> {
			parent.addGbufferOrShadowSamplers(samplerHolder, imageHolder, flipped, isShadowPass, inputs.hasTex(), inputs.hasLight(), inputs.hasOverlay());
		}, isIntensity, parent, overrides, customUniforms);
	}

	public static FallbackShader createFallback(String name, GlFramebuffer writingToBeforeTranslucent,
												GlFramebuffer writingToAfterTranslucent, AlphaTest alpha,
												VertexFormat vertexFormat, BlendModeOverride blendModeOverride,
												IrisRenderingPipeline parent, FogMode fogMode, boolean entityLighting,
												boolean isGlint, boolean isText, boolean intensityTex, boolean isFullbright) throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText);

		// TODO: Is this check sound in newer versions?
		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);


		String shaderJsonString = "{\n" +
			"    \"blend\": {\n" +
			"        \"func\": \"add\",\n" +
			"        \"srcrgb\": \"srcalpha\",\n" +
			"        \"dstrgb\": \"1-srcalpha\"\n" +
			"    },\n" +
			"    \"vertex\": \"" + name + "\",\n" +
			"    \"fragment\": \"" + name + "\",\n" +
			"    \"attributes\": [\n" +
			"        \"Position\",\n" +
			"        \"Color\",\n" +
			(inputs.hasTex() ? "        \"UV0\",\n" : "") +
			(inputs.hasOverlay() ? "        \"UV1\",\n" : "") +
			(inputs.hasLight() ? "        \"UV2\",\n" : "") +
			"        \"Normal\"\n" +
			"    ],\n" +
			"    \"uniforms\": [\n" +
			"        { \"name\": \"TextureMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"ModelViewMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"ProjMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
			"        { \"name\": \"ChunkOffset\", \"type\": \"float\", \"count\": 3, \"values\": [ 0.0, 0.0, 0.0 ] },\n" +
			"        { \"name\": \"ColorModulator\", \"type\": \"float\", \"count\": 4, \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n" +
			"        { \"name\": \"GlintAlpha\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
			"        { \"name\": \"Light0_Direction\", \"type\": \"float\", \"count\": 3, \"values\": [0.0, 0.0, 0.0] },\n" +
			"        { \"name\": \"Light1_Direction\", \"type\": \"float\", \"count\": 3, \"values\": [0.0, 0.0, 0.0] },\n" +
			"        { \"name\": \"FogStart\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.0 ] },\n" +
			"        { \"name\": \"FogEnd\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
			"        { \"name\": \"FogDensity\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
			"        { \"name\": \"FogIsExp2\", \"type\": \"int\", \"count\": 1, \"values\": [ 0 ] },\n" +
			"        { \"name\": \"AlphaTestValue\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.0 ] },\n" +
			"        { \"name\": \"LineWidth\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
			"        { \"name\": \"ScreenSize\", \"type\": \"float\", \"count\": 2, \"values\": [ 1.0, 1.0 ] },\n" +
			"        { \"name\": \"FogColor\", \"type\": \"float\", \"count\": 4, \"values\": [ 0.0, 0.0, 0.0, 0.0 ] }\n" +
			"    ]\n" +
			"}";

		ShaderPrinter.printProgram(name)
			.addSource(PatchShaderType.VERTEX, vertex)
			.addSource(PatchShaderType.FRAGMENT, fragment)
			.addJson(shaderJsonString)
			.print();

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, null, null, null, fragment);

		return new FallbackShader(shaderResourceFactory, name, vertexFormat, writingToBeforeTranslucent,
			writingToAfterTranslucent, blendModeOverride, alpha.reference(), parent);
	}

	private record IrisProgramResourceFactory(String json, String vertex, String geometry, String tessControl,
											  String tessEval, String fragment) implements ResourceProvider {

		@Override
		public Optional<Resource> getResource(ResourceLocation id) {
			final String path = id.getPath();

			if (path.endsWith("json")) {
				return Optional.of(new StringResource(id, json));
			} else if (path.endsWith("vsh")) {
				return Optional.of(new StringResource(id, vertex));
			} else if (path.endsWith("gsh")) {
				if (geometry == null) {
					return Optional.empty();
				}
				return Optional.of(new StringResource(id, geometry));
			} else if (path.endsWith("tcs")) {
				if (tessControl == null) {
					return Optional.empty();
				}
				return Optional.of(new StringResource(id, tessControl));
			} else if (path.endsWith("tes")) {
				if (tessEval == null) {
					return Optional.empty();
				}
				return Optional.of(new StringResource(id, tessEval));
			} else if (path.endsWith("fsh")) {
				return Optional.of(new StringResource(id, fragment));
			}

			return Optional.empty();
		}
	}

	private static class StringResource extends Resource {
		private final String content;

		private StringResource(ResourceLocation id, String content) {
			super(new PathPackResources("<iris shaderpack shaders>", FabricLoader.getInstance().getConfigDir(), true), () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
			this.content = content;
		}

		@Override
		public InputStream open() {
			return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
		}
	}
}
