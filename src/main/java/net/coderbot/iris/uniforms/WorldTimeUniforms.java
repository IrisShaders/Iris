package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

import java.util.Objects;

import net.coderbot.iris.gl.uniform.UniformHolder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

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
		long timeOfDay = getWorld().getTimeOfDay();
		long dayTime = timeOfDay % 24000L;

		return (int) dayTime;
	}

	private static int getWorldDay() {
		long timeOfDay = getWorld().getTimeOfDay();
		long day = timeOfDay / 24000L;

		return (int) day;
	}

	private static ClientWorld getWorld() {
		return Objects.requireNonNull(MinecraftClient.getInstance().world);
	}
}
