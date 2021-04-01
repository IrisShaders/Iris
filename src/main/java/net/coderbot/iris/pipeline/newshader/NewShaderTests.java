package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NewShaderTests {
	public static Shader test(ProgramSet programSet) throws IOException {
		ProgramSource source = programSet.getGbuffersTextured().flatMap(ProgramSource::requireValid).orElseThrow(RuntimeException::new);

		String vertex = TriforcePatcher.patch(source.getVertexSource().orElseThrow(RuntimeException::new), ShaderType.VERTEX);
		String fragment = TriforcePatcher.patch(source.getFragmentSource().orElseThrow(RuntimeException::new), ShaderType.FRAGMENT);

		// TODO: Assert that the unpatched programs do not contain any "#moj_import" statements

		String shaderJson = "{\n" +
				"    \"blend\": {\n" +
				"        \"func\": \"add\",\n" +
				"        \"srcrgb\": \"srcalpha\",\n" +
				"        \"dstrgb\": \"1-srcalpha\"\n" +
				"    },\n" +
				"    \"vertex\": \"gbuffers_textured\",\n" +
				"    \"fragment\": \"gbuffers_textured\",\n" +
				"    \"attributes\": [\n" +
				"        \"Position\",\n" +
				"        \"Color\",\n" +
				"        \"UV0\",\n" +
				"        \"UV2\",\n" +
				"        \"Normal\"\n" +
				"    ],\n" +
				"    \"samplers\": [\n" +
				"        { \"name\": \"Sampler0\" },\n" +
				"        { \"name\": \"Sampler2\" }\n" +
				"    ],\n" +
				"    \"uniforms\": [\n" +
				"        { \"name\": \"ModelViewMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"ProjMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n" +
				"        { \"name\": \"ChunkOffset\", \"type\": \"float\", \"count\": 3, \"values\": [ 0.0, 0.0, 0.0 ] },\n" +
				"        { \"name\": \"ColorModulator\", \"type\": \"float\", \"count\": 4, \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n" +
				"        { \"name\": \"FogStart\", \"type\": \"float\", \"count\": 1, \"values\": [ 0.0 ] },\n" +
				"        { \"name\": \"FogEnd\", \"type\": \"float\", \"count\": 1, \"values\": [ 1.0 ] },\n" +
				"        { \"name\": \"FogColor\", \"type\": \"float\", \"count\": 4, \"values\": [ 0.0, 0.0, 0.0, 0.0 ] }\n" +
				"    ]\n" +
				"}";

		ResourceFactory shaderResourceFactory = new IrisProgramResourceFactory(shaderJson, vertex, fragment);

		// TODO: Not always the same vertex format.
		return new Shader(shaderResourceFactory, "gbuffers_textured", IrisVertexFormats.TERRAIN);
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
