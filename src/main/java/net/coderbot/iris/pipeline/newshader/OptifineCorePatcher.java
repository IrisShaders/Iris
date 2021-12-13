package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class OptifineCorePatcher {
	public static String patch(String source, AlphaTest alpha) {
		if (source.contains("moj_import")) {
			throw new IllegalStateException("Iris shader programs may not use moj_import directives.");
		}

		StringTransformations transformations = new StringTransformations(source);

		transformations.replaceExact("vaPosition", "iris_Position");
		transformations.replaceExact("vaColor", "iris_Color");
		transformations.replaceExact("vaUV", "iris_UV");
		transformations.replaceExact("vaNormal", "iris_Normal");
		transformations.replaceExact("chunkOffset", "iris_chunkOffset");

		transformations.injectLine(Transformations.InjectionPoint.DEFINES, "const mat4 TEXTURE_MATRIX_2 = mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0));");
		transformations.replaceExact("uniform mat4 modelViewMatrix;", "");
		transformations.replaceExact("uniform mat4 projectionMatrix;", "");
		transformations.replaceExact("uniform mat4 modelViewMatrixInverse;", "");
		transformations.replaceExact("uniform mat4 projectionMatrixInverse;", "");
		transformations.replaceExact("uniform float alphaTestRef;", "");
		transformations.define("alphaTestRef", String.valueOf(alpha.getReference()));
		transformations.define("modelViewMatrixInverse", "inverse(iris_ModelViewMat))");
		transformations.define("projectionMatrixInverse", "inverse(iris_ProjMat))");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform mat4 iris_ModelViewMat;");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform mat4 iris_ProjMat;");
		transformations.define("modelViewMatrix", "iris_ModelViewMat");
		transformations.define("projectionMatrix", "iris_ProjMat");

		return transformations.toString();
	}
}
