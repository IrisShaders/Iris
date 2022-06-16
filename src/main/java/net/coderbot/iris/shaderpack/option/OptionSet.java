package net.coderbot.iris.shaderpack.option;

import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.Iris;

import java.util.HashMap;
import java.util.Map;

public class OptionSet {
	private final ImmutableMap<String, MergedBooleanOption> booleanOptions;
	private final ImmutableMap<String, MergedStringOption> stringOptions;

	private OptionSet(Builder builder) {
		this.booleanOptions = ImmutableMap.copyOf(builder.booleanOptions);
		this.stringOptions = ImmutableMap.copyOf(builder.stringOptions);
	}

	public ImmutableMap<String, MergedBooleanOption> getBooleanOptions() {
		return this.booleanOptions;
	}

	public ImmutableMap<String, MergedStringOption> getStringOptions() {
		return this.stringOptions;
	}

	public boolean isBooleanOption(String name) {
		return booleanOptions.containsKey(name);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final Map<String, MergedBooleanOption> booleanOptions;
		private final Map<String, MergedStringOption> stringOptions;

		public Builder() {
			this.booleanOptions = new HashMap<>();
			this.stringOptions = new HashMap<>();
		}

		public void addAll(OptionSet other) {
			if (this.booleanOptions.isEmpty()) {
				this.booleanOptions.putAll(other.booleanOptions);
			} else {
				other.booleanOptions.values().forEach(this::addBooleanOption);
			}

			if (this.stringOptions.isEmpty()) {
				this.stringOptions.putAll(other.stringOptions);
			} else {
				other.stringOptions.values().forEach(this::addStringOption);
			}
		}

		public void addBooleanOption(OptionLocation location, BooleanOption option) {
			addBooleanOption(new MergedBooleanOption(location, option));
		}

		public void addBooleanOption(MergedBooleanOption proposed) {
			BooleanOption option = proposed.getOption();
			MergedBooleanOption existing = booleanOptions.get(option.getName());

			MergedBooleanOption merged;

			if (existing != null) {
				merged = existing.merge(proposed);

				if (merged == null) {
					// TODO: Warn about ambiguous options better
					Iris.logger.warn("Ignoring ambiguous boolean option " + option.getName());
					booleanOptions.remove(option.getName());
					return;
				}
			} else {
				merged = proposed;
			}

			booleanOptions.put(option.getName(), merged);
		}

		public void addStringOption(OptionLocation location, StringOption option) {
			addStringOption(new MergedStringOption(location, option));
		}

		public void addStringOption(MergedStringOption proposed) {
			StringOption option = proposed.getOption();
			MergedStringOption existing = stringOptions.get(option.getName());

			MergedStringOption merged;

			if (existing != null) {
				merged = existing.merge(proposed);

				if (merged == null) {
					// TODO: Warn about ambiguous options better
					Iris.logger.warn("Ignoring ambiguous string option " + option.getName());
					stringOptions.remove(option.getName());
					return;
				}
			} else {
				merged = proposed;
			}

			stringOptions.put(option.getName(), merged);
		}

		public OptionSet build() {
			return new OptionSet(this);
		}
	}
}
