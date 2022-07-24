package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;

public class AlphaTestTransformer {
	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			Parameters parameters,
			AlphaTest alpha) {
		if (parameters.type == ShaderType.FRAGMENT) {
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "uniform float iris_currentAlphaTest;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"uniform float iris_currentAlphaTest;");

			// Create our own main function to wrap the existing main function, so that we
			// can run the alpha test at the
			// end.
			// transformations.replaceExact("main", "irisMain");
			root.renameAll("main", "irisMain");

			// transformations.injectLine(Transformations.InjectionPoint.END, "void main()
			// {\n" +
			// " irisMain();\n" +
			// alpha.toExpression(" ") +
			// "}");
			tree.parseAndInjectNode(t, ASTInjectionPoint.END, "void irisMain() {" +
					" irisMain();" + alpha.toExpression(" ") + "}");
		}
	}
}
