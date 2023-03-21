package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.parameter.VanillaParameters;

public class VanillaCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			VanillaParameters parameters) {
		root.rename("alphaTestRef", "iris_currentAlphaTest");
		root.rename("modelViewMatrix", "iris_ModelViewMat");
		if (root.rename("gl_ModelViewMatrix", "iris_ModelViewMat")) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMat;");
		}
		root.rename("modelViewMatrixInverse", "iris_ModelViewMatInverse");
		if (root.rename("gl_ModelViewMatrixInverse", "iris_ModelViewMatInverse")) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMatInverse;");
		}
		root.rename("projectionMatrix", "iris_ProjMat");
		if (root.rename("gl_ProjectionMatrix", "iris_ProjMat")) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjMat;");
		}
		root.rename("projectionMatrixInverse", "iris_ProjMatInverse");
		if (root.rename("gl_ProjectionMatrixInverse", "iris_ProjMatInverse")) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjMatInverse;");
		}
		root.rename("textureMatrix", "iris_TextureMat");

		if (root.rename("gl_TextureMatrix[0]", "iris_TextureMat") || root.rename("gl_TextureMatrix[1]", "iris_TextureMat") || root.rename("gl_TextureMatrix[2]", "iris_TextureMat")) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_TextureMat;");
		}
		root.rename("normalMatrix", "iris_NormalMat");
		if (root.rename("gl_NormalMatrix", "iris_NormalMat")) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat3 iris_NormalMat;");
		}
		root.rename("chunkOffset", "iris_ChunkOffset");

		for (StorageQualifier qualifier : root.nodeIndex.get(StorageQualifier.class)) {
			if (qualifier.storageType == StorageQualifier.StorageType.ATTRIBUTE) {
				qualifier.storageType = StorageQualifier.StorageType.IN;
			} else if (qualifier.storageType == StorageQualifier.StorageType.VARYING) {
				qualifier.storageType = parameters.type.glShaderType == ShaderType.VERTEX
					? StorageQualifier.StorageType.OUT
					: StorageQualifier.StorageType.IN;
			}
		}

		if (parameters.type == PatchShaderType.VERTEX) {
			if (root.rename("gl_Vertex", "iris_PositionFinal")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec3 iris_Position;", "vec4 iris_PositionFinal = vec4(iris_Position, 1.0);");
			}
			root.rename("vaColor", "iris_Color");
			if (root.rename("gl_Color", "iris_Color")) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec4 iris_Color;");
			}
			root.rename("vaNormal", "iris_Normal");
			if (root.rename("gl_Normal", "iris_Normal")) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec3 iris_Normal;");
			}
			root.rename("vaUV0", "iris_UV0");
			if (root.rename("gl_MultiTexCoord0", "iris_UV0Final")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec2 iris_UV0;", "vec4 iris_UV0Final = vec4(iris_UV0, 0.0, 1.0);");
			}
			root.rename("vaUV1", "iris_UV1");
			root.rename("vaUV2", "iris_UV2");
			if (root.rename("gl_MultiTexCoord1", "iris_UV2Final") || root.rename("gl_MultiTexCoord2", "iris_UV2Final")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in vec2 iris_UV2;", "vec4 iris_UV2Final = vec4(iris_UV2, 0.0, 1.0);");
			}
		}

		if (parameters.inputs.hasOverlay() && (root.identifierIndex.has("iris_Color") || root.identifierIndex.has("entityColor"))) {
			AttributeTransformer.patchOverlayColor(t, tree, root, parameters);
		}
	}
}
