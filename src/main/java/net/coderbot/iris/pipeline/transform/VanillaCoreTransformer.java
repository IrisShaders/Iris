package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;

public class VanillaCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			VanillaParameters parameters) {
		if (parameters.type == PatchShaderType.VERTEX) {
			root.rename("vaPosition", "iris_Position");
			root.rename("vaColor", "iris_Color");
			root.rename("vaNormal", "iris_Normal");
			root.rename("vaUV0", "iris_UV0");
			root.rename("vaUV1", "iris_UV1");
			root.rename("vaUV2", "iris_UV2");
		}

		if (parameters.inputs.hasOverlay()) {
			AttributeTransformer.patchOverlayColor(t, tree, root, parameters);
		}
	}
}
