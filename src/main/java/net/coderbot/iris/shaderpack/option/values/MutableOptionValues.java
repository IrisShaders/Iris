package net.coderbot.iris.shaderpack.option.values;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.coderbot.iris.shaderpack.option.OptionSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MutableOptionValues implements OptionValues {
	private final OptionSet options;
	private final Set<String> flippedBooleanValues;
	private final Map<String, String> stringValues;

	MutableOptionValues(OptionSet options, Set<String> flippedBooleanValues, Map<String, String> stringValues) {
		this.options = options;
		this.flippedBooleanValues = flippedBooleanValues;
		this.stringValues = stringValues;
	}

	public MutableOptionValues(OptionSet options, Map<String, String> values) {
		this.options = options;
		this.flippedBooleanValues = new HashSet<>();
		this.stringValues = new HashMap<>();

		this.addAll(values);
	}

	public OptionSet getOptions() {
		return options;
	}

	public Set<String> getFlippedBooleanValues() {
		return flippedBooleanValues;
	}

	public Map<String, String> getStringValues() {
		return stringValues;
	}

	public void addAll(Map<String, String> values) {
		options.getBooleanOptions().forEach((name, option) -> {
			String value = values.get(name);
			boolean booleanValue;

			if (value == null) {
				return;
			}

			if (value.equals("false")) {
				booleanValue = false;
			} else if (value.equals("true")) {
				booleanValue = true;
			} else {
				// Invalid value specified, ignore it
				// TODO: Diagnostic message?
				return;
			}

			if (booleanValue != option.getOption().getDefaultValue()) {
				flippedBooleanValues.add(name);
			}
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
				// Ignore the value if it's a default.
				return;
			}

			stringValues.put(name, value);
		});
	}

	@Override
	public boolean isBooleanFlipped(String name) {
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
		return new ImmutableOptionValues(options, ImmutableSet.copyOf(flippedBooleanValues),
				ImmutableMap.copyOf(stringValues));
	}
}
