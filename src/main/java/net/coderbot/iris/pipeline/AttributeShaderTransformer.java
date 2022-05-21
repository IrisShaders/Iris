package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.*;

public class AttributeShaderTransformer {
	public static String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry) {
		if (source.contains("iris_")) {
			throw new IllegalStateException("Shader is attempting to exploit internal Iris code!");
		}

		StringTransformations transformations = new StringTransformations(source);

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
			if (transformations.contains("irisMain")) {
				throw new IllegalStateException("Shader already contains \"irisMain\"???");
			}

			transformations.replaceExact("main", "irisMain");
			transformations.injectLine(Transformations.InjectionPoint.END, "void main() {\n" +
					"	vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[2] * gl_MultiTexCoord2).xy);\n" +
					"	entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);\n" +
					"\n" +
					"    irisMain();\n" +
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

		return transformations.toString();
	}
}
