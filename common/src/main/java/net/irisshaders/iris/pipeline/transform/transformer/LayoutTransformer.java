package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.declaration.InterfaceBlockDeclaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExtensionDirective;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.NamedLayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.ArraySpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeReference;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructBody;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructMember;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.Glsl;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class LayoutTransformer {
	private static final List<PatchShaderType> PIPELINE = List.of(PatchShaderType.VERTEX, PatchShaderType.TESS_CONTROL,
		PatchShaderType.TESS_EVAL, PatchShaderType.GEOMETRY, PatchShaderType.FRAGMENT);

	public static void transformGrouped(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees, boolean vulkan) {
		Set<String> memberBlocks = new HashSet<>();

		for (var tree : trees.values()) {
			tree.getRoot().nodeIndex.getStream(InterfaceBlockDeclaration.class).filter(block -> block.getStructBody().getMembers().stream()
					.anyMatch(member -> Glsl.layout(member.getType().getTypeQualifier(), "location") != null))
				.forEach(block -> memberBlocks.add(block.getBlockName().getName()));
		}

		flattenStructures(parser, trees);

		List<Variable> previous = List.of();

		for (PatchShaderType stage : PIPELINE) {
			TranslationUnit tree = trees.get(stage);

			if (tree == null) {
				continue;
			}

			List<Variable> inputs = collect(tree, stage, StorageType.IN, memberBlocks);
			assign(previous, inputs, vulkan);
			previous = collect(tree, stage, StorageType.OUT, memberBlocks);
		}

		assign(previous, List.of(), vulkan);

		if (!vulkan) {
			for (var stage : PIPELINE) {
				var tree = trees.get(stage);
				if (tree == null) continue;
				requireExtension(parser, tree, "GL_ARB_separate_shader_objects");
				boolean enhanced = tree.getRoot().nodeIndex.getStream(InterfaceBlockDeclaration.class)
					.anyMatch(block -> !block.getBlockName().getName().startsWith("gl_")
						&& (Glsl.hasStorage(block.getTypeQualifier(), StorageType.IN)
							|| Glsl.hasStorage(block.getTypeQualifier(), StorageType.OUT)))
					|| tree.getRoot().nodeIndex.getStream(NamedLayoutQualifierPart.class)
						.anyMatch(part -> part.getName().getName().equals("component"));
				if (enhanced && tree.getVersionStatement().version.number < 440) {
					requireExtension(parser, tree, "GL_ARB_enhanced_layouts");
				}
			}
		}
	}

	private static void requireExtension(ASTParser parser, TranslationUnit tree, String name) {
		tree.getRoot().nodeIndex.getStream(ExtensionDirective.class).filter(extension -> extension.getName().equals(name))
			.toList().forEach(ExtensionDirective::detachAndDelete);
		tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_ALL, "#extension " + name + " : require\n");
	}

	private static void flattenStructures(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees) {
		if (trees.containsKey(PatchShaderType.TESS_CONTROL)) {
			return;
		}

		Set<String> blocked = new HashSet<>();
		Set<String> identifiers = new HashSet<>();
		trees.values().forEach(tree -> tree.getRoot().nodeIndex.getStream(io.github.douira.glsl_transformer.ast.node.Identifier.class).forEach(id -> identifiers.add(id.getName())));
		for (var entry : trees.entrySet()) {
			var root = entry.getValue().getRoot();
			for (var declaration : root.nodeIndex.getStream(TypeAndInitDeclaration.class).toList()) {
				var qualifier = declaration.getType().getTypeQualifier();
				if (!Glsl.hasStorage(qualifier, StorageType.IN) && !Glsl.hasStorage(qualifier, StorageType.OUT)) {
					continue;
				}
				if (declaration.getMembers().size() != 1 || location(qualifier, root) != null || declaration.getType().getTypeSpecifier().getArraySpecifier() != null) {
					declaration.getMembers().forEach(member -> blocked.add(member.getName().getName()));
				}
				declaration.getMembers().stream().filter(member -> member.getArraySpecifier() != null)
					.forEach(member -> blocked.add(member.getName().getName()));
			}
		}

		Map<String, String> names = new HashMap<>();
		for (var entry : trees.entrySet()) {
			var tree = entry.getValue();
			var root = tree.getRoot();
			StringBuilder inputs = new StringBuilder();
			StringBuilder outputs = new StringBuilder();
			root.indexBuildSession(() -> {
				for (var external : List.copyOf(tree.getChildren())) {
					if (!(external instanceof DeclarationExternalDeclaration declaration) || !(declaration.getDeclaration() instanceof TypeAndInitDeclaration typed) || typed.getMembers().size() != 1) {
						continue;
					}
					var qualifier = typed.getType().getTypeQualifier();
					boolean input = Glsl.hasStorage(qualifier, StorageType.IN);
					boolean output = Glsl.hasStorage(qualifier, StorageType.OUT);
					String name = typed.getMembers().getFirst().getName().getName();
					if ((!input && !output) || input && entry.getKey() == PatchShaderType.VERTEX || output && entry.getKey() == PatchShaderType.FRAGMENT || blocked.contains(name)) {
						continue;
					}
					var leaves = new ArrayList<Leaf>();
					if (structure(typed.getType().getTypeSpecifier(), root) == null || !leaves(typed.getType().getTypeSpecifier(), "", root, leaves)) {
						continue;
					}
					String qualifiers = ASTPrinter.print(PrintType.COMPACT, qualifier);
					for (var leaf : leaves) {
						String key = name + leaf.path;
						String varying = names.computeIfAbsent(key, ignored -> {
							String candidate = "iris_varying_" + names.size();
							while (!identifiers.add(candidate)) candidate += "_";
							return candidate;
						});
						tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_FUNCTIONS, qualifiers + " " + leaf.type + " " + varying + ";");
						(input ? inputs : outputs).append(input ? key + "=" + varying : varying + "=" + key).append(';');
					}
					typed.getType().setTypeQualifier(null);
				}
				if (!inputs.isEmpty() || !outputs.isEmpty()) {
					String main = "iris_varying_main";
					while (root.identifierIndex.has(main)) main += "_";
					root.rename("main", main);
					if (entry.getKey() == PatchShaderType.GEOMETRY && !outputs.isEmpty()) {
						for (var call : root.nodeIndex.getStream(FunctionCallExpression.class).toList()) {
							if (call.getFunctionName() == null) continue;
							String function = call.getFunctionName().getName();
							if (!function.equals("EmitVertex") && !function.equals("EmitStreamVertex")) continue;
							String wrapper = "iris_varying_" + function;
							if (!root.identifierIndex.has(wrapper)) {
								tree.getChildren().add(0, parser.parseExternalDeclaration(root, "void " + wrapper + (function.equals("EmitVertex") ? "(){" + outputs + "EmitVertex();}" : "(int stream){" + outputs + "EmitStreamVertex(stream);}")));
							}
							call.getFunctionName().setName(wrapper);
						}
					}
					tree.getChildren().add(parser.parseExternalDeclaration(root, "void main(){" + inputs + main + "();" + outputs + "}"));
				}
			});
		}
	}

	private static StructSpecifier structure(TypeSpecifier type, Root root) {
		if (type instanceof StructSpecifier struct) return struct;
		if (!(type instanceof TypeReference reference)) return null;
		return root.nodeIndex.getStream(StructSpecifier.class).filter(struct -> struct.getName() != null && struct.getName().getName().equals(reference.getReference().getName())).findFirst().orElse(null);
	}

	private static boolean leaves(TypeSpecifier type, String path, Root root, List<Leaf> leaves) {
		if (type.getArraySpecifier() != null) return false;
		if (type instanceof BuiltinNumericTypeSpecifier) {
			leaves.add(new Leaf(path, ASTPrinter.print(PrintType.COMPACT, type)));
			return true;
		}
		var struct = structure(type, root);
		if (struct == null) return false;
		for (var member : struct.getStructBody().getMembers()) {
			for (var declarator : member.getDeclarators()) {
				if (declarator.getArraySpecifier() != null || !leaves(member.getType().getTypeSpecifier(), path + "." + declarator.getName().getName(), root, leaves)) return false;
			}
		}
		return true;
	}

	public record Leaf(String path, String type) {
	}

	private static List<Variable> collect(TranslationUnit tree, PatchShaderType stage, StorageType direction, Set<String> memberBlocks) {
		List<Variable> variables = new ArrayList<>();
		Root root = tree.getRoot();
		root.indexBuildSession(() -> {
			for (var external : List.copyOf(tree.getChildren())) {
				if (!(external instanceof DeclarationExternalDeclaration declaration)) {
					continue;
				}

				if (declaration.getDeclaration() instanceof TypeAndInitDeclaration typed) {
					var qualifier = typed.getType().getTypeQualifier();

					if (!Glsl.hasStorage(qualifier, direction)) {
						continue;
					}

					Integer explicit = location(qualifier, root);
					int nextLocation = explicit == null ? 0 : explicit;

					for (var member : List.copyOf(typed.getMembers())) {
						if (member.getName().getName().startsWith("gl_")) {
							continue;
						}

						var split = new TypeAndInitDeclaration(typed.getType().cloneInto(root), Stream.of(member.cloneInto(root)));
						var splitQualifier = split.getType().getTypeQualifier();
						int size = locations(typed.getType().getTypeSpecifier(), member.getArraySpecifier(), root,
							perVertex(stage, direction, qualifier));

						if (explicit != null) {
							Glsl.setLayout(splitQualifier, "location", nextLocation);
						}

						variables.add(new Variable(member.getName().getName(), splitQualifier, size, typed.getType().getTypeSpecifier()));
						tree.getChildren().add(tree.getChildren().indexOf(declaration), new DeclarationExternalDeclaration(split));
						nextLocation = Math.addExact(nextLocation, size);
					}

					if (typed.getMembers().stream().noneMatch(m -> m.getName().getName().startsWith("gl_"))) {
						declaration.detachAndDelete();
					}
				} else if (declaration.getDeclaration() instanceof InterfaceBlockDeclaration block) {
					var qualifier = block.getTypeQualifier();

					if (!Glsl.hasStorage(qualifier, direction) || block.getBlockName().getName().startsWith("gl_")) {
						continue;
					}

					if (memberBlocks.contains(block.getBlockName().getName())) {
						collectBlock(block, stage, direction, root, variables);
						continue;
					}

					int size = blockLocations(block.getStructBody(), root);
					size = arrayLocations(size, root, perVertex(stage, direction, qualifier), block.getArraySpecifier());
					variables.add(new Variable(block.getBlockName().getName(), qualifier, size, null));
				}
			}
		});

		return variables;
	}

	private static void collectBlock(InterfaceBlockDeclaration block, PatchShaderType stage, StorageType direction, Root root,
		List<Variable> variables) {
		var qualifier = block.getTypeQualifier();

		if (arrayLocations(1, root, perVertex(stage, direction, qualifier), block.getArraySpecifier()) != 1) {
			throw new IllegalArgumentException("member locations cannot be used on interface block arrays");
		}

		Integer nextLocation = location(qualifier, root);
		var body = block.getStructBody();

		for (var member : List.copyOf(body.getMembers())) {
			Integer explicit = location(member.getType().getTypeQualifier(), root);

			if (explicit != null) {
				nextLocation = explicit;
			}

			for (var declarator : member.getDeclarators()) {
				var split = new StructMember(member.getType().cloneInto(root), Stream.of(declarator.cloneInto(root)));
				var splitQualifier = split.getType().getTypeQualifier();

				if (splitQualifier == null) {
					splitQualifier = new TypeQualifier(Stream.empty());
					split.getType().setTypeQualifier(splitQualifier);
				}

				int size = locations(member.getType().getTypeSpecifier(), declarator.getArraySpecifier(), root, false);

				if (nextLocation != null) {
					Glsl.setLayout(splitQualifier, "location", nextLocation);
					nextLocation = Math.addExact(nextLocation, size);
				}

				variables.add(new Variable(block.getBlockName().getName() + "." + declarator.getName().getName(), splitQualifier, size,
					member.getType().getTypeSpecifier(), block.getBlockName().getName()));
				body.getMembers().add(body.getMembers().indexOf(member), split);
			}

			member.detachAndDelete();
		}

		Glsl.removeLayout(qualifier, "location");
	}

	private static boolean perVertex(PatchShaderType stage, StorageType direction, TypeQualifier qualifier) {
		if (Glsl.hasStorage(qualifier, StorageType.PATCH)) {
			return false;
		}

		return direction == StorageType.IN && (stage == PatchShaderType.TESS_CONTROL || stage == PatchShaderType.TESS_EVAL || stage == PatchShaderType.GEOMETRY) || direction == StorageType.OUT && stage == PatchShaderType.TESS_CONTROL;
	}

	private static int locations(TypeSpecifier type, ArraySpecifier array, Root root, boolean perVertex) {
		int size;

		if (type instanceof BuiltinNumericTypeSpecifier numeric) {
			int[] shape = numeric.type.getDimensions();
			int columns = numeric.type.isMatrix() ? shape[0] : 1;
			int rows = numeric.type.isScalar() ? 1 : shape[numeric.type.isMatrix() ? 1 : 0];
			size = columns * ((Math.max(32, numeric.type.getBitDepth()) * rows + 127) / 128);
		} else if (type instanceof StructSpecifier struct) {
			size = blockLocations(struct.getStructBody(), root);
		} else if (type instanceof TypeReference reference) {
			var struct = root.nodeIndex.getStream(StructSpecifier.class)
				.filter(s -> s.getName() != null && s.getName().getName().equals(reference.getReference().getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("missing interface struct: " + reference.getReference().getName()));
			size = blockLocations(struct.getStructBody(), root);
		} else {
			throw new IllegalArgumentException("unsupported stage interface type: " + type);
		}

		return arrayLocations(size, root, perVertex, array, type.getArraySpecifier());
	}

	private static int arrayLocations(int size, Root root, boolean skipOuter, ArraySpecifier... arrays) {
		for (var array : arrays) {
			if (array == null) {
				continue;
			}

			for (var dimension : array.getDimensions()) {
				if (skipOuter) {
					skipOuter = false;
					continue;
				}

				int length = Glsl.integer(dimension, root);

				if (length <= 0) {
					throw new IllegalArgumentException("invalid interface array size");
				}

				size = Math.multiplyExact(size, length);
			}
		}

		return size;
	}

	private static int blockLocations(StructBody body, Root root) {
		int size = 0;

		for (var member : body.getMembers()) {
			if (Glsl.layout(member.getType().getTypeQualifier(), "location") != null) {
				throw new IllegalArgumentException("explicit interface block member locations need separate allocation");
			}

			for (var declarator : member.getDeclarators()) {
				size = Math.addExact(size, locations(member.getType().getTypeSpecifier(), declarator.getArraySpecifier(), root, false));
			}
		}

		return size;
	}

	private static Integer location(TypeQualifier qualifier, Root root) {
		var part = Glsl.layout(qualifier, "location");

		return part == null ? null : Glsl.integer(part.getExpression(), root);
	}

	private static void assign(List<Variable> outputs, List<Variable> inputs, boolean packComponents) {
		Map<String, List<Variable>> groups = new TreeMap<>();
		Map<String, BitSet> occupiedOutputs = new HashMap<>();
		Map<String, BitSet> occupiedInputs = new HashMap<>();

		for (var side : List.of(outputs, inputs)) {
			Map<String, BitSet> occupied = side == outputs ? occupiedOutputs : occupiedInputs;

			for (Variable variable : side) {
				groups.computeIfAbsent(variable.namespace() + ":" + variable.name, ignored -> new ArrayList<>()).add(variable);

				if (variable.location != null) {
					reserve(occupied, variable, variable.location);
				}
			}
		}

		for (var group : groups.values()) {
			Integer explicit = null;

			for (Variable variable : group) {
				if (variable.location == null) {
					continue;
				}

				if (explicit != null && !explicit.equals(variable.location)) {
					throw new IllegalArgumentException("conflicting stage locations: " + variable.name);
				}

				explicit = variable.location;
			}

			if (explicit != null) {
				int component = group.stream().filter(variable -> variable.location != null).findFirst().orElseThrow().component;
				if (group.stream().anyMatch(variable -> variable.location != null && variable.component != component)) {
					throw new IllegalArgumentException("conflicting stage components: " + group.getFirst().name);
				}
				assignGroup(group, outputs, occupiedOutputs, occupiedInputs, explicit, component, false);
			}
		}

		for (var group : groups.values()) {
			if (group.getFirst().location != null) {
				continue;
			}

			boolean packed = packComponents && !outputs.isEmpty() && !inputs.isEmpty()
				&& group.stream().allMatch(variable -> variable.packable);
			int position = 0;
			while (!fits(group, outputs, inputs, occupiedOutputs, occupiedInputs, position / 4, packed ? position % 4 : 0, packed)) {
				position += packed ? 1 : 4;
			}
			assignGroup(group, outputs, occupiedOutputs, occupiedInputs, position / 4, packed ? position % 4 : 0, packed);
		}
	}

	private static boolean fits(List<Variable> group, List<Variable> outputs, List<Variable> inputs, Map<String, BitSet> out, Map<String, BitSet> in,
		int location, int component, boolean packed) {
		for (var variable : group) {
			if (packed && (component + variable.components > 4 || component % variable.alignment != 0)) return false;
			var occupied = (outputs.contains(variable) ? out : in).computeIfAbsent(variable.namespace(), ignored -> new BitSet());
			for (int i = 0; i < variable.size; i++) {
				int start = Math.addExact(Math.multiplyExact(Math.addExact(location, i), 4), component);
				int end = Math.addExact(start, packed || Glsl.layout(variable.qualifier, "component") != null ? variable.components : 4);
				int used = occupied.nextSetBit(start);

				if (used >= 0 && used < end) {
					return false;
				}
			}
			if (packed) {
				for (var other : outputs.contains(variable) ? outputs : inputs) {
					if (other.location != null && other.namespace().equals(variable.namespace()) && other.location < location + variable.size && location < other.location + other.size && !other.packing.equals(variable.packing)) return false;
				}
			}
		}

		return true;
	}

	private static void assignGroup(List<Variable> group, List<Variable> outputs, Map<String, BitSet> out, Map<String, BitSet> in,
		int location, int component, boolean packed) {
		for (var variable : group) {
			if (variable.location != null) {
				continue;
			}

			variable.component = component;
			reserve(outputs.contains(variable) ? out : in, variable, location);
			variable.qualifier.getRoot().indexBuildSession(() -> {
				Glsl.setLayout(variable.qualifier, "location", location);
				if (packed || component != 0) Glsl.setLayout(variable.qualifier, "component", component);
			});
			variable.location = location;
		}
	}

	private static void reserve(Map<String, BitSet> map, Variable variable, int location) {
		if (location < 0 || location > 65535) {
			throw new IllegalArgumentException("invalid interface location: " + location);
		}

		var occupied = map.computeIfAbsent(variable.namespace(), ignored -> new BitSet());

		for (int i = 0; i < variable.size; i++) {
			int start = Math.addExact(Math.multiplyExact(Math.addExact(location, i), 4), variable.component);
			int end = Math.addExact(start, variable.components);
			int used = occupied.nextSetBit(start);

			if (used >= 0 && used < end) {
				throw new IllegalArgumentException("overlapping interface locations: " + variable.name);
			}

			occupied.set(start, end);
		}
	}

	public static class Variable {
		private final String name;
		private final TypeQualifier qualifier;
		private final int size;
		private int component;
		private final int components;
		private final boolean packable;
		private final int alignment;
		private final String packing;
		private Integer location;

		private Variable(String name, TypeQualifier qualifier, int size, TypeSpecifier type) {
			this(name, qualifier, size, type, null);
		}

		private Variable(String name, TypeQualifier qualifier, int size, TypeSpecifier type, String interfaceBlock) {
			this.name = name;
			this.qualifier = qualifier;
			this.size = size;
			this.location = location(qualifier, qualifier.getRoot());
			var component = Glsl.layout(qualifier, "component");
			this.component = component == null ? 0 : Glsl.integer(component.getExpression(), qualifier.getRoot());
			int components = type instanceof BuiltinNumericTypeSpecifier numeric && !numeric.type.isMatrix() ? (numeric.type.isScalar() ? 1 : numeric.type.getDimensions()[0]) * Math.max(
				32, numeric.type.getBitDepth()) / 32 : 4;
			this.components = component == null && components > 4 ? 4 : components;
			this.packable = type instanceof BuiltinNumericTypeSpecifier numeric && !numeric.type.isMatrix() && components <= 4 && size == 1;
			this.alignment = type instanceof BuiltinNumericTypeSpecifier numeric && numeric.type.getBitDepth() == 64 ? 2 : 1;
			this.packing = interfaceBlock + ":" + (type instanceof BuiltinNumericTypeSpecifier numeric ? numeric.type.getNumberType() + ":" + numeric.type.getBitDepth() + ":" + qualifier.getParts().stream()
				.filter(part -> !(part instanceof io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifier) && !(part instanceof io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier))
				.map(part -> ASTPrinter.print(PrintType.COMPACT, part)).sorted().toList() : "aggregate");

			if (this.component < 0 || this.component + this.components > 4 || component != null && (!(type instanceof BuiltinNumericTypeSpecifier numeric) || numeric.type.isMatrix())) {
				throw new IllegalArgumentException("invalid interface component: " + name);
			}
		}

		private String namespace() {
			var index = Glsl.layout(this.qualifier, "index");

			return (Glsl.hasStorage(this.qualifier, StorageType.PATCH) ? "patch:" : "vertex:") + (index == null ? 0 : Glsl.integer(index.getExpression(), this.qualifier.getRoot()));
		}
	}
}
