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
				"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

		transformShared(t, tree, root, parameters);

		root.replaceReferenceExpressions(t, "gl_Vertex",
				"vec4((iris_Pos * u_ModelScale) + iris_ModelOffset.xyz, 1.0)");
		root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
				"vec4(iris_TexCoord * u_TextureScale, 0.0, 1.0)");
		root.rename("gl_Color", "iris_Color");
		root.rename("gl_Normal", "iris_Normal");
		root.rename("ftransform", "iris_ftransform");

		replaceLightmapForSodium(t, tree, root, parameters);
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
				"uniform mat4 iris_NormalMatrix;");
		root.rename("gl_ModelViewMatrix", "iris_ModelViewMatrix");
		root.rename("gl_ModelViewProjectionMatrix", "u_ModelViewProjectionMatrix");
		root.replaceReferenceExpressions(t,
				"gl_NormalMatrix", "mat3(iris_NormalMatrix)");

		root.replaceExpressionMatches(
				t,
				glTextureMatrix0,
				"mat4(1.0)");
	}

	private static final Matcher<Expression> glTextureMatrixMultMember = new Matcher<>(
			"(gl_TextureMatrix[1] * ___coord).___suffix", Matcher.expressionPattern, "___");
	private static final Matcher<Expression> glTextureMatrixMultS = new Matcher<>(
			"(gl_TextureMatrix[1] * ___coord).s", Matcher.expressionPattern, "___");
	private static final Matcher<Expression> glTextureMatrixMult = new Matcher<>(
			"gl_TextureMatrix[1] * ___coord", Matcher.expressionPattern, "___");
	private static final Matcher<Expression> xyDivision = new Matcher<>(
			"___coord.xy / 255.0", Matcher.expressionPattern, "___");

	private static final String lightmapCoordsExpression = "iris_LightCoord";
	private static final String lightmapCoordsExpressionS = lightmapCoordsExpression + ".s";
	private static final String lightmapCoordsExpressionWrapped = "vec4(" + lightmapCoordsExpression + ", 0.0, 1.0)";

	private static final List<Expression> replaceExpressions = new ArrayList<>();
	private static final List<Expression> replaceSExpressions = new ArrayList<>();
	private static final List<Expression> replaceWrapExpressions = new ArrayList<>();

	private static void processCoord(Root root, String coord) {
		for (Identifier identifier : root.identifierIndex.get(coord)) {
			MemberAccessExpression memberAccess = identifier.getAncestor(MemberAccessExpression.class);
			if (memberAccess != null && glTextureMatrixMultMember.matchesExtract(memberAccess)) {
				String suffix = glTextureMatrixMultMember.getStringDataMatch("suffix");
				if (glTextureMatrixMultMember.getStringDataMatch("coord").equals(coord)
						&& suffix != null && ("st".equals(suffix) || "xy".equals(suffix))) {
					replaceExpressions.add(memberAccess);
					return;
				}
			}

			if (memberAccess != null
					&& glTextureMatrixMultS.matchesExtract(memberAccess)
					&& glTextureMatrixMultS.getStringDataMatch("coord").equals(coord)) {
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
					&& xyDivision.getStringDataMatch("coord").equals(coord)) {
				replaceExpressions.add(division);
				return;
			}

			MultiplicationExpression mult = identifier.getAncestor(MultiplicationExpression.class);
			if (mult != null
					&& glTextureMatrixMult.matchesExtract(mult)
					&& glTextureMatrixMult.getStringDataMatch("coord").equals(coord)) {
				replaceWrapExpressions.add(mult);
				return;
			}
		}
	}

	private static final AutoHintedMatcher<Expression> glTextureMatrix1 = new AutoHintedMatcher<>(
			"gl_TextureMatrix[1]", Matcher.expressionPattern);

	/**
	 * Replaces BuiltinUniformReplacementTransformer and does what it does but a
	 * little more general.
	 */
	private static void replaceLightmapForSodium(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
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

		root.replaceExpressionMatches(t, glTextureMatrix1, "iris_LightmapTextureMatrix");
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
