package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.parameter.SodiumParameters;

public class SodiumCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		root.rename("alphaTestRef", "iris_currentAlphaTest");
		root.rename("modelViewMatrix", "iris_ModelViewMatrix");
		root.rename("modelViewMatrixInverse", "iris_ModelViewMatrixInverse");
		root.rename("projectionMatrix", "iris_ProjectionMatrix");
		root.rename("projectionMatrixInverse", "iris_ProjectionMatrixInverse");
		root.rename("normalMatrix", "iris_NormalMatrix");
		root.rename("chunkOffset", "u_RegionOffset");

		if (parameters.type == PatchShaderType.VERTEX) {
			root.replaceReferenceExpressions(t, "vaPosition", "Chunks[_draw_id].offset.xyz + _vert_position");
			root.replaceReferenceExpressions(t, "vaColor", "_vert_color");
			root.rename("vaNormal", "iris_Normal");
			root.replaceReferenceExpressions(t, "vaUV0", "_vert_tex_diffuse_coord");
			root.replaceReferenceExpressions(t, "vaUV1", "ivec2(0, 10)");
			root.rename("vaUV2", "a_LightCoord");

			root.replaceReferenceExpressions(t, "textureMatrix", "mat4(1.0)");

			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					// translated from sodium's chunk_vertex.glsl
					"vec3 _vert_position;",
					"vec2 _vert_tex_diffuse_coord;",
					"ivec2 _vert_tex_light_coord;",
					"vec4 _vert_color;",
					"uint _draw_id;",
					"in vec4 a_PosId;",
					"in vec4 a_Color;",
					"in vec2 a_TexCoord;",
					"void _vert_init() {" +
							"_vert_position = (a_PosId.xyz * " + parameters.positionScale + " + "
							+ parameters.positionOffset + ");" +
							"_vert_tex_diffuse_coord = (a_TexCoord * " + parameters.textureScale + ");" +
							"_vert_color = a_Color;" +
							"_draw_id = uint(a_PosId.w); }",

					// translated from sodium's chunk_parameters.glsl
					// Comment on the struct:
					// Older AMD drivers can't handle vec3 in std140 layouts correctly The alignment
					// requirement is 16 bytes (4 float components) anyways, so we're not wasting
					// extra memory with this, only fixing broken drivers.
					"struct DrawParameters { vec4 offset; };",
					"layout(std140) uniform ubo_DrawParameters {DrawParameters Chunks[256]; };");
			tree.prependMainFunctionBody(t, "_vert_init();");
		}
	}
}
