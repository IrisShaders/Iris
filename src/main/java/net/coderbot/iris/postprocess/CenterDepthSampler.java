package net.coderbot.iris.postprocess;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
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
	private final int altTexture;

	private int index;
	private int nextIndex;
	public CenterDepthSampler(RenderTargets renderTargets) {
		fakeNotifier = new FrameUpdateNotifier();

		this.renderTargets = renderTargets;

		// NB: This will always be one frame behind compared to the current frame.
		if (supportsPBO()) {
			this.texture = GlStateManager._genTexture();
			this.altTexture = GlStateManager._genTexture();
			this.framebuffer = new GlFramebuffer();

			RenderSystem.bindTexture(texture);
			setupColorTexture(renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight());
			RenderSystem.bindTexture(altTexture);
			setupColorTexture(renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight());
			RenderSystem.bindTexture(0);

			this.framebuffer.addColorAttachment(0, texture);
			ProgramBuilder builder = ProgramBuilder.begin("centerDepthSmooth", "#version 120\n" +
				" varying vec2 screenCoord; \n" +
				" void main() { gl_Position = ftransform(); screenCoord = (gl_MultiTexCoord0).xy; }", null, "#version 120\n" +
				" varying vec2 screenCoord; \n" +
				" uniform sampler2D depth; \n" +
				" uniform sampler2D altDepth; \n" +
				" uniform float lastFrameTime; \n" +
				" uniform float decay; \n" +
				" void main() { float currentDepth = texture2D(depth, screenCoord).r; float decay2 = 1.0 - exp(-decay * lastFrameTime); gl_FragColor = vec4(mix(texture2D(altDepth, screenCoord).r, currentDepth, decay2), 0, 0, 0); }", ImmutableSet.of());
			builder.addDynamicSampler(() -> Minecraft.getInstance().getMainRenderTarget().getDepthTextureId(), "depth");
			builder.addDynamicSampler(() -> altTexture, "altDepth");
			builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "lastFrameTime", SystemTimeUniforms.TIMER::getLastFrameTime);
			builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "decay", () -> computeDecay(0.1F));
			this.program = builder.build();

			centerDepthSmooth = new SmoothedFloat(1.0f, 1.0f, this::sampleCenterDepthPBO, fakeNotifier);
		} else {
			centerDepthSmooth = new SmoothedFloat(1.0f, 1.0f, this::sampleCenterDepthOld, fakeNotifier);
			this.program = null;
			this.framebuffer = null;
			this.texture = 0;
			this.altTexture = 0;
		}
	}

	private static final double LN2 = Math.log(2);

	private float computeDecay(float halfLife) {
		// Compute the decay constant from the half life
		// https://en.wikipedia.org/wiki/Exponential_decay#Measuring_rates_of_decay
		// https://en.wikipedia.org/wiki/Exponential_smoothing#Time_constant
		// k = 1 / τ
		return (float) (1.0f / (halfLife / LN2));
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
		GL11.glScissor(renderTargets.getCurrentWidth() / 2 - 1, renderTargets.getCurrentHeight() / 2 - 1, 2, 2);

		GL11.glEnable(GL11C.GL_SCISSOR_TEST);

		FullScreenQuadRenderer.INSTANCE.render();

		GlStateManager._glUseProgram(0);

		GL11C.glDisable(GL11C.GL_SCISSOR_TEST);

		this.framebuffer.bind();

		GlStateManager._bindTexture(altTexture);
		DepthCopyStrategy.fastest(false).copy(this.framebuffer, texture, null, altTexture, renderTargets.getCurrentWidth(), renderTargets.getCurrentHeight());
		GlStateManager._bindTexture(0);
		return 0F;
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
	public int getCenterDepthSmoothSample() {
		everRetrieved = true;

		return altTexture;
	}

	public void destroy() {
		if (supportsPBO()) {
			GlStateManager._deleteTexture(texture);
			framebuffer.destroy();
			program.destroy();
		}
	}
}
