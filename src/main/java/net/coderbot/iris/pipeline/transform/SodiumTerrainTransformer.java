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
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;

/**
 * Does the sodium terrain transformations using glsl-transformer AST.
 */
class SodiumTerrainTransformer {
	private static final Matcher<Expression> glTextureMatrixMultMember = new Matcher<>(
			"(gl_TextureMatrix[1] * ___coord).___suffix", Matcher.expressionPattern, "___");
	private static final Matcher<Expression> glTextureMatrixMultS = new Matcher<>(
			"(gl_TextureMatrix[1] * ___coord).s", Matcher.expressionPattern, "___");
	private static final Matcher<Expression> glTextureMatrixMult = new Matcher<>(
			"gl_TextureMatrix[1] * ___coord", Matcher.expressionPattern, "___");
	private static final Matcher<Expression> xyDivision = new Matcher<>(
			"___coord.xy / 255.0", Matcher.expressionPattern, "___");

	private static final List<Expression> replaceExpressions = new ArrayList<>();
	private static final List<Expression> replaceSExpressions = new ArrayList<>();
	private static final List<Expression> replaceWrapExpressions = new ArrayList<>();

	private static void processCoord(Root root, String coord) {
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

	/**
	 * Replaces BuiltinUniformReplacementTransformer and does what it does but a
	 * little more general.
	 */
	public static void replaceLightmapForSodium(
			final String lightmapCoordsExpression,
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root) {
		final String lightmapCoordsExpressionS = lightmapCoordsExpression + ".s";
		final String lightmapCoordsExpressionWrapped = "vec4(" + lightmapCoordsExpression + ", 0.0, 1.0)";

		replaceExpressions.clear();
		replaceSExpressions.clear();
		replaceWrapExpressions.clear();

		// gl_MultiTexCoord1 and gl_MultiTexCoord2 are both aliases of the lightmap
		// coords
		processCoord(root, "gl_MultiTexCoord1");
		processCoord(root, "gl_MultiTexCoord2");

		Root.replaceExpressionsConcurrent(t, replaceExpressions, lightmapCoordsExpression);
		Root.replaceExpressionsConcurrent(t, replaceSExpressions, lightmapCoordsExpressionS);
		Root.replaceExpressionsConcurrent(t, replaceWrapExpressions, lightmapCoordsExpressionWrapped);

		replaceExpressions.clear();
		replaceSExpressions.clear();
		replaceWrapExpressions.clear();

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord1", "vec4("
				+ lightmapCoordsExpression + " * 255.0, 0.0, 1.0)");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord2", "vec4("
				+ lightmapCoordsExpression + " * 255.0, 0.0, 1.0)");

		// If there are references to the fallback lightmap texture matrix, then make it
		// available to the shader program.
		if (root.identifierIndex.has("iris_LightmapTextureMatrix")) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform mat4 iris_LightmapTextureMatrix;");
		}
	}
}
