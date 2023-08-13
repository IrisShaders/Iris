package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.AlphaTests;
import net.coderbot.iris.pipeline.transform.parameter.SodiumParameters;

import java.util.Set;

import static net.coderbot.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class SodiumTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters, false);

		replaceMidTexCoord(t, tree, root, 1.0f / 65536.0f);

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.replaceReferenceExpressions(t, "at_midBlock", "iris_midBlock.xyz");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "uniform mat4 iris_LightmapTextureMatrix;");
		root.rename("gl_ProjectionMatrix", "iris_ProjectionMatrix");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// Alias of gl_MultiTexCoord1 on 1.15+ for OptiFine
			// See https://github.com/IrisShaders/Iris/issues/1149
			root.rename("gl_MultiTexCoord2", "gl_MultiTexCoord1");

			if (parameters.inputs.hasTex()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(_vert_tex_coord, 0.0, 1.0)");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"vec4(0.0, 0.0, 0.0, 1.0)");
			}

			if (parameters.inputs.hasLight()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
						"vec4(_vert_light, 0.0, 1.0)");
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
			root.replaceReferenceExpressions(t, "gl_Color", "vec4(_vert_color, " + (BlockRenderingSettings.INSTANCE.shouldUseSeparateAo() ? "iris_midBlock.w" : "1.0") + ")");
			if (parameters.type.glShaderType == ShaderType.VERTEX) {
				addIfNotExists(root, t, tree, "iris_midBlock", Type.F32VEC4, StorageQualifier.StorageType.IN);
			}

		} else {
			root.replaceReferenceExpressions(t, "gl_Color", "vec4(1.0)");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			if (parameters.inputs.hasNormal()) {
				root.replaceReferenceExpressions(t, "gl_Normal", "iris_Normal");
				addIfNotExists(root, t, tree, "iris_Normal", Type.F32VEC3, StorageQualifier.StorageType.IN);
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
					"vec4 getVertexPosition() { return vec4(_vert_position + u_RegionOffset + _get_draw_translation(_vert_mesh_id), 1.0); }");
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
				"in uvec4 in_VertexData;",
				"vec3 _vert_position;",
				"vec2 _vert_tex_coord;",
				"vec3 _vert_color;",
				"vec2 _vert_light;",
				"uint _vert_material;",
				"uint _vert_mesh_id;",
				"const uint MATERIAL_USE_MIP_OFFSET = 0u;",
					"float _material_mip_bias(uint material) {\n" +
					"    return ((material >> MATERIAL_USE_MIP_OFFSET) & 1u) != 0u ? 0.0f : -4.0f;\n" +
					"}",
			"""
				void _vert_init() {
				    // Vertex Position
				    uvec3 packed_position = uvec3(
				        (in_VertexData[0] >>  0) & 0xFFFFu,
				        (in_VertexData[0] >> 16) & 0xFFFFu,
				        (in_VertexData[1] >>  0) & 0xFFFFu
				    );
				    _vert_position = (vec3(packed_position) * (32.0 / 65536.0)) - 8.0;

				    // Vertex Material
				    _vert_material = (in_VertexData[1] >> 16) & 0xFFu;

				    // Vertex Mesh ID
				    _vert_mesh_id  = (in_VertexData[1] >> 24) & 0xFFu;

				    // Vertex Color
				    uvec3 packed_color = (uvec3(in_VertexData[2]) >> uvec3(0, 8, 16)) & uvec3(0xFFu);
				    _vert_color = vec3(packed_color) * (1.0 / 255.0);

				    // Vertex Light
				    uvec2 packed_light = (uvec2(in_VertexData[2]) >> uvec2(24, 28)) & uvec2(0xFu);
				    _vert_light = vec2(packed_light) * 16.0;

				    // Vertex Texture Coords
				    uvec2 packed_tex_coord = (uvec2(in_VertexData[3]) >> uvec2(0, 16)) & uvec2(0xFFFFu);
				    _vert_tex_coord = vec2(packed_tex_coord) * (1.0 / 65536.0);
				}""",
				"vec3 _get_draw_translation(uint pos) {\n" +
				"    return (uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u)) * vec3(16.0f);\n" +
				"}\n");
		addIfNotExists(root, t, tree, "in_VertexData", Type.U32VEC4, StorageQualifier.StorageType.IN);
		if (BlockRenderingSettings.INSTANCE.shouldUseSeparateAo()) {
			addIfNotExists(root, t, tree, "iris_midBlock", Type.F32VEC4, StorageQualifier.StorageType.IN);
		}
		tree.prependMainFunctionBody(t, "_vert_init();");
	}


	public static void replaceMidTexCoord(ASTParser t,
										  TranslationUnit tree, Root root, float textureScale) {
		Type dimension = Type.BOOL;
		for (Identifier id : root.identifierIndex.get("mc_midTexCoord")) {
			TypeAndInitDeclaration initDeclaration = (TypeAndInitDeclaration) id.getAncestor(
				2, 0, TypeAndInitDeclaration.class::isInstance);
			if (initDeclaration == null) {
				continue;
			}
			DeclarationExternalDeclaration declaration = (DeclarationExternalDeclaration) initDeclaration.getAncestor(
				1, 0, DeclarationExternalDeclaration.class::isInstance);
			if (declaration == null) {
				continue;
			}
			if (initDeclaration.getType().getTypeSpecifier() instanceof BuiltinNumericTypeSpecifier numeric) {
				dimension = numeric.type;

				declaration.detachAndDelete();
				initDeclaration.detachAndDelete();
				id.detachAndDelete();
				break;
			}
		}


		root.replaceReferenceExpressions(t, "mc_midTexCoord", "iris_MidTex");

		switch (dimension) {
			case BOOL:
				return;
			case FLOAT32:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "float iris_MidTex = (mc_midTexCoord.x * " + textureScale + ").x;");
				break;
			case F32VEC2:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec2 iris_MidTex = (mc_midTexCoord.xy * " + textureScale + ").xy;");
				break;
			case F32VEC3:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec3 iris_MidTex = vec3(mc_midTexCoord.xy * " + textureScale + ", 0.0);");
				break;
			case F32VEC4:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec4 iris_MidTex = vec4(mc_midTexCoord.xy * " + textureScale + ", 0.0, 1.0);");
				break;
			default:
				throw new IllegalStateException("Somehow got a midTexCoord that is *above* 4 dimensions???");
		}

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec2 mc_midTexCoord;");
	}
}
