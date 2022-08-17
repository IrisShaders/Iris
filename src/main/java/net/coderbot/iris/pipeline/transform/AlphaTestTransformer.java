package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.blending.AlphaTest;

public class AlphaTestTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters,
			AlphaTest alpha) {
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"uniform float iris_currentAlphaTest;");
		tree.appendMain(t, "{" + alpha.toExpression(" ") + "}");
	}
}
