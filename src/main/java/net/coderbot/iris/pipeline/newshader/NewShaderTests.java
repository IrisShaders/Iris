package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.SamplerUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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
										NewWorldRenderingPipeline parent, FogMode fogMode) throws IOException {
		AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);

		ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat);
		String vertex = TriforcePatcher.patch(source.getVertexSource().orElseThrow(RuntimeException::new), ShaderType.VERTEX, alpha, true, inputs);
		String fragment = TriforcePatcher.patch(source.getFragmentSource().orElseThrow(RuntimeException::new), ShaderType.FRAGMENT, alpha, true, inputs);

		String shaderJson = "{\n" +
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
				"        \"UV2\",\n" +
				"        \"Normal\"\n" +
				"    ],\n" +
				"    \"samplers\": [\n" +
				// TODO: Don't duplicate these definitions!
				"        { \"name\": \"gtexture\" },\n" +
				"        { \"name\": \"texture\" },\n" +
				"        { \"name\": \"tex\" },\n" +
				"        { \"name\": \"lightmap\" },\n" +
				"        { \"name\": \"normals\" },\n" +
				"        { \"name\": \"specular\" },\n" +
				"        { \"name\": \"shadow\" },\n" +
				"        { \"name\": \"watershadow\" },\n" +
				"        { \"name\": \"shadowtex0\" },\n" +
				"        { \"name\": \"shadowtex1\" },\n" +
				"        { \"name\": \"depthtex0\" },\n" +
				"        { \"name\": \"depthtex1\" },\n" +
				"        { \"name\": \"noisetex\" },\n" +
				"        { \"name\": \"colortex0\" },\n" +
				"        { \"name\": \"colortex1\" },\n" +
				"        { \"name\": \"colortex2\" },\n" +
				"        { \"name\": \"colortex3\" },\n" +
				"        { \"name\": \"colortex4\" },\n" +
				"        { \"name\": \"colortex5\" },\n" +
				"        { \"name\": \"colortex6\" },\n" +
				"        { \"name\": \"colortex7\" },\n" +
				"        { \"name\": \"gaux1\" },\n" +
				"        { \"name\": \"gaux2\" },\n" +
				"        { \"name\": \"gaux3\" },\n" +
				"        { \"name\": \"gaux4\" },\n" +
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
				"        { \"name\": \"iris_FogColor\", \"type\": \"float\", \"count\": 4, \"values\": [ 0.0, 0.0, 0.0, 0.0 ] }\n" +
				"    ]\n" +
				"}";

		ResourceFactory shaderResourceFactory = new IrisProgramResourceFactory(shaderJson, vertex, fragment);

		final Path debugOutDir = FabricLoader.getInstance().getGameDir().resolve("patched_shaders");

		Files.write(debugOutDir.resolve(name + ".vsh"), vertex.getBytes(StandardCharsets.UTF_8));
		Files.write(debugOutDir.resolve(name + ".fsh"), fragment.getBytes(StandardCharsets.UTF_8));
		Files.write(debugOutDir.resolve(name + ".json"), shaderJson.getBytes(StandardCharsets.UTF_8));

		return new ExtendedShader(shaderResourceFactory, name, vertexFormat, writingToBeforeTranslucent, writingToAfterTranslucent, baseline, uniforms -> {
			CommonUniforms.addCommonUniforms(uniforms, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), updateNotifier, fogMode);
			//SamplerUniforms.addWorldSamplerUniforms(uniforms);
			//SamplerUniforms.addDepthSamplerUniforms(uniforms);
			BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
		}, parent);
	}

	private static class IrisProgramResourceFactory implements ResourceFactory {
		private final String json;
		private final String vertex;
		private final String fragment;

		public IrisProgramResourceFactory(String json, String vertex, String fragment) {
			this.json = json;
			this.vertex = vertex;
			this.fragment = fragment;
		}

		@Override
		public Resource getResource(Identifier id) throws IOException {
			final String path = id.getPath();

			if (path.endsWith("json")) {
				return new StringResource(id, json);
			} else if (path.endsWith("vsh")) {
				return new StringResource(id, vertex);
			} else if (path.endsWith("fsh")) {
				return new StringResource(id, fragment);
			}

			throw new IOException("Couldn't load " + id);
		}
	}

	private static class StringResource implements Resource {
		private final Identifier id;
		private final String content;

		private StringResource(Identifier id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public Identifier getId() {
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
		public <T> @Nullable T getMetadata(ResourceMetadataReader<T> metaReader) {
			return null;
		}

		@Override
		public String getResourcePackName() {
			return "<iris shaderpack shaders>";
		}

		@Override
		public void close() throws IOException {
			// No resources to release
		}
	}
}
