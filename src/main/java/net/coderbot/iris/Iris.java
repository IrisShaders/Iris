package net.coderbot.iris;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.lwjgl.opengl.GL20;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static ShaderPack internal;
	private static Program gbuffersTextured;

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
		Path internalShaderpackPath = FabricLoader.getInstance().getModContainer("iris")
				.orElseThrow(() -> new RuntimeException("Iris doesn't exist???")).getRootPath();

		try {
			internal = new ShaderPack(internalShaderpackPath);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load internal shaderpack!", e);
		}
	}
}
