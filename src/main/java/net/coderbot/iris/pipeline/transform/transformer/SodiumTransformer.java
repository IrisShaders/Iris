package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.parameter.GeometryInfoParameters;
import net.coderbot.iris.pipeline.transform.parameter.SodiumParameters;

public class SodiumTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters, (parameters.hasGeometry ? "iris_alphaTestValueGS" : "iris_alphaTestValue"));

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "uniform mat4 iris_LightmapTextureMatrix;");
		root.rename("gl_ProjectionMatrix", "iris_ProjectionMatrix");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// Alias of gl_MultiTexCoord1 on 1.15+ for OptiFine
			// See https://github.com/IrisShaders/Iris/issues/1149
			root.rename("gl_MultiTexCoord2", "gl_MultiTexCoord1");

			if (parameters.inputs.hasTex()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(_vert_tex_diffuse_coord, 0.0, 1.0)");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(0.0, 0.0, 0.0, 1.0)");
			}

			if (parameters.inputs.hasLight()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
						"vec4(_vert_tex_light_coord, 0.0, 1.0)");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
						"vec4(240.0, 240.0, 0.0, 1.0)");
			}

			AttributeTransformer.patchMultiTexCoord3(t, tree, root, parameters);

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs (with
			// gl_MultiTexCoord2 and gl_MultiTexCoord3 as aliases), other texture
			// coordinates are not valid inputs.
			CommonTransformer.replaceGlMultiTexCoordBounded(t, root, 4, 7);
		}

		if (parameters.inputs.hasColor()) {
			// TODO: Handle the fragment shader here
			root.rename("gl_Color", "_vert_color");
		} else {
			root.replaceReferenceExpressions(t, "gl_Color", "vec4(1.0)");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			if (parameters.inputs.hasNormal()) {
				root.rename("gl_Normal", "iris_Normal");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec3 iris_Normal;");
			} else {
				root.replaceReferenceExpressions(t, "gl_Normal", "vec3(0.0, 0.0, 1.0)");
			}
		}

		// TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		root.replaceReferenceExpressions(t, "gl_NormalMatrix",
				"mat3(iris_NormalMatrix)");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_NormalMatrix;");

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_ModelViewMatrixInverse;");

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_ProjectionMatrixInverse;");

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		root.rename("gl_ModelViewMatrix", "iris_ModelViewMatrix");
		root.rename("gl_ModelViewMatrixInverse", "iris_ModelViewMatrixInverse");
		root.rename("gl_ProjectionMatrixInverse", "iris_ProjectionMatrixInverse");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// TODO: Vaporwave-Shaderpack expects that vertex positions will be aligned to
			// chunks.
			if (root.identifierIndex.has("ftransform")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
			}
			String separateAoValue = parameters.isSeparateAo ? "a_Color" : "vec4(a_Color.rgb * a_Color.a, 1.0)";
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					// translated from sodium's chunk_vertex.glsl
					"vec3 _vert_position;",
					"vec2 _vert_tex_diffuse_coord;",
					"ivec2 _vert_tex_light_coord;",
					"vec4 _vert_color;",
					"uint _draw_id;",
					"uint _material_params;",
					"in uvec4 a_PosId;",
					"in vec4 a_Color;",
					"in vec2 a_TexCoord;",
					"in ivec2 a_LightCoord;",
					"out float iris_alphaTestValue;",
				"const uint MATERIAL_USE_MIP_OFFSET = 0u;",
				"const uint MATERIAL_ALPHA_CUTOFF_OFFSET = 1u;",
				"const float[4] ALPHA_CUTOFF = float[4](0.0f, 0.1f, 0.1f, 1.0f);",
				"float _material_alpha_cutoff(uint material) {\n" +
					"    return ALPHA_CUTOFF[(material >> MATERIAL_ALPHA_CUTOFF_OFFSET) & 3u];\n" +
					"}",
					"void _vert_init() {" +
							"_vert_position = (vec3(a_PosId.xyz) * " + parameters.positionScale + " + "
							+ parameters.positionOffset + ");" +
							"_vert_tex_diffuse_coord = (a_TexCoord * " + parameters.textureScale + ");" +
							"_vert_tex_light_coord = a_LightCoord;" +
							"_vert_color = " + separateAoValue + ";" +
							"_draw_id = (a_PosId.w >> 8u) & 0xFFu;" +
							"_material_params = (a_PosId.w >> 0u) & 0xFFu;" +
							"iris_alphaTestValue = _material_alpha_cutoff(_material_params); }",

					"uvec3 _get_relative_chunk_coord(uint pos) { return uvec3(pos) >> uvec3(5u, 3u, 0u) & uvec3(7u, 3u, 7u); }",

					"vec3 _get_draw_translation(uint pos) { return _get_relative_chunk_coord(pos) * vec3(16.0f); }",
					"uniform mat4 iris_ProjectionMatrix;",
					"uniform mat4 iris_ModelViewMatrix;",
					"uniform vec3 u_RegionOffset;",
					"vec4 getVertexPosition() { return vec4(_vert_position + u_RegionOffset + _get_draw_translation(_draw_id), 1.0); }");
			tree.prependMainFunctionBody(t, "_vert_init();");
			root.replaceReferenceExpressions(t, "gl_Vertex", "getVertexPosition()");
		} else {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform mat4 iris_ModelViewMatrix;",
					"uniform mat4 iris_ProjectionMatrix;");
		}

		if (parameters.type == PatchShaderType.GEOMETRY) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"in float iris_alphaTestValue[];",
				"out float iris_alphaTestValueGS;",
				"void _geom_init() { iris_alphaTestValueGS = iris_alphaTestValue[0]; }");
			tree.prependMainFunctionBody(t, "_geom_init();");
		} else if (parameters.type == PatchShaderType.FRAGMENT) {
			boolean hasGeometry = parameters.hasGeometry;
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "const uint MATERIAL_USE_MIP_OFFSET = 0u;",
				"in float " + (hasGeometry ? "iris_alphaTestValueGS" : "iris_alphaTestValue") + ";");
		}

		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
				"(iris_ProjectionMatrix * iris_ModelViewMatrix)");

		CommonTransformer.applyIntelHd4000Workaround(root);
	}
}
