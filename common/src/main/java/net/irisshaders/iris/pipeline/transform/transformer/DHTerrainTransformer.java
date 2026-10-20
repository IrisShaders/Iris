package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.ArrayAccessExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.AssignmentExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.MemberAccessExpression;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;

import java.util.Map;

import static net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class DHTerrainTransformer {
	public static void transform(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		CommonTransformer.transform(t, tree, root, parameters, false);


		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "mat4(1.0)");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "mat4(1.0)");
		root.rename("gl_ProjectionMatrix", "iris_ProjectionMatrix");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// Alias of gl_MultiTexCoord1 on 1.15+ for OptiFine
			// See https://github.com/IrisShaders/Iris/issues/1149
			root.rename("gl_MultiTexCoord2", "gl_MultiTexCoord1");

			root.replaceReferenceExpressions(t, "gl_MultiTexCoord0", "vec4(0.0, 0.0, 0.0, 1.0)");

			root.replaceReferenceExpressions(t, "gl_MultiTexCoord1", "vec4(_vert_tex_light_coord, 0.0, 1.0)");


			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs (with
			// gl_MultiTexCoord2 and gl_MultiTexCoord3 as aliases), other texture
			// coordinates are not valid inputs.
			CommonTransformer.replaceGlMultiTexCoordBounded(t, root, 4, 7);
		}

		if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform sampler2D dhBlockAtlas;", """
					bool dh_hasTexture() { return iris_TexId.x != 0u; }""", """
					vec2 dh_blockFaceUv() {
						vec3 pos = fract(iris_vBlockPos);
					      switch (iris_TexId.y)
					      {
					          case 0u: return vec2(pos.x, 1.0 - pos.z); // down
					          case 1u: return vec2(pos.x, pos.z); // up
					          case 2u: return vec2(1.0 - pos.x, 1.0 - pos.y); // north
					          case 3u: return vec2(pos.x, 1.0 - pos.y); // south
					          case 4u: return vec2(pos.z, 1.0 - pos.y); // west
					          default: return vec2(1.0 - pos.z, 1.0 - pos.y); // east
					      }
					}""", """
					vec4 dh_sampleTexture() {
						ivec2 atlasSize = textureSize(dhBlockAtlas, 0);
					          vec2 tileOrigin = vec2(float(iris_TexId.x % 256u), float(iris_TexId.x / 256u)) * 16.0;
					          vec2 uv = (tileOrigin + dh_blockFaceUv() * 16.0) / vec2(atlasSize);
					          return texture(dhBlockAtlas, uv);
					 }
					""");
		}

		root.rename("gl_Color", "_vert_color");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			root.replaceReferenceExpressions(t, "gl_Normal", "_vert_normal");

		}

		// TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		root.replaceReferenceExpressions(t, "gl_NormalMatrix", "iris_NormalMatrix");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat3 iris_NormalMatrix;");

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMatrixInverse;");

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjectionMatrixInverse;");


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
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjectionMatrix;",
				"uniform mat4 iris_ModelViewMatrix;",
				// _draw_translation replaced with Chunks[_draw_id].offset.xyz
				"vec4 getVertexPosition() { return vec4(modelOffset + _vert_position, 1.0); }");
			root.replaceReferenceExpressions(t, "gl_Vertex", "getVertexPosition()");

			// inject here so that _vert_position is available to the above. (injections
			// inject in reverse order if performed piece-wise but in correct order if
			// performed as an array of injections)
			injectVertInit(t, tree, root, parameters);
		} else {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMatrix;",
				"uniform mat4 iris_ProjectionMatrix;");
		}

		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix", "(iris_ProjectionMatrix * iris_ModelViewMatrix)");

		CommonTransformer.applyIntelHd4000Workaround(root);
	}

	public static void injectVertInit(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
			// translated from sodium's chunk_vertex.glsl
			"vec3 _vert_position;", "vec2 _vert_tex_light_coord;", "int dhMaterialId;", "vec4 _vert_color;", "vec3 _vert_normal;",
			"const float mircoOffset = 0.01;", "uniform vec3 modelOffset;",
			"const vec3 irisNormals[6] = vec3[](vec3(0,-1,0),vec3(0,1,0),vec3(0,0,-1),vec3(0,0,1),vec3(-1,0,0),vec3(1,0,0));",
			"void _vert_init() {" + "    uint meta = vPosition.a;\n" + "uint mirco = (meta & 0xFF00u) >> 8u; // mirco offset which is a xyz 2bit value\n" + "    // 0b00 = no offset\n" + "    // 0b01 = positive offset\n" + "    // 0b11 = negative offset\n" + "    // format is: 0b00zzyyxx\n" + "    float mx = (mirco & 1u)!=0u ? mircoOffset : 0.0;\n" + "    mx = (mirco & 2u)!=0u ? -mx : mx;\n" + "    float my = (mirco & 4u)!=0u ? mircoOffset : 0.0;\n" + "    my = (mirco & 8u)!=0u ? -my : my;\n" + "    float mz = (mirco & 16u)!=0u ? mircoOffset : 0.0;\n" + "    mz = (mirco & 32u)!=0u ? -mz : mz;\n" + "        uint lights = meta & 0xFFu;\n" + "_vert_position = vec3(vPosition.xyz) + vec3(mx, my, mz);" + "_vert_normal = irisNormals[irisExtra.y];" + "dhMaterialId = int(irisExtra.x);" + "_vert_tex_light_coord = vec2((float(lights/16u)+0.5) / 16.0, (mod(float(lights), 16.0)+0.5) / 16.0);" + "iris_vBlockPos = vec3(vPosition.xyz);" + "iris_TexId =  uvec2(irisExtra.z | (irisExtra.w << 8u), irisExtra.y);" + "_vert_color = iris_color; }");
		addIfNotExists(root, t, tree, "iris_color", Type.F32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "vPosition", Type.U32VEC4, StorageQualifier.StorageType.IN);
		addIfNotExists(root, t, tree, "irisExtra", Type.U32VEC4, StorageQualifier.StorageType.IN);
		tree.prependMainFunctionBody(t, "_vert_init();");
	}

	public static void transformGrouped(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees) {
		var geometry = trees.get(PatchShaderType.GEOMETRY);
		String vertexIndex = "0";
		if (geometry != null) {
			vertexIndex = "iris_dhVertexIndex";
			var root = geometry.getRoot();
			root.indexBuildSession(() -> {
				geometry.parseAndInjectNodes(parser, ASTInjectionPoint.BEFORE_DECLARATIONS, "int iris_dhInputIndex = 0;", "int iris_dhVertexIndex = 0;");
				// EmitVertex has no index argument. Follow the input selected by gl_in[].gl_Position.
				root.process(root.nodeIndex.getStream(ArrayAccessExpression.class)
					.filter(access -> access.getLeft() instanceof ReferenceExpression ref && ref.getIdentifier().getName().equals("gl_in"))
					.filter(access -> access.getParent() instanceof MemberAccessExpression member && member.getMember().getName().equals("gl_Position")), access -> {
					String index = ASTPrinter.print(PrintType.COMPACT, access.getRight());
					access.getRight().replaceByAndDelete(parser.parseExpression(root, "(iris_dhInputIndex = int(" + index + "))"));
				});
				// Keep the index selected for the output position, even if other inputs are read before EmitVertex.
				root.process(root.nodeIndex.getStream(AssignmentExpression.class)
					.filter(assignment -> assignment.getLeft() instanceof ReferenceExpression ref && ref.getIdentifier().getName().equals("gl_Position")), assignment -> {
					String value = ASTPrinter.print(PrintType.COMPACT, assignment);
					assignment.replaceByAndDelete(parser.parseExpression(root, "(" + value + ", iris_dhVertexIndex = iris_dhInputIndex, gl_Position)"));
				});
			});
		}
		VaryingTransformer.add(parser, trees, Type.F32VEC3, "iris_vBlockPos", false, vertexIndex);
		VaryingTransformer.add(parser, trees, Type.U32VEC2, "iris_TexId", true, vertexIndex);
	}
}
