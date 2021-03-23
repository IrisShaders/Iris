package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.Iris;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.coderbot.iris.gl.shader.StandardMacros.getMcVersion;

public class PropertiesPreprocessor {
	private static final ImmutableMap<String, String> MACRO_CONSTANTS = ImmutableMap.of("MC_VERSION", getMcVersion());
	
	public static List<String> parseProperties(String fileName, String fileContents) {
		List<String> lines = new ArrayList<>();

		boolean currentlyParsingConditionalProperties = false;
		String currentConditional = null;

		for (String line : fileContents.split("\\R")) {
			String trimmedLine = line.trim();

			if (trimmedLine.startsWith("#if ")) {
				String[] splitLine = trimmedLine.split(" ");

				if (splitLine.length < 4) continue;

				currentConditional = splitLine[0];

				String variable = splitLine[1];
				if (MACRO_CONSTANTS.containsKey(variable)) {
					String operator = splitLine[2];
					String value = splitLine[3];
					switch (operator) {
						case "==":
							if (value.equals(MACRO_CONSTANTS.get(variable))) {
								currentlyParsingConditionalProperties = true;
								continue;
							}
							break;
						case "!=":
							if (!value.equals(MACRO_CONSTANTS.get(variable))) {
								currentlyParsingConditionalProperties = true;
								continue;
							}
							break;
						case ">":
							try {
								int intValue = Integer.parseInt(value);
								int macroIntValue = Integer.parseInt(MACRO_CONSTANTS.get(variable));
								if (macroIntValue > intValue) {
									currentlyParsingConditionalProperties = true;
									continue;
								}
							} catch (NumberFormatException e) {
								Iris.logger.error("Cannot compare non-integer condition value and macro value with > in " + fileName);
							}
							break;
						case ">=":
							try {
								int intValue = Integer.parseInt(value);
								int macroIntValue = Integer.parseInt(MACRO_CONSTANTS.get(variable));
								if (macroIntValue >= intValue) {
									currentlyParsingConditionalProperties = true;
									continue;
								}
							} catch (NumberFormatException e) {
								Iris.logger.error("Cannot compare non-integer condition value and macro value with >= in " + fileName);
							}
							break;
						case "<":
							try {
								int intValue = Integer.parseInt(value);
								int macroIntValue = Integer.parseInt(MACRO_CONSTANTS.get(variable));
								if (macroIntValue < intValue) {
									currentlyParsingConditionalProperties = true;
									continue;
								}
							} catch (NumberFormatException e) {
								Iris.logger.error("Cannot compare non-integer condition value and macro value with < in " + fileName);
							}
							break;
						case "<=":
							try {
								int intValue = Integer.parseInt(value);
								int macroIntValue = Integer.parseInt(MACRO_CONSTANTS.get(variable));
								if (macroIntValue <= intValue) {
									currentlyParsingConditionalProperties = true;
									continue;
								}
							} catch (NumberFormatException e) {
								Iris.logger.error("Cannot compare non-integer condition value and macro value with <= in " + fileName);
							}
							break;
						default: {
							Iris.logger.error("Invalid operator " + operator + " in " + fileName);
							continue;
						}
					}
				} else {
					Iris.logger.warn("Unknown variable fileName " + variable + " in " + fileName);
					currentlyParsingConditionalProperties = false;
				}

				continue;
			}

			else if (trimmedLine.startsWith("#else")) {
				if (Objects.equals(currentConditional, "#if")) {
					currentConditional = "#else";
					currentlyParsingConditionalProperties = !currentlyParsingConditionalProperties;
				} else {
					Iris.logger.error("#else without #if in " + fileName);
				}

				continue;
			}

			else if (trimmedLine.startsWith("#endif")) {
				if (Objects.equals(currentConditional, "#if") || Objects.equals(currentConditional, "#else")) {
					currentConditional = null;
					currentlyParsingConditionalProperties = false;
				} else {
					Iris.logger.error("#endif without #if in " + fileName);
				}

				continue;
			}

			if ((Objects.equals(currentConditional, "#if") || Objects.equals(currentConditional, "#else")) && !currentlyParsingConditionalProperties) {
				continue;
			}

			lines.add(line);
		}

		return lines;
	}
}
