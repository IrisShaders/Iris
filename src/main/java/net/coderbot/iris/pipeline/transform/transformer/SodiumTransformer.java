package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.transform.parameter.SodiumParameters;

import static net.coderbot.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class SodiumTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters, false);

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.rename("gl_ProjectionMatrix", "iris_ProjectionMatrix");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			if (parameters.inputs.hasTex()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(_vert_tex_diffuse_coord, 0.0, 1.0)");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(0.0, 0.0, 0.0, 1.0)");
			}

			if (parameters.inputs.hasLight()) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_LightmapTextureMatrix;");
				root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");

				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1", "vec4(_vert_tex_light_coord, 0, 1)");
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord2", "vec4(_vert_tex_light_coord, 0, 1)");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
						"vec4(0.0, 0.0, 0.0, 1.0)");
			}

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs, other
			// texture coordinates are not valid inputs.
			CommonTransformer.replaceGlMultiTexCoordBounded(t, root, 2, 7);
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
				"iris_NormalMatrix");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat3 iris_NormalMatrix;");

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
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform mat4 iris_ProjectionMatrix;",
					"uniform mat4 iris_ModelViewMatrix;",
					"uniform vec3 u_RegionOffset;",
					// _draw_translation replaced with Chunks[_draw_id].offset.xyz
					"vec4 getVertexPosition() { return vec4(u_RegionOffset + Chunks[_draw_id].offset.xyz + _vert_position, 1.0); }");
			root.replaceReferenceExpressions(t, "gl_Vertex", "getVertexPosition()");

			// inject here so that _vert_position is available to the above. (injections
			// inject in reverse order if performed piece-wise but in correct order if
			// performed as an array of injections)
			injectVertInit(t, tree, root, parameters);
		} else {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform mat4 iris_ModelViewMatrix;",
					"uniform mat4 iris_ProjectionMatrix;");
		}

		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
				"(iris_ProjectionMatrix * iris_ModelViewMatrix)");

		CommonTransformer.applyIntelHd4000Workaround(root);
	}

	public static void injectVertInit(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				// translated from sodium's chunk_vertex.glsl
				"vec3 _vert_position;",
				"vec2 _vert_tex_diffuse_coord;",
				"ivec2 _vert_tex_light_coord;",
				"vec4 _vert_color;",
				"uint _draw_id;",
				"void _vert_init() {" +
						"_vert_position = (a_PosId.xyz * " + parameters.positionScale + " + "
						+ parameters.positionOffset + ");" +
						"_vert_tex_diffuse_coord = (a_TexCoord * " + parameters.textureScale + ");" +
						"_vert_tex_light_coord = a_LightCoord;" +
						"_vert_color = a_Color;" +
						"_draw_id = uint(a_PosId.w); }",

				// translated from sodium's chunk_parameters.glsl
				// Comment on the struct:
				// Older AMD drivers can't handle vec3 in std140 layouts correctly The alignment
				// requirement is 16 bytes (4 float components) anyways, so we're not wasting
				// extra memory with this, only fixing broken drivers.
				"struct DrawParameters { vec4 offset; };",
				"layout(std140) uniform ubo_DrawParameters {DrawParameters Chunks[256]; };");
		addIfNotExists(root, t, tree, "a_PosId", Type.F32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_TexCoord", Type.F32VEC2, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_Color", Type.F32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_LightCoord", Type.I32VEC2, StorageQualifier.StorageType.IN);
		tree.prependMainFunctionBody(t, "_vert_init();");
	}
}
