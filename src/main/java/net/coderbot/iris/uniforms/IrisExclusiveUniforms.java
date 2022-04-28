package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;

public class IrisExclusiveUniforms {
	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "cameraType", IrisExclusiveUniforms::getPerspective);
	}

	private static int getPerspective() {
		switch (Minecraft.getInstance().options.getCameraType()) {
			case FIRST_PERSON: return 0;
			case THIRD_PERSON_BACK: return 1;
			case THIRD_PERSON_FRONT: return 2;
			default: return 0;
		}
	}
}
