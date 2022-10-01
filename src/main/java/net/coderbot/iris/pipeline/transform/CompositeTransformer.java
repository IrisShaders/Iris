package net.coderbot.iris.pipeline.transform;

import java.util.stream.Stream;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;

class CompositeTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root) {
		CompositeDepthTransformer.transform(t, tree, root);

		// if using a lod texture sampler and on version 120, patch in the extension
		// #extension GL_ARB_shader_texture_lod : require
		if (tree.getVersionStatement().version.number <= 120
				&& Stream.concat(
						root.identifierIndex.getStream("texture2DLod"),
						root.identifierIndex.getStream("texture3DLod"))
						.filter(id -> id.getParent() instanceof FunctionCallExpression)
						.findAny().isPresent()) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"#extension GL_ARB_shader_texture_lod : require\n");
		}
	}
}
