package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;

public class IrisExclusiveUniforms {
	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "thunderStrength", IrisExclusiveUniforms::getThunderStrength);
		uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "cameraType", IrisExclusiveUniforms::getPerspective);
	}


	private static float getThunderStrength() {
		return Minecraft.getInstance().level.getThunderLevel(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	private static int getPerspective() {
		// If camera type is not explicitly third-person, assume it's first-person.
		switch (Minecraft.getInstance().options.getCameraType()) {
			case THIRD_PERSON_BACK:
			case THIRD_PERSON_FRONT:
				return 1;
			default: return 0;
		}
	}
}
