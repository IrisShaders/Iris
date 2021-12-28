package net.coderbot.iris.shaderpack.option;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.shaderpack.option.values.OptionValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Profile {
	public final String name;
	public final Map<String, String> optionValues;
	public final List<String> disabledPrograms;

	private Profile(String name, Map<String, String> optionValues, List<String> disabledPrograms) {
		this.name = name;
		this.optionValues = optionValues;
		this.disabledPrograms = disabledPrograms;
	}

	public static Profile parse(String name, Map<String, List<String>> tree) throws IllegalArgumentException {
		Builder builder = new Builder(name);
		List<String> options = tree.get(name);

		if (options == null) {
			throw new IllegalArgumentException("Profile \"" + name + "\" does not exist!");
		}

		for (String option : options) {
			if (option.startsWith("!program.")) {
				builder.disableProgram(option.substring("!program.".length()));
			} else if (option.startsWith("profile.")) {
				String dependency = option.substring("profile.".length());

				if (name.equals(dependency)) {
					throw new IllegalArgumentException("Error parsing profile \"" + name + "\", tries to include itself!");
				}

				builder.addAll(parse(dependency, tree));
			} else if (option.startsWith("!")) {
				builder.option(option.substring(1), "false");
			} else if (option.contains("=")) {
				int splitPoint = option.indexOf("=");
				builder.option(option.substring(0, splitPoint), option.substring(splitPoint + 1));
			} else if (option.contains(":")) {
				int splitPoint = option.indexOf(":");
				builder.option(option.substring(0, splitPoint), option.substring(splitPoint + 1));
			} else {
				builder.option(option, "true");
			}
		}

		return builder.build();
	}

	public boolean matches(OptionSet options, OptionValues values) {
		for (Map.Entry<String, String> entry : this.optionValues.entrySet()) {
			String option = entry.getKey();
			String value = entry.getValue();

			if (options.getBooleanOptions().containsKey(option)) {
				boolean currentValue = options.getBooleanOptions().get(option).getOption().getDefaultValue() != values.isBooleanFlipped(option);

				if (!Boolean.toString(currentValue).equals(value)) {
					return false;
				}
			}
			if (options.getStringOptions().containsKey(option)) {
				String currentValue = values.getStringValue(option).orElse(options.getStringOptions().get(option).getOption().getDefaultValue());

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
