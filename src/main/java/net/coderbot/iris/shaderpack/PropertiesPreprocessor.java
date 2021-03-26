package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.shader.StandardMacros;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PropertiesPreprocessor {
	private static final ImmutableMap<String, String> MACRO_CONSTANTS = ImmutableMap.of("MC_VERSION", StandardMacros.getMcVersion());
	
	public static String process(String fileName, String fileContents) {
		List<String> lines = new ArrayList<>();

		boolean currentConditionPassed = false;
		String currentConditional = null;

		for (String line : fileContents.split("\\R")) {
			String trimmedLine = line.trim();

			if (trimmedLine.startsWith("#if ")) {
				String[] splitLine = trimmedLine.split(" ");

				if (splitLine.length < 4) continue;
				if (Objects.equals(currentConditional, "#if")) {
					Iris.logger.error("Nested if conditions in " + fileName + " are not supported yet, but the current shaderpack is trying to use them!");
					continue;
				}

				currentConditional = splitLine[0];

				String variable = splitLine[1];
				if (MACRO_CONSTANTS.containsKey(variable)) {
					String operator = splitLine[2];
					String value = splitLine[3];
					switch (operator) {
						case "==":
							if (value.equals(MACRO_CONSTANTS.get(variable))) {
								currentConditionPassed = true;
								continue;
							}
							break;
						case "!=":
							if (!value.equals(MACRO_CONSTANTS.get(variable))) {
								currentConditionPassed = true;
								continue;
							}
							break;
						case ">":
							try {
								int intValue = Integer.parseInt(value);
								int macroIntValue = Integer.parseInt(MACRO_CONSTANTS.get(variable));
								if (macroIntValue > intValue) {
									currentConditionPassed = true;
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
									currentConditionPassed = true;
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
									currentConditionPassed = true;
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
									currentConditionPassed = true;
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
					currentConditionPassed = false;
				}

				continue;
			}

			else if (trimmedLine.startsWith("#else")) {
				if (Objects.equals(currentConditional, "#if")) {
					currentConditional = "#else";
					currentConditionPassed = !currentConditionPassed;
				} else {
					Iris.logger.error("#else without #if in " + fileName);
				}

				continue;
			}

			else if (trimmedLine.startsWith("#endif")) {
				if (Objects.equals(currentConditional, "#if") || Objects.equals(currentConditional, "#else")) {
					currentConditional = null;
					currentConditionPassed = false;
				} else {
					Iris.logger.error("#endif without #if in " + fileName);
				}

				continue;
			}

			else if (trimmedLine.startsWith("#elif")) {
				Iris.logger.error("#elif conditions in " + fileName + " are not yet supported, but the current shaderpack is trying to use them!");
				continue;
			}

			if ((Objects.equals(currentConditional, "#if") || Objects.equals(currentConditional, "#else")) && !currentConditionPassed) {
				continue;
			}

			lines.add(line);
		}

		StringBuilder processed = new StringBuilder();
		for (String line : lines) {
			processed.append(line);
			processed.append('\n');
		}

		return processed.toString();
	}


}
