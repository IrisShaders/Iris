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
	private static final Pattern BOOLEAN_OPTION_PATTERN = Pattern.compile("^(?<startingComment>//+)?\\s*(?<define>#define)\\s+(?<name>\\w+)\\s*(?<comment>//.*)?$");
	//Regex for matching ifdef patterns for boolean options
	//Match the ifdef or ifndef keyword
	//match whitespace that must be there
	//match a word that is the name of the keyword
	private static final Pattern IFDEF_IFNDEF_PATTERN = Pattern.compile("^(?<ifdef>#ifdef|#ifndef)\\s+(?<name>\\w+)(?<other>.*)");
	//Regex that matches for integer and float options
	//Match 1 or more whitespace after #define
	//match a word (name of the option). Put in parenthesis for grouping
	//match 1 or more whitespace after the name
	   //match a negative if there
	   //match f or F float keyword
	   //match 0-9
	   //match a "." for floats
	 //match 1 or more of the char group
	//match 0 or more whitespace
	//match if there is a comment following the line or not
	private static final Pattern FLOAT_INTEGER_OPTION_PATTERN = Pattern.compile("^(?<define>#define)\\s+(?<name>\\w+)\\s+(?<value>-?[\\d.fF]+)\\s*(?<comment>//.*)?$");
	//Regex that matches for only integers and not floats
	//Same as above but in the char class we remove the float specific checks
		//remove matching a "."
		//remove matching a "f" or a "F"
	private static final Pattern INTEGER_OPTION_PATTERN = Pattern.compile("^(?<define>#define)\\s+(?<name>\\w+)\\s+(?<value>-?\\d+)\\s*(?<comment>//.*)?");

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
		test("float option with comment", true, () -> FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 1.53F //cool stuffz [1 2 3 4]").matches());
		test("float option with wrong letter", false, () -> FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 1.53R").matches());
		test("float option without comment or letter", true, () -> FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 0.1").matches());
		test("float option as int", true, () -> FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 1 //jkhljhlklkjhlkjlkjhlkhj").matches());
		//ifdef tests
		test("ifdef with a comment after it", true, () -> IFDEF_IFNDEF_PATTERN.matcher("#ifdef Godrays //hihihi").matches());
		test("ifdef normal", true, () -> IFDEF_IFNDEF_PATTERN.matcher("#ifdef Godrays").matches());
		test("ifndef", true, () -> IFDEF_IFNDEF_PATTERN.matcher("#ifndef Godrays").matches());
		test("ifndef with stuff after it", true, () -> IFDEF_IFNDEF_PATTERN.matcher("#ifndef Godrays //hi hi hi").matches());
		//int tests
		test("integer option", true, () -> INTEGER_OPTION_PATTERN.matcher("#define VL 5 //comments and stuff").matches());
		test("integer with floating point option",false, () -> INTEGER_OPTION_PATTERN.matcher("#define VL 5.4F //comments").matches());
		test("integer option with char", false, () -> INTEGER_OPTION_PATTERN.matcher("#define VL 4F").matches());
		test("integer option without comment", true, () -> INTEGER_OPTION_PATTERN.matcher("#define Shadows 4").matches());
		//grouping
		testMatcher(FLOAT_INTEGER_OPTION_PATTERN.matcher("#define Density 1.53F //cool stuffz [1F 2.1 3.3 4F]"));
		testMatcher(BOOLEAN_OPTION_PATTERN.matcher("/// #define Stuffs //Caldsfkj;laskfdjlaskjdf;lasfalskjdf"));
		testMatcher(IFDEF_IFNDEF_PATTERN.matcher("#ifndef Godrays //godrays"));
		testMatcher(INTEGER_OPTION_PATTERN.matcher("#define Bloom_Strength 43 //Bloom Strength [4 5 2 3]"));
	}

	private static void testMatcher(Matcher matcher) {
		System.out.println("testing matcher with input: " + matcher);
		if (matcher.matches()) {
			List<String> list = new ArrayList<>();
			for (int i = 0; i < matcher.groupCount(); i++) {
				if (i > 0) list.add(matcher.group(i));
			}
			System.out.println(list);
		} else {
			System.out.println("Matcher failed");
		}
	}

	public static List<String> processConfigOptions(List<String> lines, ShaderPackConfig config) {
		for (int i = 0; i < lines.size(); i++) {

			String trimmedLine = lines.get(i).trim();

			Matcher booleanMatcher = BOOLEAN_OPTION_PATTERN.matcher(trimmedLine);
			Matcher numberMatcher = FLOAT_INTEGER_OPTION_PATTERN.matcher(trimmedLine);

			if (!booleanMatcher.matches() && !numberMatcher.matches()) {
				continue;
			}

			if (booleanMatcher.matches()) {

				boolean containsIfDef = false;

				String name = group(booleanMatcher, "name");
				String startingComment = group(booleanMatcher, "startingComment");
				String trailingComment = group(booleanMatcher,"comment");

				if (name == null) continue; //continue if the name is not apparent. Not sure how this is possible if the regex matches, but to be safe, let's ignore it

				for (String line : lines) {
					Matcher ifdef = IFDEF_IFNDEF_PATTERN.matcher(line);
					if (ifdef.matches()) {
						String ifdefname = group(ifdef, "name");
						if (name.equals(ifdefname)) {
							containsIfDef = true;
						}
					}
				}

				if (!containsIfDef || name.startsWith("MC_")) {
					continue;
				}


				Option<Boolean> option = createBooleanOption(name, trailingComment, startingComment, config); //create a boolean option and sync it with the config

				 String line = trimmedLine; //line to be set

				//if the option is true but there is a comment at the beginning
				//this indicates that the option in config is true, but the line is false
				if (option.getValue() && startingComment != null) {
					line = trimmedLine.replace(startingComment, "");

					//if the option is false but there is no comment at the beginning
					//this indicates that the option in the config is false, but the line is true
				} else if (!option.getValue() && startingComment == null) {
					line = "//" + trimmedLine;
				}
				lines.set(i, line);

			} else if (numberMatcher.matches()) { //matches floats and int options
				Matcher integerMatcher = INTEGER_OPTION_PATTERN.matcher(trimmedLine); //check if it is explicitly integer
				if (!integerMatcher.matches()) { //if it is a float
					String name = group(numberMatcher, "name");
					String value = group(numberMatcher, "value");
					String comment = group(numberMatcher, "comment");

					if (name == null || value == null) continue; //if null, continue

					if (name.startsWith("MC_")) continue;

					Option<Float> floatOption = createFloatOption(name, comment, value, config);

					if (floatOption != null) {
						String line = trimmedLine.replace(value, floatOption.getValue().toString());
						lines.set(i, line);
					}

				} else { //if it  is a int option
					String name = group(integerMatcher, "name");
					String value = group(integerMatcher, "value");
					String comment = group(integerMatcher, "comment");

					if (name == null || value == null) continue;

					if (name.startsWith("MC_")) continue;

					Option<Integer> integerOption = createIntegerOption(name, comment, value, config);

					if (integerOption != null) {
						String line = trimmedLine.replace(value, integerOption.getValue().toString());
						lines.set(i, line);
					}
				}
			}
		}

		return lines;
	}

	private static Option<Boolean> createBooleanOption(String name, String comment, String startingComment, ShaderPackConfig config) {
		boolean defaultValue = startingComment == null;//if the starting comment is not present, then it is default on, otherwise it is off

		Option<Boolean> booleanOption = new Option<>(comment, Arrays.asList(true, false), name, defaultValue, Option.OptionType.BOOLEAN);

		booleanOption = config.processOption(booleanOption, Boolean::parseBoolean);
		config.getBooleanOptions().put(booleanOption.getName(), booleanOption);

		return booleanOption;
	}

	private static Option<Float> createFloatOption(String name, String comment, String value, ShaderPackConfig config) {
		float floatValue;
		try {
			floatValue = Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return null;
		}
		List<Float> floats = new ArrayList<>();
		if (comment != null && comment.contains("[") && comment.contains("]")) {
			String array = comment.substring(comment.indexOf("["), comment.indexOf("]") + 1);
			comment = comment.replace(array, "");
			floats = parseArray(array, Float::parseFloat);
		}

		Option<Float> floatOption = new Option<>(comment, floats, name, floatValue, Option.OptionType.FLOAT);

		floatOption = config.processOption(floatOption, Float::parseFloat);
		config.getFloatOptions().put(floatOption.getName(), floatOption);

		return floatOption;
	}

	private static Option<Integer> createIntegerOption(String name, String comment, String value, ShaderPackConfig config) {
		int intValue;

		try {
			intValue = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}

		List<Integer> integers = new ArrayList<>();

		if (comment != null && comment.contains("[") && comment.contains("]")) {
			String array = comment.substring(comment.indexOf("["), comment.indexOf("]") + 1);
			comment = comment.replace(array, "");
			integers = parseArray(array, Integer::parseInt);
		}

		Option<Integer> integerOption = new Option<>(comment, integers, name, intValue, Option.OptionType.INTEGER);

		integerOption = config.processOption(integerOption, string -> (int)Float.parseFloat(string));//parse as float and cast to string to be flexible
		return integerOption;
	}

	private static String group(Matcher matcher, String name) {
		try {
			return matcher.group(name);
		} catch (IllegalArgumentException e) {
			return null;
		}
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

}
