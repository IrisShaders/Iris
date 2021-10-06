package net.irisshaders.iris.uniforms;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

import java.util.Objects;

import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.mixin.DimensionTypeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class WorldTimeUniforms {
	private WorldTimeUniforms() {
	}

	/**
	 * Makes world time uniforms available to the given program
	 *
	 * @param uniforms the program to make the uniforms available to
	 */
	public static void addWorldTimeUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1i(PER_TICK, "worldTime", WorldTimeUniforms::getWorldDayTime)
			.uniform1i(PER_TICK, "worldDay", WorldTimeUniforms::getWorldDay)
			.uniform1i(PER_TICK, "moonPhase", () -> getWorld().getMoonPhase());
	}

	private static int getWorldDayTime() {
		long timeOfDay = getWorld().getDayTime();

		long dayTime = ((DimensionTypeAccessor) getWorld().dimensionType()).getFixedTime()
																		  .orElse(timeOfDay % 24000L);

		return (int) dayTime;
	}

	private static int getWorldDay() {
		long timeOfDay = getWorld().getDayTime();
		long day = timeOfDay / 24000L;

		return (int) day;
	}

	private static ClientLevel getWorld() {
		return Objects.requireNonNull(Minecraft.getInstance().level);
	}
}
