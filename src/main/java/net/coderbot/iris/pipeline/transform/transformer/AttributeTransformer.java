package net.coderbot.iris.pipeline.transform.transformer;

import java.util.stream.Stream;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.abstract_node.ASTNode;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.transform.TransformationException;
import io.github.douira.glsl_transformer.parser.ParseShape;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.parameter.AttributeParameters;
import net.coderbot.iris.pipeline.transform.parameter.GeometryInfoParameters;
import net.coderbot.iris.pipeline.transform.parameter.Parameters;
import net.coderbot.iris.pipeline.transform.parameter.VanillaParameters;

/**
 * Implements AttributeShaderTransformer
 */
public class AttributeTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			AttributeParameters parameters) {
		if (tree.getVersionStatement().getNormalizedProfile().isCore()) {
			if (parameters.type == PatchShaderType.VERTEX) {
				throw new TransformationException("Vertex shaders must be in the compatibility profile to run properly!");
			}
			return;
		}

		// gl_MultiTexCoord1 and gl_MultiTexCoord2 are both ways to refer to the
		// lightmap texture coordinate.
		// See https://github.com/IrisShaders/Iris/issues/1149
		if (parameters.inputs.lightmap) {
			root.rename("gl_MultiTexCoord1", "gl_MultiTexCoord2");
		}

		Stream<Identifier> stream = Stream.empty();
		boolean hasItems = false;
		if (!parameters.inputs.lightmap) {
			stream = Stream.concat(stream,
					root.identifierIndex.getStream("gl_MultiTexCoord1"));
			stream = Stream.concat(stream,
					root.identifierIndex.getStream("gl_MultiTexCoord2"));
			hasItems = true;
		}
		if (!parameters.inputs.texture) {
			stream = Stream.concat(stream,
					root.identifierIndex.getStream("gl_MultiTexCoord0"));
			hasItems = true;
		}
		if (hasItems) {
			root.replaceReferenceExpressions(t, stream, "vec4(240.0, 240.0, 0.0, 1.0)");
		}

		patchTextureMatrices(t, tree, root, parameters.inputs.lightmap);

		if (parameters.inputs.overlay) {
			patchOverlayColor(t, tree, root, parameters);
		}

