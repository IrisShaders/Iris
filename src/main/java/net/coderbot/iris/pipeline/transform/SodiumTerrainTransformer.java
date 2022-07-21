package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;

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
		// "attribute vec3 iris_Normal; // The vertex normal\n" +
		// "uniform mat4 iris_ModelViewMatrix;\n" +
		// "uniform mat4 u_ModelViewProjectionMatrix;\n" +
		// "uniform mat4 iris_NormalMatrix;\n" +
		// "uniform vec3 u_ModelScale;\n" +
		// "uniform vec2 u_TextureScale;\n" +
		// "attribute vec4 iris_ModelOffset;\n" +
		// "vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

		tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"attribute vec3 iris_Pos;",
				"attribute vec4 iris_Color;",
				"attribute vec2 iris_TexCoord;",
				"attribute vec2 iris_LightCoord;",
				"attribute vec3 iris_Normal;",
				"uniform mat4 iris_ModelViewMatrix;",
				"uniform mat4 u_ModelViewProjectionMatrix;",
				"uniform mat4 iris_NormalMatrix;",
				"uniform vec3 u_ModelScale;",
				"uniform vec2 u_TextureScale;",
				"attribute vec4 iris_ModelOffset;",
				"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

		// transformations.define("gl_Vertex", "vec4((iris_Pos * u_ModelScale) +
		// iris_ModelOffset.xyz, 1.0)");
		

		// transformations.define("gl_MultiTexCoord0", "vec4(iris_TexCoord *
		// u_TextureScale, 0.0, 1.0)");

		// transformations.define("gl_Color", "iris_Color");

		// transformations.define("gl_ModelViewMatrix", "iris_ModelViewMatrix");

		// transformations.define("gl_ModelViewProjectionMatrix",
		// "u_ModelViewProjectionMatrix");

		// transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");

		// transformations.define("gl_NormalMatrix", "mat3(iris_NormalMatrix)");

		// transformations.define("gl_Normal", "iris_Normal");

		// transformations.define("ftransform", "iris_ftransform");

		// new
		// BuiltinUniformReplacementTransformer("iris_LightCoord").apply(transformations);
	}

	public static void transformFragment(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {

	}
}
