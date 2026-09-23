package net.irisshaders.iris.opengl;

import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanGlsl;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GlShaderPatcher {
	private GlShaderPatcher() {
	}

	public static Result patch(PatchedShader input) {
		var bindings = getBindings(input.layout());

		var sources = new EnumMap<PatchShaderType, String>(PatchShaderType.class);
		var parser = new SingleASTTransformer<>();
		input.sources().forEach((stage, text) -> {
			var tree = parser.parseSeparateTranslationUnit(text);
			tree.getRoot().indexBuildSession(() -> {
				for (var qualifier : tree.getRoot().nodeIndex.getStream(TypeQualifier.class).toList()) {
					if (VulkanGlsl.layout(qualifier, "set") != null) {
						VulkanGlsl.removeLayout(qualifier, "set", "binding");
					}
				}
			});
			sources.put(stage, ASTPrinter.print(PrintType.INDENTED, tree));
		});

		return new Result(new PatchedShader(sources, input.layout(), null, input.writtenAttachments()), bindings);
	}

	public static Map<String, Integer> getBindings(ShaderResourceLayout layout) {
		var bindings = new LinkedHashMap<String, Integer>();
		var counts = new EnumMap<ShaderResourceLayout.Kind, Integer>(ShaderResourceLayout.Kind.class);
		for (var descriptor : layout.descriptors()) {
			int binding = counts.getOrDefault(descriptor.kind(), 0);
			counts.put(descriptor.kind(), binding + descriptor.count());
			bindings.put(descriptor.name(), binding);
		}
		return Map.copyOf(bindings);
	}

	public record Result(PatchedShader source, Map<String, Integer> bindings) {
	}
}
