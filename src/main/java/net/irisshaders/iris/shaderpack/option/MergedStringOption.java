package net.irisshaders.iris.shaderpack.option;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Nullable;

public class MergedStringOption {
	private final StringOption option;
	private final ImmutableSet<OptionLocation> locations;

	MergedStringOption(StringOption option, ImmutableSet<OptionLocation> locations) {
		this.option = option;
		this.locations = locations;
	}

	public MergedStringOption(OptionLocation location, StringOption option) {
		this.option = option;
		this.locations = ImmutableSet.of(location);
	}

	@Nullable
	public MergedStringOption merge(MergedStringOption other) {
		if (!this.option.getDefaultValue().equals(other.option.getDefaultValue())) {
			return null;
		}

		StringOption option;

		// TODO: Collect all known comments
		if (this.option.getComment().isPresent()) {
			option = this.option;
		} else {
			option = other.option;
		}

		ImmutableSet.Builder<OptionLocation> mergedLocations = ImmutableSet.builder();

		mergedLocations.addAll(this.locations);
		mergedLocations.addAll(other.locations);

		return new MergedStringOption(option, mergedLocations.build());
	}

	public StringOption getOption() {
		return option;
	}

	public ImmutableSet<OptionLocation> getLocations() {
		return locations;
	}
}
