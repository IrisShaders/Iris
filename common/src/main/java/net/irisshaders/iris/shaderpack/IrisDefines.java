package net.irisshaders.iris.shaderpack;

import com.google.common.collect.ImmutableList;
import net.irisshaders.iris.gl.shader.StandardMacros;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.parsing.BiomeCategories;
import net.irisshaders.iris.uniforms.BiomeUniforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class IrisDefines {
	private static final Pattern SEMVER_PATTERN = Pattern.compile("(?<major>\\d+)\\.(?<minor>\\d+)\\.*(?<bugfix>\\d*)(.*)");

	private static void define(List<StringPair> defines, String key) {
		defines.add(new StringPair(key, ""));
	}

	private static void define(List<StringPair> defines, String key, String value) {
		defines.add(new StringPair(key, value));
	}

	public static ImmutableList<StringPair> createIrisReplacements() {
		ArrayList<StringPair> s = new ArrayList<>(StandardMacros.createStandardEnvironmentDefines());

		BiomeUniforms.getBiomeMap().forEach((biome, id) -> define(s, "BIOME_" + biome.location().getPath().toUpperCase(Locale.ROOT), String.valueOf(id)));

		BiomeCategories[] categories = BiomeCategories.values();
		for (int i = 0; i < categories.length; i++) {
			define(s, "CAT_" + categories[i].name().toUpperCase(Locale.ROOT), String.valueOf(i));
		}

		define(s, "PPT_NONE", "0");
		define(s, "PPT_RAIN", "1");
		define(s, "PPT_SNOW", "2");

		return ImmutableList.copyOf(s);
	}
}
