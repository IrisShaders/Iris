package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.shader.ShaderType;

public class CompositeTransformer {
	private static final AutoHintedMatcher<Expression> glTextureMatrix0To8 = new AutoHintedMatcher<Expression>(
			"gl_TextureMatrix[index]", Matcher.expressionPattern) {
		{
			markClassedPredicateWildcard("index",
					pattern.getRoot().identifierIndex.getOne("index").getAncestor(ReferenceExpression.class),
					LiteralExpression.class,
					literalExpression -> {
						if (!literalExpression.isInteger()) {
							return false;
						}
						long index = literalExpression.integerValue;
						return index >= 0 && index < 8;
					});
		}
	};

	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters);
		CompositeDepthTransformer.transform(t, tree, root);

		// TODO: More solid way to handle texture matrices
		// TODO: Provide these values with uniforms

		// for (int i = 0; i < 8; i++) {
		// transformations.replaceExact("gl_TextureMatrix[" + i + "]", "mat4(1.0)");
		// transformations.replaceExact("gl_TextureMatrix [" + i + "]", "mat4(1.0)");
		// }
		root.replaceExpressionMatches(t, glTextureMatrix0To8, "mat4(1.0)");

		// TODO: Other fog things

		if (parameters.type == ShaderType.VERTEX) {
			// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
			// gl_MultiTexCoord0 vec4(UV0, 0.0, 1.0)");
			root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
					"vec4(UV0, 0.0, 1.0)");

			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
			// vec2 UV0;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "in vec2 UV0;");

			// gl_MultiTexCoord0 is the only valid input, all other inputs
			// for (int i = 1; i < 8; i++) {
			// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
			// gl_MultiTexCoord" + i + " vec4(0.0, 0.0, 0.0, 1.0)");
			// }
			root.replaceReferenceExpressions(t,
					root.identifierIndex.prefixQueryFlat("gl_MultiTexCoord")
							.filter(identifier -> !identifier.getName().equals("gl_MultiTexCoord0")),
					"vec4(0.0, 0.0, 0.0, 1.0)");
		}

		// No color attributes, the color is always solid white.
		// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
		// gl_Color vec4(1.0, 1.0, 1.0, 1.0)");
		root.replaceReferenceExpressions(t, "gl_Color", "vec4(1.0, 1.0, 1.0, 1.0)");

		if (parameters.type == ShaderType.VERTEX) {
			// https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glNormal.xml
			// The initial value of the current normal is the unit vector, (0, 0, 1).
			// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
			// gl_Normal vec3(0.0, 0.0, 1.0)");
			root.replaceReferenceExpressions(t, "gl_Normal", "vec3(0.0, 0.0, 1.0)");
		}

		// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
		// gl_NormalMatrix mat3(1.0)");
		root.replaceReferenceExpressions(t, "gl_NormalMatrix", "mat3(1.0)");

		if (parameters.type == ShaderType.VERTEX) {
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
			// vec3 Position;");
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4
			// ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "in vec3 Position;",
					"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

			// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
			// gl_Vertex vec4(Position, 1.0)");
			root.replaceReferenceExpressions(t, "gl_Vertex", "vec4(Position, 1.0)");
		}

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
		// gl_ModelViewProjectionMatrix (gl_ProjectionMatrix * gl_ModelViewMatrix)");
		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
				"(gl_ProjectionMatrix * gl_ModelViewMatrix)");

		// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
		// gl_ModelViewMatrix mat4(1.0)");
		root.replaceReferenceExpressions(t, "gl_ModelViewMatrix", "mat4(1.0)");

		// This is used to scale the quad projection matrix from (0, 1) to (-1, 1).
		// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
		// gl_ProjectionMatrix mat4(vec4(2.0, 0.0, 0.0, 0.0), vec4(0.0, 2.0, 0.0, 0.0),
		// vec4(0.0), vec4(-1.0, -1.0, 0.0, 1.0))");
		root.replaceReferenceExpressions(t, "gl_ProjectionMatrix",
				"mat4(vec4(2.0, 0.0, 0.0, 0.0), vec4(0.0, 2.0, 0.0, 0.0), vec4(0.0), vec4(-1.0, -1.0, 0.0, 1.0))");

		CommonTransformer.applyIntelHd4000Workaround(root);
	}
}
