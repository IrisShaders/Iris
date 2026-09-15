package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL31C;

public final class IrisBindings {
	public static final int RESOURCE_COUNT = 32;

	public static final int ALBEDO_TEXTURE = 0;
	public static final int OVERLAY_TEXTURE = 1;
	public static final int LIGHTMAP_TEXTURE = 2;
	public static final int AUX_TEXTURE = 3;

	public static final int DYNAMIC_TRANSFORMS = 0;
	public static final int CLOUD_INFO = 1;
	public static final int PROJECTION = 2;
	public static final int GLOBALS = 3;
	public static final int FOG = 4;
	public static final int LIGHTING = 5;
	public static final int PUSH_CONSTANTS = 6;
	public static final int SODIUM_GLOBALS = 0;

	private IrisBindings() {
	}

	public static void apply(int program) {
		if (GL20C.glGetProgrami(program, GL20C.GL_LINK_STATUS) == GL20C.GL_FALSE) {
			return;
		}

		bindUniformBlocks(program, DYNAMIC_TRANSFORMS, "iris_DynamicTransforms", "DynamicTransforms", "TerrainUniform");
		bindUniformBlocks(program, CLOUD_INFO, "iris_CloudInfo", "CloudInfo");
		bindUniformBlocks(program, PROJECTION, "iris_Projection", "Projection");
		bindUniformBlocks(program, GLOBALS, "iris_Globals", "Globals");
		bindUniformBlocks(program, FOG, "iris_Fog", "Fog");
		bindUniformBlocks(program, LIGHTING, "iris_Lighting", "Lighting");
		bindUniformBlocks(program, PUSH_CONSTANTS, "iris_SodiumPushConstants");
		bindUniformBlocks(program, SODIUM_GLOBALS, "u_Globals");

		int previousProgram = GL20C.glGetInteger(GL20C.GL_CURRENT_PROGRAM);
		GlStateManager._glUseProgram(program);
		try {
			bindSamplers(program, ALBEDO_TEXTURE, "tex", "texture", "gtexture", "u_MainSampler", "Sampler0", "u_BlockTex");
			bindSamplers(program, OVERLAY_TEXTURE, "iris_overlay", "Sampler1");
			bindSamplers(program, LIGHTMAP_TEXTURE, "lightmap", "Sampler2", "u_LightTex");
			bindSamplers(program, AUX_TEXTURE, "CloudFaces", "u_SectionTimeInfo");
		} finally {
			GlStateManager._glUseProgram(previousProgram);
		}
	}

	private static void bindUniformBlocks(int program, int binding, String... names) {
		for (String name : names) {
			int index = GL31C.glGetUniformBlockIndex(program, name);
			if (index != GL31C.GL_INVALID_INDEX) {
				GL31C.glUniformBlockBinding(program, index, binding);
			}
		}
	}

	private static void bindSamplers(int program, int binding, String... names) {
		for (String name : names) {
			int location = GlStateManager._glGetUniformLocation(program, name);
			if (location >= 0) {
				GlStateManager._glUniform1i(location, binding);
			}
		}
	}
}
