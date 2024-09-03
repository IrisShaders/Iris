package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;

import static net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class SodiumFutureTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		SodiumParameters parameters) {
		String viewPositionSetup = "", clipPositionSetup = "";

		if (root.identifierIndex.has("iris_getClipPosition") || root.identifierIndex.has("iris_getViewPosition")) {
			viewPositionSetup = "irisInt_viewPosition = iris_ModelViewMatrix * irisInt_modelPosition;";
		}
		if (root.identifierIndex.has("iris_getClipPosition")) {
			clipPositionSetup = "irisInt_clipPosition = iris_ProjectionMatrix * irisInt_viewPosition;";
		}

		FutureTransformer.addBasicFunctions(t, tree, root, parameters.type, parameters.getAlphaTest(), "irisInt_Color.a");

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjectionMatrix;");
		root.replaceReferenceExpressions(t, "iris_projectionMatrix", "iris_ProjectionMatrix");
		root.replaceReferenceExpressions(t, "iris_projectionMatrixInverse", "iris_ProjectionMatrixInverse");
		root.replaceReferenceExpressions(t, "iris_modelViewMatrix", "iris_ModelViewMatrix");
		root.replaceReferenceExpressions(t, "iris_modelViewMatrixInverse", "iris_ModelViewMatrixInverse");
		root.replaceReferenceExpressions(t, "iris_normalMatrix", "iris_NormalMatrix");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMatrix;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_LightmapTextureMatrix;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat3 iris_NormalMatrix;");

		if (parameters.type == PatchShaderType.VERTEX) {

			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "void sodium_init() {\n" +
				"vec3 _vert_position = ((_deinterleave_u20x3(irisInt_PositionHi, irisInt_PositionLo) * VERTEX_SCALE) + VERTEX_OFFSET);" +
				"iris_midTexCoord = irisInt_MidTexCoord * " + (1.0 / 32768.0) + ";\n" +
				"iris_texCoord = _get_texcoord();" +
				"iris_blockEmission = int(irisInt_midBlock.w);" +
				"iris_Normal = irisInt_Normal.rgb;" +
				"iris_Tangent = irisInt_Tangent;" +
				"iris_ambientOcclusion = irisInt_Normal.a;" +
				"iris_blockId = irisInt_BlockInfo.x;" +
				"iris_vertexColor = irisInt_Color;" +
				"iris_lightCoord = (iris_LightmapTextureMatrix * vec4(irisInt_LightAndData.xy, 0.0, 1.0)).xy;" +
				"irisInt_modelPosition = vec4(_vert_position + u_RegionOffset + _get_draw_translation(irisInt_LightAndData[3]), 1.0);" +
				viewPositionSetup +
				clipPositionSetup +
				"iris_overlayColor = vec4(0.0, 0.0, 0.0, 1.0);" +
				"}");

			addIfNotExists(root, t, tree, "irisInt_Normal", Type.F32VEC4, StorageQualifier.StorageType.IN, 10);
			addIfNotExists(root, t, tree, "irisInt_Tangent", Type.F32VEC4, StorageQualifier.StorageType.IN, 13);
			addIfNotExists(root, t, tree, "irisInt_MidTexCoord", Type.F32VEC2, StorageQualifier.StorageType.IN, 12);
			addIfNotExists(root, t, tree, "irisInt_midBlock", Type.F32VEC4, StorageQualifier.StorageType.IN, 14);
			addIfNotExists(root, t, tree, "irisInt_BlockInfo", Type.F32VEC2, StorageQualifier.StorageType.IN, 11);
			addIfNotExists(root, t, tree, "irisInt_PositionHi", Type.UINT32, StorageQualifier.StorageType.IN, ChunkShaderBindingPoints.ATTRIBUTE_POSITION_HI);
			addIfNotExists(root, t, tree, "irisInt_PositionLo", Type.UINT32, StorageQualifier.StorageType.IN, ChunkShaderBindingPoints.ATTRIBUTE_POSITION_LO);
			addIfNotExists(root, t, tree, "irisInt_TexCoord", Type.U32VEC2, StorageQualifier.StorageType.IN, ChunkShaderBindingPoints.ATTRIBUTE_TEXTURE);
			addIfNotExists(root, t, tree, "irisInt_Color", Type.F32VEC4, StorageQualifier.StorageType.IN, ChunkShaderBindingPoints.ATTRIBUTE_COLOR);
			addIfNotExists(root, t, tree, "irisInt_LightAndData", Type.U32VEC4, StorageQualifier.StorageType.IN, ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_MATERIAL_INDEX);
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"const uint POSITION_BITS        = 20u;",
				"const uint POSITION_MAX_COORD   = 1u << POSITION_BITS;",
				"const uint POSITION_MAX_VALUE   = POSITION_MAX_COORD - 1u;",

				"const uint TEXTURE_BITS         = 15u;",
				"const uint TEXTURE_MAX_COORD    = 1u << TEXTURE_BITS;",
				"const uint TEXTURE_MAX_VALUE    = TEXTURE_MAX_COORD - 1u;",

				"const float VERTEX_SCALE = 32.0 / POSITION_MAX_COORD;",
				"const float VERTEX_OFFSET = -8.0;",
				"const float TEXTURE_FUZZ_AMOUNT = 1.0 / 64.0;",
				"const float TEXTURE_GROW_FACTOR = (1.0 - TEXTURE_FUZZ_AMOUNT) / TEXTURE_MAX_COORD;",
				"const uint MATERIAL_USE_MIP_OFFSET = 0u;",

				"""
						vec2 _get_texcoord() {
							 return vec2(irisInt_TexCoord & TEXTURE_MAX_VALUE) / float(TEXTURE_MAX_COORD);
						 }
					""",
				"""
						vec2 _get_texcoord_bias() {
							 return mix(vec2(-TEXTURE_GROW_FACTOR), vec2(TEXTURE_GROW_FACTOR), bvec2(irisInt_TexCoord >> TEXTURE_BITS));
						 }
					""",
				"uvec3 _get_relative_chunk_coord(uint pos) {\n" +
					"    // Packing scheme is defined by LocalSectionIndex\n" +
					"    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);\n" +
					"}",
				"vec3 _get_draw_translation(uint pos) {\n" +
					"    return _get_relative_chunk_coord(pos) * vec3(16.0f);\n" +
					"}\n");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, """
					uvec3 _deinterleave_u20x3(uint packed_hi, uint packed_lo) {
					     uvec3 hi = (uvec3(packed_hi) >> uvec3(0u, 10u, 20u)) & 0x3FFu;
					     uvec3 lo = (uvec3(packed_lo) >> uvec3(0u, 10u, 20u)) & 0x3FFu;

					     return (hi << 10u) | lo;
					 }
				""");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform vec3 u_RegionOffset;");
			tree.prependMainFunctionBody(t, "sodium_init();");

			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec2 iris_midTexCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec2 iris_texCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec2 iris_lightCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec3 iris_Normal;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 iris_Tangent;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 iris_vertexColor;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 irisInt_modelPosition;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 irisInt_clipPosition;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 irisInt_viewPosition;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 iris_overlayColor;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "float iris_blockId;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "float iris_ambientOcclusion;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "int iris_blockEmission;");
		}
	}
}
