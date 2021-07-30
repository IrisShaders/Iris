package net.coderbot.iris.shaderpack.option;

import net.coderbot.iris.Iris;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class OptionSet {
	private final ImmutableMap<String, MergedBooleanOption> booleanOptions;

	private OptionSet(Builder builder) {
		this.booleanOptions = ImmutableMap.copyOf(builder.booleanOptions);
	}

	public ImmutableMap<String, MergedBooleanOption> getBooleanOptions() {
		return this.booleanOptions;
	}

	public ImmutableMap<String, StringOption> getStringOptions() {
		// TODO: MergedStringOption
		throw new UnsupportedOperationException("TODO: not yet implemented");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final Map<String, MergedBooleanOption> booleanOptions;

		public void addBooleanOption(OptionLocation location, BooleanOption option) {
			MergedBooleanOption existing = booleanOptions.get(option.getName());
			MergedBooleanOption proposed = new MergedBooleanOption(location, option);

			MergedBooleanOption merged;

			if (existing != null) {
				merged = existing.merge(proposed);

				if (merged == null) {
					// TODO: Warn about ambiguous options
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
			// TODO
			throw new UnsupportedOperationException("TODO: not yet implemented");
		}

		public Builder() {
			this.booleanOptions = new HashMap<>();
		}

		public OptionSet build() {
			return new OptionSet(this);
		}
	}
}
