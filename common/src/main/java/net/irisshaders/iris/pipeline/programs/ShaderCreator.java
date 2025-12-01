package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.JsonOps;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
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
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.VanillaUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.KHRDebug;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ShaderCreator {
	public static ShaderSupplier create(WorldRenderingPipeline pipeline, String name, ShaderKey shaderKey, ProgramSource source, ProgramId programId, GlFramebuffer writingToBeforeTranslucent,
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
			alpha, isLines, shaderKey == ShaderKey.CLOUDS, true, inputs, pipeline.getTextureMap());
		String vertex = transformed.get(PatchShaderType.VERTEX);
		String geometry = transformed.get(PatchShaderType.GEOMETRY);
		String tessControl = transformed.get(PatchShaderType.TESS_CONTROL);
		String tessEval = transformed.get(PatchShaderType.TESS_EVAL);
		String fragment = transformed.get(PatchShaderType.FRAGMENT);

		String shaderJsonString = String.format("""
			    {
			    "blend": {
			        "func": "add",
			        "srcrgb": "srcalpha",
			        "dstrgb": "1-srcalpha"
			    },
			    "vertex": "%s",
			    "fragment": "%s",
			    "attributes": [
			        "Position",
			        "Color",
			        "UV0",
			        "UV1",
			        "UV2",
			        "Normal"
			    ],
			    "uniforms": [
			        { "name": "iris_TextureMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ModelViewMatInverse", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ProjMatInverse", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_NormalMat", "type": "matrix3x3", "count": 9, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 ] },
			        { "name": "iris_ModelOffset", "type": "float", "count": 3, "values": [ 0.0, 0.0, 0.0 ] },
			        { "name": "iris_ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] },
			        { "name": "iris_GlintAlpha", "type": "float", "count": 1, "values": [ 1.0 ] },
			        { "name": "iris_FogStart", "type": "float", "count": 1, "values": [ 0.0 ] },
			        { "name": "iris_FogEnd", "type": "float", "count": 1, "values": [ 1.0 ] },
			        { "name": "iris_FogColor", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] },
			        {
			                    "name": "iris_OverlayUV",
			                    "type": "int",
			                    "count": 2,
			                    "values": [
			                        0,
			                        0
			                    ]
			                },
			                {
			                    "name": "iris_LightUV",
			                    "type": "int",
			                    "count": 2,
			                    "values": [
			                        0,
			                        0
			                    ]
			                }
			    ]
			}""", name, name);

		ShaderPrinter.printProgram(name).addSources(transformed).addJson(shaderJsonString).print();

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, geometry, tessControl, tessEval, fragment);

		List<BufferBlendOverride> overrides = new ArrayList<>();
		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
			if (index > -1) {
				overrides.add(new BufferBlendOverride(index, information.blendMode()));
			}
		});

		PartialShader id = link(name, vertex, geometry, tessControl, tessEval, fragment, vertexFormat, false);


		return new ShaderSupplier(shaderKey, id, () -> {
			try {
				return new ExtendedShader(id.getFinally(), name, vertexFormat, tessControl != null || tessEval != null, writingToBeforeTranslucent, writingToAfterTranslucent, blendModeOverride, alpha, uniforms -> {
					CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
					customUniforms.assignTo(uniforms);
					BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
					VanillaUniforms.addVanillaUniforms(uniforms);
				}, (samplerHolder, imageHolder) -> {
					parent.addGbufferOrShadowSamplers(samplerHolder, imageHolder, flipped, isShadowPass, inputs.hasTex(), inputs.hasLight(), inputs.hasOverlay());
				}, isIntensity, parent, overrides, customUniforms);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}



	public static PartialShader link(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment, VertexFormat vertexFormat, boolean isFallback) throws ShaderCompileException {
		int i = GlStateManager.glCreateProgram();
		if (i <= 0) {
			throw new RuntimeException("Could not create shader program (returned program ID " + i + ")");
		} else {
			int vertexS = createShader(name, ShaderType.VERTEX, vertex);
			int geometryS = createShader(name, ShaderType.GEOMETRY, geometry);
			int tessContS = createShader(name, ShaderType.TESSELATION_CONTROL, tessControl);
			int tessEvalS = createShader(name, ShaderType.TESSELATION_EVAL, tessEval);
			int fragS = createShader(name, ShaderType.FRAGMENT, fragment);

			attachIfValid(i, vertexS);
			attachIfValid(i, geometryS);
			attachIfValid(i, tessContS);
			attachIfValid(i, tessEvalS);
			attachIfValid(i, fragS);

			((VertexFormatExtension) vertexFormat).bindAttributesIris(isFallback, i);

			GlStateManager.glLinkProgram(i);

			return new PartialShader(i, vertexS, fragS, geometryS, tessContS, tessEvalS);
		}
	}

	private static void attachIfValid(int i, int s) {
		if (s >= 0) {
			GlStateManager.glAttachShader(i, s);
		}
	}

	private static void detachIfValid(int i, int s) {
		if (s >= 0) {
			IrisRenderSystem.detachShader(i, s);
			GlStateManager.glDeleteShader(s);
		}
	}

	private static int createShader(String name, ShaderType shaderType, String source) {
		if (source == null) return -1;

		int shader = GlStateManager.glCreateShader(shaderType.id);
		GlStateManager.glShaderSource(shader, source);
		GlStateManager.glCompileShader(shader);
		String log = IrisRenderSystem.getShaderInfoLog(shader);

		if (!log.isEmpty()) {
			Iris.logger.warn("Shader compilation log for " + name + ": " + log);
		}

		int result = GlStateManager.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new ShaderCompileException(name, log);
		}

		return shader;
	}

	public static ShaderSupplier createFallback(String name, ShaderKey shaderKey, GlFramebuffer writingToBeforeTranslucent,
												GlFramebuffer writingToAfterTranslucent, AlphaTest alpha,
												VertexFormat vertexFormat, BlendModeOverride blendModeOverride,
												IrisRenderingPipeline parent, FogMode fogMode, boolean entityLighting,
												boolean isGlint, boolean isText, boolean intensityTex, boolean isFullbright) throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

		// TODO: Is this check sound in newer versions?
		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);

		ShaderPrinter.printProgram(name)
			.addSource(PatchShaderType.VERTEX, vertex)
			.addSource(PatchShaderType.FRAGMENT, fragment)
			.print();


		PartialShader id = link(name, vertex, null, null, null, fragment, vertexFormat, true);

		// TODO 24w34a FALLBACK
		return new ShaderSupplier(shaderKey, id, () -> {
			try {
				GLDebug.nameObject(KHRDebug.GL_PROGRAM, id.program(), name + "_fallback");

				// TODO 1.21.5 (oh no)
				return new FallbackShader(id.getFinally(), RenderPipelines.ENTITY_CUTOUT, name, vertexFormat, writingToBeforeTranslucent,
					writingToAfterTranslucent, blendModeOverride, alpha.reference(), parent);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static ShaderSupplier createFallbackShadow(String name, ShaderKey shaderKey, Supplier<ShadowRenderTargets> shadowSupplier, AlphaTest alpha,
												VertexFormat vertexFormat, BlendModeOverride blendModeOverride,
												IrisRenderingPipeline parent, FogMode fogMode, boolean entityLighting,
												boolean isGlint, boolean isText, boolean intensityTex, boolean isFullbright) throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

		// TODO: Is this check sound in newer versions?
		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);

		ShaderPrinter.printProgram(name)
			.addSource(PatchShaderType.VERTEX, vertex)
			.addSource(PatchShaderType.FRAGMENT, fragment)
			.print();

		PartialShader id = link(name, vertex, null, null, null, fragment, vertexFormat, true);

		// TODO 24w34a FALLBACK
		return new ShaderSupplier(shaderKey, id, () -> {
			try {
				// TODO: Fix
				GlFramebuffer framebuffer = shadowSupplier.get().createShadowFramebuffer(ImmutableSet.of(), new int[]{0});
				return new FallbackShader(id.getFinally(), RenderPipelines.ENTITY_CUTOUT, name, vertexFormat, framebuffer, framebuffer, blendModeOverride, alpha.reference(), parent);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static ShaderSupplier createShadow(WorldRenderingPipeline pipeline, String name, ShaderKey shaderKey, ProgramSource source, ProgramId programId, Supplier<ShadowRenderTargets> shadowSupplier, AlphaTest fallbackAlpha,
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
			alpha, isLines, shaderKey == ShaderKey.CLOUDS, true, inputs, pipeline.getTextureMap());
		String vertex = transformed.get(PatchShaderType.VERTEX);
		String geometry = transformed.get(PatchShaderType.GEOMETRY);
		String tessControl = transformed.get(PatchShaderType.TESS_CONTROL);
		String tessEval = transformed.get(PatchShaderType.TESS_EVAL);
		String fragment = transformed.get(PatchShaderType.FRAGMENT);

		ShaderPrinter.printProgram(name).addSources(transformed).print();

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory("", vertex, geometry, tessControl, tessEval, fragment);

		List<BufferBlendOverride> overrides = new ArrayList<>();
		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
			if (index > -1) {
				overrides.add(new BufferBlendOverride(index, information.blendMode()));
			}
		});

		PartialShader id = link(name, vertex, geometry, tessControl, tessEval, fragment, vertexFormat, false);


		return new ShaderSupplier(shaderKey, id, () -> {
			GlFramebuffer framebuffer = shadowSupplier.get().createShadowFramebuffer(ImmutableSet.of(), source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0, 1} : source.getDirectives().getDrawBuffers());
			try {
				return new ExtendedShader(id.getFinally(), name, vertexFormat, tessControl != null || tessEval != null, framebuffer, framebuffer, blendModeOverride, alpha, uniforms -> {
					CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
					customUniforms.assignTo(uniforms);
					BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
					VanillaUniforms.addVanillaUniforms(uniforms);
				}, (samplerHolder, imageHolder) -> {
					parent.addGbufferOrShadowSamplers(samplerHolder, imageHolder, flipped, isShadowPass, inputs.hasTex(), inputs.hasLight(), inputs.hasOverlay());
				}, isIntensity, parent, overrides, customUniforms);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
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
			super(new PathPackResources(new PackLocationInfo("<iris shaderpack shaders>", Component.literal("iris"), PackSource.BUILT_IN, Optional.of(new KnownPack("iris", "shader", "1.0"))), IrisPlatformHelpers.getInstance().getConfigDir()), () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            this.content = content;
		}

		@Override
		public InputStream open() {
			return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
		}
	}
}
