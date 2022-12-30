package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.HintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;

class CompositeDepthTransformer {
	private static final HintedMatcher<ExternalDeclaration> uniformFloatCenterDepthSmooth = new HintedMatcher<>(
			"uniform float name;", Matcher.externalDeclarationPattern, "centerDepthSmooth") {
		{
			markClassWildcard("name*",
					pattern.getRoot().identifierIndex.getUnique("name").getAncestor(DeclarationMember.class));
		}
	};

	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root) {
		// replace original declaration
		if (root.processMatches(t, uniformFloatCenterDepthSmooth, (match) -> {
			TypeAndInitDeclaration declaration = ((TypeAndInitDeclaration) ((DeclarationExternalDeclaration) match)
					.getDeclaration());
			DeclarationMember memberToDelete = null;
			for (DeclarationMember member : declaration.getMembers()) {
				if (member.getName().getName().equals("centerDepthSmooth")) {
					memberToDelete = member;
					break;
				}
			}
			if (memberToDelete != null) {
				if (declaration.getMembers().size() == 1) {
					match.detachAndDelete();
				} else {
					memberToDelete.detachAndDelete();
				}
			}
		})) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform sampler2D iris_centerDepthSmooth;");

			// if centerDepthSmooth is not declared as a uniform, we don't make it available
			root.replaceReferenceExpressions(t, "centerDepthSmooth",
					"texture(iris_centerDepthSmooth, vec2(0.5)).r");
		}
	}
}
