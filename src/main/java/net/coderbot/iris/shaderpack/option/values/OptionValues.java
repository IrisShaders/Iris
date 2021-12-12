package net.coderbot.iris.shaderpack.option.values;

import java.util.Optional;

public interface OptionValues {
	boolean isBooleanFlipped(String name);
	Optional<String> getStringValue(String name);

	MutableOptionValues mutableCopy();
	ImmutableOptionValues toImmutable();
}
