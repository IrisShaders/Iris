package net.coderbot.iris.pipeline;

import java.util.Objects;
import java.util.Optional;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.lwjgl.opengl.GL30C;

public class SodiumTerrainPipeline {
	String terrainVertex;
	String terrainFragment;
	GlFramebuffer framebuffer;
	ShaderPack pack;

	public SodiumTerrainPipeline(ShaderPack pack, RenderTargets renderTargets) {
		Optional<ShaderPack.ProgramSource> source = first(pack.getGbuffersTerrain(), pack.getGbuffersTexturedLit(), pack.getGbuffersTextured(), pack.getGbuffersBasic());

		this.pack = pack;

		source.ifPresent(sources -> {
			terrainVertex = sources.getVertexSource().orElse(null);
			terrainFragment = sources.getFragmentSource().orElse(null);

			framebuffer = renderTargets.createFramebufferWritingToMain(sources.getDirectives().getDrawBuffers());
		});

		if (terrainVertex != null) {
			int splitPoint = terrainVertex.indexOf("\n") + 1;

			String versionString = terrainVertex.substring(0, splitPoint);
			System.out.println("VersionString: \"" + versionString + "\n");

			String body = terrainVertex.substring(splitPoint);
			String injections = "attribute vec3 a_Pos; // The position of the vertex\n" +
				"attribute vec4 a_Color; // The color of the vertex\n" +
				"attribute vec2 a_TexCoord; // The block texture coordinate of the vertex\n" +
				"attribute vec2 a_LightCoord; // The light map texture coordinate of the vertex\n" +
				"uniform mat4 u_ModelViewMatrix;\n" +
				"uniform vec3 u_ModelScale;\n" +
				"\n" +
				"// The model translation for this draw call.\n" +
				"// If multi-draw is enabled, then the model offset will come from an attribute buffer.\n" +
				"#ifdef USE_MULTIDRAW\n" +
				"attribute vec4 d_ModelOffset;\n" +
				"#else\n" +
				"uniform vec4 d_ModelOffset;\n" +
				"#endif\n";

			terrainVertex = versionString + injections + body;

			terrainVertex = terrainVertex
				.replace("gl_Vertex", "vec4((a_Pos * u_ModelScale) + d_ModelOffset.xyz, 1.0)")
				.replace("gl_MultiTexCoord0", "vec4(a_TexCoord, 0.0, 1.0)")
				.replace("gl_MultiTexCoord1", "vec4(a_LightCoord, 0.0, 1.0)")
				.replace("gl_Color", "a_Color")
				.replace("gl_ModelViewMatrix", "u_ModelViewMatrix")
				.replace("gl_TextureMatrix[0]", "mat4(1.0)")
				.replace("gl_TextureMatrix[1]", "mat4(1.0)");

			System.out.println("Final patched source:");
			System.out.println(terrainVertex);
		}

		if (framebuffer == null) {
			framebuffer = renderTargets.createFramebufferWritingToMain(new int[] {0});
		}
	}

	public static SodiumTerrainPipeline create() {
		return new SodiumTerrainPipeline(Objects.requireNonNull(Iris.getCurrentPack()), Iris.getRenderTargets());
	}

	public Optional<String> getTerrainVertexShaderSource() {
		return Optional.ofNullable(terrainVertex);
	}

	public Optional<String> getTerrainFragmentShaderSource() {
		return Optional.ofNullable(terrainFragment);
	}

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		CommonUniforms.addCommonUniforms(uniforms, pack.getIdMap());

		return uniforms.buildUniforms();
	}

	public void bindFramebuffer() {
		this.framebuffer.bind();
	}

	public void unbindFramebuffer() {
		GlStateManager.bindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
	}

	@SafeVarargs
	private static <T> Optional<T> first(Optional<T>... candidates) {
		for (Optional<T> candidate : candidates) {
			if (candidate.isPresent()) {
				return candidate;
			}
		}

		return Optional.empty();
	}
}
