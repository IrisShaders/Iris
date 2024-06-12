package net.irisshaders.iris.shaderpack.parsing;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;

import java.util.EmptyStackException;
import java.util.Stack;

public class BooleanParser {
	private enum Operation {
		AND {
			@Override
			boolean compute(boolean value, Stack<Boolean> valueStack) {
				return valueStack.pop() && value;
			}
		}, OR {
			@Override
			boolean compute(boolean value, Stack<Boolean> valueStack) {
				return valueStack.pop() || value;
			}
		}, NOT {
			@Override
			boolean compute(boolean value, Stack<Boolean> valueStack) {
				return !value;
			}
		}, OPEN;

		boolean compute(boolean value, Stack<Boolean> valueStack) {
			return value;
		}
	}

	/**
	 * parses the given expression
	 * @param expression expression to parse
	 * @param valueLookup lookup of shadow options
	 * @return result of the expression, or true if there was an error
	 */
	public static boolean parse(String expression, OptionValues valueLookup) {
		try {
			String option = "";
			Stack<Operation> operationStack = new Stack<>();
			Stack<Boolean> valueStack = new Stack<>();
			for (int i = 0; i < expression.length(); i++) {
				char c = expression.charAt(i);
				switch (c) {
					case '!' -> operationStack.push(Operation.NOT);
					case '&' -> {
						// add value first, because this checks for preceding NOTs
						if (!option.isEmpty()) {
							valueStack.push(processValue(option, valueLookup, operationStack));
							option = "";
						}
						// AND operators have priority, so add a bracket if it's the first AND
						if (operationStack.isEmpty() || !operationStack.peek().equals(Operation.AND)) {
							operationStack.push(Operation.OPEN);
						}
						i++;
						operationStack.push(Operation.AND);
					}
					case '|' -> {
						// add value first, because this checks for preceding NOTs
						if (!option.isEmpty()) {
							valueStack.push(processValue(option, valueLookup, operationStack));
							option = "";
						}
						// if there was an AND before, that needs to be evaluated because it takes priority
						if (!operationStack.isEmpty() && operationStack.peek().equals(Operation.AND)) {
							evaluate(operationStack, valueStack, true);
						}
						i++;
						operationStack.push(Operation.OR);
					}
					case '(' -> operationStack.push(Operation.OPEN);
					case ')' -> {
						// add value first, because this checks for preceding NOTs
						if (!option.isEmpty()) {
							valueStack.push(processValue(option, valueLookup, operationStack));
							option = "";
						}
						// if there was an AND before, that needs to be evaluated because it added its own bracket
						if (!operationStack.isEmpty() && operationStack.peek().equals(Operation.AND)) {
							evaluate(operationStack, valueStack, true);
						}
						evaluate(operationStack, valueStack, true);
					}
					case ' ' -> {}
					default -> option += c;
				}
			}
			if (!option.isEmpty()) {
				valueStack.push(processValue(option, valueLookup, operationStack));
			}
			evaluate(operationStack, valueStack, false);
			boolean result = valueStack.pop();
			if (!valueStack.isEmpty() || !operationStack.isEmpty()) {
				Iris.logger.warn(
					"Failed to parse the following boolean operation correctly, stacks not empty, defaulting to true!: '{}'",
					expression);
				return true;
			}
			return result;
		} catch (EmptyStackException emptyStackException) {
			Iris.logger.warn(
				"Failed to parse the following boolean operation correctly, stacks empty when it shouldn't, defaulting to true!: '{}'",
				expression);
			return true;
		}
	}

	/**
	 * gets the value for the given string and negates it if there is a NOT in the operationStack
	 */
	private static boolean processValue(String value, OptionValues valueLookup, Stack<Operation> operationStack) {
		boolean booleanValue = switch (value) {
			case "true", "1" -> true;
			case "false", "0" -> false;
			default -> valueLookup != null && valueLookup.getBooleanValueOrDefault(value);
		};
		if (!operationStack.isEmpty() && operationStack.peek() == Operation.NOT) {
			// if there is a NOT, that needs to be handled immediately
			operationStack.pop();
			return !booleanValue;
		} else {
			return booleanValue;
		}
	}

	/**
	 * evaluates the operation stack backwards, to the next bracket, or the whole way
	 * @param operationStack Stack with operations
	 * @param valueStack Stack with values
	 * @param currentBracket only evaluates the current bracket
	 */
	private static void evaluate(Stack<Operation> operationStack, Stack<Boolean> valueStack, boolean currentBracket) {
		boolean value = valueStack.pop();
		while (!operationStack.isEmpty() && (!currentBracket || operationStack.peek() != Operation.OPEN)) {
			value = operationStack.pop().compute(value, valueStack);
		}

		// if there is a bracket check if the whole bracket should be negated
		if (!operationStack.isEmpty() && operationStack.peek() == Operation.OPEN) {
			operationStack.pop();
			if (!operationStack.isEmpty() && operationStack.peek() == Operation.NOT) {
				value = operationStack.pop().compute(value, valueStack);
			}
		}
		valueStack.push(value);
	}
}
