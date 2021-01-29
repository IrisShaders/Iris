package net.coderbot.iris.shaderpack.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import net.coderbot.iris.shaderpack.config.Option;
import net.coderbot.iris.shaderpack.config.ShaderPackConfig;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.util.Util;

import static net.coderbot.iris.shaderpack.parse.CommentDirectiveParser.Tests.test;

public class ConfigOptionParser {
	//Regex for matching boolean options
	//Match if or if not the line starts with anynumber of backslashes that are more than 2 ("//")
	//Match 0 or more whitspace after the "//"
	//Match the #define
	//Match 1 whitespace after that
	//Match any letter, number, or underscore name
	//Match 0 or more whitespace after that
	//Match any comments on the option after that
	private static final Pattern BOOLEAN_OPTION_PATTERN = Pattern.compile("^(//+)?\\s*#define\\s+\\w+\\s*(//.*)?$");
	//Regex that matches for integer and float options
	//Match 1 or more whitespace after #define
	//match a word (name of the option)
	//match 1 or more whitespace after the name
	   //match a negative if there
	   //match f or F float keyword
	   //match 0-9
	   //match a "." for floats
	 //match 1 or more of the char group
	//match 0 or more whitespace
	//match if there is a comment following the line or not
	private static final Pattern FLOAT_INTEGER_OPTION_PATTERN = Pattern.compile("^#define\\s+\\w+\\s+(-?[0-9.fF]+)\\s*(//.*)?$");
	//Regex that matches for only integers and not floats
	//Same as above but in the char class we remove the float specific checks
		//remove matching a "."
		//remove matching a "f" or a "F"
	private static final Pattern INTEGER_OPTION_PATTERN = Pattern.compile("^#define\\s+\\w+\\s+(-?[0-9])\\s*(//.*)?");

	//Testing
	public static void main(String[] args) {
		//boolean tests
		test("default off boolean option", true, () -> BOOLEAN_OPTION_PATTERN.matcher("//#define Godrays").matches());
		test("default on boolean option", true, () -> BOOLEAN_OPTION_PATTERN.matcher("#define Godrays").matches());
		test("default off boolean option with a comment", true, () -> BOOLEAN_OPTION_PATTERN.matcher("//#define Godrays //Good Times").matches());
		test("default on boolean option with a comment", true, () -> BOOLEAN_OPTION_PATTERN.matcher("#define Godrays //Godrays").matches());
		test("boolean option with incorrect spacing", false, () -> BOOLEAN_OPTION_PATTERN.matcher("#defineBooleanStuff").matches());
		test("boolean option with multiple backslash comments", true, ()-> BOOLEAN_OPTION_PATTERN.matcher("///#define Boolean").matches());
		test("boolean option with too little comments", false, () -> BOOLEAN_OPTION_PATTERN.matcher("/#define fail").matches());//should not compile anyway
		test("boolean option with multiple words and comment", false, () -> BOOLEAN_OPTION_PATTERN.matcher("#define FeelsBad To Be Bad //Bad").matches());
		//float tests
		test("float option with comment", true, () -> FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 1.53F //cool stuffz").matches());
		test("float option with wrong letter", false, () -> FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 1.53R").matches());
		Matcher matcher = FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 45.0 //Density Of Feeling [45 4 2]");
		System.out.println("#define Density 45.0 //Density Of Feeling [45 4 2]");
		System.out.println(matcher.matches());
		System.out.println(matcher.groupCount());
		System.out.println(matcher.group(0));
	}

	//Some shaderpacks like sildurs have #define directives that are named with the program name
	//like #define gbuffers_textured
	//optifine does not use these in their config so we will not as well
	private static final Set<String> IGNORED_PROGRAM_NAMES = Util.make(new HashSet<>(), (set) -> {
		for (int i = 0; i < 16; i++) {
			set.add("composite" + i);
		}
		set.add("final");
		set.add("deferred");
		set.add("gbuffers_basic");
		set.add("gbuffers_textured");
		set.add("gbuffers_textured_lit");
		set.add("gbuffers_terrain");
		set.add("gbuffers_water");
		set.add("gbuffers_skybasic");
		set.add("gbuffers_skytextured");
		set.add("gbuffers_clouds");
		set.add("gbuffers_entities");
		set.add("gbuffers_block");
		set.add("gbuffers_weather");
		set.add("gbuffers_hand");
		set.add("gbuffers_shadows");
	});

	/**
	 * Does most of the processing relating to config options here
	 * @param line the line to process
	 * @param config the current pack shaderpack instance
	 * @return the processed line to set the line to
	 */
	public static String processConfigOptions(String line, ShaderPackConfig config) {

			String trimmedLine = line.trim();

			if (!trimmedLine.startsWith("#define") && !trimmedLine.startsWith("//#define")) return line;

			ParsedLineContainer values = parseConfigLine(trimmedLine);

			if (values.name == null) return line;

			if (values.name.startsWith("MC_") || IGNORED_PROGRAM_NAMES.contains(values.name)) {
				return line;
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
							return "//" + line;
						}

						break;

					case INTEGER:
						Option<Integer> integerOption = createIntegerOption(trimmedLine, config);

						if (integerOption != null) {
							//replace the default value with the new one
							return line.replaceFirst(values.defaultValue, integerOption.getValue().toString());
						}

						break;

					case FLOAT:
						Option<Float> option = createFloatOption(trimmedLine, config);
						if (option != null) {
							//replace the default value with the new one
							return line.replaceFirst(values.defaultValue, option.getValue().toString());
						}
						break;

				}
			} else if (trimmedLine.startsWith("//#define ")) {//boolean val that is default off. Remove comments to turn on
				//since it is default commented out, we can assume it is a boolean variable
				if (getOptionType(trimmedLine) == Option.OptionType.BOOLEAN) {

					Option<Boolean> option = createBooleanOption(trimmedLine, config);
					//if the option was true
					if (option.getValue()) {
						//replace the very first comment (the one that is before the "#define") with air, being careful not to get rid of any other comments
						return line.replaceFirst("//", "");
					}
				}
			}
		return line;
	}

