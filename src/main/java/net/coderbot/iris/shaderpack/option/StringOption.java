package net.coderbot.iris.shaderpack.option;

import com.google.common.collect.ImmutableList;

public class StringOption extends BaseOption {
	private final String defaultValue;
	private final ImmutableList<String> allowedValues;

	private StringOption(OptionType type, String name, String defaultValue) {
		super(type, name, null);

		this.defaultValue = defaultValue;
		this.allowedValues = ImmutableList.of(defaultValue);
	}

	private StringOption(OptionType type, String name, String comment, String defaultValue, ImmutableList<String> allowedValues) {
		super(type, name, comment);

		this.defaultValue = defaultValue;
		this.allowedValues = allowedValues;
	}

	public static StringOption createUncommented(OptionType type, String name, String defaultValue) {
		return new StringOption(type, name, defaultValue);
	}

	public static StringOption create(OptionType type, String name, String comment, String defaultValue) {
		if (comment == null) {
			return new StringOption(type, name, null, defaultValue, ImmutableList.of(defaultValue));
		}

		int openingBracket = comment.indexOf('[');

		if (openingBracket == -1) {
			return new StringOption(type, name, comment, defaultValue, ImmutableList.of(defaultValue));
		}

		int closingBracket = comment.indexOf(']', openingBracket);

		if (closingBracket == -1) {
			return new StringOption(type, name, comment, defaultValue, ImmutableList.of(defaultValue));
		}

		String[] allowedValues = comment.substring(openingBracket + 1, closingBracket).split(" ");
		comment = comment.substring(0, openingBracket) + comment.substring(closingBracket + 1);
		boolean allowedValuesContainsDefaultValue = false;

		for (String value : allowedValues) {
			if (defaultValue.equals(value)) {
				allowedValuesContainsDefaultValue = true;
				break;
			}
		}

		ImmutableList.Builder<String> builder = ImmutableList.builder();

		builder.add(allowedValues);

		if (!allowedValuesContainsDefaultValue) {
			builder.add(defaultValue);
		}

		return new StringOption(type, name, comment.trim(), defaultValue, builder.build());
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public ImmutableList<String> getAllowedValues() {
		return allowedValues;
	}
}
