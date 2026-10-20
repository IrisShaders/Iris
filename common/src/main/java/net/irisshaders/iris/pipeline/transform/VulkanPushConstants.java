package net.irisshaders.iris.pipeline.transform;

import java.util.List;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class VulkanPushConstants {
	public static final int UNIFORM_SIZE = 20;
	public static final int NATIVE_OFFSET = 32;
	public static final int NATIVE_SIZE = 20;
	public static final int MAX_SIZE = 128;
	public static final Map<String, ShaderResourceLayout.Uniform> UNIFORMS = Map.of(
		"atlasSize", uniform(0, "ivec2", 2),
		"entityId", uniform(8, "int", 1),
		"iris_currentAlphaTest", uniform(12, "float", 1),
		"renderStage", uniform(16, "int", 1));
	public static final Map<String, ShaderResourceLayout.Uniform> DH_UNIFORMS = Map.of(
		"modelOffset", uniform(32, "vec3", 3),
		"uSkyLight", uniform(44, "int", 1),
		"uBlockLight", uniform(48, "int", 1));

	public static final String MEMBERS = """
		ivec2 iris_draw_atlasSize;
		int iris_draw_entityId;
		float iris_draw_iris_currentAlphaTest;
		int iris_draw_renderStage;
		""";

	private static final String DH_MEMBERS = """
		vec3 iris_draw_modelOffset;
		int iris_draw_uSkyLight;
		int iris_draw_uBlockLight;
		""";

	public static Map<String, ShaderResourceLayout.Uniform> fixedUniforms(UniformPlan.Profile profile) {
		var uniforms = new LinkedHashMap<>(UNIFORMS);
		if (profile == UniformPlan.Profile.DH) {
			uniforms.putAll(DH_UNIFORMS);
		}
		return uniforms;
	}

	public static String members(ShaderResourceLayout layout, UniformPlan.Profile profile, String nativeMembers) {
		var members = new StringBuilder(MEMBERS);
		if (profile == UniformPlan.Profile.DH) {
			members.append(DH_MEMBERS);
		} else if (nativeMembers != null) {
			members.append(nativeMembers);
		}
		var fixed = fixedUniforms(profile);
		layout.pushUniforms().entrySet().stream().filter(entry -> !fixed.containsKey(entry.getKey()))
			.sorted(Comparator.comparingInt(entry -> entry.getValue().offset()))
			.forEach(entry -> members.append(entry.getValue().type().declaration("iris_draw_" + entry.getKey())));
		return members.toString();
	}

	private static ShaderResourceLayout.Uniform uniform(int offset, String glsl, int rows) {
		return new ShaderResourceLayout.Uniform(offset, new Std140Type(glsl, 4, 1, rows, List.of()));
	}

}
