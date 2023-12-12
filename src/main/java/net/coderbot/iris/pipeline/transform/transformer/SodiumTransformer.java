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
		replaceMCEntity(t, tree, root);

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "mat4(1.0)");
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
		root.replaceReferenceExpressions(t, "at_tangent", "iris_Tangent");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			if (parameters.inputs.hasNormal()) {
				root.rename("gl_Normal", "iris_Normal");
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
				// _draw_translation replaced with Chunks[_mesh_id].offset.xyz
				"vec4 getVertexPosition() { return vec4(_vert_position + _get_draw_translation(_mesh_id), 1.0); }");
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
		String separateAo = "vec4(((color.xyz >> (corner_index << 3)) & 0xFFu) / 255.0, unpackUnorm4x8(color.a).wzyx[corner_index])";
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
			"""
				struct IrisQuad {
				    uvec3 position_hi;    // offset: 0    size: 16
				    uvec3 position_lo;    // offset: 16   size: 16

				    uvec4 color;          // offset: 32   size: 16

				    uvec2 tex_diffuse_hi; // offset: 48   size:  8
				    uvec2 tex_diffuse_lo; // offset: 56   size:  8

				    uvec2 light;          // offset: 64   size:  8

				    uint material;        // offset: 72   size:  4
				    uint mesh_id;         // offset: 76   size:  4

				    uint midTexCoord;     // offset: 80   size:  4
				    uint normal;          // offset: 84   size:  4
				    uint tangent;         // offset: 88   size:  4
				    uint blockInfo;       // offset: 92   size:  4
				    // midBlock users mald for now
				};
				""",
			"""
				layout(std430, binding = 15) buffer QuadBuffer {
				    IrisQuad ssbo_Quads[];
				};
				""",
			"""
				vec3 _unpack_position(int quad_index, int corner_index) {
				    return vec3(
				         ((ssbo_Quads[quad_index].position_lo >> (corner_index << 3)) & 0xFFu) |
				        (((ssbo_Quads[quad_index].position_hi >> (corner_index << 3)) & 0xFFu) << 8)
				    ) * 0.00048828125 - 8.0;
				}
				""",
			"""
				vec2 _unpack_texcoord(int quad_index, int corner_index) {
				    return vec2(
				         ((ssbo_Quads[quad_index].tex_diffuse_lo >> (corner_index << 3)) & 0xFFu) |
				        (((ssbo_Quads[quad_index].tex_diffuse_hi >> (corner_index << 3)) & 0xFFu) << 8)
				    ) / 65535.0;
				}
				""",
			"""
				const vec2 CORNERS[4] = vec2[] (
				    vec2(0.0, 0.0),
				    vec2(1.0, 0.0),
				    vec2(1.0, 1.0),
				    vec2(0.0, 1.0)
				);
				""",
			// translated from sodium's chunk_vertex.glsl
			"vec3 _vert_position;",
			"vec2 _vert_tex_diffuse_coord;",
			"vec2 _vert_tex_light_coord;",
			"vec4 _vert_color;",
			"vec3 iris_Normal;",
			"vec2 iris_Entity;",
			"vec2 mc_midTexCoord;",
			"vec4 iris_Tangent;",
			"uint _mesh_id;",
			"const uint MATERIAL_USE_MIP_OFFSET = 0u;",
			"float _material_mip_bias(uint material) {\n" +
				"    return ((material >> MATERIAL_USE_MIP_OFFSET) & 1u) != 0u ? 0.0f : -4.0f;\n" +
				"}",
			"void _vert_init() {" +

				"int quad_index   = gl_VertexID >> 2;" +
				"int corner_index = gl_VertexID  & 3;" +
				"vec2 v_RelCoord = CORNERS[corner_index];" +
				"uvec4 color = ssbo_Quads[quad_index].color;" +
				"vec4 light01 = unpackUnorm4x8(ssbo_Quads[quad_index].light[0]);" + // (c0.x, c0.y, c1.x, c1.y)
				"vec4 light23 = unpackUnorm4x8(ssbo_Quads[quad_index].light[1]);" + // (c3.x, c3.y, c2.x, c2.y)

				"""
					vec2 uv = mix(
					                           mix(light01.xy, light01.zw, v_RelCoord.x),
					                           mix(light23.zw, light23.xy, v_RelCoord.x),
					                           v_RelCoord.y);""" +

				"_vert_tex_light_coord = clamp(uv, vec2(0.5 / 16.0), vec2(15.5 / 16.0));" +
				"_vert_position = _unpack_position(quad_index, corner_index);" +
				"_vert_tex_diffuse_coord = _unpack_texcoord(quad_index, corner_index);" +
				"iris_Normal = unpackUnorm4x8(ssbo_Quads[quad_index].normal).xyz;" +
				"iris_Tangent = unpackUnorm4x8(ssbo_Quads[quad_index].tangent);" +
				"iris_Entity = vec2(ssbo_Quads[quad_index].blockInfo & 0xFFFF, ssbo_Quads[quad_index].blockInfo >> 16);" +
				"mc_midTexCoord = vec2(ssbo_Quads[quad_index].midTexCoord & 0xFFFF, ssbo_Quads[quad_index].midTexCoord >> 16);" +
				"_vert_color = " + separateAo + ";" +
				"_mesh_id = ssbo_Quads[quad_index].mesh_id; }",

			"uvec3 _get_relative_chunk_coord(uint pos) {\n" +
				"    // Packing scheme is defined by LocalSectionIndex\n" +
				"    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);\n" +
				"}",
			"vec3 _get_draw_translation(uint pos) {\n" +
				"    return u_RegionOffset + _get_relative_chunk_coord(pos) * vec3(16.0f);\n" +
				"}");
		addIfNotExists(root, t, tree, "a_PosId", Type.U32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_TexCoord", Type.F32VEC2, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_Color", Type.F32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_LightCoord", Type.I32VEC2, StorageQualifier.StorageType.IN);
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
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "float iris_MidTex;");
				tree.prependMainFunctionBody(t, "iris_MidTex = (mc_midTexCoord.x * " + textureScale + ").x;");
				break;
			case F32VEC2:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec2 iris_MidTex;");
				tree.prependMainFunctionBody(t, "iris_MidTex = (mc_midTexCoord.xy * " + textureScale + ").xy;");
				break;
			case F32VEC3:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec3 iris_MidTex;");
				tree.prependMainFunctionBody(t, "iris_MidTex = vec3((mc_midTexCoord.xy * " + textureScale + ").xy, 0.0);");
				break;
			case F32VEC4:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec4 iris_MidTex;");
				tree.prependMainFunctionBody(t, "iris_MidTex = vec4((mc_midTexCoord.xy * " + textureScale + ").xy, 0.0, 1.0);");
				break;
			default:
				throw new IllegalStateException("Somehow got a midTexCoord that is *above* 4 dimensions???");
		}

	}

	public static void replaceMCEntity(ASTParser t,
										  TranslationUnit tree, Root root) {
		Type dimension = Type.BOOL;
		for (Identifier id : root.identifierIndex.get("mc_Entity")) {
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

		switch (dimension) {
			case BOOL:
				return;
			case FLOAT32:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "float mc_Entity;");
				tree.prependMainFunctionBody(t, "mc_Entity = iris_Entity.x;");
				break;
			case F32VEC2:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec2 mc_Entity;");
				tree.prependMainFunctionBody(t, "mc_Entity = iris_Entity.xy;");
				break;
			case F32VEC3:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec3 mc_Entity;");
				tree.prependMainFunctionBody(t, "mc_Entity = vec3(iris_Entity.xy, 0.0);");
				break;
			case F32VEC4:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec4 mc_Entity;");
				tree.prependMainFunctionBody(t, "mc_Entity = vec4(iris_Entity.xy, 0.0, 1.0);");
				break;
			default:
				throw new IllegalStateException("Somehow got a midTexCoord that is *above* 4 dimensions???");
		}
	}
}
