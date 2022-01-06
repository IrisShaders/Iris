package net.coderbot.iris.shaderpack.option.values;

import net.coderbot.iris.shaderpack.OptionalBoolean;

import java.util.Optional;

public interface OptionValues {
	OptionalBoolean getBooleanValue(String name);
	Optional<String> getStringValue(String name);
	int getOptionsChanged();

	MutableOptionValues mutableCopy();
	ImmutableOptionValues toImmutable();
}
