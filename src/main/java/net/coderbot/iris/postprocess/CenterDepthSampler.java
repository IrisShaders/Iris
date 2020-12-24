package net.coderbot.iris.postprocess;

import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import org.lwjgl.opengl.GL11C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public class CenterDepthSampler {
	private final SmoothedFloat centerDepthSmooth;
	private float centerDepthSmoothSample;

	public CenterDepthSampler() {
		centerDepthSmooth = new SmoothedFloat(1.0f, CenterDepthSampler::sampleCenterDepth);
	}

	private static float sampleCenterDepth() {
		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();

		// We're actually reading from the framebuffer, but it needs to be bound to the GL_FRAMEBUFFER target
		main.beginWrite(false);

		float[] depthValue = new float[1];
		// Read a single pixel from the depth buffer
		// TODO: glReadPixels forces a full pipeline stall / flush, and probably isn't too great for performance
		GL11C.glReadPixels(
			main.textureWidth / 2, main.textureHeight / 2, 1, 1,
			GL11C.GL_DEPTH_COMPONENT, GL11C.GL_FLOAT, depthValue
		);

		return depthValue[0];
	}

	public void endWorldRendering() {
		centerDepthSmoothSample = centerDepthSmooth.getAsFloat();
	}

	public float getCenterDepthSmoothSample() {
		return centerDepthSmoothSample;
	}
}
