package net.coderbot.iris.postprocess;

import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;

public class PostProcessUniforms {
	public static final int COLOR_TEX_0 = 0;
	public static final int COLOR_TEX_1 = 1;
	public static final int COLOR_TEX_2 = 2;
	public static final int COLOR_TEX_3 = 3;
	public static final int COLOR_TEX_4 = 7;
	public static final int COLOR_TEX_5 = 8;
	public static final int COLOR_TEX_6 = 9;
	public static final int COLOR_TEX_7 = 10;

	public static final int DEPTH_TEX_0 = 6;
	public static final int DEPTH_TEX_1 = 11;
	public static final int DEPTH_TEX_2 = 12;

	public static final int NOISE_TEX = 15;

	public static final int DEFAULT_COLOR = COLOR_TEX_0;
	public static final int DEFAULT_DEPTH = DEPTH_TEX_0;

	public static void addPostProcessUniforms(ProgramBuilder builder, CompositeRenderer renderer) {
		// TODO: Some of these are shared uniforms

		// Generic samplers
		addSampler(builder, COLOR_TEX_0, "gcolor", "colortex0");
		addSampler(builder, COLOR_TEX_1, "gdepth", "colortex1");
		addSampler(builder, COLOR_TEX_2, "gnormal", "colortex2");
		addSampler(builder, COLOR_TEX_3, "composite", "colortex3");
		addSampler(builder, COLOR_TEX_4, "gaux1", "colortex4");
		addSampler(builder, COLOR_TEX_5, "gaux2", "colortex5");
		addSampler(builder, COLOR_TEX_6, "gaux3", "colortex6");
		addSampler(builder, COLOR_TEX_7, "gaux4", "colortex7");

		// Shadow
		addSampler(builder, 4, "watershadow", "shadowtex0");

		// Note: This will make it so that "watershadow" is printed twice to the log, oh well
		// Check if the "watershadow" uniform is active. If so, the "shadow" texture will have a separate texture unit
		boolean waterShadowEnabled = builder.location("watershadow").isPresent();

		addSampler(builder, waterShadowEnabled ? 5 : 4, "shadow");

		addSampler(builder, 5, "shadowtex1");
		addSampler(builder, 13, "shadowcolor", "shadowcolor0");
		addSampler(builder, 14, "shadowcolor1");

		// Depth
		addSampler(builder, DEFAULT_DEPTH, "gdepthtex", "depthtex0");
		addSampler(builder, DEPTH_TEX_1, "depthtex1");
		addSampler(builder, DEPTH_TEX_2, "depthtex2");

		// Noise
		addSampler(builder, NOISE_TEX, "noisetex");

		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "centerDepthSmooth", renderer.centerDepthSampler::getCenterDepthSmoothSample);
	}

	private static void addSampler(ProgramBuilder builder, int textureUnit, String... names) {
		for (String name : names) {
			builder.uniform1i(UniformUpdateFrequency.ONCE, name, () -> textureUnit);
		}
	}
}
