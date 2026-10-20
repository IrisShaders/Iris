package net.irisshaders.iris.shaderpack.option.values;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.shaderpack.option.OptionSet;

import java.util.HashMap;
import java.util.Optional;

public class ImmutableOptionValues implements OptionValues {
	private final OptionSet options;
	private final Object2BooleanMap<String> booleanValues;
	private final ImmutableMap<String, String> stringValues;

	ImmutableOptionValues(OptionSet options, Object2BooleanMap<String> booleanValues, ImmutableMap<String, String> stringValues) {
		this.options = options;
		this.booleanValues = booleanValues;
		this.stringValues = stringValues;
	}

	@Override
	public OptionalBoolean getBooleanValue(String name) {
		if (booleanValues.containsKey(name)) {
			return booleanValues.getBoolean(name) ? OptionalBoolean.TRUE : OptionalBoolean.FALSE;
		} else {
			return OptionalBoolean.DEFAULT;
		}
	}

	@Override
	public Optional<String> getStringValue(String name) {
		return Optional.ofNullable(stringValues.get(name));
	}

	@Override
	public int getOptionsChanged() {
		return this.stringValues.size() + this.booleanValues.size();
	}

	@Override
	public MutableOptionValues mutableCopy() {
		return new MutableOptionValues(options, new Object2BooleanOpenHashMap<>(booleanValues), new HashMap<>(stringValues));
	}

	@Override
	public ImmutableOptionValues toImmutable() {
		return this;
	}

	@Override
	public OptionSet getOptionSet() {
		return options;
	}
}
