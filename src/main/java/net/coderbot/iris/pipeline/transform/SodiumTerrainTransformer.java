package net.coderbot.iris.pipeline.transform;

import java.util.stream.Stream;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.ArrayAccessExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.DivisionExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.MultiplicationExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.MemberAccessExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTBuilder;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import io.github.douira.glsl_transformer.ast.transform.Matcher;

public class SodiumTerrainTransformer {
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

	private static final Matcher<Expression> glTextureMatrix0 = new Matcher<>(
			"gl_TextureMatrix[0]", GLSLParser::expression, ASTBuilder::visitExpression);

	public static void transformVertex(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "attribute vec3 iris_Pos; // The position of the vertex\n" +
		// "attribute vec4 iris_Color; // The color of the vertex\n" +
		// "attribute vec2 iris_TexCoord; // The block texture coordinate of the
		// vertex\n" +
		// "attribute vec2 iris_LightCoord; // The light map texture coordinate of the
		// vertex\n" +
		// "attribute vec3 iris_Normal; // The vertex normal\n" + (some removed since
		// shared)
		// "uniform vec3 u_ModelScale;\n" +
		// "uniform vec2 u_TextureScale;\n" +
		// "attribute vec4 iris_ModelOffset;\n" +
		// "vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
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

		// transformations.define("gl_Vertex", "vec4((iris_Pos * u_ModelScale) +
		// iris_ModelOffset.xyz, 1.0)");
		root.replaceAllReferenceExpressions(transformer, "gl_Vertex",
				"vec4((iris_Pos * u_ModelScale) + iris_ModelOffset.xyz, 1.0)");

		// transformations.define("gl_MultiTexCoord0", "vec4(iris_TexCoord *
		// u_TextureScale, 0.0, 1.0)");
		root.replaceAllReferenceExpressions(transformer, "gl_MultiTexCoord0",
				"vec4(iris_TexCoord * u_TextureScale, 0.0, 1.0)");

		// transformations.define("gl_Color", "iris_Color");
		root.renameAll("gl_Color", "iris_Color");

		// transformations.define("gl_Normal", "iris_Normal");
		root.renameAll("gl_Normal", "iris_Normal");

		// transformations.define("ftransform", "iris_ftransform");
		root.renameAll("ftransform", "iris_ftransform");

		// new
		// BuiltinUniformReplacementTransformer("iris_LightCoord").apply(transformations);
		replaceLightmapForSodium(transformer, tree, root, parameters);
	}

	public static void transformFragment(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// interestingly there is nothing that isn't shared
		transformShared(transformer, tree, root, parameters);
	}

	// does the things that transformVertex and transformFragment have in common
	private static void transformShared(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform mat4 iris_ModelViewMatrix;\n" +
		// "uniform mat4 u_ModelViewProjectionMatrix;\n" +
		// "uniform mat4 iris_NormalMatrix;\n");
		tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_ModelViewMatrix;",
				"uniform mat4 u_ModelViewProjectionMatrix;",
				"uniform mat4 iris_NormalMatrix;");

		// transformations.define("gl_ModelViewMatrix", "iris_ModelViewMatrix");
		root.renameAll("gl_ModelViewMatrix", "iris_ModelViewMatrix");

		// transformations.define("gl_ModelViewProjectionMatrix",
		// "u_ModelViewProjectionMatrix");
		root.renameAll("gl_ModelViewProjectionMatrix", "u_ModelViewProjectionMatrix");

		// transformations.define("gl_NormalMatrix", "mat3(iris_NormalMatrix)");
		root.replaceAllReferenceExpressions(transformer,
				"gl_NormalMatrix", "mat3(iris_NormalMatrix)");

		// transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");
		root.replaceAllExpressions(
				transformer,
				root.identifierIndex.getStream("gl_TextureMatrix")
						.map(identifier -> identifier.getAncestor(ArrayAccessExpression.class))
						.distinct()
						.filter(glTextureMatrix0::matches),
				"mat4(1.0)");
	}

	private static final Matcher<Expression> glTextureMatrix1 = new Matcher<>(
			"gl_TextureMatrix[1]", GLSLParser::expression, ASTBuilder::visitExpression);
	private static final String lightmapCoordsExpression = "iris_LightCoord";
	private static final String lightmapCoordsExpressionS = lightmapCoordsExpression + ".s";
	private static final String lightmapCoordsExpressionWrapped = "vec4(" + lightmapCoordsExpression + ", 0.0, 1.0)";

	// replaces BuiltinUniformReplacementTransformer
	private static void replaceLightmapForSodium(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		replaceMultiTexCoord(transformer, root, "gl_MultiTexCoord1");
		replaceMultiTexCoord(transformer, root, "gl_MultiTexCoord2");

		// transformations.replaceExact("gl_TextureMatrix[1]",
		// "iris_LightmapTextureMatrix");
		root.replaceAllExpressions(
				transformer,
				root.identifierIndex.getStream("gl_TextureMatrix")
						.map(identifier -> identifier.getAncestor(ArrayAccessExpression.class))
						.distinct()
						.filter(glTextureMatrix1::matches),
				"iris_LightmapTextureMatrix");

		// transformations.replaceExact(
		// "gl_MultiTexCoord1",
		// "vec4(" + NORMALIZED_PLACEHOLDER + " * 255.0, 0.0, 1.0)"
		// );
		root.replaceAllReferenceExpressions(transformer, "gl_MultiTexCoord1", "vec4("
				+ lightmapCoordsExpression + " * 255.0, 0.0, 1.0)");

		// transformations.replaceExact(
		// "gl_MultiTexCoord2",
		// "vec4(" + NORMALIZED_PLACEHOLDER + " * 255.0, 0.0, 1.0)"
		// );
		root.replaceAllReferenceExpressions(transformer, "gl_MultiTexCoord2", "vec4("
				+ lightmapCoordsExpression + " * 255.0, 0.0, 1.0)");

		if (root.identifierIndex.has("iris_LightmapTextureMatrix")) {
			// transformations.injectLine(
			// Transformations.InjectionPoint.BEFORE_CODE,
			// "uniform mat4 iris_LightmapTextureMatrix;"
			// );
			tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform mat4 iris_LightmapTextureMatrix;");
		}
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

	private static void replaceMultiTexCoord(
			ASTTransformer<?> transformer,
			Root root,
			String coordName) {
		// replacement with basic lightmap coords
		root.replaceAllExpressions(transformer, Stream.concat(
				root.identifierIndex.getStream(coordName)
						.map(identifier -> identifier.getAncestor(MemberAccessExpression.class))
						.filter(expression -> {
							if (expression == null || !glTextureMatrixMultMember.matchesExtract(expression)) {
								return false;
							}
							String suffix = glTextureMatrixMultMember.getStringDataMatch("suffix");
							return glTextureMatrixMultMember.getStringDataMatch("coord") == coordName
									&& suffix != null && ("st".equals(suffix) || "xy".equals(suffix));
						}),
				root.identifierIndex.getStream(coordName)
						.map(identifier -> identifier.getAncestor(DivisionExpression.class))
						.filter(expression -> expression != null
								&& xyDivision.matchesExtract(expression)
								&& xyDivision.getStringDataMatch("coord") == coordName)),
				lightmapCoordsExpression);

		// replacement with lightmap coord .s
		root.replaceAllExpressions(transformer,
				root.identifierIndex.getStream(coordName)
						.map(identifier -> identifier.getAncestor(MemberAccessExpression.class))
						.filter(expression -> expression != null
								&& glTextureMatrixMultS.matchesExtract(expression)
								&& glTextureMatrixMultS.getStringDataMatch("coord") == coordName),
				lightmapCoordsExpressionS);

		// replacement with wrapped lightmap coord
		root.replaceAllExpressions(transformer,
				root.identifierIndex.getStream(coordName)
						.map(identifier -> identifier.getAncestor(MultiplicationExpression.class))
						.filter(expression -> expression != null
								&& glTextureMatrixMult.matchesExtract(expression)
								&& glTextureMatrixMult.getStringDataMatch("coord") == coordName),
				lightmapCoordsExpressionWrapped);
	}
}
