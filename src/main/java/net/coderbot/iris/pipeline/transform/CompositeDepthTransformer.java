package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;

class CompositeDepthTransformer {
	private static final AutoHintedMatcher<ExternalDeclaration> uniformFloatCenterDepthSmooth = new AutoHintedMatcher<>(
			"uniform float centerDepthSmooth;", Matcher.externalDeclarationPattern);

	private static boolean found;

	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root) {
		// replace original declaration
		found = false;
		root.processMatches(t, uniformFloatCenterDepthSmooth,
				node -> {
					found = true;
					node.detachAndDelete();
				});
		if (found) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform sampler2D iris_centerDepthSmooth;");

			// if centerDepthSmooth is not declared as a uniform, we don't make it available
			root.replaceReferenceExpressions(t, "centerDepthSmooth",
					"texture(iris_centerDepthSmooth, vec2(0.5)).r");
		}
	}
}
