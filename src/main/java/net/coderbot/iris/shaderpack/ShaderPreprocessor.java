package net.coderbot.iris.shaderpack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.coderbot.iris.shaderpack.config.Option;
import net.coderbot.iris.shaderpack.config.OptionType;
import net.coderbot.iris.shaderpack.config.ShaderPackConfig;


public class ShaderPreprocessor {
	public static String process(Path rootPath, Path shaderPath, String source, ShaderPackConfig config) throws IOException {
		StringBuilder processed = new StringBuilder();

		List<String> lines = processInternal(rootPath, shaderPath, source, config);

		for (String line : lines) {
			processed.append(line);
			processed.append('\n');
		}

		return processed.toString();
	}

	private static List<String> processInternal(Path rootPath, Path shaderPath, String source, ShaderPackConfig config) throws IOException {
		List<String> lines = new ArrayList<>();

		// Match any valid newline sequence
		// https://stackoverflow.com/a/31060125
		for (String line : source.split("\\R")) {
			String trimmedLine = line.trim();

			if (trimmedLine.startsWith("#include ")) {
				try {
					lines.addAll(include(rootPath, shaderPath, trimmedLine, config));
				} catch (IOException e) {
					throw new IOException("Failed to read file from #include directive", e);
				}

				continue;
			}

			lines.add(line);

			if (line.startsWith("#version")) {
				// That was the first line. Add our preprocessor lines
				lines.add("#define MC_RENDER_QUALITY 1.0");
				lines.add("#define MC_SHADOW_QUALITY 1.0");
			}
		}

		processConfigOptions(lines, config);

		return lines;
	}

	/**
	 * Does most of the processing relating to config options here
	 * @param lines the lines of the current shaderpack file after all other operations are done
	 * @param config the current pack shaderpack instance
	 */
	private static void processConfigOptions(List<String> lines, ShaderPackConfig config) {
		//iterate over lines so we get the included shader settings as well
		for (int index = 0; index < lines.size(); index++) {

			String line = lines.get(index);

			String trimmedLine = line.trim();
			String[] values = parseConfigLine(trimmedLine);

			if (values[0].startsWith("MC_")) {
				continue;//as per https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt#L649-L652, do not parse any config values from those
			}

			//parse config lines that start with #define
			if (trimmedLine.startsWith("#define ")) {
				//get the option type of the line (boolean, int, float)
				switch (getOptionType(trimmedLine)) {

					case BOOLEAN:
						Option<Boolean> booleanOption = createBooleanOption(trimmedLine, config);

						//since we know it is a boolean option, we can simply check if the value of the boolean is false and then comment out the #defined line
						//we can do this by simply adding a "//" to the beginning of the line
						if (!booleanOption.getValue()) {
							String newLine = "//" + line;
							lines.set(index, newLine);
						}
						System.out.println(booleanOption);
						break;

					case INTEGER:
						Option<Integer> integerOption = createIntegerOption(trimmedLine, config);

						if (integerOption != null) {
							//replace the default value with the new one
							String newLine = line.replaceFirst(values[1], integerOption.getValue().toString());
							lines.set(index, newLine);
						}
						System.out.println(integerOption);
						break;

					case FLOAT:
						Option<Float> option = createFloatOption(trimmedLine, config);

						if (option != null) {
							//replace the default value with the new one
							String newLine = line.replaceFirst(values[1], option.getValue().toString());
							lines.set(index, newLine);
						}
						System.out.println(option);
						break;

				}
			} else if (trimmedLine.startsWith("//#define ")) {//boolean val that is default off. Remove comments to turn on
				//since it is default commented out, we can assume it is a boolean variable
				if (getOptionType(trimmedLine) == OptionType.BOOLEAN) {

					Option<Boolean> option = createBooleanOption(trimmedLine, config);
					//if the option was true
					if (option.getValue()) {
						//replace the very first comment (the one that is before the "#define") with air, being careful not to get rid of any other comments
						String newLine = line.replaceFirst("//", "");
						lines.set(index, newLine);
					}
				}
			}
		}
	}

	/**
	 * Returns the option type of the #define line
	 * Indicates how the line should be processed
	 *
	 * @param trimmedLine the trimmed version of the #define
	 * @return the option type specifying the type of the config option
	 */
	private static OptionType getOptionType(String trimmedLine) {
		String defaultValue = parseConfigLine(trimmedLine)[1];

		if (defaultValue == null) {
			return OptionType.BOOLEAN;
		} else if (defaultValue.contains(".")) {
			return OptionType.FLOAT;
		} else {
			return OptionType.INTEGER;
		}
	}

	/**
	 * Creates a boolean option and processes it from the shaderpack config
	 * @param trimmedLine the line of the config
	 * @param config the processed pack's instance
	 * @return an Option that is synced to the config instance and properties
	 */
	private static Option<Boolean> createBooleanOption(String trimmedLine, ShaderPackConfig config) {
		boolean defaultValue = !trimmedLine.startsWith("//");

		String[] values = parseConfigLine(trimmedLine);
		Option<Boolean> option = new Option<>(values[2], Arrays.asList(true, false), values[0], defaultValue);

		option = config.processOption(option, Boolean::parseBoolean);

		config.getBooleanOptions().put(option.getName(), option);
		return option;
	}

	/**
	 * Returns a integer option and sets it's value from the shaderpack config
	 * @param trimmedLine the line that is being processed
	 * @param config the current pack config instance
	 * @return the new option or null if a method was processed
	 */
	private static Option<Integer> createIntegerOption(String trimmedLine, ShaderPackConfig config) {
		String[] values = parseConfigLine(trimmedLine);
		int value;

		try {
			value = Integer.parseInt(values[1]);
		} catch (NumberFormatException e) {
			//should we fail silently?
			return null;
		}

		List<Integer> integers = parseArray(values[3], ShaderPreprocessor::parseInt);

		Option<Integer> option = new Option<>(values[2], integers, values[0], value);
		option = config.processOption(option, Integer::parseInt);

		config.getIntegerOptions().put(option.getName(), option);
		return option;
	}

