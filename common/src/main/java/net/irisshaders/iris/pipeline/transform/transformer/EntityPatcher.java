package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.abstract_node.ASTNode;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.parser.ParseShape;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;

public class EntityPatcher {
	private static final AutoHintedMatcher<ExternalDeclaration> uniformVec4EntityColor = new AutoHintedMatcher<>(
		"uniform vec4 entityColor;", ParseShape.EXTERNAL_DECLARATION);

	private static final AutoHintedMatcher<ExternalDeclaration> uniformIntEntityId = new AutoHintedMatcher<>(
		"uniform int entityId;", ParseShape.EXTERNAL_DECLARATION);

	private static final AutoHintedMatcher<ExternalDeclaration> uniformIntBlockEntityId = new AutoHintedMatcher<>(
		"uniform int blockEntityId;", ParseShape.EXTERNAL_DECLARATION);

	private static final AutoHintedMatcher<ExternalDeclaration> uniformIntCurrentRenderedItemId = new AutoHintedMatcher<>(
		"uniform int currentRenderedItemId;", ParseShape.EXTERNAL_DECLARATION);

	// Add entity color -> overlay color attribute support.
	public static void patchOverlayColor(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		VanillaParameters parameters) {
		// delete original declaration
		root.processMatches(t, uniformVec4EntityColor, ASTNode::detachAndDelete);

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// add our own declarations
			// TODO: We're exposing entityColor to this stage even if it isn't declared in
			// this stage. But this is needed for the pass-through behavior.
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform sampler2D iris_overlay;",
				parameters.inputs.isIE() ? "uniform ivec2 iris_OverlayUV;" : "in ivec2 iris_UV1;");

			// Create our own main function to wrap the existing main function, so that we
			// can pass through the overlay color at the end to the geometry or fragment
			// stage.
			tree.prependMainFunctionBody(t,
				"vec4 overlayColor = texelFetch(iris_overlay, " + (parameters.inputs.isIE() ? "iris_OverlayUV" : "iris_UV1") + ", 0);",
				"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);",
				"iris_vertexColor = iris_Color;",
				// Workaround for a shader pack bug:
				// https://github.com/IrisShaders/Iris/issues/1549
				// Some shader packs incorrectly ignore the alpha value, and assume that rgb
				// will be zero if there is no hit flash, we try to emulate that here
				"entityColor.rgb *= float(entityColor.a != 0.0);");
		} else if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			tree.prependMainFunctionBody(t, "float iris_vertexColorAlpha = iris_vertexColor.a;");
		}

		VaryingTransformer.add(t, tree, parameters, Type.F32VEC4, "entityColor");
		VaryingTransformer.add(t, tree, parameters, Type.F32VEC4, "iris_vertexColor");
	}

	public static void patchEntityId(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		VanillaParameters parameters) {
		// delete original declaration
		root.processMatches(t, uniformIntEntityId, ASTNode::detachAndDelete);
		root.processMatches(t, uniformIntBlockEntityId, ASTNode::detachAndDelete);
		root.processMatches(t, uniformIntCurrentRenderedItemId, ASTNode::detachAndDelete);

		root.replaceReferenceExpressions(t, "entityId", "iris_entityInfo.x");
		root.replaceReferenceExpressions(t, "blockEntityId", "iris_entityInfo.y");
		root.replaceReferenceExpressions(t, "currentRenderedItemId", "iris_entityInfo.z");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "in ivec3 iris_Entity;");
			tree.prependMainFunctionBody(t, "iris_entityInfo = iris_Entity;");
		}

		VaryingTransformer.add(t, tree, parameters, Type.I32VEC3, "iris_entityInfo");
	}
}
