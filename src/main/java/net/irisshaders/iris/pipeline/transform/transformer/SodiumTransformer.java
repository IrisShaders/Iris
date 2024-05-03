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
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;

import static net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class SodiumTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters, false);

		replaceMidTexCoord(t, tree, root, 1.0f / 32768.0f);

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "mat4(1.0)");
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

			CommonTransformer.patchMultiTexCoord3(t, tree, root, parameters);

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
		String separateAo = WorldRenderingSettings.INSTANCE.shouldUseSeparateAo() ? "a_Color" : "vec4(a_Color.rgb * a_Color.a, 1.0)";
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
			// translated from sodium's chunk_vertex.glsl
			"vec3 _vert_position;",
			"vec2 _vert_tex_diffuse_coord;",
			"vec2 _vert_tex_light_coord;",
			"vec4 _vert_color;",
			"const float POSITION_MAX_COORD   = 1 << 20;",
			"const float TEXTURE_MAX_COORD    = 1 << 15;",
			"const float VERTEX_SCALE = 32.0 / POSITION_MAX_COORD;",
			"const float VERTEX_OFFSET = -8.0;",
			"const float TEXTURE_BIAS_VALUE = (1.0 - (1.0 / 64.0)) / TEXTURE_MAX_COORD;",
			"const float LIGHT_MAX_COORD      = 1 << 8;",
			"uint _draw_id;",
			"const uint MATERIAL_USE_MIP_OFFSET = 0u;",
			"""
				vec3 _decode_position(vec3 hi, vec3 lo) {
				    // The 2.10.10.10 vertex formats do not support being interpreted as integer data within the shader.
				    // Because of this, we need to emulate the bitwise ops with floating-point arithmetic. There is probably no
				    // performance penalty to doing this (other than making things uglier) since GPUs typically have the same
				    // throughput for Fp32 mul/add and Int32 shl/or operations.
				   \s
				    vec3 interleaved = (hi * (1 << 10)) + lo; // (hi << 10) | lo
				    vec3 normalized = (interleaved * VERTEX_SCALE) + VERTEX_OFFSET;
				   \s
				    return normalized;
				}
			\t""",
			"""
				vec2 _decode_texcoord(vec2 value) {
				    // Magnitude is within range (0, 32768)
				    // Sign bit encodes bias direction
				    vec2 texcoord = abs(value) / TEXTURE_MAX_COORD;
				    vec2 bias = sign(value) * TEXTURE_BIAS_VALUE;
				   \s
				    return texcoord - bias;
				}
			""",
			"""
				vec2 _decode_light(uvec2 value) {
				    return vec2(value) / LIGHT_MAX_COORD;
				}
				""",
			"float _material_mip_bias(uint material) {\n" +
				"    return ((material >> MATERIAL_USE_MIP_OFFSET) & 1u) != 0u ? 0.0f : -4.0f;\n" +
				"}",
			"void _vert_init() {" +
				"_vert_position = _decode_position(a_PositionHi, a_PositionLo);" +
				"_vert_tex_diffuse_coord = _decode_texcoord(a_TexCoord);" +
				"_vert_tex_light_coord = _decode_light(a_LightAndData.xy);" +
				"_vert_color = " + separateAo + ";" +
				"_draw_id = a_LightAndData[3]; }",

			"uvec3 _get_relative_chunk_coord(uint pos) {\n" +
				"    // Packing scheme is defined by LocalSectionIndex\n" +
				"    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);\n" +
				"}",
			"vec3 _get_draw_translation(uint pos) {\n" +
				"    return _get_relative_chunk_coord(pos) * vec3(16.0f);\n" +
				"}\n");
		addIfNotExists(root, t, tree, "a_PositionHi", Type.F32VEC3, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_PositionLo", Type.F32VEC3, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_TexCoord", Type.F32VEC2, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_Color", Type.F32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "a_LightAndData", Type.U32VEC4, StorageQualifier.StorageType.IN);
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
