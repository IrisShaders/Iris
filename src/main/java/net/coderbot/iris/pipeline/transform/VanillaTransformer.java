package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.AlphaTests;

/**
 * TODO TRANSFORM: the attribute -> in etc. replacements done in common have to
 * be applied after all the other things have happened since they might
 * introduce new attribute usages. (or replace them manually)
 */
public class VanillaTransformer {
	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			VanillaParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters);

		if (parameters.inputs.hasOverlay()) {
			AttributeTransformer.patchOverlayColor(t, tree, root, parameters);
		}

		// transformations.define("gl_ProjectionMatrix", "iris_ProjMat");
		root.renameAll("gl_ProjectionMatrix", "iris_ProjMat");
		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform mat4 iris_ProjMat;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"uniform mat4 iris_ProjMat;");

		if (parameters.type == ShaderType.VERTEX) {
			if (parameters.inputs.hasTex()) {
				// transformations.define("gl_MultiTexCoord0", "vec4(iris_UV0, 0.0, 1.0)");
				root.replaceAllReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(iris_UV0, 0.0, 1.0)");
				// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
				// vec2 iris_UV0;");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"in vec2 iris_UV0;");
			} else {
				// transformations.define("gl_MultiTexCoord0", "vec4(0.5, 0.5, 0.0, 1.0)");
				root.replaceAllReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(0.5, 0.5, 0.0, 1.0)");
			}

			if (parameters.inputs.hasLight()) {
				// transformations.define("gl_MultiTexCoord1", "vec4(iris_UV2, 0.0, 1.0)");
				root.replaceAllReferenceExpressions(t, "gl_MultiTexCoord1",
						"vec4(iris_UV2, 0.0, 1.0)");
				// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
				// ivec2 iris_UV2;");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"in ivec2 iris_UV2;");
			} else {
				// transformations.define("gl_MultiTexCoord1", "vec4(240.0, 240.0, 0.0, 1.0)");
				root.replaceAllReferenceExpressions(t, "gl_MultiTexCoord1",
						"vec4(240.0, 240.0, 0.0, 1.0)");
			}

			// Alias of gl_MultiTexCoord1 on 1.15+ for OptiFine
			// See https://github.com/IrisShaders/Iris/issues/1149
			// transformations.define("gl_MultiTexCoord2", "gl_MultiTexCoord1");
			root.renameAll("gl_MultiTexCoord2", "gl_MultiTexCoord1");

			AttributeTransformer.patchMultiTexCoord3(t, tree, root, parameters);

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs (with
			// gl_MultiTexCoord2 and gl_MultiTexCoord3 as aliases), other texture
			// coordinates are not valid inputs.
			// for (int i = 4; i < 8; i++) {
			// transformations.define("gl_MultiTexCoord" + i, " vec4(0.0, 0.0, 0.0, 1.0)");
			// }
			root.replaceAllReferenceExpressions(t,
					root.identifierIndex.prefixQueryFlat("gl_MultiTexCoord")
							.filter(id -> {
								int index = Integer.parseInt(id.getName().substring("gl_MultiTexCoord".length()));
								return index >= 4 && index < 8;
							}),
					"vec4(0.0, 0.0, 0.0, 1.0)");
		}

		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform vec4 iris_ColorModulator;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"uniform vec4 iris_ColorModulator;");

		if (parameters.inputs.hasColor()) {
			// TODO: Handle the fragment / geometry shader here
			if (parameters.alpha == AlphaTests.VERTEX_ALPHA) {
				// iris_ColorModulator.a should be applied regardless of the alpha test state.
				// transformations.define("gl_Color", "vec4((iris_Color *
				// iris_ColorModulator).rgb, iris_ColorModulator.a)");
				root.replaceAllReferenceExpressions(t, "gl_Color",
						"vec4((iris_Color * iris_ColorModulator).rgb, iris_ColorModulator.a)");
			} else {
				// transformations.define("gl_Color", "(iris_Color * iris_ColorModulator)");
				root.replaceAllReferenceExpressions(t, "gl_Color",
						"(iris_Color * iris_ColorModulator)");
			}

			if (parameters.type == ShaderType.VERTEX) {
				// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
				// vec4 iris_Color;");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"in vec4 iris_Color;");
			}
		} else {
			// iris_ColorModulator should be applied regardless of the alpha test state.
			// transformations.define("gl_Color", "iris_ColorModulator");
			root.renameAll("gl_Color", "iris_ColorModulator");
		}

		if (parameters.type == ShaderType.VERTEX) {
			if (parameters.inputs.hasNormal()) {
				if (!parameters.inputs.isNewLines()) {
					// transformations.define("gl_Normal", "iris_Normal");
					root.renameAll("gl_Normal", "iris_Normal");
				} else {
					// transformations.define("gl_Normal", "vec3(0.0, 0.0, 1.0)");
					root.replaceAllReferenceExpressions(t, "gl_Normal",
							"vec3(0.0, 0.0, 1.0)");
				}

				// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
				// vec3 iris_Normal;");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"in vec3 iris_Normal;");
			} else {
				// transformations.define("gl_Normal", "vec3(0.0, 0.0, 1.0)");
				root.replaceAllReferenceExpressions(t, "gl_Normal",
						"vec3(0.0, 0.0, 1.0)");
			}
		}

		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform mat4 iris_LightmapTextureMatrix;");
		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform mat4 iris_TextureMat;");
		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform mat4 iris_ModelViewMat;");
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"uniform mat4 iris_LightmapTextureMatrix;",
				"uniform mat4 iris_TextureMat;",
				"uniform mat4 iris_ModelViewMat;");

		// TODO: More solid way to handle texture matrices
		// transformations.replaceExact("gl_TextureMatrix[0]", "iris_TextureMat");
		// transformations.replaceExact("gl_TextureMatrix[1]",
		// "iris_LightmapTextureMatrix");

		// TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		// transformations.define("gl_NormalMatrix",
		// "mat3(transpose(inverse(gl_ModelViewMatrix)))");

		if (parameters.hasChunkOffset) {
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "uniform vec3 iris_ChunkOffset;");
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "mat4 _iris_internal_translate(vec3 offset) {\n" +
			// " // NB: Column-major order\n" +
			// " return mat4(1.0, 0.0, 0.0, 0.0,\n" +
			// " 0.0, 1.0, 0.0, 0.0,\n" +
			// " 0.0, 0.0, 1.0, 0.0,\n" +
			// " offset.x, offset.y, offset.z, 1.0);\n" +
			// "}");
			// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
			// "#define gl_ModelViewMatrix (iris_ModelViewMat *
			// _iris_internal_translate(iris_ChunkOffset))");
		} else if (parameters.inputs.isNewLines()) {
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "const float iris_VIEW_SHRINK = 1.0 - (1.0 / 256.0);\n" +
			// "const mat4 iris_VIEW_SCALE = mat4(\n" +
			// " iris_VIEW_SHRINK, 0.0, 0.0, 0.0,\n" +
			// " 0.0, iris_VIEW_SHRINK, 0.0, 0.0,\n" +
			// " 0.0, 0.0, iris_VIEW_SHRINK, 0.0,\n" +
			// " 0.0, 0.0, 0.0, 1.0\n" +
			// ");");
			// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
			// "#define gl_ModelViewMatrix (iris_VIEW_SCALE * iris_ModelViewMat)");
		} else {
			// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
			// "#define gl_ModelViewMatrix iris_ModelViewMat");
		}

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
		// "#define gl_ModelViewProjectionMatrix (gl_ProjectionMatrix *
		// gl_ModelViewMatrix)");

		if (parameters.type == ShaderType.VERTEX) {
			if (parameters.inputs.isNewLines()) {
				// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec3
				// iris_vertex_offset = vec3(0.0);");
				// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
				// "#define gl_Vertex vec4(iris_Position + iris_vertex_offset, 1.0)");

				// Create our own main function to wrap the existing main function, so that we
				// can do our line shenanagains.
				// transformations.replaceExact("main", "irisMain");

				// transformations.injectLine(Transformations.InjectionPoint.END,
				// "uniform vec2 iris_ScreenSize;\n" +
				// "uniform float iris_LineWidth;\n" +
				// "\n" +
				// "// Widen the line into a rectangle of appropriate width\n" +
				// "// Isolated from Minecraft's rendertype_lines.vsh\n" +
				// "// Both arguments are positions in NDC space (the same space as
				// gl_Position)\n" +
				// "void iris_widen_lines(vec4 linePosStart, vec4 linePosEnd) {\n" +
				// " vec3 ndc1 = linePosStart.xyz / linePosStart.w;\n" +
				// " vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;\n" +
				// "\n" +
				// " vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) *
				// iris_ScreenSize);\n" +
				// " vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) *
				// iris_LineWidth / iris_ScreenSize;\n"
				// +
				// "\n" +
				// " if (lineOffset.x < 0.0) {\n" +
				// " lineOffset *= -1.0;\n" +
				// " }\n" +
				// "\n" +
				// " if (gl_VertexID % 2 == 0) {\n" +
				// " gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w,
				// linePosStart.w);\n" +
				// " } else {\n" +
				// " gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w,
				// linePosStart.w);\n" +
				// " }\n" +
				// "}\n" +
				// "\n" +
				// "void main() {\n" +
				// " iris_vertex_offset = iris_Normal;\n" +
				// " irisMain();\n" +
				// " vec4 linePosEnd = gl_Position;\n" +
				// " gl_Position = vec4(0.0);\n" +
				// "\n" +
				// " iris_vertex_offset = vec3(0.0);\n" +
				// " irisMain();\n" +
				// " vec4 linePosStart = gl_Position;\n" +
				// "\n" +
				// " iris_widen_lines(linePosStart, linePosEnd);\n" +
				// "}");
			} else {
				// transformations.injectLine(Transformations.InjectionPoint.DEFINES,
				// "#define gl_Vertex vec4(iris_Position, 1.0)");
			}

			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
			// vec3 iris_Position;");
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
		}
		AlphaTestTransformer.transform(t, tree, root, parameters, parameters.alpha);
	}
}
