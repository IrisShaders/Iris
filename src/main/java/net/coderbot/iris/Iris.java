package net.coderbot.iris;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import net.coderbot.iris.uniforms.TexturedUniforms;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static boolean shadersCreated = false;
	private static GlShader vertexTextured;
	private static GlShader fragmentTextured;
	private static GlProgram program;
	private static TexturedUniforms programTexturedUniforms;

	private static InputStream vertexTexturedSource;
	private static InputStream fragmentTexturedSource;


	public static void useTerrainShaders() {
		if (!shadersCreated) {
			createShaders();
		}

		GlProgramManager.useProgram(program.getProgramRef());
		programTexturedUniforms.update();
	}

	private static void createShaders() {
		try {
			vertexTextured = GlShader.createFromResource(GlShader.Type.VERTEX, "assets/iris/shaders/gbuffers_textured.vsh", vertexTexturedSource, "");
			fragmentTextured = GlShader.createFromResource(GlShader.Type.FRAGMENT, "assets/iris/shaders/gbuffers_textured.fsh", fragmentTexturedSource, "");
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

		programTexturedUniforms = new TexturedUniforms(program);

		shadersCreated = true;
	}

	@Override
	public void onInitializeClient() {
		vertexTexturedSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/assets/iris/shaders/gbuffers_textured.vsh"));
		fragmentTexturedSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/assets/iris/shaders/gbuffers_textured.fsh"));
	}
}
