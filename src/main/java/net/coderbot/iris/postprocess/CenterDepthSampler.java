package net.coderbot.iris.postprocess;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL21C;

public class CenterDepthSampler {
	private final RenderTargets renderTargets;
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private final FrameUpdateNotifier fakeNotifier;
	private final Program program;
	private final GlFramebuffer framebuffer;
	private final int texture;
	private final int altTexture;
	public CenterDepthSampler(RenderTargets renderTargets) {
		fakeNotifier = new FrameUpdateNotifier();

		this.renderTargets = renderTargets;

		// NB: This will always be one frame behind compared to the current frame.
		this.texture = GlStateManager._genTexture();
		this.altTexture = GlStateManager._genTexture();
		this.framebuffer = new GlFramebuffer();

		fakeNotifier.addListener(this::sampleCenterDepthPBO);

		// Fall back to a less precise format if the system doesn't support OpenGL 3
		InternalTextureFormat format = GL.getCapabilities().OpenGL30 ? InternalTextureFormat.R32F : InternalTextureFormat.RGB16;
		RenderSystem.bindTexture(texture);
		setupColorTexture(format);
		RenderSystem.bindTexture(altTexture);
		setupColorTexture(format);
		RenderSystem.bindTexture(0);

		this.framebuffer.addColorAttachment(0, texture);
		ProgramBuilder builder = ProgramBuilder.begin("centerDepthSmooth", "#version 120\n" +
			" void main() { gl_Position = ftransform(); }", null, "#version 120\n" +
			" uniform sampler2D depth; \n" +
			" uniform sampler2D altDepth; \n" +
			" uniform float lastFrameTime; \n" +
			" uniform float decay; \n" +
			" void main() { float currentDepth = texture2D(depth, vec2(0.5)).r; float decay2 = 1.0 - exp(-decay * lastFrameTime); gl_FragColor = vec4(mix(texture2D(altDepth, vec2(0.5)).r, currentDepth, decay2), 0, 0, 0); }", ImmutableSet.of());
		builder.addDynamicSampler(() -> Minecraft.getInstance().getMainRenderTarget().getDepthTextureId(), "depth");
		builder.addDynamicSampler(() -> altTexture, "altDepth");
		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "lastFrameTime", SystemTimeUniforms.TIMER::getLastFrameTime);
		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "decay", () -> computeDecay(0.1F));
		this.program = builder.build();
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

	private void sampleCenterDepthPBO() {
		if (hasFirstSample && (!everRetrieved)) {
			// If the shaderpack isn't reading center depth values, don't bother sampling it
			// This improves performance with most shaderpacks
			return;
		}

		hasFirstSample = true;

		this.framebuffer.bind();
		this.program.use();

		RenderSystem.viewport(0, 0, 3, 3);

		FullScreenQuadRenderer.INSTANCE.render();

		GlStateManager._glUseProgram(0);

		GL11C.glDisable(GL11C.GL_SCISSOR_TEST);

		this.framebuffer.bind();

		GlStateManager._bindTexture(altTexture);
		DepthCopyStrategy.fastest(false).copy(this.framebuffer, texture, null, altTexture, 3, 3);
		GlStateManager._bindTexture(0);

		//Reset viewport
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
	}

	public void setupColorTexture(InternalTextureFormat format) {
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_MIN_FILTER, GL21C.GL_LINEAR);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_MAG_FILTER, GL21C.GL_LINEAR);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_WRAP_S, GL21C.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_WRAP_T, GL21C.GL_CLAMP_TO_EDGE);

		GlStateManager._texImage2D(GL21C.GL_TEXTURE_2D, 0, format.getGlFormat(), 3, 3, 0, format.getPixelFormat().getGlFormat(), PixelType.FLOAT.getGlFormat(), null);
	}

	public int getCenterDepthTexture() {
		everRetrieved = true;

		return altTexture;
	}

	public void destroy() {
		if (GL.getCapabilities().OpenGL30) {
			GlStateManager._deleteTexture(texture);
			GlStateManager._deleteTexture(altTexture);
			framebuffer.destroy();
			program.destroy();
		}
	}
}
