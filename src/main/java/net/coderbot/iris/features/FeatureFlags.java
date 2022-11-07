package net.coderbot.iris.features;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;
import java.util.function.BooleanSupplier;

public enum FeatureFlags {
	SEPARATE_HARDWARE_SAMPLERS(() -> true, () -> true),
	PER_BUFFER_BLENDING(() -> true, IrisRenderSystem::supportsBufferBlending),
	COMPUTE_SHADERS(() -> true, IrisRenderSystem::supportsCompute),
	ENTITY_TRANSLUCENT(() -> true, () -> true),
	UNKNOWN(() -> false, () -> false);

	private final BooleanSupplier irisRequirement;
	private final BooleanSupplier hardwareRequirement;

	FeatureFlags(BooleanSupplier irisRequirement, BooleanSupplier hardwareRequirement) {
		this.irisRequirement = irisRequirement;
		this.hardwareRequirement = hardwareRequirement;
	}

	public static String getInvalidStatus(List<FeatureFlags> invalidFeatureFlags) {
		boolean unsupportedHardware = false, unsupportedIris = false;
		FeatureFlags[] flags = invalidFeatureFlags.toArray(new FeatureFlags[0]);
		for (FeatureFlags flag : flags) {
			unsupportedIris |= !flag.irisRequirement.getAsBoolean();
			unsupportedHardware |= !flag.hardwareRequirement.getAsBoolean();
		}

		if (unsupportedIris) {
			if (unsupportedHardware) {
				return I18n.get("iris.unsupported.irisorpc");
			}

			return I18n.get("iris.unsupported.iris");
		} else if (unsupportedHardware) {
			return I18n.get("iris.unsupported.pc");
		} else {
			return null;
		}
	}

	public String getHumanReadableName() {
		return WordUtils.capitalize(name().replace("_", " ").toLowerCase());
	}

	public boolean isUsable() {
		return irisRequirement.getAsBoolean() && hardwareRequirement.getAsBoolean();
	}

	public static boolean isInvalid(String name) {
		try {
			return !FeatureFlags.valueOf(name).isUsable();
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	public static FeatureFlags getValue(String value) {
		try {
			return FeatureFlags.valueOf(value);
		} catch (IllegalArgumentException e) {
			return FeatureFlags.UNKNOWN;
		}
	}
}
