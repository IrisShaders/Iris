package net.irisshaders.iris.pipeline.transform.vulkan;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionDeclaration;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionParameter;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.ConditionExpression;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.ArrayAccessExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.GroupingExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.type.initializer.NestedInitializer;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.PreciseQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.PrecisionQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.transformer.LayoutTransformer;
import net.irisshaders.iris.samplers.SamplerBindingPlan;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import net.irisshaders.iris.targets.ColorAttachmentPlan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class VulkanShaderPatcher {
	private VulkanShaderPatcher() {
	}

	public static Result patch(Map<PatchShaderType, String> sources) {
		return patch(sources, null);
	}

	public static Result patch(Map<PatchShaderType, String> sources, SamplerBindingPlan samplers) {
		return patch(sources, samplers, null);
	}

	public static Result patch(Map<PatchShaderType, String> sources, SamplerBindingPlan samplers, ColorAttachmentPlan.Program outputs) {
		return patch(sources, samplers, outputs, name -> false);
	}

	public static Result discover(Map<PatchShaderType, String> sources) {
		return patch(sources, null, null, name -> true);
	}

	public static Result patch(Map<PatchShaderType, String> sources, SamplerBindingPlan samplers, ColorAttachmentPlan.Program outputs,
		Predicate<String> registered) {
		return patch(sources, samplers, outputs, registered, null, name -> true);
	}

	public static Result patch(Map<PatchShaderType, String> sources, SamplerBindingPlan samplers, ColorAttachmentPlan.Program outputs,
		Predicate<String> registered, VulkanResourceLayout frameLayout, Predicate<String> dynamic) {
		var parser = new SingleASTTransformer<>();
		var trees = new EnumMap<PatchShaderType, TranslationUnit>(PatchShaderType.class);

		for (var entry : sources.entrySet()) {
			if (entry.getValue() != null) {
				trees.put(entry.getKey(), parser.parseSeparateTranslationUnit(entry.getValue()));
			}
		}

		trees.forEach((stage, tree) -> {
			removeUnreachableFunctions(tree);

			for (var declaration : tree.getRoot().nodeIndex.getStream(TypeAndInitDeclaration.class).toList()) {
				var qualifier = declaration.getType().getTypeQualifier();
				boolean input = stage != PatchShaderType.VERTEX && VulkanGlsl.hasStorage(qualifier, StorageQualifier.StorageType.IN);
				boolean opaqueUniform = VulkanGlsl.hasStorage(qualifier, StorageQualifier.StorageType.UNIFORM) &&
					declaration.getType().getTypeSpecifier() instanceof BuiltinFixedTypeSpecifier opaque &&
					(opaque.type.kind == BuiltinFixedTypeSpecifier.BuiltinType.TypeKind.SAMPLER ||
						opaque.type.kind == BuiltinFixedTypeSpecifier.BuiltinType.TypeKind.IMAGE);

				if (!input && !opaqueUniform) {
					continue;
				}

				for (var member : new ArrayList<>(declaration.getMembers())) {
					String name = member.getName().getName();
					boolean used = tree.getRoot().identifierIndex.getStream(name).map(id -> id.getParent())
						.filter(ReferenceExpression.class::isInstance).map(ReferenceExpression.class::cast)
						.anyMatch(reference -> !VulkanGlsl.isShadowed(reference, name));

					if (!name.startsWith("gl_") && !used) {
						member.detachAndDelete();
					}
				}

				if (declaration.getMembers().isEmpty()) {
					declaration.getParent().detachAndDelete();
				}
			}
		});
		LayoutTransformer.transformGrouped(parser, trees, null);

		BitSet written = outputs == null ? null : remapFragmentOutputs(parser, trees.get(PatchShaderType.FRAGMENT), outputs);
		VulkanResourceLayout layout = VulkanResourceTransformer.transform(parser, trees, registered, frameLayout, dynamic);

		if (samplers != null) {
			layout = lowerSamplerHeap(parser, trees, layout, samplers);
		}

		String samplerName = "iris_legacySampler";

		for (int suffix = 0; ; suffix++) {
			String candidate = samplerName;

			if (trees.values().stream().noneMatch(tree -> tree.getRoot().identifierIndex.has(candidate))) {
				break;
			}

			samplerName = "iris_legacySampler" + suffix;
		}

		for (var tree : trees.values()) {
			tree.getRoot().rename("sampler", samplerName);
		}

		for (var tree : trees.values()) {
			for (String name : List.of("tanh", "fma")) {
				boolean declared = tree.getRoot().nodeIndex.getStream(FunctionDefinition.class)
					.anyMatch(function -> function.getFunctionPrototype().getName().getName().equals(name));

				if (!declared) {
					continue;
				}

				String replacement = "iris_legacy_" + name;

				for (int suffix = 0; tree.getRoot().identifierIndex.has(replacement); suffix++) {
					replacement = "iris_legacy_" + name + suffix;
				}

				tree.getRoot().rename(name, replacement);
			}
		}

		var result = new EnumMap<PatchShaderType, String>(PatchShaderType.class);

		for (var entry : trees.entrySet()) {
			initializeGlobals(parser, entry.getValue());
			result.put(entry.getKey(), ASTPrinter.print(PrintType.INDENTED, entry.getValue()));
		}

		return new Result(result, layout, samplers, written);
	}

	private static void initializeGlobals(ASTParser parser, TranslationUnit tree) {
		boolean initialized = false;

		for (var external : List.copyOf(tree.getChildren())) {
			if (!(external instanceof DeclarationExternalDeclaration declaration) ||
				!(declaration.getDeclaration() instanceof TypeAndInitDeclaration variables) ||
				variables.getType().getTypeSpecifier() instanceof BuiltinFixedTypeSpecifier) {
				continue;
			}

			var qualifier = variables.getType().getTypeQualifier();
			if (qualifier != null && qualifier.getParts().stream().anyMatch(StorageQualifier.class::isInstance)) {
				continue;
			}

			for (var member : variables.getMembers()) {
				if (member.getInitializer() == null && !member.getName().getName().startsWith("gl_")) {
					member.setInitializer(new NestedInitializer());
					initialized = true;
				}
			}
		}

		if (initialized) {
			tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_ALL, "#extension GL_EXT_null_initializer : require\n");
		}
	}

	private static void removeUnreachableFunctions(TranslationUnit tree) {
		var calls = new HashMap<String, Set<String>>();
		var pending = new ArrayDeque<String>();
		pending.add("main");

		for (var call : tree.getRoot().nodeIndex.getStream(FunctionCallExpression.class).toList()) {
			if (call.getFunctionName() == null) {
				continue;
			}

			String name = call.getFunctionName().getName();
			var function = call.getAncestor(FunctionDefinition.class);

			if (function == null) {
				pending.add(name);
			} else {
				calls.computeIfAbsent(function.getFunctionPrototype().getName().getName(), ignored -> new HashSet<>()).add(name);
			}
		}

		var reachable = new HashSet<String>();

		while (!pending.isEmpty()) {
			String name = pending.removeFirst();

			if (reachable.add(name)) {
				pending.addAll(calls.getOrDefault(name, Set.of()));
			}
		}

		for (var function : tree.getRoot().nodeIndex.getStream(FunctionDefinition.class).toList()) {
			if (!reachable.contains(function.getFunctionPrototype().getName().getName())) {
				function.detachAndDelete();
			}
		}

		for (var declaration : tree.getRoot().nodeIndex.getStream(FunctionDeclaration.class).toList()) {
			if (!reachable.contains(declaration.getFunctionPrototype().getName().getName())) {
				declaration.getParent().detachAndDelete();
			}
		}
	}

	public record Result(Map<PatchShaderType, String> sources, VulkanResourceLayout layout, SamplerBindingPlan samplers,
		BitSet writtenAttachments) {
		public Result {
			sources = Map.copyOf(sources);
			writtenAttachments = writtenAttachments == null ? null : (BitSet) writtenAttachments.clone();
		}

		@Override
		public BitSet writtenAttachments() {
			return writtenAttachments == null ? null : (BitSet) writtenAttachments.clone();
		}
	}

	private static BitSet remapFragmentOutputs(ASTParser parser, TranslationUnit tree, ColorAttachmentPlan.Program plan) {
		var written = new BitSet(plan.attachmentCount());

		if (tree == null) {
			return written;
		}

		var root = tree.getRoot();
		var copies = new StringBuilder();
		root.indexBuildSession(() -> {
			for (var external : List.copyOf(tree.getChildren())) {
				if (!(external instanceof DeclarationExternalDeclaration declaration) ||
					!(declaration.getDeclaration() instanceof TypeAndInitDeclaration typed)) {
					continue;
				}

				var qualifier = typed.getType().getTypeQualifier();

				if (!VulkanGlsl.hasStorage(qualifier, StorageQualifier.StorageType.OUT)) {
					continue;
				}

				if (typed.getMembers().stream().allMatch(m -> m.getName().getName().startsWith("gl_"))) {
					continue;
				}

				if (typed.getMembers().size() != 1) {
					throw new IllegalArgumentException("Split output declarations before remapping");
				}

				var member = typed.getMembers().getFirst();

				if (!(typed.getType().getTypeSpecifier() instanceof BuiltinNumericTypeSpecifier numeric) || numeric.type.isMatrix()) {
					throw new IllegalArgumentException("Unsupported fragment output type: " + member.getName().getName());
				}

				var index = VulkanGlsl.layout(qualifier, "index");

				if (index != null && VulkanGlsl.integer(index.getExpression(), root) != 0) {
					throw new IllegalArgumentException("Dual-source fragment outputs require a separate pipeline");
				}

				var location = VulkanGlsl.layout(qualifier, "location");

				if (location == null) {
					throw new IllegalArgumentException("Allocate output locations before remapping");
				}

				int first = VulkanGlsl.integer(location.getExpression(), root);
				var dimensions = VulkanGlsl.dimensions(root, member.getArraySpecifier(), numeric.getArraySpecifier());

				if (dimensions.size() > 1) {
					throw new IllegalArgumentException("Multidimensional fragment output arrays are unsupported");
				}

				int count = dimensions.isEmpty() ? 1 : dimensions.getFirst();
				int mapped = plan.attachment(first);

				if (dimensions.isEmpty() && mapped >= 0) {
					VulkanGlsl.setLayout(qualifier, "location", mapped);
					written.set(mapped);
					continue;
				}

				var outputQualifier = qualifier.cloneInto(root);

				for (var part : List.copyOf(qualifier.getParts())) {
					if (!(part instanceof PrecisionQualifier || part instanceof PreciseQualifier)) {
						part.detachAndDelete();
					}
				}

				for (int i = 0; i < count; i++) {
					int attachment = plan.attachment(Math.addExact(first, i));

					if (attachment < 0) {
						continue;
					}

					String name = "iris_vkColorOutput" + attachment;

					if (root.identifierIndex.has(name)) {
						throw new IllegalArgumentException("Reserved fragment output name: " + name);
					}

					var scalar = numeric.cloneInto(root);

					if (scalar.getArraySpecifier() != null) {
						scalar.getArraySpecifier().detachAndDelete();
					}

					VulkanGlsl.setLayout(outputQualifier, "location", attachment);
					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
						ASTPrinter.print(PrintType.COMPACT, outputQualifier) + " " + ASTPrinter.print(PrintType.COMPACT, scalar) + " " +
							name + ";");
					copies.append(name).append('=').append(member.getName().getName());

					if (!dimensions.isEmpty()) {
						copies.append('[').append(i).append(']');
					}

					copies.append(';');
					written.set(attachment);
				}
			}

			if (!copies.isEmpty()) {
				if (root.identifierIndex.has("iris_vkFragmentMain")) {
					throw new IllegalArgumentException("Reserved fragment entry point");
				}

				root.rename("main", "iris_vkFragmentMain");
				tree.getChildren().add(parser.parseExternalDeclaration(root, "void main(){iris_vkFragmentMain();" + copies + "}"));
			}
		});

		return written;
	}


	private static VulkanResourceLayout lowerSamplerHeap(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees,
		VulkanResourceLayout resources, SamplerBindingPlan plan) {
		int indexBinding =
			resources.descriptors().stream().filter(d -> d.set() == 0).mapToInt(VulkanResourceLayout.Descriptor::binding).max().orElse(-1) +
				1;
		Map<SamplerHeapLayout.Type, Set<PatchShaderType>> heapStages = new EnumMap<>(SamplerHeapLayout.Type.class);

		for (var entry : trees.entrySet()) {
			TranslationUnit tree = entry.getValue();

			if (tree == null) {
				continue;
			}

			var root = tree.getRoot();
			Set<SamplerHeapLayout.Type> used = EnumSet.noneOf(SamplerHeapLayout.Type.class);
			root.indexBuildSession(() -> {
				for (var external : List.copyOf(tree.getChildren())) {
					if (!(external instanceof DeclarationExternalDeclaration declaration) ||
						!(declaration.getDeclaration() instanceof TypeAndInitDeclaration variables) ||
						!VulkanGlsl.hasStorage(variables.getType().getTypeQualifier(), StorageType.UNIFORM) ||
						!(variables.getType().getTypeSpecifier() instanceof BuiltinFixedTypeSpecifier opaque) ||
						opaque.type.kind != BuiltinFixedTypeSpecifier.BuiltinType.TypeKind.SAMPLER) {
						continue;
					}

					SamplerHeapLayout.Type type;

					try {
						type = SamplerHeapLayout.Type.valueOf(opaque.type.name());
					} catch (IllegalArgumentException exception) {
						throw new IllegalArgumentException("Unsupported sampler heap type: " + opaque.type, exception);
					}

					for (var member : variables.getMembers()) {
						String name = member.getName().getName();
						var dimensions = VulkanGlsl.dimensions(root, member.getArraySpecifier(), opaque.getArraySpecifier());
						int count = dimensions.stream().reduce(1, Math::multiplyExact);
						var slots = plan.require(name, type, count);
						var references =
							root.identifierIndex.get(name).stream().filter(id -> id.getParent() instanceof ReferenceExpression)
							.map(id -> (ReferenceExpression) id.getParent()).toList();

						for (var reference : references) {
							if (VulkanGlsl.isShadowed(reference, name)) {
								continue;
							}

							Expression access = reference;
							int element = 0;

							for (int dimension : dimensions) {
								if (!(access.getParent() instanceof ArrayAccessExpression array) || array.getLeft() != access) {
									throw new IllegalArgumentException("Sampler arrays must be indexed before heap lowering: " + name);
								}

								int index;

								try {
									index = VulkanGlsl.integer(array.getRight(), root);
								} catch (IllegalArgumentException exception) {
									throw new IllegalArgumentException(
										"Dynamic sampler array indexing needs a draw/material index plan: " + name, exception);
								}

								if (index < 0 || index >= dimension) {
									throw new IllegalArgumentException("Sampler array index out of range: " + name);
								}

								element = Math.addExact(Math.multiplyExact(element, dimension), index);
								access = array;
							}

							var slot = slots.get(element);
							access.replaceByAndDelete(parser.parseExpression(root, type.uniformName() + "[" + (plan.indirect() ?
								"iris_samplerIndices.slots[" + plan.indexSlots().indexOf(slot) / 4 + "][" +
									plan.indexSlots().indexOf(slot) % 4 + "]" : slot.index()) + "]"));
							used.add(type);
						}
					}

					declaration.detachAndDelete();
				}

				if (lowerSelections(parser, tree, used)) {
					markSamplerParameters(parser, tree);
					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_ALL, "#extension GL_EXT_nonuniform_qualifier : require\n");
				}

				if (plan.indirect() && !used.isEmpty()) {
					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"layout(std140,set=0,binding=" + indexBinding + ") uniform " + SamplerBindingPlan.INDEX_BLOCK + " { ivec4 slots[" +
							plan.indexBytes() / 16 + "]; } iris_samplerIndices;");
				}

				for (var type : used) {
					if (root.identifierIndex.has(type.uniformName())) {

						boolean declared = root.identifierIndex.get(type.uniformName()).stream()
							.anyMatch(id -> id.getParent() instanceof DeclarationMember || id.getParent() instanceof FunctionParameter);

						if (declared) {
							throw new IllegalArgumentException("Reserved sampler heap identifier: " + type.uniformName());
						}
					}

					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"layout(set = " + SamplerHeapLayout.DESCRIPTOR_SET + ", binding = " + type.binding() + ") uniform " + type.glsl +
							" " + type.uniformName() + "[" + plan.heap().heaps().get(type).size() + "];");
					heapStages.computeIfAbsent(type, ignored -> EnumSet.noneOf(PatchShaderType.class)).add(entry.getKey());
				}
			});
		}

		List<VulkanResourceLayout.Descriptor> descriptors = new ArrayList<>();

		for (var descriptor : resources.descriptors()) {
			if (descriptor.kind() != VulkanResourceLayout.Kind.SAMPLER) {
				descriptors.add(descriptor);
			}
		}

		heapStages.forEach((type, stages) -> descriptors.add(
			new VulkanResourceLayout.Descriptor(type.uniformName(), VulkanResourceLayout.Kind.SAMPLER, type.name(),
				plan.heap().heaps().get(type).size(), SamplerHeapLayout.DESCRIPTOR_SET, type.binding(), null, stages)));

		if (plan.indirect() && !heapStages.isEmpty()) {
			var stages = EnumSet.noneOf(PatchShaderType.class);
			heapStages.values().forEach(stages::addAll);
			descriptors.add(
				new VulkanResourceLayout.Descriptor(SamplerBindingPlan.INDEX_BLOCK, VulkanResourceLayout.Kind.UNIFORM_BUFFER, "block", 1
					, 0,
					indexBinding, null, stages));
		}

		return new VulkanResourceLayout(resources.uniformBufferSize(), resources.uniforms(), descriptors);
	}

	private static boolean lowerSelections(ASTParser parser, TranslationUnit tree, Set<SamplerHeapLayout.Type> used) {
		var heaps = new HashSet<String>();
		used.forEach(type -> heaps.add(type.uniformName()));

		boolean modified = false;
		boolean changed;

		do {
			changed = false;

			for (var conditional : tree.getRoot().nodeIndex.getStream(ConditionExpression.class).toList()) {
				var left = heapAccess(conditional.getSecond(), heaps);
				var right = heapAccess(conditional.getThird(), heaps);

				if (left == null || right == null || !left.heap().equals(right.heap())) {
					continue;
				}

				String selector = ASTPrinter.print(PrintType.COMPACT, conditional.getFirst());
				conditional.replaceByAndDelete(parser.parseExpression(tree.getRoot(),
					left.heap() + "[nonuniformEXT((" + selector + ") ? (" + left.index() + ") : (" + right.index() + "))]"));
				modified = changed = true;
				break;
			}
		} while (changed);

		return modified;
	}

	private static void markSamplerParameters(ASTParser parser, TranslationUnit tree) {
		var root = tree.getRoot();

		for (var parameter : root.nodeIndex.getStream(FunctionParameter.class).toList()) {
			if (parameter.getName() == null || !(parameter.getType().getTypeSpecifier() instanceof BuiltinFixedTypeSpecifier opaque) ||
				opaque.type.kind != BuiltinFixedTypeSpecifier.BuiltinType.TypeKind.SAMPLER) {
				continue;
			}

			var function = parameter.getAncestor(FunctionDefinition.class);

			if (function == null) {
				continue;
			}

			String name = parameter.getName().getName();

			for (var identifier : root.identifierIndex.getStream(name).toList()) {
				if (!(identifier.getParent() instanceof ReferenceExpression reference) ||
					reference.getAncestor(FunctionDefinition.class) != function || VulkanGlsl.isShadowed(reference, name, parameter)) {
					continue;
				}

				Expression access = reference;

				while (access.getParent() instanceof ArrayAccessExpression array && array.getLeft() == access) {
					access = array;
				}

				String source = ASTPrinter.print(PrintType.COMPACT, access);
				access.replaceByAndDelete(parser.parseExpression(root, "nonuniformEXT(" + source + ")"));
			}
		}
	}

	private static HeapAccess heapAccess(Expression expression, Set<String> heaps) {
		while (expression instanceof GroupingExpression grouping) {
			expression = grouping.getOperand();
		}

		if (!(expression instanceof ArrayAccessExpression access) || !(access.getLeft() instanceof ReferenceExpression reference) ||
			!heaps.contains(reference.getIdentifier().getName())) {
			return null;
		}

		return new HeapAccess(reference.getIdentifier().getName(), ASTPrinter.print(PrintType.COMPACT, access.getRight()));
	}

	private record HeapAccess(String heap, String index) {
	}

}
