package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;

public class VanillaTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		VanillaParameters parameters) {
		// this happens before common to make sure the renaming of attributes is done on
		// attribute inserted by this
		if (parameters.inputs.hasOverlay()) {
			EntityPatcher.patchOverlayColor(t, tree, root, parameters);
			EntityPatcher.patchEntityId(t, tree, root, parameters);
		} else if (parameters.inputs.isText()) {
			EntityPatcher.patchEntityId(t, tree, root, parameters);
		}

		CommonTransformer.transform(t, tree, root, parameters, false);

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"""
				layout(std140) uniform iris_Fog {
				    vec4 FogColor;
				    float FogEnvironmentalStart;
				    float FogEnvironmentalEnd;
				    float FogRenderDistanceStart;
				    float FogRenderDistanceEnd;
				    float FogSkyEnd;
				    float FogCloudsEnd;
				} iris_fogP;
				""",
			"struct iris_FogParameters {" +
				"vec4 color;" +
				"float density;" +
				"float start;" +
				"float end;" +
				"float scale;" +
				"};",
			"iris_FogParameters irisInt_Fog = iris_FogParameters(iris_fogP.FogColor, 0.0, iris_fogP.FogEnvironmentalStart, iris_fogP.FogEnvironmentalEnd, 1.0 / (iris_fogP.FogEnvironmentalEnd - iris_fogP.FogEnvironmentalStart));");

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, """
			layout(std140) uniform iris_DynamicTransforms {
			    mat4 ModelViewMat;
			    vec4 ColorModulator;
			    vec3 ModelOffset;
			    mat4 TextureMat;
			    float LineWidth;
			} iris_transforms;
			""",
			"""
				layout(std140) uniform iris_Projection {
				    mat4 iris_ProjMat;
				};
				""",
			"""
				layout(std140) uniform iris_Globals {
				    vec2 ScreenSize;
				    float GlintAlpha;
				    float GameTime;
				    int MenuBlurRadius;
				} iris_globalInfo;
				""");
		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// Alias of gl_MultiTexCoord1 on 1.15+ for OptiFine
			// See https://github.com/IrisShaders/Iris/issues/1149
			root.rename("gl_MultiTexCoord2", "gl_MultiTexCoord1");

			if (parameters.inputs.hasTex() && !parameters.isClouds()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
					"vec4(iris_UV0, 0.0, 1.0)");
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"in vec2 iris_UV0;");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
					"vec4(0.5, 0.5, 0.0, 1.0)");
			}

			if (parameters.inputs.isIE()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
					"vec4(iris_LightUV, 0.0, 1.0)");

				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform ivec2 iris_LightUV;");
			} else if (parameters.inputs.hasLight()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1",
					"vec4(iris_UV2, 0.0, 1.0)");

				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"in ivec2 iris_UV2;");
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

		if (parameters.inputs.hasColor() && parameters.type == PatchShaderType.VERTEX) {
			// TODO: Handle the fragment / geometry shader here
			if (parameters.alpha.reference() == Float.MAX_VALUE) {
				root.replaceReferenceExpressions(t, "gl_Color",
					"vec4((iris_Color * iris_transforms.ColorModulator).rgb, iris_transforms.ColorModulator.a)");
			} else if (parameters.isClouds()) {
				root.replaceReferenceExpressions(t, "gl_Color", "iris_cloudCol");
			} else {
				root.replaceReferenceExpressions(t, "gl_Color",
					"(iris_Color * iris_transforms.ColorModulator)");
			}

			if (parameters.type.glShaderType == ShaderType.VERTEX) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"in vec4 iris_Color;");
			}
		} else if (parameters.inputs.isGlint()) {
			// iris_ColorModulator should be applied regardless of the alpha test state.
			root.replaceReferenceExpressions(t, "gl_Color", "vec4(iris_transforms.ColorModulator.rgb, iris_transforms.ColorModulator.a * iris_globalInfo.GlintAlpha)");
		} else {
			// iris_ColorModulator should be applied regardless of the alpha test state.
			root.replaceReferenceExpressions(t, "gl_Color", "iris_transforms.ColorModulator");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			if (!parameters.isClouds()) {
				if (parameters.inputs.hasNormal()) {
					if (!parameters.inputs.isNewLines()) {
						root.rename("gl_Normal", "iris_Normal");
					} else {
						root.replaceReferenceExpressions(t, "gl_Normal",
							"vec3(0.0, 0.0, 1.0)");
					}

					tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"in vec3 iris_Normal;");
				} else {
					root.replaceReferenceExpressions(t, "gl_Normal",
						"vec3(0.0, 0.0, 1.0)");
				}
			}
		}

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"uniform mat4 iris_LightmapTextureMatrix;");

		// TODO: More solid way to handle texture matrices
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "iris_transforms.TextureMat");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");

		// TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		root.replaceReferenceExpressions(t, "gl_NormalMatrix",
			"iris_NormalMat");

		root.replaceReferenceExpressions(t, "gl_ModelViewMatrixInverse",
			"iris_ModelViewMatInverse");

		root.replaceReferenceExpressions(t, "gl_ProjectionMatrixInverse",
			"iris_ProjMatInverse");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat3 iris_NormalMat;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjMatInverse;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMatInverse;");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"in vec3 iris_Position;");
			if (root.identifierIndex.has("ftransform")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
			}

			if (parameters.inputs.isNewLines()) {
				root.replaceReferenceExpressions(t, "gl_Vertex",
					"vec4(iris_Position + iris_vertex_offset, 1.0)");

				// Create our own main function to wrap the existing main function, so that we
				// can do our line shenanigans.
				// TRANSFORM: this is fine since the AttributeTransformer has a different name
				// in the vertex shader
				root.rename("main", "irisMain");
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"vec3 iris_vertex_offset = vec3(0.0);");
				tree.parseAndInjectNodes(t, ASTInjectionPoint.END,
					"void iris_widen_lines(vec4 linePosStart, vec4 linePosEnd) {" +
						"vec3 ndc1 = linePosStart.xyz / linePosStart.w;" +
						"vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;" +
						"vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * iris_globalInfo.ScreenSize);" +
						"vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * iris_transforms.LineWidth / iris_globalInfo.ScreenSize;"
						+
						"if (lineOffset.x < 0.0) {" +
						"    lineOffset *= -1.0;" +
						"}" +
						"if (gl_VertexID % 2 == 0) {" +
						"    gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);" +
						"} else {" +
						"    gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);" +
						"}}",
					"void main() {" +
						"iris_vertex_offset = iris_Normal;" +
						"irisMain();" +
						"vec4 linePosEnd = gl_Position;" +
						"gl_Position = vec4(0.0);" +
						"iris_vertex_offset = vec3(0.0);" +
						"irisMain();" +
						"vec4 linePosStart = gl_Position;" +
						"iris_widen_lines(linePosStart, linePosEnd);}");
			} else if (parameters.isClouds()) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, """
					layout(std140) uniform iris_CloudInfo {
					    vec4 CloudColor;
					    vec3 CloudOffset;
					    vec3 CellSize;
					} iris_Clouds;
					""",
					"""
						const vec3[] iris_cloudVertices = vec3[](
						    // Bottom face
						    vec3(1, 0, 0),
						    vec3(1, 0, 1),
						    vec3(0, 0, 1),
						    vec3(0, 0, 0),
						    // Top face
						    vec3(0, 1, 0),
						    vec3(0, 1, 1),
						    vec3(1, 1, 1),
						    vec3(1, 1, 0),
						    // North face
						    vec3(0, 0, 0),
						    vec3(0, 1, 0),
						    vec3(1, 1, 0),
						    vec3(1, 0, 0),
						    // South face
						    vec3(1, 0, 1),
						    vec3(1, 1, 1),
						    vec3(0, 1, 1),
						    vec3(0, 0, 1),
						    // West face
						    vec3(0, 0, 1),
						    vec3(0, 1, 1),
						    vec3(0, 1, 0),
						    vec3(0, 0, 0),
						    // East face
						    vec3(1, 0, 0),
						    vec3(1, 1, 0),
						    vec3(1, 1, 1),
						    vec3(1, 0, 1)
						);
						""",
					"""
						const vec3[] iris_cloudNormals = vec3[](
						    // Bottom face
						    vec3(0, -1, 0),
						    // Top face
						    vec3(0, 1, 0),
						    // North face
						    vec3(0, 0, -1),
						    // South face
						    vec3(0, 0, 1),
						    // West face
						    vec3(-1, 0, 0),
						    // East face
						    vec3(1, 0, 0)
						);
						""",
					"""
						const vec4[] iris_faceColors = vec4[](
						    // Bottom face
						    vec4(0.7, 0.7, 0.7, 0.8),
						    // Top face
						    vec4(1.0, 1.0, 1.0, 0.8),
						    // North face
						    vec4(0.8, 0.8, 0.8, 0.8),
						    // South face
						    vec4(0.8, 0.8, 0.8, 0.8),
						    // West face
						    vec4(0.9, 0.9, 0.9, 0.8),
						    // East face
						    vec4(0.9, 0.9, 0.9, 0.8)
						);
						""",
					"""
						vec3 iris_cloudPos;""",
					"""
						vec3 iris_cloudNormal;""",
						"""
						vec4 iris_cloudCol;
						""",

					"const int FLAG_MASK_DIR = 7;",
					"const int FLAG_INSIDE_FACE = 1 << 4;",
					"const int FLAG_USE_TOP_COLOR = 1 << 5;",
					"const int FLAG_EXTRA_Z = 1 << 6;",
					"const int FLAG_EXTRA_X = 1 << 7;",
					"uniform isamplerBuffer CloudFaces;",
					"""
					void iris_cloudsMain() {
					    int quadVertex = gl_VertexID % 4;
					    int index = (gl_VertexID / 4) * 3;

					    int cellX = texelFetch(CloudFaces, index).r;
					    int cellZ = texelFetch(CloudFaces, index + 1).r;
					    int dirAndFlags = texelFetch(CloudFaces, index + 2).r;
					    int direction = dirAndFlags & FLAG_MASK_DIR;
					    bool isInsideFace = (dirAndFlags & FLAG_INSIDE_FACE) == FLAG_INSIDE_FACE;
					    bool useTopColor = (dirAndFlags & FLAG_USE_TOP_COLOR) == FLAG_USE_TOP_COLOR;
					    cellX = (cellX << 1) | ((dirAndFlags & FLAG_EXTRA_X) >> 7);
					    cellZ = (cellZ << 1) | ((dirAndFlags & FLAG_EXTRA_Z) >> 6);
					    vec3 faceVertex = iris_cloudVertices[(direction * 4) + (isInsideFace ? 3 - quadVertex : quadVertex)];
					    iris_cloudPos = (faceVertex * iris_Clouds.CellSize) + (vec3(cellX, 0, cellZ) * iris_Clouds.CellSize) + iris_Clouds.CloudOffset;
					    iris_cloudNormal = iris_cloudNormals[direction];
					    iris_cloudCol = (useTopColor ? iris_faceColors[1] : iris_faceColors[direction]) * iris_Clouds.CloudColor;
					    }
					""");
				tree.prependMainFunctionBody(t, "iris_cloudsMain();");
				root.replaceReferenceExpressions(t, "gl_Vertex", "vec4(iris_cloudPos, 1.0)");
				root.replaceReferenceExpressions(t, "gl_Normal", "iris_cloudNormal");
			} else {
				root.replaceReferenceExpressions(t, "gl_Vertex", "vec4(iris_Position, 1.0)");
			}
		}

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
			"(gl_ProjectionMatrix * gl_ModelViewMatrix)");

		if (parameters.hasChunkOffset) {
			boolean doInjection = root.replaceReferenceExpressionsReport(t, "gl_ModelViewMatrix",
				"(iris_transforms.ModelViewMat * _iris_internal_translate(iris_transforms.ModelOffset))");
			if (doInjection) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"mat4 _iris_internal_translate(vec3 offset) {" +
						"return mat4(1.0, 0.0, 0.0, 0.0," +
						"0.0, 1.0, 0.0, 0.0," +
						"0.0, 0.0, 1.0, 0.0," +
						"offset.x, offset.y, offset.z, 1.0); }");
			}
		} else if (parameters.inputs.isNewLines()) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"const float iris_VIEW_SHRINK = 1.0 - (1.0 / 256.0);",
				"const mat4 iris_VIEW_SCALE = mat4(" +
					"iris_VIEW_SHRINK, 0.0, 0.0, 0.0," +
					"0.0, iris_VIEW_SHRINK, 0.0, 0.0," +
					"0.0, 0.0, iris_VIEW_SHRINK, 0.0," +
					"0.0, 0.0, 0.0, 1.0);");
			root.replaceReferenceExpressions(t, "gl_ModelViewMatrix",
				"(iris_VIEW_SCALE * iris_transforms.ModelViewMat)");
		} else {
			root.rename("gl_ModelViewMatrix", "iris_transforms.ModelViewMat");
		}

		root.rename("gl_ProjectionMatrix", "iris_ProjMat");
	}
}
