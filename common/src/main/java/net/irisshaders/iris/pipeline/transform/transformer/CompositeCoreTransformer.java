package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;

public class CompositeCoreTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		Parameters parameters) {
		CompositeDepthTransformer.transform(t, tree, root);

		if (parameters.type == PatchShaderType.VERTEX) {
			root.rename("vaPosition", "Position");
			root.rename("vaUV0", "UV0");
			root.replaceReferenceExpressions(t, "modelViewMatrix", "mat4(1.0)");
			// This is used to scale the quad projection matrix from (0, 1) to (-1, 1).
			root.replaceReferenceExpressions(t, "projectionMatrix",
				"mat4(vec4(2.0, 0.0, 0.0, 0.0), vec4(0.0, 2.0, 0.0, 0.0), vec4(0.0), vec4(-1.0, -1.0, 0.0, 1.0))");
			root.replaceReferenceExpressions(t, "modelViewMatrixInverse", "mat4(1.0)");
			root.replaceReferenceExpressions(t, "projectionMatrixInverse",
				"inverse(mat4(vec4(2.0, 0.0, 0.0, 0.0), vec4(0.0, 2.0, 0.0, 0.0), vec4(0.0), vec4(-1.0, -1.0, 0.0, 1.0)))");
			root.replaceReferenceExpressions(t, "textureMatrix", "mat4(1.0)");
		}
	}
}
