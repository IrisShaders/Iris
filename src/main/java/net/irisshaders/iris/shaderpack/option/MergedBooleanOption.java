package net.irisshaders.iris.shaderpack.option;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Nullable;

public class MergedBooleanOption {
	private final BooleanOption option;
	private final ImmutableSet<OptionLocation> locations;

	MergedBooleanOption(BooleanOption option, ImmutableSet<OptionLocation> locations) {
		this.option = option;
		this.locations = locations;
	}

	public MergedBooleanOption(OptionLocation location, BooleanOption option) {
		this.option = option;
		this.locations = ImmutableSet.of(location);
	}

	@Nullable
	public MergedBooleanOption merge(MergedBooleanOption other) {
		if (this.option.getDefaultValue() != other.option.getDefaultValue()) {
			return null;
		}

		BooleanOption option;

		// TODO: Collect all known comments
		if (this.option.getComment().isPresent()) {
			option = this.option;
		} else {
			option = other.option;
		}

		ImmutableSet.Builder<OptionLocation> mergedLocations = ImmutableSet.builder();

		mergedLocations.addAll(this.locations);
		mergedLocations.addAll(other.locations);

		return new MergedBooleanOption(option, mergedLocations.build());
	}

	public BooleanOption getOption() {
		return option;
	}

	public ImmutableSet<OptionLocation> getLocations() {
		return locations;
	}
}
