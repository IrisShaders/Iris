package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.DepthBufferTracker;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.nio.ByteBuffer;

public class CenterDepthSampler {
	private final SmoothedFloat centerDepthSmooth;
	private final RenderTargets renderTargets;
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private FrameUpdateNotifier fakeNotifier;

	private int index;
	private int nextIndex;
	private int[] pboIds;
	private int pixelFormat;
	public CenterDepthSampler(RenderTargets renderTargets) {
		fakeNotifier = new FrameUpdateNotifier();

		// NB: This will always be one frame behind compared to the current frame.
		// That's probably for the best, since it can help avoid some pipeline stalls.
		// We're still going to get stalls, though.
		centerDepthSmooth = new SmoothedFloat(1.0f, 1.0f, this::sampleCenterDepth, fakeNotifier);

		// Prior to OpenGL 4.1, all framebuffers must have at least 1 color target.
		this.renderTargets = renderTargets;

		DepthBufferFormat format = DepthBufferTracker.INSTANCE.getFormat(renderTargets.getDepthTexture());
		this.pixelFormat = format.getGlFormat();
		pboIds = new int[2];
		GL30C.glGenBuffers(pboIds);
		GL30C.glBindBuffer(GL30C.GL_PIXEL_PACK_BUFFER, pboIds[0]);
		GL30C.glBufferData(GL30C.GL_PIXEL_PACK_BUFFER, 4, GL30C.GL_STREAM_READ);
		GL30C.glBindBuffer(GL30C.GL_PIXEL_PACK_BUFFER, pboIds[1]);
		GL30C.glBufferData(GL30C.GL_PIXEL_PACK_BUFFER, 4, GL30C.GL_STREAM_READ);

		GL30C.glBindBuffer(GL30C.GL_PIXEL_PACK_BUFFER, 0);
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

		index = (index + 1) % 2;// for ping-pong PBO
		nextIndex = (index + 1) % 2;// for ping-pong PBO

		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, Minecraft.getInstance().getMainRenderTarget().frameBufferId);

		float depthValue = 0;
		// Read a single pixel from the depth buffer
		// TODO: glReadPixels forces a full pipeline stall / flush, and probably isn't too great for performance
		GL30C.glBindBuffer(GL30C.GL_PIXEL_PACK_BUFFER, pboIds[index]);
		IrisRenderSystem.readPixels(
			renderTargets.getCurrentWidth() / 2, renderTargets.getCurrentHeight() / 2, 1, 1,
			GL43C.GL_DEPTH_COMPONENT, pixelFormat, null
		);

		GL30C.glBindBuffer(GL30C.GL_PIXEL_PACK_BUFFER, pboIds[nextIndex]);
		ByteBuffer buffer = GL30C.glMapBuffer(GL30C.GL_PIXEL_PACK_BUFFER, GL30C.GL_READ_ONLY);
		switch (pixelFormat) {
			case GL43C.GL_UNSIGNED_SHORT:
				int unsigned = buffer.getShort() & 0xffff;
				depthValue = (float) unsigned / 65536F;
				break;
			case GL43C.GL_UNSIGNED_INT:
			case GL43C.GL_UNSIGNED_INT_24_8:
				long unsignedL = buffer.getInt() & 0xffffffffL;
				depthValue = (float) unsignedL / (float) Integer.MAX_VALUE;
				break;
			case GL43C.GL_FLOAT:
			case GL43C.GL_FLOAT_32_UNSIGNED_INT_24_8_REV:
				depthValue = buffer.getFloat();
				break;
		}
		GL30C.glUnmapBuffer(GL30C.GL_PIXEL_PACK_BUFFER);
		GL30C.glBindBuffer(GL30C.GL_PIXEL_PACK_BUFFER, 0);

		return depthValue;
	}

	public float getCenterDepthSmoothSample() {
		everRetrieved = true;

		return centerDepthSmooth.getAsFloat();
	}
}
