package net.coderbot.iris.features;

import java.util.function.BooleanSupplier;

public enum FeatureFlags {
	SEPARATE_HW_SAMPLERS("Separate Shadow Hardware Samplers", () -> true),
	// STUB: Not implemented yet, added for the names
	CUSTOM_UNIFORMS("Custom Uniforms", () -> false),
	PER_BUFFER_BLENDING("Per-Buffer Blending", () -> false),
	COMPUTE_SHADERS("Compute Shaders", () -> false);

	private final String name;
	private final BooleanSupplier optionalRequirement;

	FeatureFlags(String name, BooleanSupplier optionalRequirement) {
		this.name = name;
		this.optionalRequirement = optionalRequirement;
	}

	public String getName() {
		return name;
	}

	public boolean isUsable() {
		return optionalRequirement.getAsBoolean();
	}

	public static boolean isInvalid(String name) {
		try {
			return !FeatureFlags.valueOf(name).isUsable();
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	public static String getNameOfValue(String name) {
		try {
			return FeatureFlags.valueOf(name).getName();
		} catch (IllegalArgumentException e) {
			return name;
		}
	}
}