	/**
	 * Creates a float option
	 * @param trimmedLine the line that is being processed
	 * @param config the pack config instance
	 * @return a new config option - or null if there was an error while processing the option
	 */
	private static Option<Float> createFloatOption(String trimmedLine, ShaderPackConfig config) {
		String[] values = parseConfigLine(trimmedLine);
		float value;

		try {
			value = Float.parseFloat(values[1]);
		} catch (NumberFormatException e) {
			//fails silently, maybe log something?
			return null;
		}

		List<Float> allowedValues = parseArray(values[3], Float::parseFloat);
		Option<Float> floatOption = new Option<>(values[2], allowedValues, values[0], value);

		floatOption = config.processOption(floatOption, Float::parseFloat);
		config.getFloatOptions().put(floatOption.getName(), floatOption);
		return floatOption;
	}

	/**
	 * Parses a config line into a string array containing data for that line
	 * @param trimmedLine the whole line (trimmed) that needs to be
	 * @return a string array containing the following values:
	 * <table style="width:100%">
	 *   <tr>
	 *     <th>Index</th>
	 *     <th colspan="1">Value</th>
	 *   </tr>
	 *   <tr>
	 *     <th>0</th>
	 *     <th>Name of option</th>
	 *   </tr>
	 *   <tr>
	 *       <th rowspan = "2">1</th>
	 *       <th>The default value of the option as a string.</th>
	 *   </tr>
	 *   <tr>
	 *      <th>Returns null if it is a boolean option</th>
	 *   </tr>
	 *   <tr>
	 *       <th rowspan = "2">2</th>
	 *       <th>Returns the comment to be applied as a tooltip</th>
	 *   </tr>
	 *   <tr>
	 *       <th>Returns null if there is no comment</th>
	 *   </tr>
	 *   <tr>
	 *       <th rowspan = "2">3</th>
	 *       <th>The allowed values for this option to be set to</th>
	 *   </tr>
	 *   <tr>
	 *       <th>Returns null if there are no allowed values</th>
	 *   </tr>
	 * </table>
	 */
	private static String[] parseConfigLine(String trimmedLine) {
		String[] returnVal = new String[4];

		String base = trimmedLine.startsWith("//") ? trimmedLine.substring(2) : trimmedLine;//remove the comment from the trimmed line
		String literalComment;//represents the whole comment part of the line (everything with comments in the line)

		if (base.contains("//")) {
			literalComment = base.substring(base.indexOf("//"));
			base = (base.substring(0, base.indexOf("//"))).trim();
		} else {
			literalComment = "";
		}
		String name = null;
		String defaultValue = null;
		for (String element : base.split("\\s+")) {//split by any amount of whitespace

			//the logic inside this for each loop is fragile imo, try to find a better solution
			if (!element.contains("#define")) {

				if (name == null) {
					name = element;
				} else if (defaultValue == null) {
					defaultValue = element;
				}
			}
		}

		if (!literalComment.isEmpty()) {
			literalComment = literalComment.substring(2);//remove the "//" from the comment itself

			if (literalComment.contains("[") && literalComment.contains("]")) {

				String array = literalComment.substring(literalComment.indexOf("["), literalComment.indexOf("]") + 1);

				returnVal[3] = array;
				String tooltip = literalComment.replace(array, "").trim();//remove the array from the comment
				returnVal[2] = tooltip;
			}
		}

		returnVal[0] = name;
		returnVal[1] = defaultValue;


		return returnVal;
	}

	/**
	 * Parses a string representation of an array to a list via a function converting a string to the desired type
	 * @param array the string representation of the array minus commas. (This should be obtained via {@link ShaderPreprocessor#parseConfigLine(String)}'s 3rd index)
	 * @param parser function that converts string to the desired type
	 * @param <T> the type that should have the array parsed
	 * @return a list that contains new values parsed from the function and array
	 */
	private static <T> List<T> parseArray(String array, Function<String, T> parser) {
		List<T> list = new ArrayList<>();

		if (array == null) {
			return list;
		}

		array = array.replace("[", "").replace("]", "");

		for (String val : array.split(" ")) {
			list.add(parser.apply(val));
		}

		return list;
	}

	/**
	 * Expanded version of {@link Integer#parseInt(String)} which parses float values as integers without rounding
	 * @see Integer#parseInt(String)
	 * @param source the source string
	 * @return an integer parsed
	 */
	private static Integer parseInt(String source) {

		if (source.contains(".")) {

			if (source.startsWith(".")) {
				source = 0 + source;
			}

			source = source.substring(0, source.indexOf("."));
		}

		return Integer.parseInt(source);
	}

	private static List<String> include(Path rootPath, Path shaderPath, String directive, ShaderPackConfig config) throws IOException {
		// Remove the "#include " part so that we just have the file path
		String target = directive.substring("#include ".length()).trim();

		// Remove quotes if they're present
		// All include directives should have quotes, but I
		if (target.startsWith("\"")) {
			target = target.substring(1);
		}

		if (target.endsWith("\"")) {
			target = target.substring(0, target.length() - 1);
		}

		Path included;

		if (target.startsWith("/")) {
			target = target.substring(1);

			included = rootPath.resolve(target);
		} else {
			included = shaderPath.getParent().resolve(target);
		}

		String source = readFile(included);

		return processInternal(rootPath, included, source, config);
	}

	private static String readFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
