package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.vendored.joml.Math;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;

public class IrisExclusiveUniforms {
	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "thunderStrength", IrisExclusiveUniforms::getThunderStrength);
		uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "firstPersonCamera", IrisExclusiveUniforms::isFirstPersonCamera);
		uniforms.uniform1b(UniformUpdateFrequency.PER_TICK, "isSpectator", IrisExclusiveUniforms::isSpectator);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "eyePosition", IrisExclusiveUniforms::getEyePosition);
	}

	private static float getThunderStrength() {
		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			Minecraft.getInstance().level.getThunderLevel(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static boolean isFirstPersonCamera() {
		// If camera type is not explicitly third-person, assume it's first-person.
		switch (Minecraft.getInstance().options.getCameraType()) {
			case THIRD_PERSON_BACK:
			case THIRD_PERSON_FRONT:
				return false;
			default: return true;
		}
	}

	private static boolean isSpectator() {
		return Minecraft.getInstance().gameMode.getPlayerMode() == GameType.SPECTATOR;
	}

	private static Vector3d getEyePosition() {
		return new Vector3d(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getEyeY(), Minecraft.getInstance().player.getZ());
	}
}
