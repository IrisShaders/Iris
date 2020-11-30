package net.coderbot.iris.postprocess;

import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;

public class PostProcessUniforms {
	public static final int DEFAULT_COLOR = 0;
	public static final int DEFAULT_DEPTH = 6;

	public static void addPostProcessUniforms(ProgramBuilder builder) {
		// TODO: Some of these are shared uniforms

		// Generic samplers
		addSampler(builder, 0, "gcolor", "colortex0");
		addSampler(builder, 1, "gdepth", "colortex1");
		addSampler(builder, 2, "gnormal", "colortex2");
		addSampler(builder, 3, "composite", "colortex3");
		addSampler(builder, 7, "gaux1", "colortex4");
		addSampler(builder, 8, "gaux2", "colortex5");
		addSampler(builder, 9, "gaux3", "colortex6");
		addSampler(builder, 10, "gaux4", "colortex7");

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
		addSampler(builder, 11, "depthtex1");
		addSampler(builder, 12, "depthtex2");

		// Noise
		addSampler(builder, 15, "noisetex");
	}

	private static void addSampler(ProgramBuilder builder, int textureUnit, String... names) {
		for (String name : names) {
			builder.uniform1i(UniformUpdateFrequency.ONCE, name, () -> textureUnit);
		}
	}
}
