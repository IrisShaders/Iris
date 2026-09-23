package net.irisshaders.iris.pipeline.transform.vulkan;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.Profile;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.declaration.InterfaceBlockDeclaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.type.initializer.ExpressionInitializer;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.NamedLayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.ArraySpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinFixedTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeReference;
import io.github.douira.glsl_transformer.ast.node.type.specifier.TypeSpecifier;
import io.github.douira.glsl_transformer.ast.node.type.struct.StructSpecifier;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout.Descriptor;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout.Kind;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout.Uniform;
import net.irisshaders.iris.pipeline.transform.Std140Type;

import java.util.ArrayList;
import java.util.Collections;
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

	private static void resolveStructDefaults(ASTParser parser, TranslationUnit tree) {
		var root = tree.getRoot();
		root.indexBuildSession(() -> {
			for (var external : List.copyOf(tree.getChildren())) {
				if (!(external instanceof DeclarationExternalDeclaration declaration) || !(declaration.getDeclaration() instanceof TypeAndInitDeclaration variables) || !VulkanGlsl.hasStorage(
					variables.getType().getTypeQualifier(), StorageType.UNIFORM)) {
					continue;
				}

				var type = variables.getType().getTypeSpecifier();

				if (!(type instanceof StructSpecifier || type instanceof TypeReference)) {
					continue;
				}

				if (type instanceof StructSpecifier struct && struct.getName() == null) {
					String name = "iris_uniformStruct";

					for (int suffix = 0; root.identifierIndex.has(name); suffix++) {
						name = "iris_uniformStruct" + suffix;
					}

					struct.setName(new Identifier(name));
				}

				for (var member : variables.getMembers()) {
					if (member.getInitializer() == null) {
						member.setInitializer(
							new ExpressionInitializer(parser.parseExpression(root, zeroValue(type, member.getArraySpecifier(), root))));
					}
				}

				var qualifier = variables.getType().getTypeQualifier();
				qualifier.getParts()
					.removeIf(part -> part instanceof StorageQualifier storage && storage.storageType == StorageType.UNIFORM);
				qualifier.getParts().add(new StorageQualifier(StorageType.CONST));
				VulkanGlsl.removeLayout(qualifier, "location", "set", "binding", "offset");
			}
		});
	}

	private static String zeroValue(TypeSpecifier type, ArraySpecifier array, Root root) {
		String name;
		String value;

		if (type instanceof BuiltinNumericTypeSpecifier numeric) {
			name = numeric.type.getCompactName();
			value = name + "(0)";
		} else {
			StructSpecifier struct;

			if (type instanceof StructSpecifier definition) {
				struct = definition;
			} else if (type instanceof TypeReference reference) {
				struct = root.nodeIndex.getStream(StructSpecifier.class).filter(
						candidate -> candidate.getName() != null && candidate.getName().getName()
							.equals(reference.getReference().getName()) && candidate.getAncestor(FunctionDefinition.class) == null).findFirst()
					.orElseThrow(() -> new IllegalArgumentException("missing uniform struct: " + reference.getReference().getName()));
			} else {
				throw new IllegalArgumentException(
					"cannot initialize opaque struct uniform member: " + ASTPrinter.print(PrintType.COMPACT, type));
			}

			name = struct.getName().getName();
			List<String> members = new ArrayList<>();

			for (var member : struct.getStructBody().getMembers()) {
				for (var declarator : member.getDeclarators()) {
					members.add(zeroValue(member.getType().getTypeSpecifier(), declarator.getArraySpecifier(), root));
				}
			}

			value = name + "(" + String.join(",", members) + ")";
		}

		var dimensions = VulkanGlsl.dimensions(root, array, type.getArraySpecifier());
		String suffix = "";

		for (int i = dimensions.size() - 1; i >= 0; i--) {
			int count = dimensions.get(i);
			suffix = "[" + count + "]" + suffix;
			value = name + suffix + "(" + String.join(",", Collections.nCopies(count, value)) + ")";
		}

		return value;
	}

	private static void resolveDefaults(TranslationUnit tree, Predicate<String> registered) {
		var root = tree.getRoot();
		root.indexBuildSession(() -> {
			for (var external : List.copyOf(tree.getChildren())) {
				if (!(external instanceof DeclarationExternalDeclaration declaration) || !(declaration.getDeclaration() instanceof TypeAndInitDeclaration variables) || !VulkanGlsl.hasStorage(
					variables.getType().getTypeQualifier(), StorageType.UNIFORM) || variables.getMembers().stream()
					.noneMatch(member -> member.getInitializer() != null)) {
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

	public static ShaderResourceLayout transform(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees) {
		return transform(parser, trees, name -> false);
	}

	public static ShaderResourceLayout transform(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees,
		Predicate<String> registered) {
		return transform(parser, trees, registered, null, name -> true);
	}

	public static ShaderResourceLayout transform(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees,
		Predicate<String> registered, ShaderResourceLayout frameLayout, Predicate<String> dynamic) {
		return transform(parser, trees, registered, frameLayout, dynamic, true);
	}

	public static ShaderResourceLayout transform(ASTParser parser, Map<PatchShaderType, TranslationUnit> trees,
		Predicate<String> registered, ShaderResourceLayout frameLayout, Predicate<String> dynamic, boolean vulkan) {
		trees.values().stream().filter(Objects::nonNull).forEach(tree -> {
			resolveStructDefaults(parser, tree);
			resolveDefaults(tree, registered);
		});

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
								var split = new TypeAndInitDeclaration(variables.getType().cloneInto(root),
									Stream.of(member.cloneInto(root)));
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
							for (var member : List.copyOf(variables.getMembers())) {
								String name = member.getName().getName();
								Std140Type uniformType = VulkanGlsl.uniformType(numeric, member, root);
								var shared = frameLayout == null ? null : frameLayout.uniforms().get(name);

								if (shared != null && !dynamic.test(name) && !compatibleSharedType(uniformType, shared.type())) {
									var constantType = variables.getType().cloneInto(root);
									var constantQualifier = constantType.getTypeQualifier();
									constantQualifier.getParts().removeIf(StorageQualifier.class::isInstance);
									constantQualifier.getParts().add(new StorageQualifier(StorageType.CONST));
									VulkanGlsl.removeLayout(constantQualifier, "location", "set", "binding", "offset");
									var constant = member.cloneInto(root);
									constant.setInitializer(
										new ExpressionInitializer(parser.parseExpression(root, uniformType.zeroConstant())));
									tree.getChildren().add(tree.getChildren().indexOf(declaration),
										new DeclarationExternalDeclaration(new TypeAndInitDeclaration(constantType, Stream.of(constant))));
									member.detachAndDelete();
									continue;
								}

								stageUniforms.computeIfAbsent(tree, ignored -> new LinkedHashSet<>()).add(name);
								Std140Type previous = types.putIfAbsent(name, uniformType);

								if (previous != null && !previous.equals(uniformType)) {
									throw new IllegalArgumentException("conflicting uniform types across stages: " + name);
								}
							}

							looseUniforms.add(declaration);
						} else {
							throw new IllegalArgumentException(
								"struct uniforms require Vulkan lowering: " + ASTPrinter.print(PrintType.COMPACT, declaration));
						}
					} else if (declaration.getDeclaration() instanceof InterfaceBlockDeclaration block) {
						TypeQualifier qualifier = block.getTypeQualifier();
						Kind kind = VulkanGlsl.hasStorage(qualifier, StorageType.BUFFER) ? Kind.STORAGE_BUFFER : VulkanGlsl.hasStorage(
							qualifier, StorageType.UNIFORM) ? Kind.UNIFORM_BUFFER : null;

						if (kind == null) {
							continue;
						}

						if (vulkan && block.getBlockName().getName().equals("iris_SodiumPushConstants")) {
							VulkanGlsl.removeLayout(qualifier, "set", "binding");
							qualifier.getParts().add(0,
								new LayoutQualifier(Stream.of(new NamedLayoutQualifierPart(new Identifier("push_constant"), null))));
							continue;
						}

						if (VulkanGlsl.layout(qualifier, "push_constant") != null) {
							throw new IllegalArgumentException("shader pack push constants are not supported");
						}

						if (VulkanGlsl.layout(qualifier, "shared") != null || VulkanGlsl.layout(qualifier, "packed") != null) {
							throw new IllegalArgumentException(
								"Vulkan requires an explicit std140/std430 block layout: " + block.getBlockName().getName());
						}

						var dimensions = VulkanGlsl.dimensions(root, block.getArraySpecifier());
						int count = dimensions.stream().reduce(1, Math::multiplyExact);
						String signature = ASTPrinter.print(PrintType.COMPACT, block.getStructBody()) + dimensions + (VulkanGlsl.layout(
							qualifier, "row_major") != null) + ":" + (VulkanGlsl.layout(qualifier, "std430") != null);
						addResource(resources, block.getBlockName().getName(), kind, "block", count, signature, qualifier, entry.getKey(),
							tree);
					}
				}
			});
		}

		if (frameLayout != null && resources.containsKey(ShaderResourceLayout.FRAME_BLOCK)) {
			throw new IllegalArgumentException("reserved uniform block: " + ShaderResourceLayout.FRAME_BLOCK);
		}

		Map<String, Std140Type> frameTypes = new HashMap<>();

		if (frameLayout != null) {
			for (var entry : List.copyOf(types.entrySet())) {
				var shared = frameLayout.uniforms().get(entry.getKey());

				if (shared == null || dynamic.test(entry.getKey())) {
					continue;
				}

				var type = entry.getValue();

				frameTypes.put(entry.getKey(), type);
				types.remove(entry.getKey());
			}
		}

		StringBuilder frameBody = new StringBuilder();

		if (frameLayout != null) {
			frameLayout.uniforms().entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().offset())).forEach(entry -> {
				if (vulkan) {
					frameBody.append("layout(offset = ").append(entry.getValue().offset()).append(") ");
				}

				frameBody.append(entry.getValue().type().declaration(entry.getKey()));
			});
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
			if (vulkan) {
				body.append("layout(offset = ").append(offset).append(") ");
			}

			body.append(type.declaration(entry.getKey()));
			offset = Math.addExact(offset, type.size());
		}

		List<Descriptor> descriptors = new ArrayList<>();

		if (frameLayout != null) {
			descriptors.addAll(frameLayout.descriptors());
		}

		int binding = 0;

		if (!uniforms.isEmpty()) {
			if (resources.containsKey(ShaderResourceLayout.UNIFORM_BLOCK)) {
				throw new IllegalArgumentException("reserved uniform block: " + ShaderResourceLayout.UNIFORM_BLOCK);
			}

			Set<PatchShaderType> stages = EnumSet.noneOf(PatchShaderType.class);

			for (var entry : trees.entrySet()) {
				if (entry.getValue() != null) {
					stages.add(entry.getKey());
				}
			}

			descriptors.add(
				new Descriptor(ShaderResourceLayout.UNIFORM_BLOCK, Kind.UNIFORM_BUFFER, "block", 1, 0, binding++, null, stages));
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

			descriptors.add(
				new Descriptor(resource.name, resource.kind, resource.type, resource.count, 0, assignedBinding, resource.originalBinding,
					resource.stages));
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
					var references = tree.getRoot().identifierIndex.get(name).stream()
						.filter(id -> id.getParent() instanceof ReferenceExpression).map(id -> (ReferenceExpression) id.getParent())
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
						"layout(std140, column_major, set = 0, binding = 0) uniform " + ShaderResourceLayout.UNIFORM_BLOCK + " {" + body + "} " + uniformInstance + ";");
				}

				if (frameLayout != null) {
					tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"layout(std140, column_major, set = 2, binding = 0) uniform " + ShaderResourceLayout.FRAME_BLOCK + " {" + frameBody + "} " + frameInstance + ";");
				}

				Version minimumVersion = vulkan ? Version.GLSL45 : Version.GLSL33;

				if (tree.getVersionStatement().version.number < minimumVersion.number) {
					tree.getVersionStatement().version = minimumVersion;
				}

				tree.getVersionStatement().profile = Profile.CORE;

				if (vulkan) {
					tree.getRoot().rename("gl_VertexID", "gl_VertexIndex");

					if (!tree.getRoot().identifierIndex.get("gl_InstanceID").isEmpty()) {
						tree.getRoot().replaceReferenceExpressions(parser, "gl_InstanceID", "(gl_InstanceIndex - gl_BaseInstanceARB)");
						tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_ALL,
							"#extension GL_ARB_shader_draw_parameters : require\n");
					}
				}
			});
		}

		return new ShaderResourceLayout(Std140Type.align(offset, alignment), uniforms, descriptors);
	}

	private static boolean compatibleSharedType(Std140Type type, Std140Type shared) {
		return type.equals(shared) || Set.of("int", "bool").contains(type.glsl()) && Set.of("int", "bool")
			.contains(shared.glsl()) && type.dimensions().isEmpty() && shared.dimensions().isEmpty();
	}

	private static void addResource(Map<String, Resource> resources, String name, Kind kind, String type, int count, String signature,
		TypeQualifier qualifier, PatchShaderType stage, TranslationUnit tree) {
		var binding = VulkanGlsl.layout(qualifier, "binding");
		Integer original = binding == null ? null : VulkanGlsl.integer(binding.getExpression(), tree.getRoot());
		Resource resource = resources.computeIfAbsent(name, ignored -> new Resource(name, kind, type, count, signature));

		if (resource.kind != kind || !resource.signature.equals(signature)) {
			throw new IllegalArgumentException("conflicting resource declarations: " + name);
		}

		if (original != null) {
			if (original < 0 || resource.originalBinding != null && !resource.originalBinding.equals(original)) {
				throw new IllegalArgumentException("conflicting resource binding: " + name);
			}

			resource.originalBinding = original;
		}

		resource.qualifiers.add(qualifier);
		resource.stages.add(stage);
	}

	private static final class Resource {
		private final String name;
		private final Kind kind;
		private final String type;
		private final int count;
		private final String signature;
		private final List<TypeQualifier> qualifiers = new ArrayList<>();
		private final Set<PatchShaderType> stages = EnumSet.noneOf(PatchShaderType.class);
		private Integer originalBinding;

		private Resource(String name, Kind kind, String type, int count, String signature) {
			this.name = name;
			this.kind = kind;
			this.type = type;
			this.count = count;
			this.signature = signature;
		}
	}
}
