package net.coderbot.iris;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import net.coderbot.iris.config.ShaderProperties;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaders.ShaderManager;
import net.coderbot.iris.shaders.ShaderParser;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL20;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static Program gbuffersTextured;

	private static ShaderPack internal;
	private static ShaderPack shaderPack;

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
		ShaderPack.ProgramSource gbuffersTexturedSource = internal.getGbuffersTextured();

		// TODO: Properly handle empty shaders
		Objects.requireNonNull(gbuffersTexturedSource.getVertexSource());
		Objects.requireNonNull(gbuffersTexturedSource.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin("gbuffers_textured",
					gbuffersTexturedSource.getVertexSource().orElse(null),
					gbuffersTexturedSource.getFragmentSource().orElse(null));
		} catch (IOException e) {
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder);

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
		try {
			ShaderPack pack = new ShaderPack(Paths.get(properties.getShaderPackPath() + "/shaders"));
			setShaderPack(pack);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Path internalShaderpackPath = FabricLoader.getInstance().getModContainer("iris")
				.orElseThrow(() -> new RuntimeException("Iris doesn't exist???")).getRootPath();
		//uncomment this to use internal shaders. Make sure you have up to date files
		/*
		try {
			internal = new ShaderPack(internalShaderpackPath);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load internal shaderpack!", e);
		}
		 */
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
	private static void setShaderPack(ShaderPack shaderPack){
		Iris.shaderPack = shaderPack;
	}
	public static ShaderPack getShaderPack(){
		return shaderPack;
	}
}
