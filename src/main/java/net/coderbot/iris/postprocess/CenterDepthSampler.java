package net.coderbot.iris.postprocess;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL21C;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CenterDepthSampler {
	private static final double LN2 = Math.log(2);
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private final Program program;
	private final GlFramebuffer framebuffer;
	private final int texture;
	private final int altTexture;

	public CenterDepthSampler(float halfLife) {
		this.texture = GlStateManager._genTexture();
		this.altTexture = GlStateManager._genTexture();
		this.framebuffer = new GlFramebuffer();

		// Fall back to a less precise format if the system doesn't support OpenGL 3
		InternalTextureFormat format = GL.getCapabilities().OpenGL30 ? InternalTextureFormat.R32F : InternalTextureFormat.RGB16;
		RenderSystem.bindTexture(texture);
		setupColorTexture(format);
		RenderSystem.bindTexture(altTexture);
		setupColorTexture(format);
		RenderSystem.bindTexture(0);

		this.framebuffer.addColorAttachment(0, texture);
		ProgramBuilder builder;

		try {
			String fsh = new String(IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/centerDepth.fsh"))), StandardCharsets.UTF_8);
			String vsh = new String(IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/centerDepth.vsh"))), StandardCharsets.UTF_8);

			builder = ProgramBuilder.begin("centerDepthSmooth", vsh, null, fsh, ImmutableSet.of());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		builder.addDynamicSampler(() -> Minecraft.getInstance().getMainRenderTarget().getDepthTextureId(), "depth");
		builder.addDynamicSampler(() -> altTexture, "altDepth");
		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "lastFrameTime", SystemTimeUniforms.TIMER::getLastFrameTime);
		builder.uniform1f(UniformUpdateFrequency.ONCE, "decay", () -> (1.0f / ((halfLife * 0.1) / LN2)));
		this.program = builder.build();
	}

	public void sampleCenterDepth() {
		if (hasFirstSample && (!everRetrieved)) {
			// If the shaderpack isn't reading center depth values, don't bother sampling it
			// This improves performance with most shaderpacks
			return;
		}

		hasFirstSample = true;

		this.framebuffer.bind();
		this.program.use();

		RenderSystem.viewport(0, 0, 1, 1);

		FullScreenQuadRenderer.INSTANCE.render();

		GlStateManager._glUseProgram(0);

		this.framebuffer.bind();

		GlStateManager._bindTexture(altTexture);
		// The API contract of DepthCopyStrategy claims it can only copy depth, however the 2 non-stencil methods used are entirely capable of copying color as of now.
		DepthCopyStrategy.fastest(false).copy(this.framebuffer, texture, null, altTexture, 1, 1);
		GlStateManager._bindTexture(0);

		//Reset viewport
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
	}

	public void setupColorTexture(InternalTextureFormat format) {
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_MIN_FILTER, GL21C.GL_LINEAR);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_MAG_FILTER, GL21C.GL_LINEAR);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_WRAP_S, GL21C.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL21C.GL_TEXTURE_2D, GL21C.GL_TEXTURE_WRAP_T, GL21C.GL_CLAMP_TO_EDGE);

		GlStateManager._texImage2D(GL21C.GL_TEXTURE_2D, 0, format.getGlFormat(), 1, 1, 0, format.getPixelFormat().getGlFormat(), PixelType.FLOAT.getGlFormat(), null);
	}

	public int getCenterDepthTexture() {
		everRetrieved = true;

		return altTexture;
	}

	public void destroy() {
		GlStateManager._deleteTexture(texture);
		GlStateManager._deleteTexture(altTexture);
		framebuffer.destroy();
		program.destroy();
	}
}
