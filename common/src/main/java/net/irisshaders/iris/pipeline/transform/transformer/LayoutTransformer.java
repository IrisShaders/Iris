package net.irisshaders.iris.pipeline.transform.transformer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.InterfaceBlockDeclaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.ArraySpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeReference;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructBody;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanGlsl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public final class LayoutTransformer {
	private static final List<PatchShaderType> PIPELINE = List.of(PatchShaderType.VERTEX, PatchShaderType.TESS_CONTROL,
		PatchShaderType.TESS_EVAL, PatchShaderType.GEOMETRY, PatchShaderType.FRAGMENT);

	private LayoutTransformer() {
	}

	public static void transformGrouped(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees, Parameters parameters) {
		List<Variable> previous = List.of();

		for (PatchShaderType stage : PIPELINE) {
			TranslationUnit tree = trees.get(stage);

			if (tree == null) {
				continue;
			}

			List<Variable> inputs = collect(tree, stage, StorageType.IN);
			assign(previous, inputs);
			previous = collect(tree, stage, StorageType.OUT);
		}

		assign(previous, List.of());
	}

	private static List<Variable> collect(TranslationUnit tree, PatchShaderType stage, StorageType direction) {
		List<Variable> variables = new ArrayList<>();
		Root root = tree.getRoot();
		root.indexBuildSession(() -> {
			for (var external : List.copyOf(tree.getChildren())) {
				if (!(external instanceof DeclarationExternalDeclaration declaration)) {
					continue;
				}

				if (declaration.getDeclaration() instanceof TypeAndInitDeclaration typed) {
					var qualifier = typed.getType().getTypeQualifier();

					if (!VulkanGlsl.hasStorage(qualifier, direction)) {
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
							VulkanGlsl.setLayout(splitQualifier, "location", nextLocation);
						}

						variables.add(new Variable(member.getName().getName(), splitQualifier, size));
						tree.getChildren().add(tree.getChildren().indexOf(declaration), new DeclarationExternalDeclaration(split));
						nextLocation = Math.addExact(nextLocation, size);
					}

					if (typed.getMembers().stream().noneMatch(m -> m.getName().getName().startsWith("gl_"))) {
						declaration.detachAndDelete();
					}
				} else if (declaration.getDeclaration() instanceof InterfaceBlockDeclaration block) {
					var qualifier = block.getTypeQualifier();

					if (!VulkanGlsl.hasStorage(qualifier, direction) || block.getBlockName().getName().startsWith("gl_")) {
						continue;
					}

					int size = blockLocations(block.getStructBody(), root);
					size = arrayLocations(size, root, perVertex(stage, direction, qualifier), block.getArraySpecifier());
					variables.add(new Variable(block.getBlockName().getName(), qualifier, size));
				}
			}
		});

		return variables;
	}

	private static boolean perVertex(PatchShaderType stage, StorageType direction, TypeQualifier qualifier) {
		if (VulkanGlsl.hasStorage(qualifier, StorageType.PATCH)) {
			return false;
		}

		return direction == StorageType.IN &&
			(stage == PatchShaderType.TESS_CONTROL || stage == PatchShaderType.TESS_EVAL || stage == PatchShaderType.GEOMETRY)
			|| direction == StorageType.OUT && stage == PatchShaderType.TESS_CONTROL;
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
				.orElseThrow(() -> new IllegalArgumentException("Missing interface struct: " + reference.getReference().getName()));
			size = blockLocations(struct.getStructBody(), root);
		} else {
			throw new IllegalArgumentException("Unsupported stage interface type: " + type);
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

				int length = VulkanGlsl.integer(dimension, root);

				if (length <= 0) {
					throw new IllegalArgumentException("Invalid interface array size");
				}

				size = Math.multiplyExact(size, length);
			}
		}

		return size;
	}

	private static int blockLocations(StructBody body, Root root) {
		int size = 0;

		for (var member : body.getMembers()) {
			if (VulkanGlsl.layout(member.getType().getTypeQualifier(), "location") != null) {
				throw new IllegalArgumentException("Explicit interface block member locations need separate allocation");
			}

			for (var declarator : member.getDeclarators()) {
				size = Math.addExact(size, locations(member.getType().getTypeSpecifier(), declarator.getArraySpecifier(), root, false));
			}
		}

		return size;
	}

	private static Integer location(TypeQualifier qualifier, Root root) {
		var part = VulkanGlsl.layout(qualifier, "location");

		return part == null ? null : VulkanGlsl.integer(part.getExpression(), root);
	}

	private static void assign(List<Variable> outputs, List<Variable> inputs) {
		Map<String, List<Variable>> groups = new TreeMap<>();
		Map<String, BitSet> occupiedOutputs = new HashMap<>(), occupiedInputs = new HashMap<>();

		for (var side : List.of(outputs, inputs)) {
			Map<String, BitSet> occupied = side == outputs ? occupiedOutputs : occupiedInputs;

			for (Variable variable : side) {
				groups.computeIfAbsent(variable.namespace() + ":" + variable.name, ignored -> new ArrayList<>()).add(variable);

				if (variable.location != null) {
					reserve(occupied, variable, variable.location);
				}
			}
		}
		// Propagate explicit locations before allocating any implicit ones.
		for (var group : groups.values()) {
			Integer explicit = null;

			for (Variable variable : group) {
				if (variable.location == null) {
					continue;
				}

				if (explicit != null && !explicit.equals(variable.location)) {
					throw new IllegalArgumentException("Conflicting stage locations: " + variable.name);
				}

				explicit = variable.location;
			}

			if (explicit != null) {
				assignGroup(group, outputs, occupiedOutputs, occupiedInputs, explicit);
			}
		}

		for (var group : groups.values()) {
			if (group.getFirst().location != null) {
				continue;
			}

			int location = 0;

			while (!fits(group, outputs, occupiedOutputs, occupiedInputs, location)) {
				location++;
			}

			assignGroup(group, outputs, occupiedOutputs, occupiedInputs, location);
		}
	}

	private static boolean fits(List<Variable> group, List<Variable> outputs, Map<String, BitSet> out, Map<String, BitSet> in,
		int location) {
		for (var variable : group) {
			var occupied = (outputs.contains(variable) ? out : in).computeIfAbsent(variable.namespace(), ignored -> new BitSet());
			int start = Math.multiplyExact(location, 4);
			int end = Math.addExact(start, Math.multiplyExact(variable.size, 4));
			int used = occupied.nextSetBit(start);

			if (used >= 0 && used < end) {
				return false;
			}
		}

		return true;
	}

	private static void assignGroup(List<Variable> group, List<Variable> outputs, Map<String, BitSet> out, Map<String, BitSet> in,
		int location) {
		for (var variable : group) {
			if (variable.location != null) {
				continue;
			}

			reserve(outputs.contains(variable) ? out : in, variable, location);
			variable.qualifier.getRoot().indexBuildSession(() -> VulkanGlsl.setLayout(variable.qualifier, "location", location));
			variable.location = location;
		}
	}

	private static void reserve(Map<String, BitSet> map, Variable variable, int location) {
		if (location < 0 || location > 65535) {
			throw new IllegalArgumentException("Invalid interface location: " + location);
		}

		if (VulkanGlsl.layout(variable.qualifier, "component") != null) {
			throw new IllegalArgumentException("Component-qualified interfaces need component allocation: " + variable.name);
		}

		var occupied = map.computeIfAbsent(variable.namespace(), ignored -> new BitSet());
		int start = Math.multiplyExact(location, 4);
		int end = Math.addExact(start, Math.multiplyExact(variable.size, 4));
		int used = occupied.nextSetBit(start);

		if (used >= 0 && used < end) {
			throw new IllegalArgumentException("Overlapping interface locations: " + variable.name);
		}

		occupied.set(start, end);
	}

	private static final class Variable {
		final String name;
		final TypeQualifier qualifier;
		final int size;
		Integer location;

		Variable(String name, TypeQualifier qualifier, int size) {
			this.name = name;
			this.qualifier = qualifier;
			this.size = size;
			this.location = location(qualifier, qualifier.getRoot());
		}

		String namespace() {
			var index = VulkanGlsl.layout(qualifier, "index");

			return Integer.toString(index == null ? 0 : VulkanGlsl.integer(index.getExpression(), qualifier.getRoot()));
		}
	}
}
