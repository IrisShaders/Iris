package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class AttributeShaderTransformer {
	public static String patch(String source, ShaderType type) {
		if (source.contains("iris_")) {
			throw new IllegalStateException("Shader is attempting to exploit internal Iris code!");
		}

		StringTransformations transformations = new StringTransformations(source);

		//Add entity color -> overlay color attribute support.
		// TODO: We don't handle the geometry shader here.
		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform sampler2D iris_overlay;");
			transformations.replaceRegex("uniform\\s+vec4\\s+entityColor;", "");
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "varying vec4 entityColor;");
			if (transformations.contains("irisMain")) {
				throw new IllegalStateException("Shader already contains \"irisMain\"???");
			}

			// Create our own main function to wrap the existing main function, so that we can pass through the overlay color at the
			// end.
			transformations.replaceExact("main", "irisMain");
			transformations.injectLine(Transformations.InjectionPoint.END, "void main() {\n" +
					"	vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[2] * gl_MultiTexCoord2).xy);\n" +
					"	entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);\n" +
					"\n" +
					"    irisMain();\n" +
					"}");
		} else if (type == ShaderType.GEOMETRY) {
			transformations.replaceRegex("uniform\\s+vec4\\s+entityColor;", "in vec4 entityColor[];");
			transformations.replaceExact("entityColor", "entityColor[0]");
			// TODO: this is terrible and will not catch false positives!
			transformations.replaceExact("entityColor[0][];", "entityColor[];");
		} else if (type == ShaderType.FRAGMENT) {
			transformations.replaceRegex("uniform\\s+vec4\\s+entityColor;", "varying vec4 entityColor;");
		}

		return transformations.toString();
	}
}
