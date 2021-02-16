package net.coderbot.iris.shaderpack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import net.minecraft.util.Util;

public class ConfigOptionParser {
	/*
	 Regex for matching boolean options
	  Match if or if not the line starts with anynumber of backslashes that are more than 2 ("//")
	  Match 0 or more whitspace after the "//"
	  Match the #define
	  Match 1 whitespace after that
	  Match any letter, number, or underscore name
	  Match 0 or more whitespace after that
	  Match any comments on the option after that
	 */
	private static final Pattern BOOLEAN_OPTION_PATTERN = Pattern.compile("^(?<startingComment>//+)?\\s*(?<define>#define)\\s+(?<name>\\w+)\\s*(?<comment>(?<commentChar>//+)(?<commentContent>.*))?$");
	/*
	  Regex for matching ifdef patterns for boolean options
	  Match the ifdef or ifndef keyword
	  match whitespace that must be there
	  match a word that is the name of the keyword
	 */
	private static final Pattern IFDEF_IFNDEF_PATTERN = Pattern.compile("^(?<ifdef>#ifdef|#ifndef)\\s+(?<name>\\w+)(?<other>.*)");
	/*
	   Regex that matches for integer and float options
	   Match 1 or more whitespace after #define
	   match a word (name of the option). Put in parenthesis for grouping
	   match 1 or more whitespace after the name
	      match a negative if there
	      match f or F float keyword
	      match 0-9
	      match a "." for floats
	   match 1 or more of the char group
	   match 0 or more whitespace
	   match if there is a comment following the line or not
	 */
	private static final Pattern FLOAT_INTEGER_OPTION_PATTERN = Pattern.compile("^(?<define>#define)\\s+(?<name>\\w+)\\s+(?<value>-?[\\d.fF]+)\\s*(?<comment>(?<commentChar>//+)(?<commentContent>.*))?$");
	/*
	 Regex that matches for only integers and not floats
	 Same as above but in the char class we remove the float specific checks
		remove matching a "."
		remove matching a "f" or a "F"
	 */
	private static final Pattern INTEGER_OPTION_PATTERN = Pattern.compile("^(?<define>#define)\\s+(?<name>\\w+)\\s+(?<value>-?\\d+)\\s*(?<comment>(?<commentChar>//+)(?<commentContent>.*))?$");
	/*
	Some shaderpacks like sildurs have #define directives that are named with the program name
	like #define gbuffers_textured
	optifine does not use these in their config so we will not as well
	TODO figure what optifine does with these program defined names and implement
	 */
	private static final Set<String> IGNORED_PROGRAM_NAMES = Util.make(new HashSet<>(), (set) -> {
		for (int i = 0; i < 16; i++) {
			set.add("composite" + i);
		}
		set.add("composite");
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

	public static void processConfigOptions(List<String> lines, ShaderPackConfig config) {
		for (int i = 0; i < lines.size(); i++) {

			String trimmedLine = lines.get(i).trim();

			Matcher booleanMatcher = BOOLEAN_OPTION_PATTERN.matcher(trimmedLine);
			Matcher numberMatcher = FLOAT_INTEGER_OPTION_PATTERN.matcher(trimmedLine);

			if (!booleanMatcher.matches() && !numberMatcher.matches()) {
				continue;
			}

			if (booleanMatcher.matches()) {

				String name = group(booleanMatcher, "name");
				String startingComment = group(booleanMatcher, "startingComment");
				String trailingComment = group(booleanMatcher,"commentContent");

				if (name == null) continue; //continue if the name is not apparent. Not sure how this is possible if the regex matches, but to be safe, let's ignore it



				if (!containsIfDef(lines, name) || name.startsWith("MC_") || IGNORED_PROGRAM_NAMES.contains(name)) {
					continue;
				}


				Option<Boolean> option = createBooleanOption(name, trailingComment, startingComment, config); //create a boolean option and sync it with the config

				lines.set(i, applyBooleanOption(option, trimmedLine, startingComment));
				System.out.println(option);

			} else if (numberMatcher.matches()) { //matches floats and int options
				Matcher integerMatcher = INTEGER_OPTION_PATTERN.matcher(trimmedLine); //check if it is explicitly integer
				if (!integerMatcher.matches()) { //if it is a float
					String name = group(numberMatcher, "name");
					String value = group(numberMatcher, "value");
					String comment = group(numberMatcher, "commentContent");

					if (name == null || value == null) continue; //if null, continue

					if (name.startsWith("MC_") || IGNORED_PROGRAM_NAMES.contains(name)) continue;

					Option<Float> floatOption = createFloatOption(name, comment, value, config);

					if (floatOption != null) {
						String line = trimmedLine.replace(value, floatOption.getValue().toString());
						lines.set(i, line);
						System.out.println(floatOption);
					}

				} else { //if it  is a int option
					String name = group(integerMatcher, "name");
					String value = group(integerMatcher, "value");
					String comment = group(integerMatcher, "commentContent");

					if (name == null || value == null) continue;

					if (name.startsWith("MC_") || IGNORED_PROGRAM_NAMES.contains(name)) continue;

					Option<Integer> integerOption = createIntegerOption(name, comment, value, config);

					if (integerOption != null) {
						String line = trimmedLine.replace(value, integerOption.getValue().toString());
						lines.set(i, line);
						System.out.println(integerOption);
					}
				}
			}
		}

	}

	/**
	 * Checks if a name has a matching ifdef pattern in the same file
	 * @param lines the file, split
	 * @param name the name of the boolean option to check ifdef's for
	 * @return if the file contains an ifdef or ifndef with the correct name
	 */
	private static boolean containsIfDef(List<String> lines, String name) {
		for (String line : lines) {
			Matcher ifdef = IFDEF_IFNDEF_PATTERN.matcher(line);
			if (ifdef.matches()) {
				String ifdefname = group(ifdef, "name");
				if (name.equals(ifdefname)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String applyBooleanOption(Option<Boolean> option, String line, String startingComment) {
		if (option.getValue() && startingComment != null) {
			return line.replace(startingComment, "");

			//if the option is false but there is no comment at the beginning
			//this indicates that the option in the config is false, but the line is true
		} else if (!option.getValue() && startingComment == null) {
			return  "//" + line;
		}

		return line;
	}

	/**
	 * Creates an boolean option that is synced with a config based on members of a line
	 * @param name name of option
	 * @param comment comment of option
	 * @param startingComment the comment in front of "#define" to determine the boolean options default value
	 * @param config config instance to sync
	 * @return a new option
	 */
	private static Option<Boolean> createBooleanOption(String name, String comment, String startingComment, ShaderPackConfig config) {
		boolean defaultValue = startingComment == null;//if the starting comment is not present, then it is default on, otherwise it is off

		Option<Boolean> booleanOption = new Option<>(comment, Arrays.asList(true, false), name, defaultValue, Boolean::parseBoolean);

		booleanOption = config.processOption(booleanOption);
		config.addBooleanOption(booleanOption);

		return booleanOption;
	}

	/**
	 * Creates a float option that is synced with a config based on elements of a line
	 * @param name name of option
	 * @param comment tooltip/comment of option
	 * @param value value of option
	 * @param config config instance to sync to
	 * @return new float option
	 */
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

		Option<Float> floatOption = new Option<>(comment, floats, name, floatValue, Float::parseFloat);

		floatOption = config.processOption(floatOption);
		config.addFloatOption(floatOption);

		return floatOption;
	}

	/**
	 * Creates a boolean option based on information contained in a line
	 * @param name name of the option
	 * @param comment comment/tooltip of the option
	 * @param value value of the option
	 * @param config config instance to sync the value of the option with
	 * @return a new synced option
	 */
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

		Option<Integer> integerOption = new Option<>(comment, integers, name, intValue, (string) -> (int)Float.parseFloat(string));//parse as float and cast to string to be flexible

		integerOption = config.processOption(integerOption);
		config.addIntegerOption(integerOption);
		return integerOption;
	}

	/**
	 * Identical to {@link Matcher#group(String)} but instead of throwing an exception if the incorrect argument is entered, it returns null
	 * @param matcher the matcher to grab a group from
	 * @param name name of the group
	 * @return the string representation of that group or null if that name is not in the group or error while parsing groups
	 *
	 * @see Matcher#group(String)
	 * @see Matcher#getMatchedGroupIndex(String)  this throws the exception that we catch
	 */
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
