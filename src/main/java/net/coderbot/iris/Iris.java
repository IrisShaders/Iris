package net.coderbot.iris;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Objects;

import net.coderbot.iris.config.ShaderProperties;
import net.coderbot.iris.shaders.ShaderManager;
import net.coderbot.iris.shaders.ShaderParser;
import net.coderbot.iris.uniforms.Uniforms;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static boolean shadersCreated = false;
	private static GlShader vertex;
	private static GlShader fragment;
	private static GlProgram program;
	private static Uniforms programUniforms;

	private static ShaderProperties properties;
	private static ShaderManager manager;
	private static ShaderParser parser;
	private static InputStream vertexSource;
	private static InputStream fragmentSource;

	public static void useTerrainShaders() {
		if (!shadersCreated) {
			createShaders();
		}

		GlProgramManager.useProgram(program.getProgramRef());
		setupAttributes();
		programUniforms.update();
	}

	private static void setupAttributes() {
		// TODO: Properly add these attributes into the vertex format

		int mcEntity = GL20.glGetAttribLocation(program.getProgramRef(), "mc_Entity");

		if (mcEntity != -1) {
			float blockId = -1.0F;

			GL20.glVertexAttrib4f(mcEntity, blockId, -1.0F, -1.0F, -1.0F);
		}
	}

	private static void createShaders() {
		try {
			vertex = GlShader.createFromResource(GlShader.Type.VERTEX, "gbuffers_textured.vsh", vertexSource, "");
			fragment = GlShader.createFromResource(GlShader.Type.FRAGMENT, "gbuffers_textured.fsh", fragmentSource, "");
		} catch (IOException e) {
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
				return vertex;
			}

			@Override
			public GlShader getFragmentShader() {
				return fragment;
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
		setShaderProperties(new ShaderProperties().setDefaultPack("Vaporwave-Shaderpack-master"));
		try {
			getShaderProperties().createAndLoadProperties();
		} catch (IOException e){
			e.printStackTrace();
		}
		setShaderManager(new ShaderManager());
		setShaderParser(new ShaderParser(getShaderProperties().getShaderPackPath()));
		try {
			getShaderParser().parseBlockProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			vertexSource = new FileInputStream(FabricLoader.getInstance().getGameDir() + "/shaderpacks/Trippy-Shaderpack-master/shaders/gbuffers_textured.vsh");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
            fragmentSource = new FileInputStream(FabricLoader.getInstance().getGameDir() + "/shaderpacks/Trippy-Shaderpack-master/shaders/gbuffers_textured.fsh");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
	public static ShaderProperties getShaderProperties(){
		return properties;
	}
	public static void  setShaderProperties(ShaderProperties properties){
		Iris.properties = properties;
	}
	public static ShaderManager getShaderManager(){
		return manager;
	}
	private static void setShaderManager(ShaderManager manager){
		Iris.manager = manager;
	}
	private static void setShaderParser(ShaderParser parser){
		Iris.parser = parser;
	}
	private static ShaderParser getShaderParser(){
		return parser;
	}
}
