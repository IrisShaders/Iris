package net.coderbot.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.index.ExternalDeclarationIndex;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.parameter.VanillaParameters;
import net.coderbot.iris.shaderpack.transform.Transformations;

import static net.coderbot.iris.pipeline.transform.transformer.CommonTransformer.renameAndWrapShadow;
import static net.coderbot.iris.pipeline.transform.transformer.CommonTransformer.renameFunctionCall;

public class VanillaCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			VanillaParameters parameters) {


		if (parameters.inputs.hasOverlay()) {
			AttributeTransformer.patchOverlayColor(t, tree, root, parameters);
		}

		CommonTransformer.transform(t, tree, root, parameters, true);
		root.rename("alphaTestRef", "iris_currentAlphaTest");
		root.rename("modelViewMatrix", "iris_ModelViewMat");
		root.rename("gl_ModelViewMatrix", "iris_ModelViewMat");
		root.rename("modelViewMatrixInverse", "iris_ModelViewMatInverse");
		root.rename("gl_ModelViewMatrixInverse", "iris_ModelViewMatInverse");
		root.rename("projectionMatrix", "iris_ProjMat");
		root.rename("gl_ProjectionMatrix", "iris_ProjMat");
		root.rename("projectionMatrixInverse", "iris_ProjMatInverse");
		root.rename("gl_ProjectionMatrixInverse", "iris_ProjMatInverse");
		root.rename("textureMatrix", "iris_TextureMat");

		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "iris_TextureMat");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0))");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix2, "mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0))");
		addIfNotExists(root, t, tree, "iris_TextureMat", "mat4", false);
		addIfNotExists(root, t, tree, "iris_ProjMat", "mat4", false);
		addIfNotExists(root, t, tree, "iris_ProjMatInverse", "mat4", false);
		addIfNotExists(root, t, tree, "iris_ModelViewMat", "mat4", false);
		addIfNotExists(root, t, tree, "iris_ModelViewMatInverse", "mat4", false);
		root.rename("normalMatrix", "iris_NormalMat");
		root.rename("gl_NormalMatrix", "iris_NormalMat");
		addIfNotExists(root, t, tree, "iris_NormalMat", "mat3", false);
		root.rename("chunkOffset", "iris_ChunkOffset");
		addIfNotExists(root, t, tree, "iris_ChunkOffset", "vec3", false);

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
			root.replaceReferenceExpressions(t, "gl_Vertex", "vec4(iris_Position, 1.0)");
			root.rename("vaPosition", "iris_Position");
			root.rename("vaColor", "iris_Color");
			root.rename("gl_Color", "iris_Color");
			root.rename("vaNormal", "iris_Normal");
			root.rename("gl_Normal", "iris_Normal");
			root.rename("vaUV0", "iris_UV0");
			root.replaceReferenceExpressions(t, "gl_MultiTexCoord0", "vec4(iris_UV0, 0.0, 1.0)");
			root.replaceReferenceExpressions(t, "gl_MultiTexCoord1", "vec4(iris_UV2, 0.0, 1.0)");
			root.replaceReferenceExpressions(t, "gl_MultiTexCoord2", "vec4(iris_UV2, 0.0, 1.0)");
			root.rename("vaUV1", "iris_UV1");
			root.rename("vaUV2", "iris_UV2");

			addIfNotExists(root, t, tree, "iris_Color", "vec4", true);
			addIfNotExists(root, t, tree, "iris_Position", "vec3", true);
			addIfNotExists(root, t, tree, "iris_Normal", "vec3", true);
			addIfNotExists(root, t, tree, "iris_UV0", "vec2", true);
			addIfNotExists(root, t, tree, "iris_UV1", "vec2", true);
			addIfNotExists(root, t, tree, "iris_UV2", "vec2", true);
		}
	}

	private static void addIfNotExists(Root root, ASTParser t, TranslationUnit tree, String name, String type, boolean attribute) {
		if (root.externalDeclarationIndex.getStream(name).noneMatch((entry) -> entry.declaration() instanceof DeclarationExternalDeclaration)) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, (attribute ? "in " : "uniform ") + type + " " + name + ";");
		}
	}
}
