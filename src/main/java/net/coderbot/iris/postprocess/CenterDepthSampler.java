package net.coderbot.iris.postprocess;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import org.lwjgl.opengl.GL11C;

public class CenterDepthSampler {
	private final SmoothedFloat centerDepthSmooth;
	private final GlFramebuffer depthBufferHolder;
	private final RenderTargets renderTargets;
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private FrameUpdateNotifier fakeNotifier;

	public CenterDepthSampler(RenderTargets renderTargets) {
		fakeNotifier = new FrameUpdateNotifier();

		// NB: This will always be one frame behind compared to the current frame.
		// That's probably for the best, since it can help avoid some pipeline stalls.
		// We're still going to get stalls, though.
		centerDepthSmooth = new SmoothedFloat(1.0f, 1.0f, this::sampleCenterDepth, fakeNotifier);

		// Prior to OpenGL 4.1, all framebuffers must have at least 1 color target.
		depthBufferHolder = renderTargets.createFramebufferWritingToMain(new int[] {0});
		this.renderTargets = renderTargets;
	}

	public void updateSample() {
		fakeNotifier.onNewFrame();
	}

	private float sampleCenterDepth() {
		if (hasFirstSample && (!everRetrieved)) {
			// If the shaderpack isn't reading center depth values, don't bother sampling it
			// This improves performance with most shaderpacks
			return 0.0f;
		}

		hasFirstSample = true;

		this.depthBufferHolder.bind();

		float[] depthValue = new float[1];
		// Read a single pixel from the depth buffer
		// TODO: glReadPixels forces a full pipeline stall / flush, and probably isn't too great for performance
		IrisRenderSystem.readPixels(
			renderTargets.getCurrentWidth() / 2, renderTargets.getCurrentHeight() / 2, 1, 1,
			GL11C.GL_DEPTH_COMPONENT, GL11C.GL_FLOAT, depthValue
		);

		return depthValue[0];
	}

	public float getCenterDepthSmoothSample() {
		everRetrieved = true;

		return centerDepthSmooth.getAsFloat();
	}
}
