package net.coderbot.iris.pipeline.transform;

import java.util.ArrayList;
import java.util.List;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.DivisionExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.MultiplicationExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.MemberAccessExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;

/**
 * Does the sodium terrain transformations using glsl-transformer AST.
 */
class SodiumTerrainTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		switch (parameters.type) {
			// For Sodium patching, treat fragment and geometry the same
			case FRAGMENT:
			case GEOMETRY:
				transformFragment(t, tree, root, parameters);
				break;
			case VERTEX:
				transformVertex(t, tree, root, parameters);
				break;
			default:
				throw new IllegalStateException("Unexpected Sodium terrain patching shader type: " + parameters.type);
		}
	}

	private static final AutoHintedMatcher<Expression> glTextureMatrix0 = new AutoHintedMatcher<>(
			"gl_TextureMatrix[0]", Matcher.expressionPattern);

	/**
	 * Transforms vertex shaders.
	 */
	public static void transformVertex(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"attribute vec3 iris_Pos;",
				"attribute vec4 iris_Color;",
				"attribute vec2 iris_TexCoord;",
				"attribute vec2 iris_LightCoord;",
				"attribute vec3 iris_Normal;", // some are shared
				"uniform vec3 u_ModelScale;",
				"uniform vec2 u_TextureScale;",
				"attribute vec4 iris_ModelOffset;",
				"vec4 iris_LightTexCoord = vec4(iris_LightCoord, 0, 1);",
				"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

		transformShared(t, tree, root, parameters);

		root.replaceReferenceExpressions(t, "gl_Vertex",
				"vec4((iris_Pos * u_ModelScale) + iris_ModelOffset.xyz, 1.0)");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
				"vec4(iris_TexCoord * u_TextureScale, 0.0, 1.0)");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
				"iris_LightTexCoord");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord2",
				"iris_LightTexCoord");
		root.rename("gl_Color", "iris_Color");
		root.rename("gl_Normal", "iris_Normal");
		root.rename("ftransform", "iris_ftransform");
	}

	/**
	 * Transforms fragment shaders. The fragment shader does only the shared things
	 * from the vertex shader.
	 */
	public static void transformFragment(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// interestingly there is nothing that isn't shared
		transformShared(t, tree, root, parameters);
	}

	/**
	 * Does the things that transformVertex and transformFragment have in common.
	 */
	private static void transformShared(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_ModelViewMatrix;",
				"uniform mat4 u_ModelViewProjectionMatrix;",
				"uniform mat4 iris_NormalMatrix;",
				"uniform mat4 iris_LightmapTextureMatrix;");
		root.rename("gl_ModelViewMatrix", "iris_ModelViewMatrix");
		root.rename("gl_ModelViewProjectionMatrix", "u_ModelViewProjectionMatrix");
		root.replaceReferenceExpressions(t,
				"gl_NormalMatrix", "mat3(iris_NormalMatrix)");

		root.replaceExpressionMatches(
				t,
				glTextureMatrix0,
				"mat4(1.0)");
		root.replaceExpressionMatches(t, glTextureMatrix1, "iris_LightmapTextureMatrix");
	}
	private static final String lightmapCoordsExpression = "iris_LightCoord";
	private static final AutoHintedMatcher<Expression> glTextureMatrix1 = new AutoHintedMatcher<>(
			"gl_TextureMatrix[1]", Matcher.expressionPattern);
}
