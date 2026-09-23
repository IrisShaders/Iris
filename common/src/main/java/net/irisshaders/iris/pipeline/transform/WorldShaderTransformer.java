package net.irisshaders.iris.pipeline.transform;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
import io.github.douira.glsl_transformer.util.LRUCache;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanGlsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldShaderTransformer {
	private static final Map<String, String> matrixCache = new LRUCache<>(256);
	private static final Map<VertexInputKey, String> vertexCache = new LRUCache<>(256);
	private static final Map<AttributeKey, List<PipelineState.Attribute>> attributeCache = new LRUCache<>(256);

	private WorldShaderTransformer() {
	}

	public static int fallbackVertexBinding(List<VertexFormat> formats) {
		for (int i = 0; i < formats.size(); i++) {
			if (formats.get(i) == null) {
				return i;
			}
		}

		return formats.size();
	}

	public static Map<String, GpuFormat> availableFormats(List<VertexFormat> formats) {
		var available = new HashMap<String, GpuFormat>();

		for (var format : formats) {
			if (format != null) {
				for (var element : format.getElements()) {
					available.put(element.name(), element.format());
					available.put("iris_" + element.name(), element.format());
				}
			}
		}

		return available;
	}

	public static List<PipelineState.Attribute> attributes(String source, List<VertexFormat> formats) {
		var key = new AttributeKey(source, Collections.unmodifiableList(new ArrayList<>(formats)));
		var cached = attributeCache.get(key);

		if (cached != null) {
			return cached;
		}

		var tree = new SingleASTTransformer<JobParameters>().parseSeparateTranslationUnit(source);
		var result = new ArrayList<PipelineState.Attribute>();
		tree.getRoot().nodeIndex.getStream(TypeAndInitDeclaration.class).forEach(declaration -> {
			var qualifier = declaration.getType().getTypeQualifier();

			if (!VulkanGlsl.hasStorage(qualifier, StorageType.IN)) {
				return;
			}

			for (var member : declaration.getMembers()) {
				String name = member.getName().getName();
				int location = VulkanGlsl.integer(VulkanGlsl.layout(qualifier, "location").getExpression(), tree.getRoot());
				boolean found = false;

				for (int binding = 0; binding < formats.size(); binding++) {
					var format = formats.get(binding);

					if (format == null) {
						continue;
					}

					for (var element : format.getElements()) {
						if (name.equals(element.name()) || vertexAttributeName(name).equals(element.name())) {
							result.add(new PipelineState.Attribute(location, binding, element.format(), element.offset()));
							found = true;
						}
					}
				}

				if (!found) {
					var type = VulkanGlsl.uniformType((BuiltinNumericTypeSpecifier) declaration.getType().getTypeSpecifier(), member,
						tree.getRoot());
					GpuFormat format = zeroAttributeFormat(type);
					int columns = type.dimensions().stream().reduce(type.columns(), Math::multiplyExact);
					int locations = (type.rows() * type.scalarBytes() + 15) / 16;

					for (int column = 0; column < columns; column++) {
						result.add(new PipelineState.Attribute(location + column * locations, fallbackVertexBinding(formats), format, 0));
					}
				}
			}
		});

		var attributes = List.copyOf(result);
		attributeCache.put(key, attributes);
		return attributes;
	}

	public static GpuFormat zeroAttributeFormat(Std140Type type) {
		if (type.scalarBytes() == 8) {
			throw new UnsupportedOperationException("unsupported 64-bit vertex input format");
		}

		String channels = switch (type.rows()) {
			case 1 -> "R32";
			case 2 -> "RG32";
			case 3 -> "RGB32";
			default -> "RGBA32";
		};
		String kind = type.glsl().startsWith("u") ? "UINT" : type.glsl().startsWith("i") ? "SINT" : "FLOAT";

		return GpuFormat.valueOf(channels + "_" + kind);
	}

	public static String vertexAttributeName(String name) {
		return switch (name) {
			case "vaPosition", "vaColor", "vaNormal", "vaUV0", "vaUV1", "vaUV2" -> name.substring(2);
			default -> name.startsWith("iris_") ? name.substring(5) : name;
		};
	}

	public static Map<PatchShaderType, String> vertexInputs(Map<PatchShaderType, String> sources, Map<String, GpuFormat> available) {
		var key = new VertexInputKey(sources.get(PatchShaderType.VERTEX), Collections.unmodifiableMap(new HashMap<>(available)));
		var cached = vertexCache.get(key);

		if (cached != null) {
			var result = new EnumMap<PatchShaderType, String>(PatchShaderType.class);
			result.putAll(sources);
			result.put(PatchShaderType.VERTEX, cached);
			return result;
		}

		var parser = new SingleASTTransformer<JobParameters>();
		var tree = parser.parseSeparateTranslationUnit(sources.get(PatchShaderType.VERTEX));

		for (var external : List.copyOf(tree.getChildren())) {
			if (!(external instanceof DeclarationExternalDeclaration wrapper) || !(wrapper.getDeclaration() instanceof TypeAndInitDeclaration declaration) || !VulkanGlsl.hasStorage(
				declaration.getType().getTypeQualifier(), StorageType.IN)) {
				continue;
			}

			for (var member : new ArrayList<>(declaration.getMembers())) {
				String name = member.getName().getName();
				GpuFormat format = available.getOrDefault(name, available.get(vertexAttributeName(name)));

				if (format == null && tree.getRoot().identifierIndex.getStream(name).count() == 1) {
					member.detachAndDelete();
					continue;
				}

				String type = ASTPrinter.print(PrintType.COMPACT, declaration.getType().getTypeSpecifier()).trim();

				if (format != null) {
					String component = format.componentType().name();
					String prefix = component.startsWith("UINT_") ? "u" : component.startsWith("SINT_") ? "i" : "";
					String vector = type.replaceFirst("^[iu]", "");
					String inputType = vector.startsWith("vec") ? prefix + vector : prefix.equals("u") ? "uint" : prefix.equals(
						"i") ? "int" : "float";

					if (!type.equals(inputType)) {
						member.detachAndDelete();
						tree.getRoot().replaceReferenceExpressions(parser, name, type + "(" + name + ")");
						tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS, "in " + inputType + " " + name + ";");
					}

					continue;
				}

				String value = switch (name) {
					case "iris_UV0" -> type + "(0.5)";
					case "iris_UV1" -> type + "(0)";
					case "iris_UV2" -> type + "(240)";
					case "iris_Color" -> type + "(1)";
					case "iris_Normal" -> type + "(0,0,1)";
					case "iris_Entity", "mc_Entity", "at_tangent", "mc_midTexCoord", "at_midBlock" ->
						type.endsWith("vec4") ? type + "(0,0,0,1)" : type + "(0)";
					default -> null;
				};

				if (value == null) {
					var numeric = VulkanGlsl.uniformType((BuiltinNumericTypeSpecifier) declaration.getType().getTypeSpecifier(), member,
						tree.getRoot());

					if (numeric.scalarBytes() == 8) {
						String variable = numeric.declaration(name);
						member.detachAndDelete();
						tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS,
							"const " + variable.substring(0, variable.length() - 1) + " = " + numeric.zeroConstant() + ";");
					}

					continue;
				}

				member.detachAndDelete();
				tree.parseAndInjectNode(parser, ASTInjectionPoint.BEFORE_DECLARATIONS, "const " + type + " " + name + " = " + value + ";");
			}

			if (declaration.getMembers().isEmpty()) {
				declaration.getParent().detachAndDelete();
			}
		}

		var result = new EnumMap<PatchShaderType, String>(PatchShaderType.class);
		result.putAll(sources);
		result.put(PatchShaderType.VERTEX, ASTPrinter.print(PrintType.INDENTED, tree));
		vertexCache.put(key, result.get(PatchShaderType.VERTEX));

		return result;
	}

	public static Map<PatchShaderType, String> patchMatrices(Map<PatchShaderType, String> sources) {
		var result = new EnumMap<PatchShaderType, String>(PatchShaderType.class);
		var parser = new SingleASTTransformer<JobParameters>();
		sources.forEach((stage, source) -> {
			if (source == null) {
				return;
			}

			var cached = matrixCache.get(source);

			if (cached != null) {
				result.put(stage, cached);
				return;
			}

			var tree = parser.parseSeparateTranslationUnit(source);
			var root = tree.getRoot();
			boolean sodium = root.identifierIndex.has("u_Globals");
			String model = sodium ? "u_ModelViewMatrix" : "iris_transforms.ModelViewMat";
			String projectionFunction = root.identifierIndex.has("iris_toMinusOneToOne") ? "iris_toMinusOneToOne" : "iris_undoRevZ";
			String projection = sodium ? "u_ProjectionMatrix" : "iris_ProjMat";
			var replacements = Map.of("iris_NormalMat", "transpose(inverse(mat3(" + model + ")))", "iris_ModelViewMatInverse",
				"inverse(" + model + ")", "iris_ProjMatInverse", "inverse(" + projectionFunction + "(" + projection + "))");

			for (var declaration : root.nodeIndex.getStream(TypeAndInitDeclaration.class).toList()) {
				if (!VulkanGlsl.hasStorage(declaration.getType().getTypeQualifier(), StorageType.UNIFORM)) {
					continue;
				}

				for (var member : new ArrayList<>(declaration.getMembers())) {
					String name = member.getName().getName();
					var expression = replacements.get(name);

					if (expression == null) {
						continue;
					}

					member.detachAndDelete();
					root.replaceReferenceExpressions(parser, name, expression);
				}

				if (declaration.getMembers().isEmpty()) {
					declaration.getParent().detachAndDelete();
				}
			}

			result.put(stage, ASTPrinter.print(PrintType.INDENTED, tree));
			matrixCache.put(source, result.get(stage));
		});

		return result;
	}

	private record VertexInputKey(String source, Map<String, GpuFormat> formats) {
	}

	private record AttributeKey(String source, List<VertexFormat> formats) {
	}

}
