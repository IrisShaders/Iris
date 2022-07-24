package net.coderbot.iris.pipeline.transform;

import java.util.ArrayList;
import java.util.List;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.ArrayAccessExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.DivisionExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.MultiplicationExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.MemberAccessExpression;
import io.github.douira.glsl_transformer.ast.query.HintedMatcher;
import io.github.douira.glsl_transformer.ast.query.Matcher;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTBuilder;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;

/**
 * Does the sodium terrain transformations using glsl-transformer AST.
 */
class SodiumTerrainTransformer {
	public static void transform(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		switch (parameters.type) {
			case FRAGMENT:
				transformFragment(transformer, tree, root, parameters);
				break;
			case VERTEX:
				transformVertex(transformer, tree, root, parameters);
				break;
			default:
				throw new IllegalStateException("Unexpected Sodium terrain patching shader type: " + parameters.type);
		}
	}

	private static final HintedMatcher<Expression> glTextureMatrix0 = new HintedMatcher<>(
			"gl_TextureMatrix[0]", GLSLParser::expression, ASTBuilder::visitExpression, "gl_TextureMatrix");

	/**
	 * Transforms vertex shaders.
	 */
	public static void transformVertex(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"attribute vec3 iris_Pos;",
				"attribute vec4 iris_Color;",
				"attribute vec2 iris_TexCoord;",
				"attribute vec2 iris_LightCoord;",
				"attribute vec3 iris_Normal;", // some are shared
				"uniform vec3 u_ModelScale;",
				"uniform vec2 u_TextureScale;",
				"attribute vec4 iris_ModelOffset;",
				"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

		transformShared(transformer, tree, root, parameters);

		root.replaceReferenceExpressions(transformer, "gl_Vertex",
				"vec4((iris_Pos * u_ModelScale) + iris_ModelOffset.xyz, 1.0)");
		root.replaceReferenceExpressions(transformer, "gl_MultiTexCoord0",
				"vec4(iris_TexCoord * u_TextureScale, 0.0, 1.0)");
		root.rename("gl_Color", "iris_Color");
		root.rename("gl_Normal", "iris_Normal");
		root.rename("ftransform", "iris_ftransform");

		replaceLightmapForSodium(transformer, tree, root, parameters);
	}

	/**
	 * Transforms fragment shaders. The fragment shader does only the shared things
	 * from the vertex shader.
	 */
	public static void transformFragment(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// interestingly there is nothing that isn't shared
		transformShared(transformer, tree, root, parameters);
	}

	/**
	 * Does the things that transformVertex and transformFragment have in common.
	 */
	private static void transformShared(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_ModelViewMatrix;",
				"uniform mat4 u_ModelViewProjectionMatrix;",
				"uniform mat4 iris_NormalMatrix;");
		root.rename("gl_ModelViewMatrix", "iris_ModelViewMatrix");
		root.rename("gl_ModelViewProjectionMatrix", "u_ModelViewProjectionMatrix");
		root.replaceReferenceExpressions(transformer,
				"gl_NormalMatrix", "mat3(iris_NormalMatrix)");

