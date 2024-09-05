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
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;

import static net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer.addIfNotExists;

public class VanillaFutureTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		VanillaParameters parameters) {
		String viewPositionSetup = "", clipPositionSetup = "";

		if (root.identifierIndex.has("iris_getClipPosition") || root.identifierIndex.has("iris_getViewPosition")) {
			viewPositionSetup = "irisInt_viewPosition = iris_ModelViewMat * irisInt_modelPosition;";
		}
		if (root.identifierIndex.has("iris_getClipPosition")) {
			clipPositionSetup = "irisInt_clipPosition = iris_ProjMat * irisInt_viewPosition;";
		}

		FutureTransformer.addBasicFunctions(t, tree, root, parameters.type, parameters.getAlphaTest(), "TODO");

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjMat;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ProjMatInverse;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMat;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_ModelViewMatInverse;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat4 iris_LightmapTextureMatrix;");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform mat3 iris_NormalMat;");

		root.replaceReferenceExpressions(t, "iris_projectionMatrix", "iris_ProjMat");
		root.replaceReferenceExpressions(t, "iris_projectionMatrixInverse", "iris_ProjMatInverse");
		root.replaceReferenceExpressions(t, "iris_modelViewMatrix", "iris_ModelViewMat");
		root.replaceReferenceExpressions(t, "iris_modelViewMatrixInverse", "iris_ModelViewMatInverse");
		root.replaceReferenceExpressions(t, "iris_normalMatrix", "iris_NormalMat");
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"vec3 iris_vertex_offset = vec3(0.0);");
		if (parameters.type == PatchShaderType.VERTEX) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform sampler2D iris_overlay;");

			addIfNotExists(root, t, tree, "irisInt_Color", Type.F32VEC4, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "iris_ColorModulator", Type.F32VEC4, StorageQualifier.StorageType.UNIFORM);
			addIfNotExists(root, t, tree, "irisInt_Position", Type.F32VEC3, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "irisInt_Normal", Type.F32VEC3, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "irisInt_Tangent", Type.F32VEC4, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "irisInt_UV0", Type.F32VEC2, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "mc_Entity", Type.FLOAT32, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "irisInt_Entity", Type.I32VEC3, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "mc_midTexCoord", Type.F32VEC2, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "at_midBlock", Type.F32VEC4, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "irisInt_UV1", Type.I32VEC2, StorageQualifier.StorageType.IN);
			addIfNotExists(root, t, tree, "irisInt_UV2", Type.I32VEC2, StorageQualifier.StorageType.IN);

			if (!parameters.inputs.isNewLines()) tree.prependMainFunctionBody(t, "vanilla_init();");

			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "void vanilla_init() {\n" +
				"iris_midTexCoord = mc_midTexCoord;" +
				"iris_texCoord = irisInt_UV0;" +
				"iris_Tangent = irisInt_Tangent;" +
				(parameters.inputs.hasNormal() ?
				"iris_Normal = irisInt_Normal.rgb;" : "iris_Normal = vec3(0, 1, 0);") +
				"iris_ambientOcclusion = irisInt_Color.a;" +
				"iris_blockId = int(mc_Entity + 0.5);" +
				"iris_entityId = int(irisInt_Entity.x + 0.5);" +
				"iris_blockEntityId = int(irisInt_Entity.y + 0.5);" +
				"iris_blockEmission = int(at_midBlock.w);" +
					(parameters.inputs.hasColor() ?
				"iris_vertexColor = irisInt_Color * iris_ColorModulator;" : "iris_vertexColor = iris_ColorModulator;") +
				(parameters.inputs.hasLight() ?
					"iris_lightCoord = (iris_LightmapTextureMatrix * vec4(irisInt_UV2, 0.0, 1.0)).xy;" :
				"iris_lightCoord = vec2(240.0, 240.0)	;"		)+
				"irisInt_modelPosition = vec4(irisInt_Position + iris_ChunkOffset + iris_vertex_offset, 1.0);" +
				viewPositionSetup +
				clipPositionSetup +
				(parameters.inputs.hasOverlay() ?
				"iris_overlayColor = texelFetch(iris_overlay, irisInt_UV1, 0); iris_overlayColor.a = 1.0 - iris_overlayColor.a;" : "iris_overlayColor = vec4(0.0, 0.0, 0.0, 0.0);") +
				"}");

			addIfNotExists(root, t, tree, "iris_ChunkOffset", Type.F32VEC3, StorageQualifier.StorageType.UNIFORM);


			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec2 iris_midTexCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec2 iris_texCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec2 iris_lightCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 iris_vertexColor;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec3 iris_Normal;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 iris_Tangent;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 irisInt_modelPosition;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 irisInt_viewPosition;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 irisInt_clipPosition;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "vec4 iris_overlayColor;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "int iris_blockId;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "int iris_entityId;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "int iris_blockEntityId;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "int iris_blockEmission;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS, "float iris_ambientOcclusion;");

			if (parameters.inputs.isNewLines()) {
				// Create our own main function to wrap the existing main function, so that we
				// can do our line shenanigans.
				// TRANSFORM: this is fine since the AttributeTransformer has a different name
				// in the vertex shader

				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"uniform vec2 iris_ScreenSize;",
					"uniform float iris_LineWidth;",
					"""
						void iris_widen_lines(vec4 linePosStart, vec4 linePosEnd) {
						vec3 ndc1 = linePosStart.xyz / linePosStart.w;
						vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;
						vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * iris_ScreenSize);
						vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * iris_LineWidth / iris_ScreenSize;
						if (lineOffset.x < 0.0) {
						    lineOffset *= -1.0;
						}
						if (gl_VertexID % 2 == 0) {
						    gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
						} else {
						    gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
						}}""");

				root.rename("main", "irisMain");

				tree.parseAndInjectNode(t, ASTInjectionPoint.END, "void main() {" +
					"iris_vertex_offset = irisInt_Normal.rgb;" +
					"vanilla_init();" +
					"irisMain();" +
					"vec4 linePosEnd = gl_Position;" +
					"gl_Position = vec4(0.0);" +
					"iris_vertex_offset = vec3(0.0);" +
					"vanilla_init();" +
					"irisMain();" +
					"vec4 linePosStart = gl_Position;" +
					"iris_widen_lines(linePosStart, linePosEnd);}");

			}
		}
	}
}
