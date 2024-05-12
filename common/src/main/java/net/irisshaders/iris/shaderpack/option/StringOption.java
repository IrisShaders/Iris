package net.irisshaders.iris.shaderpack.option;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringOption extends BaseOption {
	private final String defaultValue;
	private final ImmutableList<String> allowedValues;

	private StringOption(OptionType type, String name, String defaultValue) {
		super(type, name, null);

		this.defaultValue = Objects.requireNonNull(defaultValue);
		this.allowedValues = ImmutableList.of(defaultValue);
	}

	private StringOption(OptionType type, String name, String comment, String defaultValue, ImmutableList<String> allowedValues) {
		super(type, name, comment);

		this.defaultValue = Objects.requireNonNull(defaultValue);
		this.allowedValues = allowedValues;
	}

	@Nullable
	public static StringOption create(OptionType type, String name, String comment, String defaultValue) {
		if (comment == null) {
			return null;
		}

		int openingBracket = comment.indexOf('[');

		if (openingBracket == -1) {
			return null;
		}

		int closingBracket = comment.indexOf(']', openingBracket);

		if (closingBracket == -1) {
			return null;
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

	@NotNull
	public String getDefaultValue() {
		return defaultValue;
	}

	@NotNull
	public ImmutableList<String> getAllowedValues() {
		return allowedValues;
	}
}
