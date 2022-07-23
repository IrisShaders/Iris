package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTBuilder;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import io.github.douira.glsl_transformer.ast.transform.Matcher;

class CompositeDepthTransformer {
	private static final Matcher<ExternalDeclaration> uniformFloatCenterDepthSmooth = new Matcher<>(
			"uniform float centerDepthSmooth;",
			GLSLParser::externalDeclaration,
			ASTBuilder::visitExternalDeclaration);

	private static boolean found;

	public static void transform(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root) {
		// replace original declaration
		found = false;
		root.processAll(
				root.identifierIndex.getStream("centerDepthSmooth")
						.map(identifier -> identifier.getAncestor(DeclarationExternalDeclaration.class))
						.distinct()
						.filter(uniformFloatCenterDepthSmooth::matches),
				node -> {
					found = true;
					node.detachAndDelete();
				});
		if (found) {
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform sampler2D iris_centerDepthSmooth;");

			// if centerDepthSmooth is not declared as a uniform, we don't make it available
			root.replaceAllReferenceExpressions(transformer, "centerDepthSmooth",
					"texture2D(iris_centerDepthSmooth, vec2(0.5)).r");
		}
	}
}
