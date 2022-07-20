package net.coderbot.iris.pipeline.transform;

import java.util.stream.Stream;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTBuilder;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.gl.shader.ShaderType;

/**
 * Implements AttributeShaderTransformer using glsl-transformer AST
 * transformation methods.
 */
public class AttributeTransformer {
	public static void accept(
			ASTTransformer<?> transformer,
			TranslationUnit tree,
			Root root,
			AttributeParameters parameters) {
		if (parameters.inputs.lightmap) {
			// original: transformations.replaceExact("gl_MultiTexCoord1",
			// "gl_MultiTexCoord2");
			root.identifierIndex.renameAll("gl_MultiTexCoord1", "gl_MultiTexCoord2");
		}

		Stream<Identifier> stream = Stream.empty();

		// original: transformations.replaceExact("gl_MultiTexCoord1", "vec4(240.0,
		// 240.0, 0.0, 1.0)");
		// original: transformations.replaceExact("gl_MultiTexCoord2", "vec4(240.0,
		// 240.0, 0.0, 1.0)");
		if (!parameters.inputs.lightmap) {
			stream = Stream.concat(stream,
					root.identifierIndex.get("gl_MultiTexCoord1").stream());
			stream = Stream.concat(stream,
					root.identifierIndex.get("gl_MultiTexCoord2").stream());
		}

		// original: transformations.define("gl_MultiTexCoord0", "vec4(240.0, 240.0,
		// 0.0, 1.0)");
		if (parameters.inputs.overlay) {
			stream = Stream.concat(stream,
					root.identifierIndex.get("gl_MultiTexCoord0").stream());
		}

		stream.forEach(identifier -> {
			ReferenceExpression reference = (ReferenceExpression) identifier.getParent();
			reference.replaceByAndDelete(
					transformer.parseExpression(reference.getParent(),
							"vec4(240.0, 240.0, 0.0, 1.0)"));
		});

		// TODO: patchTextureMatrices(transformations, inputs.lightmap);

		if (parameters.inputs.overlay) {
			// TODO: patchOverlayColor(transformations, type, hasGeometry);
		}

		if (parameters.type == ShaderType.VERTEX
				&& root.identifierIndex.has("gl_MultiTexCoord3")
				&& !root.identifierIndex.has("mc_midTexCoord")) {
			// TODO: proper type conversion, see original code
			// original: transformations.replaceExact("gl_MultiTexCoord3",
			// "mc_midTexCoord");
			root.identifierIndex.renameAll("gl_MultiTexCoord3", "mc_midTexCoord");

			// original:
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "attribute vec4 mc_midTexCoord;");
			tree.parseAndInjectNode(transformer, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"attribute vec4 mc_midTexCoord;");
		}
	}
}
