package net.coderbot.iris.shaderpack.option.values;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.coderbot.iris.shaderpack.option.OptionSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class ImmutableOptionValues implements OptionValues {
	private final OptionSet options;
	private final ImmutableSet<String> flippedBooleanValues;
	private final ImmutableMap<String, String> stringValues;

	ImmutableOptionValues(OptionSet options, ImmutableSet<String> flippedBooleanValues,
						  ImmutableMap<String, String> stringValues) {
		this.options = options;
		this.flippedBooleanValues = flippedBooleanValues;
		this.stringValues = stringValues;
	}

	@Override
	public boolean shouldFlip(String name) {
		return flippedBooleanValues.contains(name);
	}

	@Override
	public Optional<String> getStringValue(String name) {
		return Optional.ofNullable(stringValues.get(name));
	}

	@Override
	public MutableOptionValues mutableCopy() {
		return new MutableOptionValues(options, new HashSet<>(flippedBooleanValues), new HashMap<>(stringValues));
	}

	@Override
	public ImmutableOptionValues toImmutable() {
		return this;
	}
}
