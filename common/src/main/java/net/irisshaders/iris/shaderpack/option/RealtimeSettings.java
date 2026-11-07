package net.irisshaders.iris.shaderpack.option;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.shaderpack.include.IncludeGraph;
import net.irisshaders.iris.shaderpack.option.values.MutableOptionValues;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.StringLexerSource;
import org.anarres.cpp.Token;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
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
	public static final RealtimeSettings NONE = new RealtimeSettings(Map.of(), Map.of(), null);

	private static final String UNIFORM_PREFIX = "settingsUniform_";
	private static final Pattern USAGE = Pattern.compile(UNIFORM_PREFIX + "(\\w+)");

	private final Map<String, Type> types;
	private final Map<String, String> macros;
	private final Object2DoubleMap<String> values = new Object2DoubleOpenHashMap<>();
	private final MutableOptionValues optionValues;

	private RealtimeSettings(Map<String, Type> types, Map<String, String> macros, MutableOptionValues optionValues) {
		this.types = types;
		this.macros = macros;
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

		DirectiveScan scan = new DirectiveScan();
		graph.getNodes().forEach((path, node) -> scan.read(path.getPathString(), String.join("\n", node.getLines())));
		scan.read("shaders.properties", shadersProperties);

		Map<String, Type> types = new HashMap<>();

		for (String name : properties.stringPropertyNames()) {
			String typeName = properties.getProperty(name);
			Type type = Type.fromName(typeName);
			String problem = type == null ? typeName + " is not a supported type (float, int, bool)" : findProblem(name, type, optionSet,
				scan);

			if (problem == null) {
				types.put(name, type);
			} else {
				Iris.logger.warn("Treating " + name + " as a normal option instead of a realtime option: " + problem);
			}
		}

		RealtimeSettings settings = new RealtimeSettings(types, scan.macros, optionValues);

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
	private static String findProblem(String name, Type type, OptionSet optionSet, DirectiveScan scan) {
		MergedStringOption merged = optionSet.getStringOptions().get(name);

		if (merged == null) {
			return "it is not a #define option with a list of allowed values";
		}

		if (merged.getOption().getType() != OptionType.DEFINE) {
			return "const options cannot be changed in realtime";
		}

		for (String allowed : merged.getOption().getAllowedValues()) {
			if (Double.isNaN(type.parse(scan.macros.getOrDefault(allowed, allowed)))) {
				return allowed + " does not resolve to a valid " + type.glsl();
			}
		}

		String use = scan.conditionalUses.get(name);

		return use == null ? null : "it is used in a preprocessor condition at " + use;
	}

	public boolean isRealtime(String name) {
		return types.containsKey(name);
	}

	public boolean set(String name, String value) {
		Type type = types.get(name);
		double parsed = type == null ? Double.NaN : type.parse(macros.getOrDefault(value, value));

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
				declarations.append("uniform ").append(type.glsl()).append(' ').append(uniformName(name)).append(";\n");
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

	/**
	 * Use the preprocessor since that handles macros, line connections and comments
	 */
	private static class DirectiveScan {
		private final Map<String, String> macros = new HashMap<>();
		// line where each name is used in #if, #elif, #ifdef or #ifndef
		private final Map<String, String> conditionalUses = new HashMap<>();

		private void read(String file, String source) {
			List<Token> directive = null;

			try (StringLexerSource lexer = new StringLexerSource(source + "\n", true)) {
				for (Token token = lexer.token(); token.getType() != Token.EOF; token = lexer.token()) {
					int type = token.getType();

					if (type == Token.NL) {
						if (directive != null) {
							handle(file, directive);
						}

						directive = null;
					} else if (type == Token.HASH && directive == null) {
						directive = new ArrayList<>();
					} else if (directive != null && !isBlank(type)) {
						directive.add(token);
					}
				}
			} catch (IOException | LexerException e) {
				Iris.logger.warn("Could not read all of " + file + " to check the realtime options: " + e.getMessage());
			}
		}

		private static boolean isBlank(int tokenType) {
			return tokenType == Token.WHITESPACE || tokenType == Token.CCOMMENT || tokenType == Token.CPPCOMMENT;
		}

		// The first token is the name of the directive, the rest are its arguments
		private void handle(String file, List<Token> directive) {
			if (directive.isEmpty()) {
				return;
			}

			String name = directive.getFirst().getText();
			List<Token> arguments = directive.subList(1, directive.size());

			// to support shaders.properties which use # as comments
			if (name.equals("define")) {
				addMacro(arguments);
			} else if (name.startsWith("if") || name.equals("elif")) {
				// #if, #ifdef, #ifndef and #elif
				addConditionalUses(file, arguments);
			}
		}

		// The arguments are the name of the macro followed by its value
		private void addMacro(List<Token> arguments) {
			if (arguments.size() < 2 || arguments.getFirst().getType() != Token.IDENTIFIER) {
				return;
			}

			StringBuilder value = new StringBuilder();

			for (Token token : arguments.subList(1, arguments.size())) {
				value.append(token.getText());
			}

			macros.putIfAbsent(arguments.getFirst().getText(), value.toString());
		}

		private void addConditionalUses(String file, List<Token> condition) {
			for (Token token : condition) {
				if (token.getType() == Token.IDENTIFIER) {
					conditionalUses.putIfAbsent(token.getText(), file + ":" + token.getLine());
				}
			}
		}
	}
}
