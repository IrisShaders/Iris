package net.coderbot.iris.shaderpack.option.values;

import net.coderbot.iris.shaderpack.OptionalBoolean;
import net.coderbot.iris.shaderpack.option.OptionSet;

import java.util.Optional;

public interface OptionValues {
	OptionalBoolean getBooleanValue(String name);
	Optional<String> getStringValue(String name);

	default boolean getBooleanValueOrDefault(String name) {
		return getBooleanValue(name).orElseGet(() -> {
			if (getOptionSet().getBooleanOptions().containsKey(name)) {
				return getOptionSet().getBooleanOptions().get(name).getOption().getDefaultValue();
			} else {
				return false;
			}
		});
	}

	default String getStringValueOrDefault(String name) {
		return getStringValue(name).orElseGet(() -> {
			if (getOptionSet().getStringOptions().containsKey(name)) {
				return getOptionSet().getStringOptions().get(name).getOption().getDefaultValue();
			} else {
				return "";
			}
		});
	}

	int getOptionsChanged();

	MutableOptionValues mutableCopy();
	ImmutableOptionValues toImmutable();
	OptionSet getOptionSet();
}
