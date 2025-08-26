package net.irisshaders.iris.pipeline.transform.transformer;

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
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;

import static net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class SodiumTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters, false);

		replaceMidTexCoord(t, tree, root, 1.0f / 32768.0f);
		replaceMCEntity(t, tree, root);

		boolean needsNormal = root.identifierIndex.has("gl_Normal") || root.identifierIndex.has("at_tangent");

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "uniform mat4 iris_LightmapTextureMatrix;");
		root.rename("gl_ProjectionMatrix", "iris_ProjectionMatrix");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// Alias of gl_MultiTexCoord1 on 1.15+ for OptiFine
			// See https://github.com/IrisShaders/Iris/issues/1149
			root.rename("gl_MultiTexCoord2", "gl_MultiTexCoord1");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform vec2 u_TexCoordShrink;");

			root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
				"vec4((_vert_tex_diffuse_coord_bias * u_TexCoordShrink) + _vert_tex_diffuse_coord, 0.0, 1.0)");

			root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
				"vec4(_vert_tex_light_coord, 0.0, 1.0)");

			CommonTransformer.patchMultiTexCoord3(t, tree, root, parameters);

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs (with
			// gl_MultiTexCoord2 and gl_MultiTexCoord3 as aliases), other texture
			// coordinates are not valid inputs.
			CommonTransformer.replaceGlMultiTexCoordBounded(t, root, 4, 7);
		}

		root.rename("gl_Color", "_vert_color");

		if (parameters.type.glShaderType == ShaderType.VERTEX && needsNormal) {
			root.rename("gl_Normal", "irs_Normal");
			root.replaceReferenceExpressions(t, "at_tangent", "irs_Tangent");
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
				"vec4 getVertexPosition() { return vec4(_vert_position + u_RegionOffset + _get_draw_translation(_draw_id), 1.0); }");
			root.replaceReferenceExpressions(t, "gl_Vertex", "getVertexPosition()");

			// inject here so that _vert_position is available to the above. (injections
			// inject in reverse order if performed piece-wise but in correct order if
			// performed as an array of injections)
			injectVertInit(t, tree, root, parameters, needsNormal);
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
		SodiumParameters parameters, boolean needsNormal) {
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			// translated from sodium's chunk_vertex.glsl
			"vec3 _vert_position;",
			"vec2 _vert_tex_diffuse_coord;",
			"vec2 _vert_tex_diffuse_coord_bias;",
			"vec2 _vert_tex_light_coord;",
			"vec4 _vert_color;",
			"const uint POSITION_BITS        = 20u;",
			"const uint POSITION_MAX_COORD   = 1u << POSITION_BITS;",
			"const uint POSITION_MAX_VALUE   = POSITION_MAX_COORD - 1u;",

			"const uint TEXTURE_BITS         = 15u;",
			"const uint TEXTURE_MAX_COORD    = 1u << TEXTURE_BITS;",
			"const uint TEXTURE_MAX_VALUE    = TEXTURE_MAX_COORD - 1u;",

			"const float VERTEX_SCALE = 32.0 / POSITION_MAX_COORD;",
			"const float VERTEX_OFFSET = -8.0;",
			"uint _draw_id;",
			"vec3 irs_Normal;",
			"vec4 irs_Tangent;",
			"const uint MATERIAL_USE_MIP_OFFSET = 0u;",
			"""
vec3 oct_to_vec3(vec2 e) {
	vec2 f = vec2(e.x, e.y);
	vec3 n = vec3(f.x, f.y, 1.0f - abs(f.x) - abs(f.y));
	float t = clamp(-n.z, 0.0f, 1.0f);
	n.x += n.x >= 0.0f ? -t : t;
	n.y += n.y >= 0.0f ? -t : t;
	return normalize(n);
}
				""",
			"""
vec4 tangent_decode(vec2 e) {
	vec2 oct_compressed = e;
	oct_compressed.y *= 127.0f / 64.0f;
	float r_sign = abs(oct_compressed.y) >= 1.0f ? -1.0f : 1.0f;
	oct_compressed.y = fract(oct_compressed.y) * (64.0f / 63.0f);
	vec3 res = oct_to_vec3(oct_compressed.xy);
	return vec4(res, r_sign);
}
				""",
			"""
					uvec3 _deinterleave_u20x3(uvec2 data) {
					     uvec3 hi = (uvec3(data.x) >> uvec3(0u, 10u, 20u)) & 0x3FFu;
					     uvec3 lo = (uvec3(data.y) >> uvec3(0u, 10u, 20u)) & 0x3FFu;

					     return (hi << 10u) | lo;
					 }
				\t""",
			"""
					vec2 _get_texcoord() {
					     return vec2(a_TexCoord & TEXTURE_MAX_VALUE) / float(TEXTURE_MAX_COORD);
					 }
				""",
			"""
					vec2 _get_texcoord_bias() {
										return mix(vec2(-1.0), vec2(1.0), bvec2(a_TexCoord >> TEXTURE_BITS));
								}
				""",
			"float _material_mip_bias(uint material) {\n" +
				"    return ((material >> MATERIAL_USE_MIP_OFFSET) & 1u) != 0u ? 0.0f : -4.0f;\n" +
				"}",
			"void _vert_init() {" +
				"_vert_position = ((_deinterleave_u20x3(a_Position) * VERTEX_SCALE) + VERTEX_OFFSET);" +
				"_vert_tex_diffuse_coord = _get_texcoord();" +
				"_vert_tex_diffuse_coord_bias = _get_texcoord_bias();" +
				"_vert_tex_light_coord = vec2(a_LightAndData.xy);" +
				"_vert_color = a_Color;" +
				(needsNormal ? "irs_Normal = oct_to_vec3(iris_Normal.xy);" : "") +
				(needsNormal ? "irs_Tangent = tangent_decode(iris_Normal.zw);" : "") +
				"_draw_id = a_LightAndData[3]; }",

			"uvec3 _get_relative_chunk_coord(uint pos) {\n" +
				"    // Packing scheme is defined by LocalSectionIndex\n" +
				"    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);\n" +
				"}",
			"vec3 _get_draw_translation(uint pos) {\n" +
				"    return _get_relative_chunk_coord(pos) * vec3(16.0f);\n" +
				"}\n");
		addIfNotExists(root, t, tree, "a_Position", Type.U32VEC2, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_TexCoord", Type.U32VEC2, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_Color", Type.F32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_LightAndData", Type.U32VEC4, StorageQualifier.StorageType.IN);
		if (needsNormal) addIfNotExists(root, t, tree, "iris_Normal", Type.F32VEC4, StorageQualifier.StorageType.IN);
		tree.prependMainFunctionBody(t, "_vert_init();");
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


		root.replaceReferenceExpressions(t, "mc_Entity", "iris_Entity");

		switch (dimension) {
			case BOOL:
				return;
			case FLOAT32:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "float iris_Entity = int(mc_Entity >> 1u) - 1;");
				break;
			case F32VEC2:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec2 iris_Entity = vec2(int(mc_Entity >> 1u) - 1, mc_Entity & 1u);");
				break;
			case F32VEC3:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec3 iris_Entity = vec3(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0.0);");
				break;
			case F32VEC4:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "vec4 iris_Entity = vec4(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0.0, 1.0);");
				break;
			case INT32:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uint iris_Entity = int(mc_Entity >> 1u) - 1;");
				break;
			case I32VEC2:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "ivec2 iris_Entity = ivec2(int(mc_Entity >> 1u) - 1, mc_Entity & 1u);");
				break;
			case I32VEC3:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "ivec3 iris_Entity = ivec3(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0);");
				break;
			case I32VEC4:
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "ivec4 iris_Entity = ivec4(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0, 1);");
				break;
			default:
				throw new IllegalStateException("Got an invalid format mc_Entity (" + dimension.getCompactName() + ").");
		}

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in uint mc_Entity;");
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
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "float iris_MidTex = (mc_midTexCoord.x * " + textureScale + ");");
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
