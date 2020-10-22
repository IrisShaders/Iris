package net.coderbot.iris;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import net.coderbot.iris.uniforms.Uniforms;

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

	private static InputStream vertexSource;
	private static InputStream fragmentSource;

	public static void useTerrainShaders() {
		if (!shadersCreated) {
			createShaders();
		}

		GlProgramManager.useProgram(program.getProgramRef());
		programUniforms.update();
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
		vertexSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.vsh"));
		fragmentSource = Objects.requireNonNull(Iris.class.getResourceAsStream("/gbuffers_textured.fsh"));
	}
}
