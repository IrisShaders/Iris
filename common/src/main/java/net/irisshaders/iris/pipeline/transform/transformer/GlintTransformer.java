package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;

import java.util.Map;

public final class GlintTransformer {
	private GlintTransformer() {
	}

	private static boolean hasGlint(Parameters parameters) {
		return parameters instanceof VanillaParameters vanilla && vanilla.inputs.isGlint() && vanilla.inputs.hasOverlay();
	}

	public static void transform(ASTParser parser, TranslationUnit tree, Root root, Parameters parameters) {
		tree.parseAndInjectNodes(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"bool mc_hasGlint();", "vec3 mc_sampleGlint();");
		if (!hasGlint(parameters)) {
			tree.parseAndInjectNodes(parser, ASTInjectionPoint.END,
				"bool mc_hasGlint() { return false; }",
				"vec3 mc_sampleGlint() { return vec3(0.0); }");
			return;
		}

		if (root.externalDeclarationIndex.getStream("glintTexture")
			.noneMatch(entry -> entry.declaration() instanceof DeclarationExternalDeclaration)) {
			tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform sampler2D glintTexture;");
		}

		CommonTransformer.addIfNotExists(root, parser, tree, "glintAlpha", Type.FLOAT32, StorageType.UNIFORM);

		if (parameters.type == PatchShaderType.VERTEX) {
			String uv = ((VanillaParameters) parameters).inputs.isSpecialGlint() ? "iris_UV3" : "iris_UV0";
			CommonTransformer.addIfNotExists(root, parser, tree, uv, Type.F32VEC2, StorageType.IN);
			tree.prependMainFunctionBody(parser, "iris_glintCoord = (iris_transforms.TextureMat * vec4(" + uv + ", 0.0, 1.0)).xy;");
		}

		tree.parseAndInjectNodes(parser, ASTInjectionPoint.END,
			"bool mc_hasGlint() { return true; }",
			"vec3 mc_sampleGlint() { return glintAlpha * texture(glintTexture, iris_glintCoord).rgb; }");
	}

	public static void transformGrouped(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees, Parameters parameters) {
		if (hasGlint(parameters)) {
			VaryingTransformer.add(parser, trees, Type.F32VEC2, "iris_glintCoord");
		}
	}
}
