package net.coderbot.iris;

import net.coderbot.iris.config.ShaderProperties;
import net.coderbot.iris.uniforms.Uniforms;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static boolean shadersCreated = false;
	private static GlShader vertexTextured;
	private static GlShader fragmentTextured;
	private static GlProgram program;
	private static Uniforms programUniforms;
	private static ShaderProperties shaderProperties;
	private static InputStream vertexTexturedSource;
	private static InputStream fragmentTexturedSource;


	public static void useTerrainShaders() {
		if (!shadersCreated) {
			createShaders();
		}

		GlProgramManager.useProgram(program.getProgramRef());
		programUniforms.update();
	}

	private static void createShaders() {
		try {
			vertexTextured = GlShader.createFromResource(GlShader.Type.VERTEX, "assets/iris/shaders/gbuffers_textured.vsh", vertexTexturedSource, "");
			fragmentTextured = GlShader.createFromResource(GlShader.Type.FRAGMENT, "assets/iris/shaders/gbuffers_textured.fsh", fragmentTexturedSource, "");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to initialize Iris!", e);
		}

		int programId;

		try {
			programId = GlProgramManager.createProgram();
		} catch (IOException e) {
			e.printStackTrace();
			programId = 0;
		}

		final int finalProgramId = programId;

		program = new GlProgram() {
			@Override
			public int getProgramRef() {
				return finalProgramId;
			}

			@Override
			public void markUniformsDirty() {
				// nah
			}

			@Override
			public GlShader getVertexShader() {
				return vertexTextured;
			}

			@Override
			public GlShader getFragmentShader() {
				return fragmentTextured;
			}
		};

		try {
			GlProgramManager.linkProgram(program);
		} catch (IOException e) {
			e.printStackTrace();
		}

		programUniforms = new Uniforms(program);

		shadersCreated = true;
	}

	@Override
	public void onInitializeClient() {
		setShaderProperties(new ShaderProperties());
		try {
			getShaderProperties().createAndLoadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		vertexTexturedSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.vsh"));
		fragmentTexturedSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.fsh"));
	}
	private static void setShaderProperties(ShaderProperties shaders){
		shaderProperties = shaders;
	}
	public static ShaderProperties getShaderProperties(){
		return shaderProperties;
	}
}
