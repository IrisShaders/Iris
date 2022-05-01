package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.coderbot.iris.pipeline.newshader.fallback.ShaderSynthesizer;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class NewShaderTests {
	public static ExtendedShader create(String name, ProgramSource source, GlFramebuffer writingToBeforeTranslucent,
										GlFramebuffer writingToAfterTranslucent, GlFramebuffer baseline, AlphaTest fallbackAlpha,
										VertexFormat vertexFormat, FrameUpdateNotifier updateNotifier,
										NewWorldRenderingPipeline parent, FogMode fogMode,
										boolean isFullbright) throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
		BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride();

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright);

		String geometry = null;
		boolean hasGeometry = false;
		if (source.getGeometrySource().isPresent()) {
			hasGeometry = true;
			geometry = TriforcePatcher.patchVanilla(source.getGeometrySource().get(), ShaderType.GEOMETRY, alpha, true, inputs, true);
		}

		String vertex = TriforcePatcher.patchVanilla(source.getVertexSource().orElseThrow(RuntimeException::new), ShaderType.VERTEX, alpha, true, inputs, hasGeometry);
		String fragment = TriforcePatcher.patchVanilla(source.getFragmentSource().orElseThrow(RuntimeException::new), ShaderType.FRAGMENT, alpha, true, inputs, hasGeometry);

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
				"    \"samplers\": [\n" +
				// TODO: Don't duplicate these definitions!
				"        { \"name\": \"gtexture\" },\n" +
				"        { \"name\": \"texture\" },\n" +
				"        { \"name\": \"tex\" },\n" +
				"        { \"name\": \"iris_overlay\" },\n" +
				"        { \"name\": \"lightmap\" },\n" +
				"        { \"name\": \"normals\" },\n" +
				"        { \"name\": \"specular\" },\n" +
				"        { \"name\": \"shadow\" },\n" +
				"        { \"name\": \"watershadow\" },\n" +
				"        { \"name\": \"shadowtex0\" },\n" +
				"        { \"name\": \"shadowtex1\" },\n" +
				"        { \"name\": \"depthtex0\" },\n" +
				"        { \"name\": \"depthtex1\" },\n" +
				"        { \"name\": \"noisetex\" },\n");

		// TODO: SamplerHolder should really be responsible for this...
		for (int buffer : PackRenderTargetDirectives.BASELINE_SUPPORTED_RENDER_TARGETS) {
			if (buffer >= 4 && buffer < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				shaderJson.append("        { \"name\": \"");
				shaderJson.append(PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(buffer));
				shaderJson.append("\" },\n");
			}

			shaderJson.append("        { \"name\": \"colortex");
			shaderJson.append(buffer);
			shaderJson.append("\" },\n");
		}

		shaderJson.append(
				"        { \"name\": \"shadowcolor\" },\n" +
				"        { \"name\": \"shadowcolor0\" },\n" +
				"        { \"name\": \"shadowcolor1\" }\n" +
				"    ],\n" +
				"    \"uniforms\": [\n" +
				"        { \"name\": \"iris_TextureMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"iris_ModelViewMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"iris_ProjMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"iris_ChunkOffset\", \"type\": \"float\", \"count\": 3, \"values\": [ 0.0, 0.0, 0.0 ] },\n" +
				"        { \"name\": \"iris_ColorModulator\", \"type\": \"float\", \"count\": 4, \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n" +
				"        { \"name\": \"iris_FogStart\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.0 ] },\n" +
				"        { \"name\": \"iris_FogEnd\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
				"        { \"name\": \"iris_LineWidth\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
				"        { \"name\": \"iris_ScreenSize\", \"type\": \"float\", \"count\": 2, \"values\": [ 1.0, 1.0 ] },\n" +
				"        { \"name\": \"iris_FogColor\", \"type\": \"float\", \"count\": 4, \"values\": [ 0.0, 0.0, 0.0, 0.0 ] }\n" +
				"    ]\n" +
				"}");

		String shaderJsonString = shaderJson.toString();

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, geometry, fragment);

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			final Path debugOutDir = FabricLoader.getInstance().getGameDir().resolve("patched_shaders");

			Files.write(debugOutDir.resolve(name + ".vsh"), vertex.getBytes(StandardCharsets.UTF_8));
			Files.write(debugOutDir.resolve(name + ".fsh"), fragment.getBytes(StandardCharsets.UTF_8));
			if (geometry != null) {
				Files.write(debugOutDir.resolve(name + ".gsh"), geometry.getBytes(StandardCharsets.UTF_8));
			}
			Files.write(debugOutDir.resolve(name + ".json"), shaderJsonString.getBytes(StandardCharsets.UTF_8));
		}

		return new ExtendedShader(shaderResourceFactory, name, vertexFormat, writingToBeforeTranslucent, writingToAfterTranslucent, baseline, blendModeOverride, uniforms -> {
			CommonUniforms.addCommonUniforms(uniforms, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), updateNotifier, fogMode);
			//SamplerUniforms.addWorldSamplerUniforms(uniforms);
			//SamplerUniforms.addDepthSamplerUniforms(uniforms);
			BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
		}, isFullbright, parent);
	}

	public static FallbackShader createFallback(String name, GlFramebuffer writingToBeforeTranslucent,
										GlFramebuffer writingToAfterTranslucent, AlphaTest alpha,
										VertexFormat vertexFormat, BlendModeOverride blendModeOverride,
										NewWorldRenderingPipeline parent, FogMode fogMode, boolean entityLighting,
										boolean intensityTex, boolean isFullbright) throws IOException {
		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright);

		String vertex = ShaderSynthesizer.vsh(true, inputs, fogMode, entityLighting);
		String fragment = ShaderSynthesizer.fsh(inputs, fogMode, alpha, intensityTex);

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
				"    \"samplers\": [\n" +
				"        { \"name\": \"Sampler0\" },\n" +
				"        { \"name\": \"Sampler1\" },\n" +
				"        { \"name\": \"Sampler2\" }\n" +
				"    ],\n" +
				"    \"uniforms\": [\n" +
				"        { \"name\": \"TextureMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"ModelViewMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"ProjMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"ChunkOffset\", \"type\": \"float\", \"count\": 3, \"values\": [ 0.0, 0.0, 0.0 ] },\n" +
				"        { \"name\": \"ColorModulator\", \"type\": \"float\", \"count\": 4, \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n" +
				"        { \"name\": \"Light0_Direction\", \"type\": \"float\", \"count\": 3, \"values\": [0.0, 0.0, 0.0] },\n" +
				"        { \"name\": \"Light1_Direction\", \"type\": \"float\", \"count\": 3, \"values\": [0.0, 0.0, 0.0] },\n" +
				"        { \"name\": \"FogStart\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.0 ] },\n" +
				"        { \"name\": \"FogEnd\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
				"        { \"name\": \"FogDensity\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
				"        { \"name\": \"FogIsExp2\", \"type\": \"int\", \"count\": 1, \"values\": [ 0 ] },\n" +
				"        { \"name\": \"LineWidth\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
				"        { \"name\": \"ScreenSize\", \"type\": \"float\", \"count\": 2, \"values\": [ 1.0, 1.0 ] },\n" +
				"        { \"name\": \"FogColor\", \"type\": \"float\", \"count\": 4, \"values\": [ 0.0, 0.0, 0.0, 0.0 ] }\n" +
				"    ]\n" +
				"}";

		ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, null, fragment);

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			final Path debugOutDir = FabricLoader.getInstance().getGameDir().resolve("patched_shaders");

			Files.write(debugOutDir.resolve(name + ".vsh"), vertex.getBytes(StandardCharsets.UTF_8));
			Files.write(debugOutDir.resolve(name + ".fsh"), fragment.getBytes(StandardCharsets.UTF_8));
			Files.write(debugOutDir.resolve(name + ".json"), shaderJsonString.getBytes(StandardCharsets.UTF_8));
		}

		return new FallbackShader(shaderResourceFactory, name, vertexFormat, writingToBeforeTranslucent,
				writingToAfterTranslucent, blendModeOverride, parent);
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
		public Resource getResource(ResourceLocation id) throws IOException {
			final String path = id.getPath();

			if (path.endsWith("json")) {
				return new StringResource(id, json);
			} else if (path.endsWith("vsh")) {
				return new StringResource(id, vertex);
			} else if (path.endsWith("gsh")) {
				if (geometry == null) {
					return null;
				}
				return new StringResource(id, geometry);
			} else if (path.endsWith("fsh")) {
				return new StringResource(id, fragment);
			}

			throw new IOException("Couldn't load " + id);
		}
	}

	private static class StringResource implements Resource {
		private final ResourceLocation id;
		private final String content;

		private StringResource(ResourceLocation id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public ResourceLocation getLocation() {
			return id;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public boolean hasMetadata() {
			return false;
		}

		@Override
		public <T> @Nullable T getMetadata(MetadataSectionSerializer<T> metaReader) {
			return null;
		}

		@Override
		public String getSourceName() {
			return "<iris shaderpack shaders>";
		}

		@Override
		public void close() throws IOException {
			// No resources to release
		}
	}
}
