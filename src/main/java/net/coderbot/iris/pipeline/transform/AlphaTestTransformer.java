package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.blending.AlphaTest;

public class AlphaTestTransformer {
	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			Parameters parameters,
			AlphaTest alpha) {
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"uniform float iris_currentAlphaTest;");

		// Create our own main function to wrap the existing main function, so that we
		// can run the alpha test at the
		// end.
		root.rename("main", "irisMain");
		tree.parseAndInjectNode(t, ASTInjectionPoint.END, "void main() {" +
				" irisMain();" + alpha.toExpression(" ") + "}");
	}
}
