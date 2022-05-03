package net.coderbot.iris.postprocess;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21C;

import java.nio.ByteBuffer;

public class CenterDepthSampler {
	private final SmoothedFloat centerDepthSmooth;
	private final RenderTargets renderTargets;
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private final FrameUpdateNotifier fakeNotifier;
	private final Program program;
	private final GlFramebuffer framebuffer;
	private final int texture;

	private int index;
	private int nextIndex;
	private final int[] pboIds;
	private int quadBuffer;
	public CenterDepthSampler(RenderTargets renderTargets) {
		fakeNotifier = new FrameUpdateNotifier();

		this.renderTargets = renderTargets;

		// NB: This will always be one frame behind compared to the current frame.
		if (supportsPBO()) {
			this.texture = GlStateManager._genTexture();
			this.framebuffer = new GlFramebuffer();

			this.quadBuffer = getQuadBuffer();

			RenderSystem.bindTexture(texture);
			setupColorTexture(renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight());
			RenderSystem.bindTexture(0);

			this.framebuffer.addColorAttachment(0, texture);
			ProgramBuilder builder = ProgramBuilder.begin("centerDepthSmooth", "#version 120\n" +
				" varying vec2 screenCoord; \n" +
				" void main() { gl_Position = ftransform(); screenCoord = (gl_MultiTexCoord0).xy; }", null, "#version 120\n" +
				" varying vec2 screenCoord; \n" +
				" uniform sampler2D depth; \n" +
				" void main() { gl_FragData[0] = vec4(texture2D(depth, screenCoord).r, 0.0, 0.0, 1.0); }", ImmutableSet.of());
			builder.addDefaultSampler(() -> Minecraft.getInstance().getMainRenderTarget().getDepthTextureId(), "depth");
			this.program = builder.build();

			pboIds = new int[2];
			GL21C.glGenBuffers(pboIds);

			for (int pbo : pboIds) {
				GL21C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, pbo);
				GL21C.glBufferData(GL21C.GL_PIXEL_PACK_BUFFER, 4, GL21C.GL_STREAM_READ);
			}

			GL21C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, 0);

			centerDepthSmooth = new SmoothedFloat(1.0f, 1.0f, this::sampleCenterDepthPBO, fakeNotifier);
		} else {
			centerDepthSmooth = new SmoothedFloat(1.0f, 1.0f, this::sampleCenterDepthOld, fakeNotifier);
			this.program = null;
			this.framebuffer = null;
			this.texture = 0;
			this.pboIds = null;
		}
	}

	public void updateSample() {
		fakeNotifier.onNewFrame();
	}

	private boolean supportsPBO() {
		return GL.getCapabilities().OpenGL30 && (GL.getCapabilities().GL_ARB_pixel_buffer_object || GL.getCapabilities().GL_EXT_pixel_buffer_object);
	}

	private float sampleCenterDepthOld() {
		if (hasFirstSample && (!everRetrieved)) {
			// If the shaderpack isn't reading center depth values, don't bother sampling it
			// This improves performance with most shaderpacks
			return 0.0f;
		}

		hasFirstSample = true;

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

		float[] depthValue = new float[1];
		// Read a single pixel from the depth buffer
		// TODO: glReadPixels forces a full pipeline stall / flush, and probably isn't too great for performance
		IrisRenderSystem.readPixels(
			renderTargets.getCurrentWidth() / 2, renderTargets.getCurrentHeight() / 2, 1, 1,
			GL21C.GL_DEPTH_COMPONENT, GL21C.GL_FLOAT, depthValue
		);

		return depthValue[0];
	}

	private float sampleCenterDepthPBO() {
		if (hasFirstSample && (!everRetrieved)) {
			// If the shaderpack isn't reading center depth values, don't bother sampling it
			// This improves performance with most shaderpacks
			return 0.0f;
		}

		hasFirstSample = true;

		this.framebuffer.bind();
		this.program.use();

		RenderSystem.disableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		// scale the quad from [0, 1] to [-1, 1]
		RenderSystem.translatef(-1.0F, -1.0F, 0.0F);
		RenderSystem.scalef(2.0F, 2.0F, 0.0F);

		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager._glBindBuffer(GL21C.GL_ARRAY_BUFFER, quadBuffer);
		DefaultVertexFormat.POSITION_TEX.setupBufferState(0L);

		GlStateManager._drawArrays(GL21C.GL_POINTS, 0, 1);

		FullScreenQuadRenderer.end();

		GlStateManager._glUseProgram(0);

		index = (index + 1) % 2;// for ping-pong PBO
		nextIndex = (index + 1) % 2;// for ping-pong PBO

		this.framebuffer.bindAsReadBuffer();

		float depthValue = 0;
		// Read a single pixel from the depth buffer
		// TODO: glReadPixels forces a full pipeline stall / flush, and probably isn't too great for performance
		GL21C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, pboIds[index]);
		IrisRenderSystem.readPixels(
			renderTargets.getCurrentWidth() / 2, renderTargets.getCurrentHeight() / 2, 1, 1,
			GL21C.GL_RED, GL21C.GL_FLOAT, null
		);

		GL21C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, pboIds[nextIndex]);
		ByteBuffer buffer = GL21C.glMapBuffer(GL21C.GL_PIXEL_PACK_BUFFER, GL21C.GL_READ_ONLY);
		depthValue = buffer.getFloat();
		GL21C.glUnmapBuffer(GL21C.GL_PIXEL_PACK_BUFFER);
		GL21C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, 0);

		return depthValue;
	}

	public void setupColorTexture(int width, int height) {
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_MIN_FILTER, GL21C.GL_LINEAR);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_MAG_FILTER, GL21C.GL_LINEAR);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_WRAP_S, GL21C.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_WRAP_T, GL21C.GL_CLAMP_TO_EDGE);

		GlStateManager._texImage2D(GL21C.GL_TEXTURE_2D, 0, InternalTextureFormat.R32F.getGlFormat(), width, height, 0, PixelFormat.RED.getGlFormat(), PixelType.FLOAT.getGlFormat(), null);
	}

	private int getQuadBuffer() {
		float[] vertices = new float[] {
			// Vertex 0: Top right corner
			0.5F, 0.5F, 0.0F,
			0.5F, 0.5F,
		};

		int buffer = GlStateManager._glGenBuffers();

		GlStateManager._glBindBuffer(GL21C.GL_ARRAY_BUFFER, buffer);
		IrisRenderSystem.bufferData(GL21C.GL_ARRAY_BUFFER, vertices, GL21C.GL_STATIC_DRAW);
		GlStateManager._glBindBuffer(GL21C.GL_ARRAY_BUFFER, 0);

		return buffer;
	}
	public float getCenterDepthSmoothSample() {
		everRetrieved = true;

		return centerDepthSmooth.getAsFloat();
	}

	public void destroy() {
		if (supportsPBO()) {
			GL21C.glDeleteBuffers(pboIds);
			GlStateManager._deleteTexture(texture);
			framebuffer.destroy();
			GL21C.glDeleteBuffers(quadBuffer);
			program.destroy();
		}
	}
}
