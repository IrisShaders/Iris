package net.coderbot.iris;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import net.coderbot.iris.config.ShaderProperties;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaders.ShaderManager;
import net.coderbot.iris.shaders.ShaderParser;
import net.coderbot.iris.uniforms.Uniforms;
import org.lwjgl.opengl.GL20;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static Program gbuffersTextured;

	private static InputStream vertexSource;
	private static InputStream fragmentSource;

	private static ShaderProperties shaderProperties;
	private static ShaderParser shaderParser;
	private static ShaderManager shaderManager;

	public static void useTerrainShaders() {
		if (gbuffersTextured == null) {
			gbuffersTextured = createShaders();
		}

		gbuffersTextured.use();
		setupAttributes(gbuffersTextured);
	}

	private static void setupAttributes(Program program) {
		// TODO: Properly add these attributes into the vertex format

		int mcEntity = GL20.glGetAttribLocation(program.getProgramId(), "mc_Entity");

		if (mcEntity != -1) {
			float blockId = -1.0F;

			GL20.glVertexAttrib4f(mcEntity, blockId, -1.0F, -1.0F, -1.0F);
		}
	}

	private static Program createShaders() {
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin("gbuffers_textured", vertexSource, fragmentSource);
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize Iris!", e);
		}

		Uniforms.addCommonUniforms(builder);

		return builder.build();
	}

	@Override
	public void onInitializeClient() {
		ShaderProperties properties = new ShaderProperties().setDefaultPack("Vaporwave-Shaderpack-master");
		setShaderProperties(properties);
		try {
			properties.createAndLoadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ShaderManager manager = new ShaderManager();
		setShaderManager(manager);
		ShaderParser shaderParser = new ShaderParser(properties.getShaderPackPath());
		setShaderParser(shaderParser);
		shaderParser.parseBlockProperties();
		shaderParser.parseItemProperties();
		vertexSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.vsh"));
		fragmentSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.fsh"));
	}
	private static void setShaderProperties(ShaderProperties properties){
		shaderProperties = properties;
	}
	public static ShaderProperties getShaderProperties(){
		return shaderProperties;
	}
	private static void setShaderParser(ShaderParser parser){
		shaderParser = parser;
	}
	public static ShaderParser getShaderParser(){
		return shaderParser;
	}
	private static void setShaderManager(ShaderManager manager){
		shaderManager = manager;
	}
	public static ShaderManager getShaderManager(){
		return shaderManager;
	}
}
