package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.PatchedShaderPrinter;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.coderbot.iris.pipeline.newshader.fallback.ShaderSynthesizer;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.TransformPatcher;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.VanillaUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.fabricmc.loader.api.FabricLoader;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import com.mojang.blaze3d.vertex.VertexFormat;
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

public class NewShaderTests {
	public static ExtendedShader create(WorldRenderingPipeline pipeline, String name, ProgramSource source, ProgramId programId, GlFramebuffer writingToBeforeTranslucent,
										GlFramebuffer writingToAfterTranslucent, GlFramebuffer baseline, AlphaTest fallbackAlpha,
										VertexFormat vertexFormat, ShaderAttributeInputs inputs, FrameUpdateNotifier updateNotifier,
										NewWorldRenderingPipeline parent, Supplier<ImmutableSet<Integer>> flipped, FogMode fogMode, boolean isIntensity,
										boolean isFullbright, boolean isShadowPass, CustomUniforms customUniforms) throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
		BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());

		Map<PatchShaderType, String> transformed = TransformPatcher.patchVanilla(
			source.getVertexSource().orElseThrow(RuntimeException::new),
			source.getGeometrySource().orElse(null),
			source.getFragmentSource().orElseThrow(RuntimeException::new),
			alpha, true, inputs, pipeline.getTextureMap());
		String vertex = transformed.get(PatchShaderType.VERTEX);
		String geometry = transformed.get(PatchShaderType.GEOMETRY);
		String fragment = transformed.get(PatchShaderType.FRAGMENT);

		StringBuilder shaderJson = new StringBuilder("{\n" +
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
			"}");

		String shaderJsonString = shaderJson.toString();

		PatchedShaderPrinter.debugPatchedShaders(source.getName(), vertex, geometry, fragment, shaderJsonString);

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, geometry, fragment);

		List<BufferBlendOverride> overrides = new ArrayList<>();
		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.getIndex());
			if (index > -1) {
				overrides.add(new BufferBlendOverride(index, information.getBlendMode()));
			}
		});

		return new ExtendedShader(shaderResourceFactory, name, vertexFormat, writingToBeforeTranslucent, writingToAfterTranslucent, baseline, blendModeOverride, alpha, uniforms -> {
			CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
			customUniforms.assignTo(uniforms);
			//SamplerUniforms.addWorldSamplerUniforms(uniforms);
			//SamplerUniforms.addDepthSamplerUniforms(uniforms);
			BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
			VanillaUniforms.addVanillaUniforms(uniforms);
		}, (samplerHolder, imageHolder) -> {
			parent.addGbufferOrShadowSamplers(samplerHolder, imageHolder, flipped, isShadowPass, inputs.toAvailability());
		}, isIntensity, parent, inputs, overrides, customUniforms);
	}

	public static FallbackShader createFallback(String name, GlFramebuffer writingToBeforeTranslucent,
												GlFramebuffer writingToAfterTranslucent, AlphaTest alpha,
												VertexFormat vertexFormat, BlendModeOverride blendModeOverride,
												NewWorldRenderingPipeline parent, FogMode fogMode, boolean entityLighting,
												boolean intensityTex, boolean isFullbright) throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false);

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

		PatchedShaderPrinter.debugPatchedShaders(name, vertex, null, fragment, shaderJsonString);

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, null, fragment);

		return new FallbackShader(shaderResourceFactory, name, vertexFormat, writingToBeforeTranslucent,
			writingToAfterTranslucent, blendModeOverride, alpha.getReference(), parent);
	}

	private static class IrisProgramResourceFactory implements ResourceProvider {
		private final String json;
		private final String vertex;
		private final String geometry;
		private final String fragment;

		public IrisProgramResourceFactory(String json, String vertex, String geometry, String fragment) {
			this.json = json;
			this.vertex = vertex;
			this.geometry = geometry;
			this.fragment = fragment;
		}

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
			} else if (path.endsWith("fsh")) {
				return Optional.of(new StringResource(id, fragment));
			}

			return Optional.empty();
		}
	}

	private static class StringResource extends Resource {
		private final ResourceLocation id;
		private final String content;

		private StringResource(ResourceLocation id, String content) {
			super(new PathPackResources("<iris shaderpack shaders>", FabricLoader.getInstance().getConfigDir(), true), (IoSupplier<InputStream>) () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
			this.id = id;
			this.content = content;
		}

		@Override
		public InputStream open() throws IOException {
			return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
		}
	}
}
