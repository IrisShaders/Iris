package net.irisshaders.iris.shaderpack.option.values;

import com.google.common.collect.ImmutableMap;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.shaderpack.option.OptionSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MutableOptionValues implements OptionValues {
	private final OptionSet options;
	private final Map<String, Boolean> booleanValues;
	private final Map<String, String> stringValues;

	MutableOptionValues(OptionSet options, Map<String, Boolean> booleanValues, Map<String, String> stringValues) {
		Map<String, String> values = new HashMap<>();

		booleanValues.forEach((k, v) -> values.put(k, Boolean.toString(v)));
		values.putAll(stringValues);

		this.options = options;
		this.booleanValues = new HashMap<>();
		this.stringValues = new HashMap<>();

		this.addAll(values);
	}

	public MutableOptionValues(OptionSet options, Map<String, String> values) {
		this.options = options;
		this.booleanValues = new HashMap<>();
		this.stringValues = new HashMap<>();

		this.addAll(values);
	}

	public OptionSet getOptions() {
		return options;
	}

	public Map<String, Boolean> getBooleanValues() {
		return booleanValues;
	}

	public Map<String, String> getStringValues() {
		return stringValues;
	}

	public void addAll(Map<String, String> values) {
		options.getBooleanOptions().forEach((name, option) -> {
			String value = values.get(name);
			OptionalBoolean booleanValue;

			if (value == null) {
				return;
			}

			if (value.equals("false")) {
				booleanValue = OptionalBoolean.FALSE;
			} else if (value.equals("true")) {
				booleanValue = OptionalBoolean.TRUE;
			} else {
				// Invalid value specified, ignore it
				// TODO: Diagnostic message?
				booleanValue = OptionalBoolean.DEFAULT;
			}

			boolean actualValue = booleanValue.orElse(option.getOption().getDefaultValue());

			if (actualValue == option.getOption().getDefaultValue()) {
				// Just set it to default by removing it from the map
				booleanValues.remove(name);
				return;
			}

			booleanValues.put(name, actualValue);
		});

		options.getStringOptions().forEach((name, option) -> {
			String value = values.get(name);

			if (value == null) {
				return;
			}

			// NB: We don't check if the option is in the allowed values here. This matches OptiFine
			//     behavior, the allowed values is only used when the user is changing options in the
			//     GUI. Profiles might specify values for options that aren't in the allowed values
			//     list, and values typed manually into the config .txt are unchecked.

			if (value.equals(option.getOption().getDefaultValue())) {
				stringValues.remove(name);
				return;
			}

			stringValues.put(name, value);
		});
	}

	@Override
	public OptionalBoolean getBooleanValue(String name) {
		if (booleanValues.containsKey(name)) {
			return booleanValues.get(name) ? OptionalBoolean.TRUE : OptionalBoolean.FALSE;
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
		return new MutableOptionValues(options, new HashMap<>(booleanValues), new HashMap<>(stringValues));
	}

	@Override
	public ImmutableOptionValues toImmutable() {
		return new ImmutableOptionValues(options, ImmutableMap.copyOf(booleanValues),
			ImmutableMap.copyOf(stringValues));
	}

	@Override
	public OptionSet getOptionSet() {
		return options;
	}
}
