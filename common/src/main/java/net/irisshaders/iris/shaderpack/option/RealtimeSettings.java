package net.irisshaders.iris.shaderpack.option;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.shaderpack.include.IncludeGraph;
import net.irisshaders.iris.shaderpack.option.values.MutableOptionValues;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Options listed in realtime.properties are delivered to the shaders as frame uniforms instead of being
 * written into the source, so they can be changed without reloading the pack.
 */
public class RealtimeSettings {
	public static final String FILE_NAME = "realtime.properties";
	public static final RealtimeSettings NONE = new RealtimeSettings(Map.of(), null);

	private static final String UNIFORM_PREFIX = "settingsUniform_";
	private static final Pattern USAGE = Pattern.compile(UNIFORM_PREFIX + "(\\w+)");

	private final Map<String, Type> types;
	private final Object2DoubleMap<String> values = new Object2DoubleOpenHashMap<>();
	private final MutableOptionValues optionValues;

	private RealtimeSettings(Map<String, Type> types, MutableOptionValues optionValues) {
		this.types = types;
		this.optionValues = optionValues;
	}

	public static String uniformName(String optionName) {
		return UNIFORM_PREFIX + optionName;
	}

	public static RealtimeSettings create(Properties properties, OptionSet optionSet, IncludeGraph graph, String shadersProperties,
		MutableOptionValues optionValues) {
		if (properties.isEmpty()) {
			return NONE;
		}

		Map<String, String> conditionalUses = findConditionalUses(graph, shadersProperties, properties.stringPropertyNames());
		Map<String, Type> types = new HashMap<>();

		properties.forEach((key, value) -> {
			String name = (String) key;
			Type type = Type.fromName((String) value);
			String problem = type == null ? value + " is not a supported type (float, int, bool)" : findProblem(name, type, optionSet,
				conditionalUses);

			if (problem == null) {
				types.put(name, type);
			} else {
				Iris.logger.warn("Treating " + name + " as a normal option instead of a realtime option: " + problem);
			}
		});

		RealtimeSettings settings = new RealtimeSettings(types, optionValues);

		types.keySet().forEach(name -> {
			String value = optionValues.getStringValueOrDefault(name);

			if (!settings.set(name, value)) {
				String defaultValue = optionSet.getStringOptions().get(name).getOption().getDefaultValue();

				Iris.logger.warn("Invalid value " + value + " for realtime option " + name + ", using " + defaultValue);
				settings.set(name, defaultValue);
			}
		});

		return settings;
	}

	@Nullable
	private static String findProblem(String name, Type type, OptionSet optionSet, Map<String, String> conditionalUses) {
		MergedStringOption merged = optionSet.getStringOptions().get(name);

		if (merged == null) {
			return "it is not a #define option with a list of allowed values";
		}

		if (merged.getOption().getType() != OptionType.DEFINE) {
			return "const options cannot be changed in realtime";
		}

		for (String allowed : merged.getOption().getAllowedValues()) {
			if (Double.isNaN(type.parse(allowed))) {
				return allowed + " is not a valid " + type.glsl();
			}
		}

		String use = conditionalUses.get(name);

		return use == null ? null : "it is used in a preprocessor condition at " + use;
	}

	private static Map<String, String> findConditionalUses(IncludeGraph graph, String shadersProperties, Set<String> names) {
		Map<String, String> uses = new HashMap<>();

		graph.getNodes().forEach((path, node) -> collectConditionalUses(path.getPathString(), node.getLines(), names, uses));
		collectConditionalUses("shaders.properties", shadersProperties.lines().toList(), names, uses);

		return uses;
	}

	private static void collectConditionalUses(String file, List<String> lines, Set<String> names, Map<String, String> uses) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).trim();

			if (!line.startsWith("#")) {
				continue;
			}

			String directive = line.substring(1).trim();

			// Covers #if, #ifdef, #ifndef and #elif
			if (directive.startsWith("if") || directive.startsWith("elif")) {
				for (String word : directive.split("\\W+")) {
					if (names.contains(word)) {
						uses.putIfAbsent(word, file + ":" + (i + 1));
					}
				}
			}
		}
	}

	public boolean isRealtime(String name) {
		return types.containsKey(name);
	}

	public boolean set(String name, String value) {
		Type type = types.get(name);
		double parsed = type == null ? Double.NaN : type.parse(value);

		if (Double.isNaN(parsed)) {
			return false;
		}

		values.put(name, parsed);
		optionValues.addAll(Map.of(name, value));

		return true;
	}

	public String declarationsFor(CharSequence source) {
		if (types.isEmpty()) {
			return "";
		}

		StringBuilder declarations = new StringBuilder();
		Set<String> declared = new HashSet<>();
		Matcher matcher = USAGE.matcher(source);

		while (matcher.find()) {
			String name = matcher.group(1);
			Type type = types.get(name);

			if (type != null && declared.add(name)) {
				declarations.append("uniform ").append(type.glsl()).append(' ').append(matcher.group()).append(";\n");
			}
		}

		return declarations.toString();
	}

	public void registerUniforms(UniformHolder holder) {
		types.forEach((name, type) -> {
			String uniform = uniformName(name);

			switch (type) {
				case FLOAT -> holder.uniform1f(UniformUpdateFrequency.CUSTOM, uniform, () -> values.getDouble(name));
				case INT -> holder.uniform1i(UniformUpdateFrequency.CUSTOM, uniform, () -> (int) values.getDouble(name));
				case BOOL -> holder.uniform1b(UniformUpdateFrequency.CUSTOM, uniform, () -> values.getDouble(name) != 0);
			}
		});
	}

	private enum Type {
		FLOAT,
		INT,
		BOOL;

		@Nullable
		private static Type fromName(String name) {
			try {
				return valueOf(name.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		private String glsl() {
			return name().toLowerCase(Locale.ROOT);
		}

		// NaN means the value is not valid for this type
		private double parse(String value) {
			try {
				return switch (this) {
					case FLOAT -> Float.parseFloat(value);
					case INT -> Integer.parseInt(value);
					case BOOL -> value.equals("true") ? 1 : value.equals("false") ? 0 : Double.NaN;
				};
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		}
	}
}