	/**
	 * Returns the option type of the #define line
	 * Indicates how the line should be processed
	 *
	 * @param trimmedLine the trimmed version of the #define
	 * @return the option type specifying the type of the config option
	 */
	private static Option.OptionType getOptionType(String trimmedLine) {
		String defaultValue = parseConfigLine(trimmedLine).defaultValue;

		if (defaultValue == null) {
			return Option.OptionType.BOOLEAN;
		} else if (defaultValue.contains(".")) {
			return Option.OptionType.FLOAT;
		} else {
			return Option.OptionType.INTEGER;
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

		ParsedLineContainer values = parseConfigLine(trimmedLine);
		Option<Boolean> option = new Option<>(values.comment, Arrays.asList(true, false), values.name, defaultValue, Option.OptionType.BOOLEAN);

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
		ParsedLineContainer values = parseConfigLine(trimmedLine);
		int value;

		try {
			value = Integer.parseInt(values.defaultValue);
		} catch (NumberFormatException e) {
			return null;
		}

		List<Integer> integers = parseArray(values.array, string -> (int)Float.parseFloat(string));//parse as a float and then cast to int

		Option<Integer> option = new Option<>(values.comment, integers, values.name, value, Option.OptionType.INTEGER);
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
		ParsedLineContainer values = parseConfigLine(trimmedLine);
		float value;

		try {
			value = Float.parseFloat(values.defaultValue);
		} catch (NumberFormatException e) {
			return null;
		}

		List<Float> allowedValues = parseArray(values.array, Float::parseFloat);
		Option<Float> floatOption = new Option<>(values.comment, allowedValues, values.name, value, Option.OptionType.FLOAT);

		floatOption = config.processOption(floatOption, Float::parseFloat);
		config.getFloatOptions().put(floatOption.getName(), floatOption);
		return floatOption;
	}

	/**
	 * Parses a config line into a string array containing data for that line
	 * @param trimmedLine the whole line (trimmed) that needs to be
	 * @return a line container that contains the 4 elements in a configurable option
	 * 	      - name
	 * 	      - default value (null if boolean option)
	 * 	      - tooltip (null if not present)
	 * 	      - array of allowed values for the default to be set to (null if not present. If null, it can be set to any value)
	 */
	private static ParsedLineContainer parseConfigLine(String trimmedLine) {
		String[] returnVal = new String[4];

		String base = trimmedLine.startsWith("//") ? trimmedLine.substring(2) : trimmedLine;//remove the comment from the trimmed line
		String literalComment;//represents the whole comment part of the line (everything with comments in the line)

		if (base.contains("//")) {
			literalComment = base.substring(base.indexOf("//"));
			base = (base.substring(0, base.indexOf("//"))).trim();
		} else {
			literalComment = "";
		}

		for (String element : base.split("\\s+")) {//split by any amount of whitespace
			//the logic inside this for each loop is fragile imo, try to find a better solution
			if (element.contains("#define")) {
				//ignore the actual #define part since we don't need it
				continue;
			}

			//since base is split in order, from beginning to last
			if (returnVal[0] == null) {//if the name is null, set it because it should be the first one
				returnVal[0] = element;
			} else if (returnVal[1] == null) {//if the default value is null and the name is not, then it is the default value
				returnVal[1] = element;
			}

		}

		if (!literalComment.isEmpty()) {
			literalComment = literalComment.substring(2);//remove the "//" from the comment itself

			if (literalComment.contains("[") && literalComment.contains("]")) {

				String array = literalComment.substring(literalComment.indexOf("["), literalComment.indexOf("]") + 1);

				returnVal[3] = array;
				String tooltip = literalComment.replace(array, "").trim();//remove the array from the comment
				returnVal[2] = tooltip;
			} else {
				returnVal[2] = literalComment;
			}
		}

		return new ParsedLineContainer(returnVal[0], returnVal[1], returnVal[2], returnVal[3]);
	}

	/**
	 * Parses a string representation of an array to a list via a function converting a string to the desired type
	 * @param array the string representation of the array minus commas.
	 * @param parser function that converts string to the desired type
	 * @param <T> the type that should have the array parsed
	 * @return a list that contains new values parsed from the function and array
	 */
	private static <T> List<T> parseArray(String array, Function<String, T> parser) {
		List<T> list = new ArrayList<>();

		if (array == null) {
			return list;
		}
		//replace the "[" and "]" of the array
		array = array.replace("[", "").replace("]", "");

		for (String val : array.split("\\s+")) {
			list.add(parser.apply(val));
		}

		return list;
	}

	private static class ParsedLineContainer {
		public final String name;
		public final String defaultValue;
		public final String comment;
		public final String array;

		private ParsedLineContainer(String name, String defaultValue, String comment, String array) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.comment = comment;
			this.array = array;
		}

		@Override
		public String toString() {   //testing
			return "ParsedLineContainer{" +
				"name='" + name + '\'' +
				", defaultValue=" + defaultValue +
				", comment='" + comment + '\'' +
				", array=" + array +
				'}';
		}
	}
}
