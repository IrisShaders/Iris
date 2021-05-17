package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.coderbot.iris.Iris;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class DispatchingDirectiveHolder implements DirectiveHolder {
	private final Map<String, BooleanConsumer> booleanConstVariables;
	private final Map<String, Consumer<String>> stringConstVariables;
	private final Map<String, IntConsumer> intConstVariables;
	private final Map<String, FloatConsumer> floatConstVariables;

	public DispatchingDirectiveHolder() {
		booleanConstVariables = new HashMap<>();
		stringConstVariables = new HashMap<>();
		intConstVariables = new HashMap<>();
		floatConstVariables = new HashMap<>();
	}

	public void processSource(String source) {
		ConstDirectiveParser.findDirectives(source).forEach(this::processDirective);
	}

	private void processDirective(ConstDirectiveParser.ConstDirective directive) {
		final ConstDirectiveParser.Type type = directive.getType();
		final String key = directive.getKey();
		final String value = directive.getValue();

		if (type == ConstDirectiveParser.Type.BOOL) {
			BooleanConsumer consumer = booleanConstVariables.get(key);

			if (consumer != null) {
				if ("true".equals(value)) {
					consumer.accept(true);
				} else if ("false".equals(value)) {
					consumer.accept(false);
				} else {
					Iris.logger.error("Failed to process " + directive + ": " + value + " is not a valid boolean value");
				}

				return;
			}

			Iris.logger.info("Found potential unhandled directive: " + directive);

			typeCheckHelper("int", intConstVariables, directive);
			typeCheckHelper("int", stringConstVariables, directive);
			typeCheckHelper("float", floatConstVariables, directive);
		} else if (type == ConstDirectiveParser.Type.INT) {
			// GLSL does not actually have a string type, so string constant directives use "const int" instead.
			Consumer<String> stringConsumer = stringConstVariables.get(key);

			if (stringConsumer != null) {
				stringConsumer.accept(value);

				return;
			}

			IntConsumer intConsumer = intConstVariables.get(key);

			if (intConsumer != null) {
				try {
					intConsumer.accept(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					Iris.logger.error("Failed to process " + directive, e);
				}

				return;
			}

			Iris.logger.info("Found potential unhandled directive: " + directive);

			typeCheckHelper("bool", booleanConstVariables, directive);
			typeCheckHelper("float", floatConstVariables, directive);
		} else if (type == ConstDirectiveParser.Type.FLOAT) {
			FloatConsumer consumer = floatConstVariables.get(key);

			if (consumer != null) {
				try {
					consumer.accept(Float.parseFloat(value));
				} catch (NumberFormatException e) {
					Iris.logger.error("Failed to process " + directive, e);
				}

				return;
			}

			Iris.logger.info("Found potential unhandled directive: " + directive);

			typeCheckHelper("bool", booleanConstVariables, directive);
			typeCheckHelper("int", intConstVariables, directive);
			typeCheckHelper("int", stringConstVariables, directive);
		} else if (type == ConstDirectiveParser.Type.VEC4) {
			Iris.logger.warn("Ignoring " + directive + " because vec4 directives are currently unsupported.");
		}
	}

	private void typeCheckHelper(String expected, Map<String, ? extends Object> candidates, ConstDirectiveParser.ConstDirective directive) {
		if (candidates.containsKey(directive.getKey())) {
			Iris.logger.warn("Ignoring " + directive + " because it is of the wrong type, a type of " + expected + " is expected.");
		}
	}

	@Override
	public void acceptCommentStringDirective(String name, Consumer<String> consumer) {
		// TODO
		Iris.logger.warn("Not looking for a comment string directive with the name " + name + " since it's not currently supported.");
	}

	@Override
	public void acceptCommentIntDirective(String name, IntConsumer consumer) {
		// TODO
		Iris.logger.warn("Not looking for a comment int directive with the name " + name + " since it's not currently supported.");
	}

	@Override
	public void acceptCommentFloatDirective(String name, FloatConsumer consumer) {
		// TODO
		Iris.logger.warn("Not looking for a comment float directive with the name " + name + " since it's not currently supported.");
	}

	@Override
	public void acceptConstBooleanDirective(String name, BooleanConsumer consumer) {
		booleanConstVariables.put(name, consumer);
	}

	@Override
	public void acceptConstStringDirective(String name, Consumer<String> consumer) {
		stringConstVariables.put(name, consumer);
	}

	@Override
	public void acceptConstIntDirective(String name, IntConsumer consumer) {
		intConstVariables.put(name, consumer);
	}

	@Override
	public void acceptConstFloatDirective(String name, FloatConsumer consumer) {
		floatConstVariables.put(name, consumer);
	}
}
