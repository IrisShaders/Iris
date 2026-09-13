package net.irisshaders.iris.pipeline.transform.vulkan;

import io.github.douira.glsl_transformer.ast.node.Profile;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.declaration.InterfaceBlockDeclaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout.Descriptor;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout.Kind;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout.Uniform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class VulkanResourceTransformer {
	private VulkanResourceTransformer() {
	}

	private static void resolveDefaults(TranslationUnit tree, Predicate<String> registered) {
		var root = tree.getRoot();
		root.indexBuildSession(() -> {
			for (var external : List.copyOf(tree.getChildren())) {
				if (!(external instanceof DeclarationExternalDeclaration declaration) ||
					!(declaration.getDeclaration() instanceof TypeAndInitDeclaration variables) ||
					!VulkanGlsl.hasStorage(variables.getType().getTypeQualifier(), StorageType.UNIFORM) ||
					variables.getMembers().stream().noneMatch(member -> member.getInitializer() != null)) {
					continue;
				}

				for (var member : variables.getMembers()) {
					var copy = member.cloneInto(root);
					var type = variables.getType().cloneInto(root);

					if (copy.getInitializer() != null) {
						if (registered.test(copy.getName().getName())) {
							copy.getInitializer().detachAndDelete();
						} else {
							var qualifier = type.getTypeQualifier();

							for (var part : List.copyOf(qualifier.getParts())) {
								if (part instanceof StorageQualifier storage && storage.storageType == StorageType.UNIFORM) {
									part.detachAndDelete();
								}
							}

							qualifier.getParts().add(new StorageQualifier(StorageType.CONST));
							VulkanGlsl.removeLayout(qualifier, "location", "set", "binding", "offset");
						}
					}

					tree.getChildren().add(tree.getChildren().indexOf(declaration),
						new DeclarationExternalDeclaration(new TypeAndInitDeclaration(type, Stream.of(copy))));
				}

				declaration.detachAndDelete();
			}
		});
	}

	public static VulkanResourceLayout transform(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees) {
		return transform(parser, trees, name -> false);
	}

	public static VulkanResourceLayout transform(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees,
		Predicate<String> registered) {
		return transform(parser, trees, registered, null, name -> true);
	}

	public static VulkanResourceLayout transform(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees,
		Predicate<String> registered, VulkanResourceLayout frameLayout,
		Predicate<String> dynamic) {
		trees.values().stream().filter(Objects::nonNull).forEach(tree -> resolveDefaults(tree, registered));

		Map<String, Std140Type> types = new TreeMap<>();
		Map<String, Resource> resources = new TreeMap<>();
		List<DeclarationExternalDeclaration> looseUniforms = new ArrayList<>();
		Map<TranslationUnit, Set<String>> stageUniforms = new IdentityHashMap<>();

		for (var entry : trees.entrySet()) {
			TranslationUnit tree = entry.getValue();

			if (tree == null) {
				continue;
			}

			var root = tree.getRoot();
			root.indexBuildSession(() -> {
				for (var external : List.copyOf(tree.getChildren())) {
					if (!(external instanceof DeclarationExternalDeclaration declaration)) {
						continue;
					}

					if (declaration.getDeclaration() instanceof TypeAndInitDeclaration variables) {
						TypeQualifier qualifier = variables.getType().getTypeQualifier();

						if (!VulkanGlsl.hasStorage(qualifier, StorageType.UNIFORM)) {
							continue;
						}

						var type = variables.getType().getTypeSpecifier();

						if (type instanceof BuiltinFixedTypeSpecifier opaque) {
							Kind kind = switch (opaque.type.kind) {
								case SAMPLER -> Kind.SAMPLER;
								case IMAGE -> Kind.IMAGE;
								default -> throw new IllegalArgumentException("Vulkan resource lowering does not support " + opaque.type);
							};

							for (var member : List.copyOf(variables.getMembers())) {
								var split =
									new TypeAndInitDeclaration(variables.getType().cloneInto(root), Stream.of(member.cloneInto(root)));
								var newDeclaration = new DeclarationExternalDeclaration(split);
								tree.getChildren().add(tree.getChildren().indexOf(declaration), newDeclaration);

								var dimensions = VulkanGlsl.dimensions(root, member.getArraySpecifier(), type.getArraySpecifier());
								int count = dimensions.stream().reduce(1, Math::multiplyExact);
								String signature = opaque.type + dimensions.toString();
								addResource(resources, member.getName().getName(), kind, opaque.type.name(), count, signature,
									split.getType().getTypeQualifier(), entry.getKey(), tree);
							}

							declaration.detachAndDelete();
						} else if (type instanceof BuiltinNumericTypeSpecifier numeric) {
							for (var member : variables.getMembers()) {
								String name = member.getName().getName();
								stageUniforms.computeIfAbsent(tree, ignored -> new LinkedHashSet<>()).add(name);

								Std140Type uniformType = VulkanGlsl.uniformType(numeric, member, root);
								Std140Type previous = types.putIfAbsent(name, uniformType);

								if (previous != null && !previous.equals(uniformType)) {
									throw new IllegalArgumentException("Conflicting uniform types across stages: " + name);
								}
							}

							looseUniforms.add(declaration);
						} else {
							throw new IllegalArgumentException("Struct uniforms require Vulkan lowering: " +
								ASTPrinter.print(PrintType.COMPACT, declaration));
						}
					} else if (declaration.getDeclaration() instanceof InterfaceBlockDeclaration block) {
						TypeQualifier qualifier = block.getTypeQualifier();
						Kind kind = VulkanGlsl.hasStorage(qualifier, StorageType.BUFFER) ? Kind.STORAGE_BUFFER
							: VulkanGlsl.hasStorage(qualifier, StorageType.UNIFORM) ? Kind.UNIFORM_BUFFER
							: null;

						if (kind == null) {
							continue;
						}

						if (VulkanGlsl.layout(qualifier, "push_constant") != null) {
							throw new IllegalArgumentException("Shader pack push constants are not supported");
						}

						if (VulkanGlsl.layout(qualifier, "shared") != null || VulkanGlsl.layout(qualifier, "packed") != null) {
							throw new IllegalArgumentException("Vulkan requires an explicit std140/std430 block layout: " +
								block.getBlockName().getName());
						}

						var dimensions = VulkanGlsl.dimensions(root, block.getArraySpecifier());
						int count = dimensions.stream().reduce(1, Math::multiplyExact);
						String signature = ASTPrinter.print(PrintType.COMPACT, block.getStructBody()) + dimensions +
							(VulkanGlsl.layout(qualifier, "row_major") != null) + ":" + (VulkanGlsl.layout(qualifier, "std430") != null);
						addResource(resources, block.getBlockName().getName(), kind, "block", count, signature, qualifier, entry.getKey(),
							tree);
					}
				}
			});
		}

		if (frameLayout != null && resources.containsKey(VulkanResourceLayout.FRAME_BLOCK)) {
			throw new IllegalArgumentException("Reserved uniform block: " + VulkanResourceLayout.FRAME_BLOCK);
		}

		Map<String, Std140Type> frameTypes = new HashMap<>();

		if (frameLayout != null) {
			for (var entry : List.copyOf(types.entrySet())) {
				var shared = frameLayout.uniforms().get(entry.getKey());

				if (shared == null || dynamic.test(entry.getKey())) {
					continue;
				}

				var type = entry.getValue();

				if (!type.equals(shared.type()) && !(Set.of("int", "bool").contains(type.glsl()) &&
					Set.of("int", "bool").contains(shared.type().glsl()) && type.dimensions().isEmpty())) {
					throw new IllegalArgumentException("Conflicting shared uniform type: " + entry.getKey());
				}

				frameTypes.put(entry.getKey(), type);
				types.remove(entry.getKey());
			}
		}

		StringBuilder frameBody = new StringBuilder();

		if (frameLayout != null) {
			frameLayout.uniforms()
				.entrySet()
				.stream()
				.sorted(Comparator.comparingInt(e -> e.getValue().offset()))
				.forEach(entry
					-> frameBody.append("layout(offset = ")
					.append(entry.getValue().offset())
					.append(") ")
					.append(entry.getValue().type().declaration(entry.getKey())));
		}

		Map<String, Uniform> uniforms = new LinkedHashMap<>();
		int offset = 0;
		int alignment = 16;
		StringBuilder body = new StringBuilder();

		for (var entry : types.entrySet()) {
			Std140Type type = entry.getValue();
			offset = Std140Type.align(offset, type.alignment());
			alignment = Math.max(alignment, type.alignment());
			uniforms.put(entry.getKey(), new Uniform(offset, type));
			body.append("layout(offset = ").append(offset).append(") ").append(type.declaration(entry.getKey()));
			offset = Math.addExact(offset, type.size());
		}

		List<Descriptor> descriptors = new ArrayList<>();

		if (frameLayout != null) {
			descriptors.addAll(frameLayout.descriptors());
		}

		int binding = 0;

		if (!uniforms.isEmpty()) {
			if (resources.containsKey(VulkanResourceLayout.UNIFORM_BLOCK)) {
				throw new IllegalArgumentException("Reserved uniform block: " + VulkanResourceLayout.UNIFORM_BLOCK);
			}

			Set<PatchShaderType> stages = EnumSet.noneOf(PatchShaderType.class);

			for (var entry : trees.entrySet()) {
				if (entry.getValue() != null) {
					stages.add(entry.getKey());
				}
			}

			descriptors.add(
				new Descriptor(VulkanResourceLayout.UNIFORM_BLOCK, Kind.UNIFORM_BUFFER, "block", 1, 0, binding++, null, stages));
		}

		for (Resource resource : resources.values()) {
			int assignedBinding = binding++;

			for (var qualifier : resource.qualifiers) {
				qualifier.getRoot().indexBuildSession(() -> {
					VulkanGlsl.removeLayout(qualifier, "location", "set", "binding");
					VulkanGlsl.setLayout(qualifier, "set", 0);
					VulkanGlsl.setLayout(qualifier, "binding", assignedBinding);
				});
			}

			descriptors.add(new Descriptor(resource.name, resource.kind, resource.type, resource.count, 0, assignedBinding,
				resource.originalBinding, resource.stages));
		}

		String instanceName = "iris_uniformData";

		for (int suffix = 0; ; suffix++) {
			String candidate = instanceName;

			if (trees.values().stream().filter(Objects::nonNull).noneMatch(tree -> tree.getRoot().identifierIndex.has(candidate))) {
				break;
			}

			instanceName = "iris_uniformData" + suffix;
		}

		final String uniformInstance = instanceName;
		String frameName = "iris_frameData";

		for (int suffix = 0; ; suffix++) {
			String candidate = frameName;

			if (trees.values().stream().filter(Objects::nonNull).noneMatch(tree -> tree.getRoot().identifierIndex.has(candidate))) {
				break;
			}

			frameName = "iris_frameData" + suffix;
		}

		final String frameInstance = frameName;

		for (TranslationUnit tree : trees.values()) {
			if (tree == null) {
				continue;
			}

			tree.getRoot().indexBuildSession(() -> {
				for (String name : stageUniforms.getOrDefault(tree, Set.of())) {
					var references = tree.getRoot()
						.identifierIndex.get(name)
						.stream()
						.filter(id -> id.getParent() instanceof ReferenceExpression)
						.map(id -> (ReferenceExpression) id.getParent())
						.toList();

					for (var reference : references) {
						if (!VulkanGlsl.isShadowed(reference, name)) {
							String expression = (frameTypes.containsKey(name) ? frameInstance : uniformInstance) + "." + name;

							if (frameTypes.containsKey(name) && !frameTypes.get(name).equals(frameLayout.uniforms().get(name).type())) {
								expression = frameTypes.get(name).glsl() + "(" + expression + ")";
							}

							reference.replaceByAndDelete(parser.parseExpression(tree.getRoot(), expression));
						}
					}
				}

				for (var declaration : looseUniforms) {
					if (declaration.getRoot() == tree.getRoot()) {
						declaration.detachAndDelete();
					}
				}

				if (!uniforms.isEmpty()) {
					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"layout(std140, column_major, set = 0, binding = 0) uniform " + VulkanResourceLayout.UNIFORM_BLOCK + " {" + body +
							"} " + uniformInstance + ";");
				}

				if (frameLayout != null) {
					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"layout(std140, column_major, set = 2, binding = 0) uniform " + VulkanResourceLayout.FRAME_BLOCK + " {" +
							frameBody +
							"} " + frameInstance + ";");
				}

				if (tree.getVersionStatement().version.number < 450) {
					tree.getVersionStatement().version = Version.GLSL45;
				}

				tree.getVersionStatement().profile = Profile.CORE;
				tree.getRoot().rename("gl_VertexID", "gl_VertexIndex");

				if (!tree.getRoot().identifierIndex.get("gl_InstanceID").isEmpty()) {
					tree.getRoot().replaceReferenceExpressions(parser, "gl_InstanceID", "(gl_InstanceIndex - gl_BaseInstanceARB)");
					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_ALL, "#extension GL_ARB_shader_draw_parameters : require\n");
				}
			});
		}

		return new VulkanResourceLayout(Std140Type.align(offset, alignment), uniforms, descriptors);
	}

	private static void addResource(Map<String, Resource> resources, String name, Kind kind, String type, int count, String signature,
		TypeQualifier qualifier, PatchShaderType stage, TranslationUnit tree) {
		var binding = VulkanGlsl.layout(qualifier, "binding");
		Integer original = binding == null ? null : VulkanGlsl.integer(binding.getExpression(), tree.getRoot());
		Resource resource = resources.computeIfAbsent(name, ignored -> new Resource(name, kind, type, count, signature));

		if (resource.kind != kind || !resource.signature.equals(signature)) {
			throw new IllegalArgumentException("Conflicting resource declarations: " + name);
		}

		if (original != null) {
			if (original < 0 || resource.originalBinding != null && !resource.originalBinding.equals(original)) {
				throw new IllegalArgumentException("Conflicting resource binding: " + name);
			}

			resource.originalBinding = original;
		}

		resource.qualifiers.add(qualifier);
		resource.stages.add(stage);
	}

	private static final class Resource {
		final String name;
		final Kind kind;
		final String type;
		final int count;
		final String signature;
		final List<TypeQualifier> qualifiers = new ArrayList<>();
		final Set<PatchShaderType> stages = EnumSet.noneOf(PatchShaderType.class);
		Integer originalBinding;

		Resource(String name, Kind kind, String type, int count, String signature) {
			this.name = name;
			this.kind = kind;
			this.type = type;
			this.count = count;
			this.signature = signature;
		}
	}
}
