package net.coderbot.iris.pipeline;

import java.util.Objects;
import java.util.Optional;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.transform.BuiltinUniformReplacementTransformer;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.SamplerUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;

public class SodiumTerrainPipeline {
	String terrainVertex;
	String terrainFragment;
	String translucentVertex;
	String translucentFragment;
	//GlFramebuffer framebuffer;
	ProgramSet programSet;

	public SodiumTerrainPipeline(ProgramSet programSet) {
		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);

		this.programSet = programSet;

		terrainSource.ifPresent(sources -> {
			terrainVertex = sources.getVertexSource().orElse(null);
			terrainFragment = sources.getFragmentSource().orElse(null);

			//framebuffer = renderTargets.createFramebufferWritingToMain(sources.getDirectives().getDrawBuffers());
		});

		translucentSource.ifPresent(sources -> {
			translucentVertex = sources.getVertexSource().orElse(null);
			translucentFragment = sources.getFragmentSource().orElse(null);

			//framebuffer = renderTargets.createFramebufferWritingToMain(sources.getDirectives().getDrawBuffers());
		});

		if (terrainVertex != null) {
			terrainVertex = transformVertexShader(terrainVertex);
		}

		if (translucentVertex != null) {
			translucentVertex = transformVertexShader(translucentVertex);
		}

		/*if (framebuffer == null) {
			framebuffer = renderTargets.createFramebufferWritingToMain(new int[] {0});
		}*/
	}

	private static String transformVertexShader(String base) {
		StringTransformations transformations = new StringTransformations(base);

		String injections = "attribute vec3 a_Pos; // The position of the vertex\n" +
			"attribute vec4 a_Color; // The color of the vertex\n" +
			"attribute vec2 a_TexCoord; // The block texture coordinate of the vertex\n" +
			"attribute vec2 a_LightCoord; // The light map texture coordinate of the vertex\n" +
			"attribute vec3 a_Normal; // The vertex normal\n" +
			"uniform mat4 u_ModelViewMatrix;\n" +
			"uniform mat4 u_NormalMatrix;\n" +
			"uniform vec3 u_ModelScale;\n" +
			"uniform vec2 u_TextureScale;\n" +
			"\n" +
			"// The model translation for this draw call.\n" +
			"// If multi-draw is enabled, then the model offset will come from an attribute buffer.\n" +
			"#ifdef USE_MULTIDRAW\n" +
			"attribute vec4 d_ModelOffset;\n" +
			"#else\n" +
			"uniform vec4 d_ModelOffset;\n" +
			"#endif\n";

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, injections);

		transformations.replaceExact("gl_Vertex", "vec4((a_Pos * u_ModelScale) + d_ModelOffset.xyz, 1.0)");
		// transformations.replaceExact("gl_MultiTexCoord1.xy/255.0", "a_LightCoord");
		transformations.replaceExact("gl_MultiTexCoord0", "vec4(a_TexCoord * u_TextureScale, 0.0, 1.0)");
		//transformations.replaceExact("gl_MultiTexCoord1", "vec4(a_LightCoord * 255.0, 0.0, 1.0)");
		transformations.replaceExact("gl_Color", "a_Color");
		transformations.replaceExact("gl_ModelViewMatrix", "u_ModelViewMatrix");
		transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");
		// transformations.replaceExact("gl_TextureMatrix[1]", "mat4(1.0 / 255.0)");
		transformations.replaceExact("gl_NormalMatrix", "mat3(u_NormalMatrix)");
		transformations.replaceExact("gl_Normal", "a_Normal");

		new BuiltinUniformReplacementTransformer("a_LightCoord").apply(transformations);

		System.out.println("Final patched source:");
		System.out.println(transformations);

		return transformations.toString();
	}

	public static Optional<SodiumTerrainPipeline> create() {
		return Iris.getCurrentPack().map(
			pack -> new SodiumTerrainPipeline(Objects.requireNonNull(pack.getProgramSet(Iris.getCurrentDimension())))
		);
	}

	public Optional<String> getTerrainVertexShaderSource() {
		return Optional.ofNullable(terrainVertex);
	}

	public Optional<String> getTerrainFragmentShaderSource() {
		return Optional.ofNullable(terrainFragment);
	}

	public Optional<String> getTranslucentVertexShaderSource() {
		return Optional.ofNullable(translucentVertex);
	}

	public Optional<String> getTranslucentFragmentShaderSource() {
		return Optional.ofNullable(translucentFragment);
	}

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		CommonUniforms.addCommonUniforms(uniforms, programSet.getPack().getIdMap(), programSet.getPackDirectives());
		SamplerUniforms.addWorldSamplerUniforms(uniforms);
		SamplerUniforms.addDepthSamplerUniforms(uniforms);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);

		return uniforms.buildUniforms();
	}

	/*public void bindFramebuffer() {
		this.framebuffer.bind();
	}

	public void unbindFramebuffer() {
		GlStateManager.bindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
	}*/

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
