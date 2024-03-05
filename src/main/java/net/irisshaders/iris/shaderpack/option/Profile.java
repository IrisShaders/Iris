package net.irisshaders.iris.shaderpack.option;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Profile {
	public final String name;
	public final int precedence; // Used for prioritizing during matching
	public final Map<String, String> optionValues;
	public final List<String> disabledPrograms;

	private Profile(String name, Map<String, String> optionValues, List<String> disabledPrograms) {
		this.name = name;
		this.optionValues = optionValues;
		this.precedence = optionValues.size();
		this.disabledPrograms = disabledPrograms;
	}

	public boolean matches(OptionSet options, OptionValues values) {
		for (Map.Entry<String, String> entry : this.optionValues.entrySet()) {
			String option = entry.getKey();
			String value = entry.getValue();

			if (options.getBooleanOptions().containsKey(option)) {
				boolean currentValue = values.getBooleanValueOrDefault(option);

				if (!Boolean.toString(currentValue).equals(value)) {
					return false;
				}
			}
			if (options.getStringOptions().containsKey(option)) {
				String currentValue = values.getStringValueOrDefault(option);

				if (!value.equals(currentValue)) {
					return false;
				}
			}
		}

		return true;
	}

	public static class Builder {
		private final String name;
		private final Map<String, String> optionValues = new HashMap<>();
		private final List<String> disabledPrograms = new ArrayList<>();

		public Builder(String name) {
			this.name = name;
		}

		public Builder option(String optionId, String value) {
			this.optionValues.put(optionId, value);

			return this;
		}

		public Builder disableProgram(String programId) {
			this.disabledPrograms.add(programId);

			return this;
		}

		public Builder addAll(Profile other) {
			this.optionValues.putAll(other.optionValues);
			this.disabledPrograms.addAll(other.disabledPrograms);

			return this;
		}

		public Profile build() {
			return new Profile(name, ImmutableMap.copyOf(optionValues), ImmutableList.copyOf(disabledPrograms));
		}
	}
}
