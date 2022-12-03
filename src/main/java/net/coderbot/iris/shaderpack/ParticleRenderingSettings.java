package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;

import java.util.Optional;

public enum ParticleRenderingSettings {
	BEFORE,
	MIXED,
	AFTER;

	public static Optional<ParticleRenderingSettings> fromString(String name) {
		try {
			return Optional.of(ParticleRenderingSettings.valueOf(name));
		} catch (IllegalArgumentException e) {
			Iris.logger.warn("Invalid particle rendering settings! " + name);
			return Optional.empty();
		}
	}
}
