package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;

public class FutureTransformer {
	public static void addBasicFunctions(ASTParser t,
										 TranslationUnit tree,
										 Root root, PatchShaderType shaderType, AlphaTest alphaTest, String vertexColorA) {
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform sampler2D mainTexture;");
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, """
				vec4 iris_sampleMainTexture(vec2 coord) {
					return texture(mainTexture, coord);
				}
				""",

				"""
				vec4 iris_sampleMainTextureOffset(vec2 coord, ivec2 offset) {
					return textureOffset(mainTexture, coord, offset);
				}
				""",

				"""
				vec4 iris_sampleMainTextureLod(vec2 coord, float lod) {
					return textureLod(mainTexture, coord, lod);
				}
				""",

				"""
				vec4 iris_sampleMainTextureGather(vec2 coord, int comp) {
					return textureGather(mainTexture, coord, comp);
				}
				""",

				"""
				vec4 iris_sampleMainTextureGrad(vec2 coord, vec2 dtdx, vec2 dtdy) {
					return textureGrad(mainTexture, coord, dtdx, dtdy);
				}
				""",

				"""
				vec4 iris_sampleMainTexture(vec2 coord, float bias) {
					return texture(mainTexture, coord, bias);
				}
				""",

				"""
				vec4 iris_fetchMainTexture(ivec2 coord, int lod) {
					return texelFetch(mainTexture, coord, lod);
				}
				""");

		if (root.identifierIndex.has("iris_sampleLightmap")) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform sampler2D lightmapTexture;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, """
				vec4 iris_sampleLightmap(vec2 coord) {
					return texture(lightmapTexture, coord);
				}
				""");
		}

		if (shaderType == PatchShaderType.FRAGMENT) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform float iris_currentAlphaTest;");

			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, """
				void iris_modifyBase(inout vec2 texCoord, inout vec2 light, inout vec4 color) {

				}
				""",
				// Optional
				"""
				void iris_modifyNormals(inout vec3 normal, inout vec3 tangent) {

				}
				""",
				"bool iris_discardFragment(vec4 color) {\n" +
					"return !(" + alphaTest.toBoolean("color.a", "iris_currentAlphaTest", vertexColorA) + ");\n" +
				"}",
				"""
				vec4 iris_modifyOverlay(vec4 color) {
					return color;
				}
				""");
		} else if (shaderType == PatchShaderType.VERTEX) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, """
				vec4 iris_getModelPosition() {
					return irisInt_modelPosition;
				}
				""",
				"""
				vec4 iris_getViewPosition() {
					return irisInt_viewPosition;
				}
				""",
				"""
				vec4 iris_getClipPosition() {
					return irisInt_clipPosition;
				}
				""");
		}
	}

	public static void replaceAndAdd(Root root, TranslationUnit tree, ASTParser t, String reference, String replacement, String injection) {
		if (root.identifierIndex.has(reference)) {
			root.replaceReferenceExpressions(t, reference, replacement);
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, injection);
		}
	}
}
