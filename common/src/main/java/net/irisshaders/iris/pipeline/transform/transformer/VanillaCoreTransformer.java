package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;

import static net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class VanillaCoreTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		VanillaParameters parameters) {

		if (parameters.inputs.hasOverlay()) {
			if (!parameters.inputs.isText()) {
				EntityPatcher.patchOverlayColor(t, tree, root, parameters);
			}
			EntityPatcher.patchEntityId(t, tree, root, parameters);
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
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1,
			"mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0))");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix2,
			"mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0))");
		addIfNotExists(root, t, tree, "iris_TextureMat", Type.F32MAT4X4, StorageType.UNIFORM);
		addIfNotExists(root, t, tree, "iris_ProjMat", Type.F32MAT4X4, StorageType.UNIFORM);
		addIfNotExists(root, t, tree, "iris_ProjMatInverse", Type.F32MAT4X4, StorageType.UNIFORM);
		addIfNotExists(root, t, tree, "iris_ModelViewMat", Type.F32MAT4X4, StorageType.UNIFORM);
		addIfNotExists(root, t, tree, "iris_ModelViewMatInverse", Type.F32MAT4X4, StorageType.UNIFORM);
		root.rename("normalMatrix", "iris_NormalMat");
		root.rename("gl_NormalMatrix", "iris_NormalMat");
		addIfNotExists(root, t, tree, "iris_NormalMat", Type.F32MAT3X3, StorageType.UNIFORM);
		root.rename("chunkOffset", "iris_ChunkOffset");
		addIfNotExists(root, t, tree, "iris_ChunkOffset", Type.F32VEC3, StorageType.UNIFORM);

		CommonTransformer.upgradeStorageQualifiers(t, tree, root, parameters);

		if (parameters.type == PatchShaderType.VERTEX) {
			root.replaceReferenceExpressions(t, "gl_Vertex", "vec4(iris_Position, 1.0)");
			root.rename("vaPosition", "iris_Position");
			if (parameters.inputs.hasColor()) {
				root.replaceReferenceExpressions(t, "vaColor", "iris_Color * iris_ColorModulator");
				root.replaceReferenceExpressions(t, "gl_Color", "iris_Color * iris_ColorModulator");
			} else {
				root.replaceReferenceExpressions(t, "vaColor", "iris_ColorModulator");
				root.replaceReferenceExpressions(t, "gl_Color", "iris_ColorModulator");
			}
			root.rename("vaNormal", "iris_Normal");
			root.rename("gl_Normal", "iris_Normal");
			root.rename("vaUV0", "iris_UV0");
			root.replaceReferenceExpressions(t, "gl_MultiTexCoord0", "vec4(iris_UV0, 0.0, 1.0)");
			if (parameters.inputs.hasLight()) {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1", "vec4(iris_UV2, 0.0, 1.0)");
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord2", "vec4(iris_UV2, 0.0, 1.0)");
				root.rename("vaUV2", "iris_UV2");
			} else {
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord1", "vec4(240.0, 240.0, 0.0, 1.0)");
				root.replaceReferenceExpressions(t, "gl_MultiTexCoord2", "vec4(240.0, 240.0, 0.0, 1.0)");
				root.rename("vaUV2", "iris_UV2");
			}
			root.rename("vaUV1", "iris_UV1");

			addIfNotExists(root, t, tree, "iris_Color", Type.F32VEC4, StorageType.IN);
			addIfNotExists(root, t, tree, "iris_ColorModulator", Type.F32VEC4, StorageType.UNIFORM);
			addIfNotExists(root, t, tree, "iris_Position", Type.F32VEC3, StorageType.IN);
			addIfNotExists(root, t, tree, "iris_Normal", Type.F32VEC3, StorageType.IN);
			addIfNotExists(root, t, tree, "iris_UV0", Type.F32VEC2, StorageType.IN);
			addIfNotExists(root, t, tree, "iris_UV1", Type.F32VEC2, StorageType.IN);
			addIfNotExists(root, t, tree, "iris_UV2", Type.F32VEC2, StorageType.IN);
		}
	}
}
