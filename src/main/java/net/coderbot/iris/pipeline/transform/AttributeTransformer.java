package net.coderbot.iris.pipeline.transform;

import java.util.stream.Stream;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.basic.ASTNode;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.HintedMatcher;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTBuilder;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.shader.ShaderType;

/**
 * Implements AttributeShaderTransformer using glsl-transformer AST
 * transformation methods.
 */
class AttributeTransformer {
	public static void transform(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			AttributeParameters parameters) {
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
			root.replaceReferenceExpressions(transformer, stream, "vec4(240.0, 240.0, 0.0, 1.0)");
		}

		patchTextureMatrices(transformer, tree, root, parameters.inputs.lightmap);

		if (parameters.inputs.overlay) {
			patchOverlayColor(transformer, tree, root, parameters);
		}

		if (parameters.type == ShaderType.VERTEX
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
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"attribute vec4 mc_midTexCoord;");
		}
	}

	private static void patchTextureMatrices(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			boolean hasLightmap) {
		root.rename("gl_TextureMatrix", "iris_TextureMatrix");

		tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_FUNCTIONS,
				"const float iris_ONE_OVER_256 = 0.00390625;",
				"const float iris_ONE_OVER_32 = iris_ONE_OVER_256 * 8;");
		if (hasLightmap) {
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"mat4 iris_LightmapTextureMatrix = gl_TextureMatrix[2];");
		} else {
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_FUNCTIONS, "mat4 iris_LightmapTextureMatrix =" +
					"mat4(iris_ONE_OVER_256, 0.0, 0.0, 0.0," +
					"     0.0, iris_ONE_OVER_256, 0.0, 0.0," +
					"     0.0, 0.0, iris_ONE_OVER_256, 0.0," +
					"     iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_256);");
		}

		// column major
		tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_FUNCTIONS, "mat4 iris_TextureMatrix[8] = mat4[8](" +
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

	private static final HintedMatcher<ExternalDeclaration> uniformVec4EntityColor = new HintedMatcher<>(
			"uniform vec4 entityColor;",
			GLSLParser::externalDeclaration,
			ASTBuilder::visitExternalDeclaration,
			"entityColor");

	// Add entity color -> overlay color attribute support.
	private static void patchOverlayColor(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			AttributeParameters parameters) {
		// delete original declaration
		root.processMatches(transformer, uniformVec4EntityColor, ASTNode::detachAndDelete);

		if (parameters.type == ShaderType.VERTEX) {
			// add our own declarations
			// TODO: We're exposing entityColor to this stage even if it isn't declared in
			// this stage. But this is needed for the pass-through behavior.
			tree.parseAndInjectNodes(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"uniform sampler2D iris_overlay;",
					"varying vec4 entityColor;");

			// Create our own main function to wrap the existing main function, so that we
			// can pass through the overlay color at the end to the geometry or fragment
			// stage.
			root.rename("main", "irisMain_overlayColor");
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.END, "void main() {" +
					"vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);" +
					"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);" +
					"irisMain_overlayColor(); }");
		} else if (parameters.type == ShaderType.GEOMETRY) {
			// replace read references to grab the color from the first vertex.
			root.replaceReferenceExpressions(transformer, "entityColor", "entityColor[0]");

			// TODO: this is passthrough behavior
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"out vec4 entityColorGS;");
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"in vec4 entityColor[];");
			root.rename("main", "irisMain");
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.END,
					"void main() { entityColorGS = entityColor[0]; irisMain(); }");
		} else if (parameters.type == ShaderType.FRAGMENT) {
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"varying vec4 entityColor;");

			// Different output name to avoid a name collision in the geometry shader.
			if (parameters.hasGeometry) {
				root.rename("entityColor", "entityColorGS");
			}
		}
	}
}
