package net.coderbot.iris.pipeline;

import java.util.Objects;
import java.util.Optional;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.transform.BuiltinUniformReplacementTransformer;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.SamplerUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.fabricmc.loader.api.FabricLoader;

public class SodiumTerrainPipeline {
	String terrainVertex;
	String terrainFragment;
	String translucentVertex;
	String translucentFragment;
	String shadowVertex;
	String shadowFragment;
	//GlFramebuffer framebuffer;
	ProgramSet programSet;

	public SodiumTerrainPipeline(ProgramSet programSet) {
		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);
		Optional<ProgramSource> shadowSource = programSet.getShadow();

		this.programSet = programSet;

		terrainSource.ifPresent(sources -> {
			terrainVertex = sources.getVertexSource().orElse(null);
			terrainFragment = sources.getFragmentSource().orElse(null);
		});

		translucentSource.ifPresent(sources -> {
			translucentVertex = sources.getVertexSource().orElse(null);
			translucentFragment = sources.getFragmentSource().orElse(null);
		});

		shadowSource.ifPresent(sources -> {
			shadowVertex = sources.getVertexSource().orElse(null);
			shadowFragment = sources.getFragmentSource().orElse(null);
		});

		if (terrainVertex != null) {
			terrainVertex = transformVertexShader(terrainVertex);
		}

		if (translucentVertex != null) {
			translucentVertex = transformVertexShader(translucentVertex);
		}

		if (shadowVertex != null) {
			shadowVertex = transformVertexShader(shadowVertex);
		}

		if (terrainFragment != null) {
			terrainFragment = transformFragmentShader(terrainFragment);
		}

		if (translucentFragment != null) {
			translucentFragment = transformFragmentShader(translucentFragment);
		}

		if (shadowFragment != null) {
			shadowFragment = transformFragmentShader(shadowFragment);
		}
	}

	private static String transformVertexShader(String base) {
		StringTransformations transformations = new StringTransformations(base);

		String injections = "attribute vec3 a_Pos; // The position of the vertex\n" +
			"attribute vec4 a_Color; // The color of the vertex\n" +
			"attribute vec2 a_TexCoord; // The block texture coordinate of the vertex\n" +
			"attribute vec2 a_LightCoord; // The light map texture coordinate of the vertex\n" +
			"attribute vec3 a_Normal; // The vertex normal\n" +
			"uniform mat4 u_ModelViewMatrix;\n" +
			"uniform mat4 u_ModelViewProjectionMatrix;\n" +
			"uniform mat4 u_NormalMatrix;\n" +
			"uniform vec3 u_ModelScale;\n" +
			"uniform vec2 u_TextureScale;\n" +
			"\n" +
			"// The model translation for this draw call.\n" +
			"attribute vec4 d_ModelOffset;\n" +
			"\n" +
			"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }";

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, injections);

		// NB: This is needed on macOS or else the driver will refuse to compile most packs making use of these
		// constants.
		ProgramBuilder.MACRO_CONSTANTS.getDefineStrings().forEach(defineString ->
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, defineString + "\n"));

		transformations.replaceExact("gl_Vertex", "vec4((a_Pos * u_ModelScale) + d_ModelOffset.xyz, 1.0)");
		// transformations.replaceExact("gl_MultiTexCoord1.xy/255.0", "a_LightCoord");
		transformations.replaceExact("gl_MultiTexCoord0", "vec4(a_TexCoord * u_TextureScale, 0.0, 1.0)");
		//transformations.replaceExact("gl_MultiTexCoord1", "vec4(a_LightCoord * 255.0, 0.0, 1.0)");
		transformations.replaceExact("gl_Color", "a_Color");
		transformations.replaceExact("gl_ModelViewMatrix", "u_ModelViewMatrix");
		transformations.replaceExact("gl_ModelViewProjectionMatrix", "u_ModelViewProjectionMatrix");
		transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");
		// transformations.replaceExact("gl_TextureMatrix[1]", "mat4(1.0 / 255.0)");
		transformations.replaceExact("gl_NormalMatrix", "mat3(u_NormalMatrix)");
		transformations.replaceExact("gl_Normal", "a_Normal");
		// Just being careful
		transformations.replaceExact("ftransform", "iris_ftransform");

		new BuiltinUniformReplacementTransformer("a_LightCoord").apply(transformations);

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			System.out.println("Final patched source:");
			System.out.println(transformations);
		}

		return transformations.toString();
	}

	private static String transformFragmentShader(String base) {
		StringTransformations transformations = new StringTransformations(base);

		// NB: This is needed on macOS or else the driver will refuse to compile most packs making use of these
		// constants.
		ProgramBuilder.MACRO_CONSTANTS.getDefineStrings().forEach(defineString ->
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, defineString + "\n"));

		return transformations.toString();
	}

	public static Optional<SodiumTerrainPipeline> create() {
		Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension(), false);

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

	public Optional<String> getShadowVertexShaderSource() {
		return Optional.ofNullable(shadowVertex);
	}

	public Optional<String> getShadowFragmentShaderSource() {
		return Optional.ofNullable(shadowFragment);
	}

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();
		CommonUniforms.addCommonUniforms(uniforms, programSet.getPack().getIdMap(), programSet.getPackDirectives(), ((DeferredWorldRenderingPipeline) pipeline).getUpdateNotifier());
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
