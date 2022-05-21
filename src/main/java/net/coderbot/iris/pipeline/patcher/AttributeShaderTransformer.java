package net.coderbot.iris.pipeline.patcher;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class AttributeShaderTransformer {
	public static String patch(String source, ShaderType type, boolean hasGeometry, InputAvailability inputs) {
		if (source.contains("iris_")) {
			throw new IllegalStateException("Shader is attempting to exploit internal Iris code!");
		}

		StringTransformations transformations = new StringTransformations(source);

		// gl_MultiTexCoord1 and gl_MultiTexCoord2 are both ways to refer to the lightmap texture coordinate.
		// See https://github.com/IrisShaders/Iris/issues/1149
		if (!inputs.lightmap) {
			transformations.replaceExact("gl_MultiTexCoord1", "vec4(240.0, 240.0, 0.0, 1.0)");
			transformations.replaceExact("gl_MultiTexCoord2", "vec4(240.0, 240.0, 0.0, 1.0)");
		} else {
			transformations.replaceExact("gl_MultiTexCoord1", "gl_MultiTexCoord2");
		}

		if (!inputs.texture) {
			transformations.define("gl_MultiTexCoord0", "vec4(240.0, 240.0, 0.0, 1.0)");
		}

		patchTextureMatrices(transformations, inputs.lightmap);

		if (inputs.overlay) {
			patchOverlayColor(transformations, type, hasGeometry);
		}

		if (transformations.contains("gl_MultiTexCoord3") && !transformations.contains("mc_midTexCoord")
			&& type == ShaderType.VERTEX) {
			// gl_MultiTexCoord3 is a super legacy alias of mc_midTexCoord. We don't do this replacement if
			// we think mc_midTexCoord could be defined just we can't handle an existing declaration robustly.
			//
			// But basically the proper way to do this is to define mc_midTexCoord only if it's not defined, and if
			// it is defined, figure out its type, then replace all occurrences of gl_MultiTexCoord3 with the correct
			// conversion from mc_midTexCoord's declared type to vec4.
			transformations.replaceExact("gl_MultiTexCoord3", "mc_midTexCoord");
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "attribute vec4 mc_midTexCoord;");
		}

		return transformations.toString();
	}

	private static void patchOverlayColor(StringTransformations transformations, ShaderType type, boolean hasGeometry) {
		// Add entity color -> overlay color attribute support.
		if (type == ShaderType.VERTEX) {
			// delete original declaration (fragile!!! we need glsl-transformer to do this robustly)
			transformations.replaceRegex("uniform\\s+vec4\\s+entityColor;", "");

			// add our own declarations
			// TODO: We're exposing entityColor to this stage even if it isn't declared in this stage. But this is
			//       needed for the pass-through behavior.
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform sampler2D iris_overlay;");
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "varying vec4 entityColor;");

			// Create our own main function to wrap the existing main function, so that we can pass through the overlay color at the
			// end to the geometry or fragment stage.
			if (transformations.contains("irisMain_overlayColor")) {
				throw new IllegalStateException("Shader already contains \"irisMain_overlayColor\"???");
			}

			transformations.replaceExact("main", "irisMain_overlayColor");
			transformations.injectLine(Transformations.InjectionPoint.END, "void main() {\n" +
					"	vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);\n" +
					"	entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);\n" +
					"\n" +
					"    irisMain_overlayColor();\n" +
					"}");
		} else if (type == ShaderType.GEOMETRY) {
			// delete original declaration (fragile!!! we need glsl-transformer to do this robustly)
			transformations.replaceRegex("uniform\\s+vec4\\s+entityColor;", "");

			// replace read references to grab the color from the first vertex.
			transformations.replaceExact("entityColor", "entityColor[0]");

			// add our own input and output declarations, after references have been replaced.
			// TODO: We're exposing entityColor to this stage even if it isn't declared in this stage. But this is
			//       needed for the pass-through behavior.
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "out vec4 entityColorGS;");
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in vec4 entityColor[];");

			// Create our own main function to wrap the existing main function, so that we can pass through the overlay color at the
			// end to the fragment stage.
			if (transformations.contains("irisMain")) {
				throw new IllegalStateException("Shader already contains \"irisMain\"???");
			}

			transformations.replaceExact("main", "irisMain");
			transformations.injectLine(Transformations.InjectionPoint.END, "void main() {\n" +
					"	 entityColorGS = entityColor[0];\n" +
					"    irisMain();\n" +
					"}");
		} else if (type == ShaderType.FRAGMENT) {
			// replace original declaration (fragile!!! we need glsl-transformer to do this robustly)
			// if entityColor is not declared as a uniform, we don't make it available
			transformations.replaceRegex("uniform\\s+vec4\\s+entityColor;", "varying vec4 entityColor;");

			if (hasGeometry) {
				// Different output name to avoid a name collision in the goemetry shader.
				transformations.replaceExact("entityColor", "entityColorGS");
			}
		}
	}

	private static void patchTextureMatrices(StringTransformations transformations, boolean hasLightmap) {
		transformations.replaceExact("gl_TextureMatrix", "iris_TextureMatrix");

		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "const float iris_ONE_OVER_256 = 0.00390625;\n");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "const float iris_ONE_OVER_32 = iris_ONE_OVER_256 * 8;\n");

		if (hasLightmap) {
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "mat4 iris_LightmapTextureMatrix = gl_TextureMatrix[2];\n");
		} else {
			// column major
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "mat4 iris_LightmapTextureMatrix =" +
					"mat4(iris_ONE_OVER_256, 0.0, 0.0, 0.0," +
					"     0.0, iris_ONE_OVER_256, 0.0, 0.0," +
					"     0.0, 0.0, iris_ONE_OVER_256, 0.0," +
					"     iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_256);");
		}

		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "mat4 iris_TextureMatrix[8] = mat4[8](" +
				"gl_TextureMatrix[0]," +
				"iris_LightmapTextureMatrix," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)" +
				");\n");
	}
}
