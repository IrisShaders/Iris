package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.shader.ShaderType;

public class SodiumTransformer {
	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters);
		AlphaTestTransformer.transform(t, tree, root, parameters, parameters.alpha);

		// transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");
		root.replaceAllExpressionMatches(t, "gl_TextureMatrix",
				CommonTransformer.glTextureMatrix1, "mat4(1.0)");

		// transformations.define("gl_ProjectionMatrix", "iris_ProjectionMatrix");

		if (parameters.type == ShaderType.VERTEX) {
			if (parameters.inputs.hasTex()) {
				// transformations.define("gl_MultiTexCoord0", "vec4(_vert_tex_diffuse_coord,
				// 0.0, 1.0)");
			} else {
				// transformations.define("gl_MultiTexCoord0", "vec4(0.0, 0.0, 0.0, 1.0)");
			}

			if (parameters.inputs.hasLight()) {
				// new
				// BuiltinUniformReplacementTransformer("_vert_tex_light_coord").apply(transformations);
			} else {
				// transformations.define("gl_MultiTexCoord1", "vec4(0.0, 0.0, 0.0, 1.0)");
			}

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs, other
			// texture coordinates are not valid inputs.
			for (int i = 2; i < 8; i++) {
				// transformations.define("gl_MultiTexCoord" + i, " vec4(0.0, 0.0, 0.0, 1.0)");
			}
		}

		if (parameters.inputs.hasColor()) {
			// TODO: Handle the fragment shader here
			// transformations.define("gl_Color", "_vert_color");

			if (parameters.type == ShaderType.VERTEX) {
			}
		} else {
			// transformations.define("gl_Color", "vec4(1.0)");
		}

		if (parameters.type == ShaderType.VERTEX) {
			if (parameters.inputs.hasNormal()) {
				// transformations.define("gl_Normal", "iris_Normal");
				// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
				// vec3 iris_Normal;");
			} else {
				// transformations.define("gl_Normal", "vec3(0.0, 0.0, 1.0)");
			}
		}

		// TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		// transformations.define("gl_NormalMatrix", "mat3(iris_NormalMatrix)");
		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform mat4 iris_NormalMatrix;");

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
		// "#define gl_ModelViewMatrix iris_ModelViewMatrix");
		// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
		// "#define gl_ModelViewProjectionMatrix (iris_ProjectionMatrix *
		// iris_ModelViewMatrix)");

		if (parameters.type == ShaderType.VERTEX) {
			// TODO: Vaporwave-Shaderpack expects that vertex positions will be aligned to
			// chunks.

			// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
			// USE_VERTEX_COMPRESSION");
			// transformations.define("VERT_POS_SCALE", String.valueOf(positionScale));
			// transformations.define("VERT_POS_OFFSET", String.valueOf(positionOffset));
			// transformations.define("VERT_TEX_SCALE", String.valueOf(textureScale));

			// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
			// SodiumTerrainPipeline.parseSodiumImport("#import
			// <sodium:include/chunk_vertex.glsl>"));
			// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
			// SodiumTerrainPipeline.parseSodiumImport("#import
			// <sodium:include/chunk_parameters.glsl>"));

			// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
			// "// The projection matrix\n" +
			// "uniform mat4 iris_ProjectionMatrix;\n" +
			// "// The model-view matrix\n" +
			// "uniform mat4 iris_ModelViewMatrix;\n" +
			// "// The model-view-projection matrix\n" +
			// "#define iris_ModelViewProjectionMatrix iris_ProjectionMatrix *
			// iris_ModelViewMatrix\n");
			// transformations.define("gl_Vertex", "getVertexPosition()");

			// Create our own main function to wrap the existing main function, so that we
			// can run the alpha test at the
			// end.
			// transformations.replaceExact("main", "irisMain");
			// transformations.injectLine(Transformations.InjectionPoint.END, "void main()
			// {\n" +
			// " _vert_init();\n" +
			// "\n" +
			// " irisMain();\n" +
			// "}");

			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "vec4 getVertexPosition() { return vec4(_draw_translation + _vert_position,
			// 1.0); }");
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
		} else {
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "uniform mat4 iris_ModelViewMatrix;");
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "uniform mat4 iris_ProjectionMatrix;");
		}

		CommonTransformer.applyIntelHd4000Workaround(root);
	}
}
