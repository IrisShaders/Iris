package net.coderbot.iris;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.lwjgl.opengl.GL20;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static Program gbuffersTextured;

	private static InputStream vertexSource;
	private static InputStream fragmentSource;

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

		CommonUniforms.addCommonUniforms(builder);

		return builder.build();
	}

	@Override
	public void onInitializeClient() {
		vertexSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.vsh"));
		fragmentSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.fsh"));
	}
}
