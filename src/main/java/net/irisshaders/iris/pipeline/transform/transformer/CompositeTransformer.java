package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.parser.ParseShape;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;

public class CompositeTransformer {
	private static final AutoHintedMatcher<Expression> glTextureMatrix0To7 = new AutoHintedMatcher<>(
		"gl_TextureMatrix[index]", ParseShape.EXPRESSION) {
		{
			markClassedPredicateWildcard("index",
				pattern.getRoot().identifierIndex.getOne("index").getAncestor(ReferenceExpression.class),
				LiteralExpression.class,
				literalExpression -> {
					if (!literalExpression.isInteger()) {
						return false;
					}
					long index = literalExpression.getInteger();
					return index >= 0 && index < 8;
				});
		}
	};

	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		Parameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters, true);
		CompositeDepthTransformer.transform(t, tree, root);

		// TODO: More solid way to handle texture matrices
		// TODO: Provide these values with uniforms

		root.replaceExpressionMatches(t, glTextureMatrix0To7, "mat4(1.0)");

		// TODO: Other fog things

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
				"vec4(UV0, 0.0, 1.0)");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec2 UV0;");
			CommonTransformer.replaceGlMultiTexCoordBounded(t, root, 1, 7);
		}

		// No color attributes, the color is always solid white.
		root.replaceReferenceExpressions(t, "gl_Color", "vec4(1.0, 1.0, 1.0, 1.0)");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glNormal.xml
			// The initial value of the current normal is the unit vector, (0, 0, 1).
			root.replaceReferenceExpressions(t, "gl_Normal", "vec3(0.0, 0.0, 1.0)");
		}

		root.replaceReferenceExpressions(t, "gl_NormalMatrix", "mat3(1.0)");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec3 Position;");
			if (root.identifierIndex.has("ftransform")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
			}
			root.replaceReferenceExpressions(t, "gl_Vertex", "vec4(Position, 1.0)");
		}

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
			"(gl_ProjectionMatrix * gl_ModelViewMatrix)");
		root.replaceReferenceExpressions(t, "gl_ModelViewMatrix", "mat4(1.0)");

		// This is used to scale the quad projection matrix from (0, 1) to (-1, 1).
		root.replaceReferenceExpressions(t, "gl_ProjectionMatrix",
			"mat4(vec4(2.0, 0.0, 0.0, 0.0), vec4(0.0, 2.0, 0.0, 0.0), vec4(0.0), vec4(-1.0, -1.0, 0.0, 1.0))");

		CommonTransformer.applyIntelHd4000Workaround(root);
	}
}