		root.replaceExpressionMatches(
				transformer,
				glTextureMatrix0,
				"mat4(1.0)");
	}

	private static final Matcher<Expression> glTextureMatrixMultMember = new Matcher<>(
			"(gl_TextureMatrix[1] * ___coord).___suffix",
			GLSLParser::expression, ASTBuilder::visitExpression, "___");
	private static final Matcher<Expression> glTextureMatrixMultS = new Matcher<>(
			"(gl_TextureMatrix[1] * ___coord).s",
			GLSLParser::expression, ASTBuilder::visitExpression, "___");
	private static final Matcher<Expression> glTextureMatrixMult = new Matcher<>(
			"gl_TextureMatrix[1] * ___coord",
			GLSLParser::expression, ASTBuilder::visitExpression, "___");
	private static final Matcher<Expression> xyDivision = new Matcher<>(
			"___coord.xy / 255.0",
			GLSLParser::expression, ASTBuilder::visitExpression, "___");

	private static final String lightmapCoordsExpression = "iris_LightCoord";
	private static final String lightmapCoordsExpressionS = lightmapCoordsExpression + ".s";
	private static final String lightmapCoordsExpressionWrapped = "vec4(" + lightmapCoordsExpression + ", 0.0, 1.0)";

	private static final List<Expression> replaceExpressions = new ArrayList<>();
	private static final List<Expression> replaceSExpressions = new ArrayList<>();
	private static final List<Expression> replaceWrapExpressions = new ArrayList<>();

	private static void processCoord(ASTTransformer<?> transformer, Root root, String coord) {
		for (Identifier identifier : root.identifierIndex.get(coord)) {
			MemberAccessExpression memberAccess = identifier.getAncestor(MemberAccessExpression.class);
			if (memberAccess != null && glTextureMatrixMultMember.matchesExtract(memberAccess)) {
				String suffix = glTextureMatrixMultMember.getStringDataMatch("suffix");
				if (glTextureMatrixMultMember.getStringDataMatch("coord") == coord
						&& suffix != null && ("st".equals(suffix) || "xy".equals(suffix))) {
					replaceExpressions.add(memberAccess);
					return;
				}
			}

			if (memberAccess != null
					&& glTextureMatrixMultS.matchesExtract(memberAccess)
					&& glTextureMatrixMultS.getStringDataMatch("coord") == coord) {
				replaceSExpressions.add(memberAccess);
				return;
			}
			// NB: Technically this isn't a correct transformation (it changes the values
			// slightly), however the shader code being replaced isn't correct to begin with
			// since it doesn't properly apply the centering / scaling transformation like
			// gl_TextureMatrix[1] would. Therefore, I think this is acceptable. This code
			// shows up in Sildur's shaderpacks.
			DivisionExpression division = identifier.getAncestor(DivisionExpression.class);
			if (division != null
					&& xyDivision.matchesExtract(division)
					&& xyDivision.getStringDataMatch("coord") == coord) {
				replaceExpressions.add(division);
				return;
			}

			MultiplicationExpression mult = identifier.getAncestor(MultiplicationExpression.class);
			if (mult != null
					&& glTextureMatrixMult.matchesExtract(mult)
					&& glTextureMatrixMult.getStringDataMatch("coord") == coord) {
				replaceWrapExpressions.add(mult);
				return;
			}
		}
	}

	private static final HintedMatcher<Expression> glTextureMatrix1 = new HintedMatcher<>(
			"gl_TextureMatrix[1]", GLSLParser::expression, ASTBuilder::visitExpression, "gl_TextureMatrix");

	/**
	 * Replaces BuiltinUniformReplacementTransformer and does what it does but a
	 * little more general.
	 */
	private static void replaceLightmapForSodium(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		replaceExpressions.clear();
		replaceSExpressions.clear();
		replaceWrapExpressions.clear();

		// gl_MultiTexCoord1 and gl_MultiTexCoord2 are both aliases of the lightmap
		// coords
		processCoord(transformer, root, "gl_MultiTexCoord1");
		processCoord(transformer, root, "gl_MultiTexCoord2");

		Root.replaceExpressionsConcurrent(transformer, replaceExpressions, lightmapCoordsExpression);
		Root.replaceExpressionsConcurrent(transformer, replaceSExpressions, lightmapCoordsExpressionS);
		Root.replaceExpressionsConcurrent(transformer, replaceWrapExpressions, lightmapCoordsExpressionWrapped);

		replaceExpressions.clear();
		replaceSExpressions.clear();
		replaceWrapExpressions.clear();

		root.replaceExpressionMatches(transformer, glTextureMatrix1, "iris_LightmapTextureMatrix");
		root.replaceReferenceExpressions(transformer, "gl_MultiTexCoord1", "vec4("
				+ lightmapCoordsExpression + " * 255.0, 0.0, 1.0)");
		root.replaceReferenceExpressions(transformer, "gl_MultiTexCoord2", "vec4("
				+ lightmapCoordsExpression + " * 255.0, 0.0, 1.0)");

		// If there are references to the fallback lightmap texture matrix, then make it
		// available to the shader program.
		if (root.identifierIndex.has("iris_LightmapTextureMatrix")) {
			tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform mat4 iris_LightmapTextureMatrix;");
		}
	}
}