		patchMultiTexCoord3(t, tree, root, parameters);
	}

	public static void patchMultiTexCoord3(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		if (parameters.type.glShaderType == ShaderType.VERTEX
				&& root.identifierIndex.has("gl_MultiTexCoord3")
				&& !root.identifierIndex.has("mc_midTexCoord")) {
			// TODO: proper type conversion
			// gl_MultiTexCoord3 is a super legacy alias of mc_midTexCoord. We don't do this
			// replacement if we think mc_midTexCoord could be defined just we can't handle
			// an existing declaration robustly. But basically the proper way to do this is
			// to define mc_midTexCoord only if it's not defined, and if it is defined,
			// figure out its type, then replace all occurrences of gl_MultiTexCoord3 with
			// the correct conversion from mc_midTexCoord's declared type to vec4.
			root.rename("gl_MultiTexCoord3", "mc_midTexCoord");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"attribute vec4 mc_midTexCoord;");
		}
	}

	private static void patchTextureMatrices(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			boolean hasLightmap) {
		root.rename("gl_TextureMatrix", "iris_TextureMatrix");

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"const float iris_ONE_OVER_256 = 0.00390625;",
				"const float iris_ONE_OVER_32 = iris_ONE_OVER_256 * 8;");
		if (hasLightmap) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"mat4 iris_LightmapTextureMatrix = gl_TextureMatrix[2];");
		} else {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "mat4 iris_LightmapTextureMatrix =" +
					"mat4(iris_ONE_OVER_256, 0.0, 0.0, 0.0," +
					"     0.0, iris_ONE_OVER_256, 0.0, 0.0," +
					"     0.0, 0.0, iris_ONE_OVER_256, 0.0," +
					"     iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_256);");
		}

		// column major
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "mat4 iris_TextureMatrix[8] = mat4[8](" +
				"gl_TextureMatrix[0]," +
				"iris_LightmapTextureMatrix," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)," +
				"mat4(1.0)" +
				");");
	}

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
			GeometryInfoParameters parameters) {
		// delete original declaration
		root.processMatches(t, uniformVec4EntityColor, ASTNode::detachAndDelete);

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// add our own declarations
			// TODO: We're exposing entityColor to this stage even if it isn't declared in
			// this stage. But this is needed for the pass-through behavior.
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform sampler2D iris_overlay;",
					"out vec4 entityColor;",
					"out vec4 iris_vertexColor;",
					"in ivec2 iris_UV1;");

			// Create our own main function to wrap the existing main function, so that we
			// can pass through the overlay color at the end to the geometry or fragment
			// stage.
			tree.prependMainFunctionBody(t,
					"vec4 overlayColor = texelFetch(iris_overlay, iris_UV1, 0);",
					"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);",
					"iris_vertexColor = iris_Color;",
					// Workaround for a shader pack bug:
					// https://github.com/IrisShaders/Iris/issues/1549
					// Some shader packs incorrectly ignore the alpha value, and assume that rgb
					// will be zero if there is no hit flash, we try to emulate that here
					"entityColor.rgb *= float(entityColor.a != 0.0);");
		} else if (parameters.type.glShaderType == ShaderType.TESSELATION_CONTROL) {
			// replace read references to grab the color from the first vertex.
			root.replaceReferenceExpressions(t, "entityColor", "entityColor[gl_InvocationID]");

			// TODO: this is passthrough behavior
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"patch out vec4 entityColorTCS;",
				"in vec4 entityColor[];",
				"out vec4 iris_vertexColorTCS[];",
				"in vec4 iris_vertexColor[];");
			tree.prependMainFunctionBody(t,
				"entityColorTCS = entityColor[gl_InvocationID];",
				"iris_vertexColorTCS[gl_InvocationID] = iris_vertexColor[gl_InvocationID];");
		} else if (parameters.type.glShaderType == ShaderType.TESSELATION_EVAL) {
			// replace read references to grab the color from the first vertex.
			root.replaceReferenceExpressions(t, "entityColor", "entityColorTCS");

			// TODO: this is passthrough behavior
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"out vec4 entityColorTES;",
				"patch in vec4 entityColorTCS;",
				"out vec4 iris_vertexColorTES;",
				"in vec4 iris_vertexColorTCS[];");
			tree.prependMainFunctionBody(t,
				"entityColorTES = entityColorTCS;",
				"iris_vertexColorTES = iris_vertexColorTCS[0];");
		} else if (parameters.type.glShaderType == ShaderType.GEOMETRY) {
			// replace read references to grab the color from the first vertex.
			root.replaceReferenceExpressions(t, "entityColor", "entityColor[0]");

			// TODO: this is passthrough behavior
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"out vec4 entityColorGS;",
					"in vec4 entityColor[];",
					"out vec4 iris_vertexColorGS;",
					"in vec4 iris_vertexColor[];");
			tree.prependMainFunctionBody(t,
					"entityColorGS = entityColor[0];",
					"iris_vertexColorGS = iris_vertexColor[0];");

			if (parameters.hasTesselation) {
				root.rename("iris_vertexColor", "iris_vertexColorTES");
				root.rename("entityColor", "entityColorTES");
			}
		} else if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"in vec4 entityColor;", "in vec4 iris_vertexColor;");

			tree.prependMainFunctionBody(t, "float iris_vertexColorAlpha = iris_vertexColor.a;");

			// Different output name to avoid a name collision in the geometry shader.
			if (parameters.hasGeometry) {
				root.rename("entityColor", "entityColorGS");
				root.rename("iris_vertexColor", "iris_vertexColorGS");
			} else if (parameters.hasTesselation) {
				root.rename("entityColor", "entityColorTES");
				root.rename("iris_vertexColor", "iris_vertexColorTES");
			}
		}
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


		if (parameters.type.glShaderType == ShaderType.GEOMETRY) {
			root.replaceReferenceExpressions(t, "entityId",
				"iris_entityInfo[0].x");

			root.replaceReferenceExpressions(t, "blockEntityId",
				"iris_entityInfo[0].y");

			root.replaceReferenceExpressions(t, "currentRenderedItemId",
				"iris_entityInfo[0].z");
		} else {
			root.replaceReferenceExpressions(t, "entityId",
				"iris_entityInfo.x");

			root.replaceReferenceExpressions(t, "blockEntityId",
				"iris_entityInfo.y");

			root.replaceReferenceExpressions(t, "currentRenderedItemId",
				"iris_entityInfo.z");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// add our own declarations
			// TODO: We're exposing entityColor to this stage even if it isn't declared in
			// this stage. But this is needed for the pass-through behavior.
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"flat out ivec3 iris_entityInfo;",
					"in ivec3 iris_Entity;");

			// Create our own main function to wrap the existing main function, so that we
			// can pass through the overlay color at the end to the geometry or fragment
			// stage.
			tree.prependMainFunctionBody(t,
					"iris_entityInfo = iris_Entity;");
		} else if (parameters.type.glShaderType == ShaderType.TESSELATION_CONTROL) {
			// TODO: this is passthrough behavior
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"flat out ivec3 iris_entityInfoTCS[];",
					"flat in ivec3 iris_entityInfo[];");
			root.replaceReferenceExpressions(t, "iris_entityInfo", "iris_EntityInfo[gl_InvocationID]");

			tree.prependMainFunctionBody(t,
					"iris_entityInfoTCS[gl_InvocationID] = iris_entityInfo[gl_InvocationID];");
		} else if (parameters.type.glShaderType == ShaderType.TESSELATION_EVAL) {
			// TODO: this is passthrough behavior
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"flat out ivec3 iris_entityInfoTES;",
					"flat in ivec3 iris_entityInfoTCS[];");
			tree.prependMainFunctionBody(t,
					"iris_entityInfoTES = iris_entityInfoTCS[0];");

			root.replaceReferenceExpressions(t, "iris_entityInfo", "iris_EntityInfoTCS[0]");

		} else if (parameters.type.glShaderType == ShaderType.GEOMETRY) {
			// TODO: this is passthrough behavior
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"flat out ivec3 iris_entityInfoGS;",
					"flat in ivec3 iris_entityInfo" + (parameters.hasTesselation ? "TES" : "") + "[];");
			tree.prependMainFunctionBody(t,
					"iris_entityInfoGS = iris_entityInfo" + (parameters.hasTesselation ? "TES" : "") + "[0];");
		} else if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"flat in ivec3 iris_entityInfo;");

			// Different output name to avoid a name collision in the geometry shader.
			if (parameters.hasGeometry) {
				root.rename("iris_entityInfo", "iris_EntityInfoGS");
			} else if (parameters.hasTesselation) {
				root.rename("iris_entityInfo", "iris_entityInfoTES");
			}
		}
	}
}
