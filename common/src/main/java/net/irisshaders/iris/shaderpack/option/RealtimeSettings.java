package net.irisshaders.iris.shaderpack.option;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.shaderpack.include.IncludeGraph;
import net.irisshaders.iris.shaderpack.option.values.MutableOptionValues;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.NumericValue;
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
 * <p>
 * An int option that the pack compares with numbers in preprocessor conditions stays realtime as long as a
 * change does not change the result of any of those comparisons. Otherwise the pack reloads.
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
	private final Map<String, Compared> compared = new HashMap<>();

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

		DirectiveScan scan = new DirectiveScan(properties.stringPropertyNames());
		graph.getNodes().forEach((path, node) -> scan.read(path.getPathString(), String.join("\n", node.getLines())));
		scan.read("shaders.properties", shadersProperties);

		Map<String, Type> types = new HashMap<>();
		RealtimeSettings settings = new RealtimeSettings(types, scan.macros, optionValues);

		for (String name : properties.stringPropertyNames()) {
			String typeName = properties.getProperty(name);
			Type type = Type.fromName(typeName);
			String problem = type == null ? typeName + " is not a supported type (float, int, bool)" : findProblem(name, type, optionSet,
				scan);

			if (problem != null) {
				Iris.logger.warn("Treating " + name + " as a normal option instead of a realtime option: " + problem);
				continue;
			}

			types.put(name, type);
			settings.loadValue(name, optionSet);

			if (scan.comparisons.containsKey(name)) {
				settings.compared.put(name, new Compared(scan.comparisons.get(name), (long) settings.values.getDouble(name)));
			}
		}

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

		if (scan.unsupportedUses.containsKey(name)) {
			return "it is used in a preprocessor condition in a way other than a comparison with a number at " + scan.unsupportedUses.get(name);
		}

		for (String macro : scan.macrosUsing.getOrDefault(name, Set.of())) {
			if (scan.conditionUses.containsKey(macro)) {
				return "the macro " + macro + " has it in its value and is used in a preprocessor condition at " + scan.conditionUses.get(macro);
			}
		}

		if (type != Type.INT && scan.comparisons.containsKey(name)) {
			return "it is compared in a preprocessor condition, which only works for int options";
		}

		return null;
	}

	private void loadValue(String name, OptionSet optionSet) {
		String value = optionValues.getStringValueOrDefault(name);

		if (!set(name, value)) {
			String defaultValue = optionSet.getStringOptions().get(name).getOption().getDefaultValue();

			Iris.logger.warn("Invalid value " + value + " for realtime option " + name + ", using " + defaultValue);
			set(name, defaultValue);
		}
	}

	public boolean isRealtime(String name) {
		return types.containsKey(name);
	}

	// False if the pack has to reload for the new value because conditions in the shaders change with it
	public boolean canApplyLive(String name, String value) {
		Compared option = compared.get(name);

		return option == null ? isRealtime(name) : option.sameResults((long) parse(name, value));
	}

	private double parse(String name, String value) {
		Type type = types.get(name);

		return type == null ? Double.NaN : type.parse(macros.getOrDefault(value, value));
	}

	public boolean set(String name, String value) {
		double parsed = parse(name, value);

		if (Double.isNaN(parsed)) {
			return false;
		}

		values.put(name, parsed);
		optionValues.addAll(Map.of(name, value));

		return true;
	}

	/**
	 * What the #define of the option is written as, or null if it is written normally. Conditions need a number, so a
	 * compared option is (value at load + uniform) and the uniform holds the difference, which conditions read as 0.
	 */
	@Nullable
	public String sourceValue(String name) {
		if (compared.containsKey(name)) {
			return "(" + compared.get(name).loaded + " + " + uniformName(name) + ")";
		}

		return isRealtime(name) ? uniformName(name) : null;
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
			int offset = compared.containsKey(name) ? (int) compared.get(name).loaded : 0;

			switch (type) {
				case FLOAT -> holder.uniform1f(UniformUpdateFrequency.CUSTOM, uniform, () -> values.getDouble(name));
				case INT -> holder.uniform1i(UniformUpdateFrequency.CUSTOM, uniform, () -> (int) values.getDouble(name) - offset);
				case BOOL -> holder.uniform1b(UniformUpdateFrequency.CUSTOM, uniform, () -> values.getDouble(name) != 0);
			}
		});
	}

	private record Comparison(int operator, long number) {
		private boolean test(long value) {
			return switch (operator) {
				case Token.EQ -> value == number;
				case Token.NE -> value != number;
				case Token.LE -> value <= number;
				case Token.GE -> value >= number;
				case '<' -> value < number;
				default -> value > number;
			};
		}
	}

	private record Compared(Set<Comparison> comparisons, long loaded) {
		private boolean sameResults(long value) {
			return comparisons.stream().allMatch(comparison -> comparison.test(value) == comparison.test(loaded));
		}
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
		private static final Set<Integer> OPERATORS = Set.of(Token.EQ, Token.NE, Token.LE, Token.GE, (int) '<', (int) '>');
		private static final Set<Integer> SEPARATORS = Set.of(-1, Token.LAND, Token.LOR, (int) '(', (int) ')');
		private final Map<String, String> macros = new HashMap<>();
		private final Map<String, Set<Comparison>> comparisons = new HashMap<>();
		private final Map<String, String> unsupportedUses = new HashMap<>();
		// A line where each name is used in #if and #elif, and the macros that have a tracked name in their value
		private final Map<String, String> conditionUses = new HashMap<>();
		private final Map<String, Set<String>> macrosUsing = new HashMap<>();
		private final Set<String> tracked;

		private DirectiveScan(Set<String> tracked) {
			this.tracked = tracked;
		}

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
			} else if (name.equals("if") || name.equals("elif")) {
				for (int i = 0; i < arguments.size(); i++) {
					Token token = arguments.get(i);

					if (token.getType() == Token.IDENTIFIER) {
						conditionUses.putIfAbsent(token.getText(), file + ":" + token.getLine());

						if (tracked.contains(token.getText())) {
							addUse(file, arguments, i);
						}
					}
				}
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

				if (token.getType() == Token.IDENTIFIER && tracked.contains(token.getText())) {
					macrosUsing.computeIfAbsent(token.getText(), name -> new HashSet<>()).add(arguments.getFirst().getText());
				}
			}

			macros.putIfAbsent(arguments.getFirst().getText(), value.toString());
		}

		private void addUse(String file, List<Token> condition, int index) {
			Token name = condition.get(index);
			Comparison comparison = comparisonAt(condition, index);

			if (comparison != null) {
				comparisons.computeIfAbsent(name.getText(), key -> new HashSet<>()).add(comparison);
			} else {
				unsupportedUses.putIfAbsent(name.getText(), file + ":" + name.getLine());
			}
		}

		// Only NAME == 5 and the like are supported, anything else next to the name or the number could change the result
		@Nullable
		private static Comparison comparisonAt(List<Token> condition, int nameIndex) {
			int operator = typeAt(condition, nameIndex + 1);
			int numberIndex = nameIndex + 2;
			boolean negative = typeAt(condition, numberIndex) == '-';

			if (negative) {
				numberIndex++;
			}

			int before = typeAt(condition, nameIndex - 1);
			int after = typeAt(condition, numberIndex + 1);

			if (!OPERATORS.contains(operator) || typeAt(condition, numberIndex) != Token.NUMBER || !SEPARATORS.contains(before)
				|| !SEPARATORS.contains(after)) {
				return null;
			}

			long number = ((NumericValue) condition.get(numberIndex).getValue()).longValue();

			return new Comparison(operator, negative ? -number : number);
		}

		// -1 outside of the tokens
		private static int typeAt(List<Token> tokens, int index) {
			return index < 0 || index >= tokens.size() ? -1 : tokens.get(index).getType();
		}
	}
}
