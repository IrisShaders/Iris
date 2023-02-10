package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.transform.parameter.SodiumParameters;

public class SodiumTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters);

		replaceMidTexCoord(t, tree, root, parameters.textureScale);

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
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
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
					"uniform vec3 u_RegionOffset;",
					// _draw_translation replaced with Chunks[_draw_id].offset.xyz
					"vec4 getVertexPosition() { return vec4(u_RegionOffset + Chunks[_draw_id].offset.xyz + _vert_position, 1.0); }");
			tree.prependMainFunctionBody(t, "_vert_init();");
			root.replaceReferenceExpressions(t, "gl_Vertex", "getVertexPosition()");
		} else {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform mat4 iris_ModelViewMatrix;",
					"uniform mat4 iris_ProjectionMatrix;");
		}

		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
				"(iris_ProjectionMatrix * iris_ModelViewMatrix)");

		CommonTransformer.applyIntelHd4000Workaround(root);
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
