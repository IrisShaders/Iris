package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;

public class SodiumTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters);

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_ALL, "#extension GL_ARB_shader_draw_parameters : require\n");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "uniform mat4 iris_LightmapTextureMatrix;");
		root.rename("gl_ProjectionMatrix", "mat_proj");

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
			root.rename("gl_Color", "_vert_color_shade");
		} else {
			root.replaceReferenceExpressions(t, "gl_Color", "vec4(1.0)");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			if (parameters.inputs.hasNormal()) {
				root.rename("gl_Normal", "iris_Normal");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "layout(location = 7) in vec3 iris_Normal;");
			} else {
				root.replaceReferenceExpressions(t, "gl_Normal", "vec3(0.0, 0.0, 1.0)");
			}
		}

		root.replaceReferenceExpressions(t, "gl_NormalMatrix",
				"mat3(transpose(inverse(mat_modelview)))");

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		root.rename("gl_ModelViewMatrix", "mat_modelview");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// TODO: Vaporwave-Shaderpack expects that vertex positions will be aligned to
			// chunks.
			if (root.identifierIndex.has("ftransform")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"vec4 ftransform() { return mat_modelviewproj * gl_Vertex; }");
			}
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
																			"vec3 _vert_position;",
																			"vec2 _vert_tex_diffuse_coord;",
																			"ivec2 _vert_tex_light_coord;",
																			"vec4 _vert_color_shade;",
																			"layout(location = 0) in vec3 in_position;",
																			"layout(location = 1) in vec4 in_color;",
																			"layout(location = 2) in vec2 in_tex_diffuse_coord;",
																			"layout(location = 3) in ivec2 in_tex_light_coord;",
																			"void _vert_init() {\n" +
																			"    _vert_position = (in_position * VERT_SCALE) + 8.0f;\n".replaceAll("VERT_SCALE", String.valueOf(parameters.vertexRange)) +
																			"    _vert_tex_diffuse_coord = in_tex_diffuse_coord;\n" +
																			"    _vert_tex_light_coord = in_tex_light_coord;\n" +
																			"    _vert_color_shade = in_color;\n" +
																			"}");

			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, """
				struct ModelTransform {
				    // Translation of the model in world-space
				    vec3 translation;
				};""",
				"""
				layout(std140, binding = 1) uniform ModelTransforms {
				    ModelTransform transforms[MAX_BATCH_SIZE];
				};""".replaceAll("MAX_BATCH_SIZE", String.valueOf(parameters.maxBatchSize)),

				"""
				vec3 _apply_view_transform(vec3 position) {
				    ModelTransform transform = transforms[transformIndex];
				    return transform.translation + position;
				}""".replaceAll("transformIndex", (parameters.baseInstanced ? "gl_BaseInstanceARB" : "gl_DrawIDARB")));
			tree.prependMain(t, "_vert_init();");
			root.replaceReferenceExpressions(t, "gl_Vertex", "vec4(_apply_view_transform(_vert_position), 1.0)");
		}

		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
				"mat_modelviewproj");

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, """
			layout(std140, binding = 0) uniform CameraMatrices {
			    // The projection matrix
			    mat4 mat_proj;

			    // The model-view matrix
			    mat4 mat_modelview;

			    // The model-view-projection matrix
			    mat4 mat_modelviewproj;
			};""");

		CommonTransformer.applyIntelHd4000Workaround(root);
	}
}
