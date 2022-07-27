package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.shader.ShaderType;

public class SodiumTransformer {
	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		// this happens before common for patching gl_FragData
		if (parameters.type == ShaderType.FRAGMENT) {
			AlphaTestTransformer.transform(t, tree, root, parameters, parameters.alpha);
		}
		CommonTransformer.transform(t, tree, root, parameters);

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.rename("gl_ProjectionMatrix", "iris_ProjectionMatrix");

		if (parameters.type == ShaderType.VERTEX) {
			if (parameters.inputs.hasTex()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(_vert_tex_diffuse_coord, 0.0, 1.0)");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(0.0, 0.0, 0.0, 1.0)");
			}

			if (parameters.inputs.hasLight()) {
				SodiumTerrainTransformer.replaceLightmapForSodium("_vert_tex_light_coord", t, tree, root);
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

		if (parameters.type == ShaderType.VERTEX) {
			if (parameters.inputs.hasNormal()) {
				root.rename("gl_Normal", "iris_Normal");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "in vec3 iris_Normal;");
			} else {
				root.replaceReferenceExpressions(t, "gl_Normal", "vec3(0.0, 0.0, 1.0)");
			}
		}

		// TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		root.replaceReferenceExpressions(t, "gl_NormalMatrix",
				"mat3(iris_NormalMatrix)");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"uniform mat4 iris_NormalMatrix;");

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		root.rename("gl_ModelViewMatrix", "iris_ModelViewMatrix");

		if (parameters.type == ShaderType.VERTEX) {
			// TODO: Vaporwave-Shaderpack expects that vertex positions will be aligned to
			// chunks.

			// Create our own main function to wrap the existing main function, so that we
			// can run the alpha test at the end.
			root.rename("main", "irisMain");
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					// translated from sodium's chunk_vertex.glsl
					"vec3 _vert_position;",
					"vec2 _vert_tex_diffuse_coord;",
					"vec2 _vert_tex_light_coord;",
					"vec4 _vert_color;",
					"uint _draw_id;",
					"in vec4 a_PosId;",
					"in vec4 a_Color;",
					"in vec2 a_TexCoord;",
					"in vec2 a_LightCoord;",
					"void _vert_init() {" +
							"_vert_position = (a_PosId.xyz * " + String.valueOf(parameters.positionScale) + " + "
							+ String.valueOf(parameters.positionOffset) + ");" +
							"_vert_tex_diffuse_coord = (a_TexCoord * " + String.valueOf(parameters.textureScale) + ");" +
							"_vert_tex_light_coord = a_LightCoord;" +
							"_vert_color = a_Color;" +
							"_draw_id = uint(a_PosId.w); }",

					// translated from sodium's chunk_parameters.glsl
					// Comment on the struct:
					// Older AMD drivers can't handle vec3 in std140 layouts correctly The alignment
					// requirement is 16 bytes (4 float components) anyways, so we're not wasting
					// extra memory with this, only fixing broken drivers.
					"struct DrawParameters { vec4 offset; };",
					"layout(std140) uniform ubo_DrawParameters {DrawParameters Chunks[256]; };",

					"uniform mat4 iris_ProjectionMatrix;",
					"uniform mat4 iris_ModelViewMatrix;",
					// _draw_translation replaced with Chunks[_draw_id].offset.xyz
					"vec4 getVertexPosition() { return vec4(Chunks[_draw_id].offset.xyz + _vert_position, 1.0); }",
					"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

			tree.parseAndInjectNode(t, ASTInjectionPoint.END,
					"void main() { _vert_init(); irisMain(); }");
			root.replaceReferenceExpressions(t, "iris_ModelViewProjectionMatrix",
					"(iris_ProjectionMatrix * iris_ModelViewMatrix)");
			root.replaceReferenceExpressions(t, "gl_Vertex", "getVertexPosition()");
		} else {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"uniform mat4 iris_ModelViewMatrix;",
					"uniform mat4 iris_ProjectionMatrix;");
		}

		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
				"(iris_ProjectionMatrix * iris_ModelViewMatrix)");

		CommonTransformer.applyIntelHd4000Workaround(root);
	}
}
