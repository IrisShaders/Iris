package net.coderbot.iris.shaderpack.transform;

public class BuiltinUniformReplacementTransformer {
	private final String normalizedLightmapCoords;

	private static final String NORMALIZED_PLACEHOLDER = "iris_NormalizedLightmapCoords";

	public BuiltinUniformReplacementTransformer(float lightmapScale) {
		// TODO: I don't think that this is the same as vanilla!
		this.normalizedLightmapCoords = "(gl_MultiTexCoord1.xy * " + lightmapScale + ")";
	}

	public BuiltinUniformReplacementTransformer(String customNormalizedLightmapCoordsExpression) {
		this.normalizedLightmapCoords = customNormalizedLightmapCoordsExpression;
	}

	public void apply(Transformations transformations) {
		// gl_MultiTexCoord1 and gl_MultiTexCoord2 are both aliases of the lightmap coords
		if (transformations.contains(NORMALIZED_PLACEHOLDER)) {
			// Bail out! Not sure what to do here.
			throw new AssertionError();
		}

		applyCommonCases(transformations, "gl_MultiTexCoord1");
		applyCommonCases(transformations, "gl_MultiTexCoord2");
		applyFallbackCases(transformations);

		transformations.replaceExact(NORMALIZED_PLACEHOLDER, normalizedLightmapCoords);
	}

	private void applyCommonCases(Transformations transformations, String coordName) {
		// Replace basic common operations
		//
		// These cases are simple and they show up a lot.
		// They are all different ways of obtaining the normalized lightmap coordinates

		transformations.replaceExact(
			"(gl_TextureMatrix[1]*" + coordName + ").st",
			NORMALIZED_PLACEHOLDER
		);

		transformations.replaceExact(
			"(gl_TextureMatrix[1] * " + coordName + ").st",
			NORMALIZED_PLACEHOLDER
		);

		transformations.replaceExact(
			"(gl_TextureMatrix[1] * " + coordName + ").xy",
			NORMALIZED_PLACEHOLDER
		);

		transformations.replaceExact(
			"(gl_TextureMatrix[1] * " + coordName + ").xy",
			NORMALIZED_PLACEHOLDER
		);

		transformations.replaceExact(
			"(gl_TextureMatrix[1] * " + coordName + ").s",
			NORMALIZED_PLACEHOLDER + ".s"
		);

		transformations.replaceExact(
			"gl_TextureMatrix[1] * " + coordName,
			"vec4(" + NORMALIZED_PLACEHOLDER + ", 0.0, 1.0)"
		);

		// NB: Technically this isn't a correct transformation (it changes the values slightly), however the shader code
		// being replaced isn't correct to begin with since it doesn't properly apply the centering / scaling
		// transformation like gl_TextureMatrix[1] would. Therefore, I think this is acceptable.
		//
		// This code shows up in Sildur's shaderpacks.
		transformations.replaceExact(
			coordName + ".xy/255.0",
			NORMALIZED_PLACEHOLDER
		);
	}

	private void applyFallbackCases(Transformations transformations) {
		transformations.replaceExact("gl_TextureMatrix[1]", "iris_LightmapTextureMatrix");
		transformations.replaceExact(
			"gl_MultiTexCoord1",
			"vec4(" + NORMALIZED_PLACEHOLDER + " * 255.0, 0.0, 1.0)"
		);

		transformations.replaceExact(
				"gl_MultiTexCoord2",
				"vec4(" + NORMALIZED_PLACEHOLDER + " * 255.0, 0.0, 1.0)"
		);

		// If there are references to the fallback lightmap texture matrix, then make it available to the shader program.
		if (transformations.contains("iris_LightmapTextureMatrix")) {
			transformations.injectLine(
				Transformations.InjectionPoint.BEFORE_CODE,
				"uniform mat4 iris_LightmapTextureMatrix;"
			);
		}
	}
}
