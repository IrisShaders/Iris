package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.CompiledShader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
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
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.VanillaUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.minecraft.client.renderer.CompiledShaderProgram;
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

		int id = link(name, vertex, geometry, tessControl, tessEval, fragment, vertexFormat);


		return new ExtendedShader(id, shaderResourceFactory, name, vertexFormat, tessControl != null || tessEval != null, writingToBeforeTranslucent, writingToAfterTranslucent, blendModeOverride, alpha, uniforms -> {
			CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
			customUniforms.assignTo(uniforms);
			BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
			VanillaUniforms.addVanillaUniforms(uniforms);
		}, (samplerHolder, imageHolder) -> {
			parent.addGbufferOrShadowSamplers(samplerHolder, imageHolder, flipped, isShadowPass, inputs.hasTex(), inputs.hasLight(), inputs.hasOverlay());
		}, isIntensity, parent, overrides, customUniforms);
	}



	public static int link(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment, VertexFormat vertexFormat) throws ShaderCompileException {
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

			((VertexFormatExtension) vertexFormat).bindAttributesIris(i);
			GlStateManager.glLinkProgram(i);

			int j = GlStateManager.glGetProgrami(i, 35714);
			if (j == 0) {
				String string = GlStateManager.glGetProgramInfoLog(i, 32768);
				throw new ShaderCompileException(
					name, string
				);
			} else {
				detachIfValid(i, vertexS);
				detachIfValid(i, geometryS);
				detachIfValid(i, tessContS);
				detachIfValid(i, tessEvalS);
				detachIfValid(i, fragS);

				return i;
			}
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

	public static FallbackShader createFallback(String name, GlFramebuffer writingToBeforeTranslucent,
												GlFramebuffer writingToAfterTranslucent, AlphaTest alpha,
												VertexFormat vertexFormat, BlendModeOverride blendModeOverride,
												IrisRenderingPipeline parent, FogMode fogMode, boolean entityLighting,
												boolean isGlint, boolean isText, boolean intensityTex, boolean isFullbright) throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

		// TODO: Is this check sound in newer versions?
		boolean isLeash = vertexFormat == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting, isLeash);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex, isLeash);


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
			        		{ "name": "TextureMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        		{ "name": "ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        		{ "name": "ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        		{ "name": "ModelOffset", "type": "float", "count": 3, "values": [ 0.0, 0.0, 0.0 ] },
			        		{ "name": "ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] },
			        		{ "name": "GlintAlpha", "type": "float", "count": 1, "values": [ 1.0 ] },
			        		{ "name": "Light0_Direction", "type": "float", "count": 3, "values": [0.0, 0.0, 0.0] },
			        		{ "name": "Light1_Direction", "type": "float", "count": 3, "values": [0.0, 0.0, 0.0] },
			        		{ "name": "FogStart", "type": "float", "count": 1, "values": [ 0.0 ] },
			        		{ "name": "FogEnd", "type": "float", "count": 1, "values": [ 1.0 ] },
			        		{ "name": "FogDensity", "type": "float", "count": 1, "values": [ 1.0 ] },
			        		{ "name": "FogIsExp2", "type": "int", "count": 1, "values": [ 0 ] },
			        		{ "name": "AlphaTestValue", "type": "float", "count": 1, "values": [ 0.0 ] },
			        		{ "name": "LineWidth", "type": "float", "count": 1, "values": [ 1.0 ] },
			        		{ "name": "ScreenSize", "type": "float", "count": 2, "values": [ 1.0, 1.0 ] },
			        		{ "name": "FogColor", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] }
			    ]
			}""", name, name);
		ShaderPrinter.printProgram(name)
			.addSource(PatchShaderType.VERTEX, vertex)
			.addSource(PatchShaderType.FRAGMENT, fragment)
			.addJson(shaderJsonString)
			.print();

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, null, null, null, fragment);

		// TODO 24w34a FALLBACK
		return new FallbackShader(0, shaderResourceFactory, name, vertexFormat, writingToBeforeTranslucent,
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
			super(new PathPackResources(new PackLocationInfo("<iris shaderpack shaders>", Component.literal("iris"), PackSource.BUILT_IN, Optional.of(new KnownPack("iris", "shader", "1.0"))), IrisPlatformHelpers.getInstance().getConfigDir()), () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            this.content = content;
		}

		@Override
		public InputStream open() {
			return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
		}
	}
}
