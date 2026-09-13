package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionDeclaration;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionParameter;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.type.specifier.FunctionPrototype;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.SequenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.AssignmentExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.SubtractionExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.GroupingExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.MemberAccessExpression;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DepthTransformer {
	private static final Set<String> DEPTH_SAMPLERS = Set.of(
		"depthtex0", "depthtex1", "depthtex2", "gdepthtex", "dhDepthTex", "dhDepthTex0", "dhDepthTex1", "vxDepthTexOpaque", "vxDepthTexTrans");
	private static final Set<String> TEXTURE_READ_FUNCTIONS = Set.of(
		"texture", "textureProj", "textureLod", "textureOffset", "textureProjOffset",
		"textureLodOffset", "textureProjLod", "textureProjLodOffset", "textureGrad",
		"textureGradOffset", "textureProjGrad", "textureProjGradOffset",
		"texelFetch", "texelFetchOffset", "textureGather", "textureGatherOffset", "textureGatherOffsets",
		"texture1D", "texture1DProj", "texture1DLod", "texture1DProjLod",
		"texture1DGrad", "texture1DProjGrad", "texture1DOffset", "texture1DProjOffset",
		"texture1DLodOffset", "texture1DProjLodOffset", "texture1DGradOffset", "texture1DProjGradOffset",
		"texture2D", "texture2DProj", "texture2DLod", "texture2DProjLod",
		"texture2DGrad", "texture2DProjGrad", "texture2DOffset", "texture2DProjOffset",
		"texture2DLodOffset", "texture2DProjLodOffset", "texture2DGradOffset", "texture2DProjGradOffset",
		"texture3D", "texture3DProj", "texture3DLod", "texture3DProjLod",
		"texture3DGrad", "texture3DProjGrad", "texture3DOffset", "texture3DProjOffset",
		"texture3DLodOffset", "texture3DProjLodOffset", "texture3DGradOffset", "texture3DProjGradOffset",
		"textureCube", "textureCubeLod", "textureCubeGrad",
		"texture2DRect", "texture2DRectProj", "texture2DRectGrad", "texture2DRectProjGrad",
		"texture2DRectOffset", "texture2DRectProjOffset", "texture2DRectGradOffset", "texture2DRectProjGradOffset",
		"texture1DArray", "texture1DArrayLod", "texture1DArrayGrad", "texture1DArrayOffset",
		"texture1DArrayLodOffset", "texture1DArrayGradOffset",
		"texture2DArray", "texture2DArrayLod", "texture2DArrayGrad", "texture2DArrayOffset",
		"texture2DArrayLodOffset", "texture2DArrayGradOffset",
		"texelFetch1D", "texelFetch2D", "texelFetch3D", "texelFetch2DRect", "texelFetchBuffer",
		"texelFetch1DArray", "texelFetch2DArray", "texelFetch1DOffset", "texelFetch2DOffset",
		"texelFetch3DOffset", "texelFetch2DRectOffset", "texelFetch1DArrayOffset", "texelFetch2DArrayOffset");

	public static void transform(ASTParser t, TranslationUnit tree, Root root, PatchShaderType type,
							 boolean shadow, boolean zZeroToOne) {
		invertDepthReads(t, tree, root);
		if (shadow && root.process(root.nodeIndex.getStream(FunctionDefinition.class)
			.filter(definition -> definition.getFunctionPrototype().getName().getName().equals("iris_undoRevZ")),
			FunctionDefinition::detachAndDelete)) {
			root.rename("iris_undoRevZ", "iris_toMinusOneToOne");
			injectClipRangeProjection(t, tree, zZeroToOne);
		}
		if (!shadow && type == PatchShaderType.FRAGMENT) {
			root.replaceReferenceExpressions(t, "gl_FragCoord",
				"vec4(gl_FragCoord.xy, 1.0 - gl_FragCoord.z, gl_FragCoord.w)");
			root.process(root.nodeIndex.getStream(AssignmentExpression.class)
				.filter(assignment -> isReference(assignment.getLeft(), "gl_FragDepth")), assignment -> {
					Expression value = assignment.getRight();
					assignment.setRight(null);
					root.indexBuildSession(() -> assignment.setRight(
						new SubtractionExpression(new LiteralExpression(Type.FLOAT32, 1.0),
							new GroupingExpression(value))));
				});
		}
	}

	public static void transformPosition(ASTParser t, Map<PatchShaderType, TranslationUnit> trees,
									 boolean zZeroToOne, boolean shadow) {
		if (shadow && !zZeroToOne) {
			return;
		}
		String positionRemap = shadow ? "gl_Position.z = 0.5f * (gl_Position.z + gl_Position.w)"
			: zZeroToOne ? "gl_Position.z = 0.5 * (gl_Position.w - gl_Position.z)"
			: "gl_Position.z = -gl_Position.z";
		TranslationUnit geometry = trees.get(PatchShaderType.GEOMETRY);
		// gl_in[].gl_Position and gl_PerVertex declarations are not output references.
		if (geometry != null && geometry.getRoot().nodeIndex.getStream(ReferenceExpression.class)
			.anyMatch(reference -> reference.getIdentifier().getName().equals("gl_Position"))) {
			Root root = geometry.getRoot();
			root.process(root.nodeIndex.getStream(FunctionCallExpression.class)
				.filter(call -> call.getFunctionName() != null)
				.filter(call -> switch (call.getFunctionName().getName()) {
					case "EmitVertex" -> call.getParameters().isEmpty();
					case "EmitStreamVertex" -> call.getParameters().size() == 1;
					default -> false;
				}), call -> {
					GroupingExpression wrapper = (GroupingExpression) t.parseExpression(root, "(" + positionRemap + ", 0)");
					call.replaceBy(wrapper);
					((SequenceExpression) wrapper.getOperand()).getExpressions().get(1).replaceByAndDelete(call);
				});
			return;
		}

		TranslationUnit lastStage = trees.get(PatchShaderType.TESS_EVAL);
		if (lastStage == null) {
			lastStage = trees.get(PatchShaderType.VERTEX);
		}
		if (lastStage != null) {
			lastStage.getRoot().rename("main", "iris_depthMain");
			lastStage.parseAndInjectNode(t, ASTInjectionPoint.END,
				"void main() { iris_depthMain(); " + positionRemap + "; }");
		}
	}

	static void injectForwardZProjection(ASTParser t, TranslationUnit tree, boolean zZeroToOne) {
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, zZeroToOne
			? """
				mat4 iris_undoRevZ(mat4 p) {
				    p[0][2] = p[0][3] - 2.0 * p[0][2];
				    p[1][2] = p[1][3] - 2.0 * p[1][2];
				    p[2][2] = p[2][3] - 2.0 * p[2][2];
				    p[3][2] = p[3][3] - 2.0 * p[3][2];
				    return p;
				}
			"""
			: """
				mat4 iris_undoRevZ(mat4 p) {
				    p[0][2] = -p[0][2];
				    p[1][2] = -p[1][2];
				    p[2][2] = -p[2][2];
				    p[3][2] = -p[3][2];
				    return p;
				}
			""");
	}

	static void injectClipRangeProjection(ASTParser t, TranslationUnit tree, boolean zZeroToOne) {
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, zZeroToOne
			? """
				mat4 iris_toMinusOneToOne(mat4 p) {
				    p[0][2] = 2.0 * p[0][2] - p[0][3];
				    p[1][2] = 2.0 * p[1][2] - p[1][3];
				    p[2][2] = 2.0 * p[2][2] - p[2][3];
				    p[3][2] = 2.0 * p[3][2] - p[3][3];
				    return p;
				}
			"""
			: "mat4 iris_toMinusOneToOne(mat4 p) { return p; }");
	}

	private static boolean isReference(Expression expression, String name) {
		while (expression instanceof GroupingExpression grouping) {
			expression = grouping.getOperand();
		}
		return expression instanceof ReferenceExpression reference && reference.getIdentifier().getName().equals(name);
	}

	private static void invertDepthReads(ASTParser t, TranslationUnit tree, Root root) {
		Set<FunctionParameter> depthParameters = specializeDepthFunctions(tree, root);
		root.process(root.nodeIndex.getStream(FunctionCallExpression.class)
			.filter(call -> isDepthRead(call, depthParameters)), call -> {
				FunctionCallExpression wrapper = (FunctionCallExpression) t.parseExpression(root, "vec4()");
				call.replaceBy(wrapper);
				root.indexBuildSession(() -> wrapper.getParameters().add(
					new SubtractionExpression(new LiteralExpression(Type.FLOAT32, 1.0),
						call.getFunctionName().getName().startsWith("textureGather")
							? call : new MemberAccessExpression(call, new Identifier("x")))));
			});
	}

	private record DepthSpecialization(String name, int arity, List<Integer> parameters) {
		boolean matches(FunctionPrototype prototype) {
			return prototype.getName().getName().equals(name) && prototype.getChildren().size() == arity
				&& parameters.stream().allMatch(index -> {
					FunctionParameter parameter = prototype.getChildren().get(index);
					return isSamplerType(parameter, BuiltinFixedTypeSpecifier.BuiltinType.SAMPLER2D);
				});
		}
	}

	private static Set<FunctionParameter> specializeDepthFunctions(TranslationUnit tree, Root root) {
		var declarations = List.copyOf(tree.getChildren());
		List<FunctionDefinition> definitions = declarations.stream()
			.filter(FunctionDefinition.class::isInstance).map(FunctionDefinition.class::cast).toList();
		Set<FunctionParameter> depthParameters = new HashSet<>();
		Map<DepthSpecialization, String> specializations = new HashMap<>();
		Map<FunctionCallExpression, String> renamedCalls = new HashMap<>();
		var calls = new ArrayDeque<>(root.nodeIndex.getStream(FunctionCallExpression.class).toList());
		while (!calls.isEmpty()) {
			FunctionCallExpression call = calls.removeFirst();
			if (call.getFunctionName() == null) {
				continue;
			}
			List<Integer> indices = new ArrayList<>();
			for (int i = 0; i < call.getParameters().size(); i++) {
				if (isDepthSampler(call.getParameters().get(i), depthParameters)) {
					indices.add(i);
				}
			}
			if (indices.isEmpty()) {
				continue;
			}
			DepthSpecialization key = new DepthSpecialization(call.getFunctionName().getName(),
				call.getParameters().size(), List.copyOf(indices));
			String name = specializations.get(key);
			if (name == null) {
				List<FunctionDefinition> overloads = definitions.stream()
					.filter(definition -> key.matches(definition.getFunctionPrototype())).toList();
				if (overloads.isEmpty()) {
					continue;
				}
				name = "iris_depth_" + specializations.size() + "_" + key.name();
				specializations.put(key, name);
				for (FunctionDefinition original : overloads) {
					FunctionDefinition specialized = original.cloneInto(root);
					specialized.getFunctionPrototype().getName().setName(name);
					for (int index : indices) {
						depthParameters.add(specialized.getFunctionPrototype().getChildren().get(index));
					}
					tree.getChildren().add(tree.getChildren().indexOf(original) + 1, specialized);
					calls.addAll(root.nodeIndex.getStream(FunctionCallExpression.class)
						.filter(nested -> nested.getAncestor(FunctionDefinition.class) == specialized).toList());
				}
				for (var declaration : declarations) {
					if (declaration instanceof DeclarationExternalDeclaration external
						&& external.getDeclaration() instanceof FunctionDeclaration function
						&& key.matches(function.getFunctionPrototype())) {
						DeclarationExternalDeclaration specialized = external.cloneInto(root);
						((FunctionDeclaration) specialized.getDeclaration()).getFunctionPrototype().getName().setName(name);
						tree.getChildren().add(tree.getChildren().indexOf(external) + 1, specialized);
					}
				}
			}
			renamedCalls.put(call, name);
		}
		renamedCalls.forEach((call, name) -> call.getFunctionName().setName(name));
		return depthParameters;
	}

	private static boolean isSamplerType(FunctionParameter parameter, BuiltinFixedTypeSpecifier.BuiltinType type) {
		return parameter.getType().getTypeSpecifier() instanceof BuiltinFixedTypeSpecifier fixed && fixed.type == type;
	}

	private static FunctionParameter getSamplerParameter(Expression expression) {
		FunctionDefinition definition = expression.getAncestor(FunctionDefinition.class);
		if (definition == null) {
			return null;
		}
		String name = getReferenceName(expression);
		return definition.getFunctionPrototype().getChildren().stream()
			.filter(parameter -> parameter.getName() != null && parameter.getName().getName().equals(name))
			.findFirst().orElse(null);
	}

	private static boolean isDepthSampler(Expression expression, Set<FunctionParameter> depthParameters) {
		FunctionParameter parameter = getSamplerParameter(expression);
		return parameter != null ? depthParameters.contains(parameter) : DEPTH_SAMPLERS.contains(getReferenceName(expression));
	}

	private static String getReferenceName(Expression sampler) {
		while (sampler instanceof GroupingExpression grouping) {
			sampler = grouping.getOperand();
		}
		return sampler instanceof ReferenceExpression reference ? reference.getIdentifier().getName() : "";
	}

	private static boolean isDepthRead(FunctionCallExpression call, Set<FunctionParameter> depthParameters) {
		if (call.getFunctionName() == null || call.getParameters().isEmpty()) {
			return false;
		}
		String name = call.getFunctionName().getName();
		if (name.endsWith("ARB") || name.endsWith("EXT") || name.endsWith("OES")) {
			name = name.substring(0, name.length() - 3);
		}
		if (!TEXTURE_READ_FUNCTIONS.contains(name)) {
			return false;
		}
		return isDepthSampler(call.getParameters().getFirst(), depthParameters);
	}

}
