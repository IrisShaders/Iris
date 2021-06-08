package net.coderbot.iris.postprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.rendertarget.*;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.SamplerUniforms;
import net.minecraft.client.texture.AbstractTexture;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Pair;

public class CompositeRenderer {
	private final RenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final AbstractTexture noiseTexture;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;

	private boolean usesShadows = false;

	public CompositeRenderer(ProgramSet pack, RenderTargets renderTargets, AbstractTexture noiseTexture,
							 FrameUpdateNotifier updateNotifier, CenterDepthSampler centerDepthSampler,
							 BufferFlipper bufferFlipper) {
		this.updateNotifier = updateNotifier;
		this.centerDepthSampler = centerDepthSampler;

		final PackRenderTargetDirectives renderTargetDirectives = pack.getPackDirectives().getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings =
				renderTargetDirectives.getRenderTargetSettings();
		final List<Pair<Program, ProgramDirectives>> programs = new ArrayList<>();

		// TODO: The final pass should be separate from composite passes.

		for (ProgramSource source : pack.getDeferred()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			programs.add(createProgram(source));
		}

		for (ProgramSource source : pack.getComposite()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			programs.add(createProgram(source));
		}

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();

		for (Pair<Program, ProgramDirectives> programEntry : programs) {
			Pass pass = new Pass();
			ProgramDirectives directives = programEntry.getRight();

			pass.program = programEntry.getLeft();
			// TODO: Don't truncate draw buffers...
			int[] drawBuffers = truncateDrawBuffers(directives.getDrawBuffers());

			boolean[] stageWritesToAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

			for (int i = 0; i < stageWritesToAlt.length; i++) {
				stageWritesToAlt[i] = !bufferFlipper.isFlipped(i);
			}

			GlFramebuffer framebuffer = renderTargets.createColorFramebuffer(stageWritesToAlt, drawBuffers);

			pass.stageReadsFromAlt = bufferFlipper.snapshot();
			pass.framebuffer = framebuffer;
			pass.viewportScale = directives.getViewportScale();
			pass.generateMipmap = new boolean[RenderTargets.MAX_RENDER_TARGETS];

			for (int i = 0; i < pass.generateMipmap.length; i++) {
				pass.generateMipmap[i] = directives.getMipmappedBuffers().contains(i);
			}

			passes.add(pass);

			// Flip the buffers that this shader wrote to
			for (int buffer : drawBuffers) {
				bufferFlipper.flip(buffer);
			}
		}

		this.passes = passes.build();
		this.renderTargets = renderTargets;

		GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);

		this.noiseTexture = noiseTexture;
	}

	private int[] truncateDrawBuffers(int[] buffers) {
		int size = 0;

		for (int buffer : buffers) {
			if (buffer < 8) {
				size += 1;
			}
		}

		int[] newBuffers = new int[size];
		int index = 0;

		for (int buffer : buffers) {
			if (buffer < 8) {
				newBuffers[index++] = buffer;
			}
		}

		return newBuffers;
	}

	private static final class Pass {
		Program program;
		GlFramebuffer framebuffer;
		ImmutableSet<Integer> stageReadsFromAlt;
		boolean[] generateMipmap;
		float viewportScale;

		private void destroy() {
			this.program.destroy();
		}
	}

	public void renderAll(ShadowMapRenderer shadowMapRenderer) {
		centerDepthSampler.endWorldRendering();

		RenderSystem.disableBlend();
		RenderSystem.disableAlphaTest();

		final Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		final int baseWidth = main.textureWidth;
		final int baseHeight = main.textureHeight;

		// Prepare "static" textures (ones that do not change during composite rendering)
		int depthAttachment = renderTargets.getDepthTexture().getTextureId();
		int depthAttachmentNoTranslucents = renderTargets.getDepthTextureNoTranslucents().getTextureId();

		bindTexture(SamplerUniforms.DEPTH_TEX_0, depthAttachment);
		bindTexture(SamplerUniforms.DEPTH_TEX_1, depthAttachmentNoTranslucents);
		// Note: Since we haven't rendered the hand yet, this won't contain any handheld items.
		// Once we start rendering the hand before composite content, this will need to be addressed.
		bindTexture(SamplerUniforms.DEPTH_TEX_2, depthAttachmentNoTranslucents);

		bindTexture(SamplerUniforms.SHADOW_TEX_0, shadowMapRenderer.getDepthTextureId());
		bindTexture(SamplerUniforms.SHADOW_TEX_1, shadowMapRenderer.getDepthTextureNoTranslucentsId());
		bindTexture(SamplerUniforms.SHADOW_COLOR_0, shadowMapRenderer.getColorTexture0Id());
		bindTexture(SamplerUniforms.SHADOW_COLOR_1, shadowMapRenderer.getColorTexture1Id());

		bindTexture(SamplerUniforms.NOISE_TEX, noiseTexture.getGlId());

		FullScreenQuadRenderer.INSTANCE.begin();

		for (Pass renderPass : passes) {
			renderPass.framebuffer.bind();

			bindRenderTarget(SamplerUniforms.COLOR_TEX_0, renderTargets.get(0), renderPass.stageReadsFromAlt.contains(0), renderPass.generateMipmap[0]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_1, renderTargets.get(1), renderPass.stageReadsFromAlt.contains(1), renderPass.generateMipmap[1]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_2, renderTargets.get(2), renderPass.stageReadsFromAlt.contains(2), renderPass.generateMipmap[2]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_3, renderTargets.get(3), renderPass.stageReadsFromAlt.contains(3), renderPass.generateMipmap[3]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_4, renderTargets.get(4), renderPass.stageReadsFromAlt.contains(4), renderPass.generateMipmap[4]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_5, renderTargets.get(5), renderPass.stageReadsFromAlt.contains(5), renderPass.generateMipmap[5]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_6, renderTargets.get(6), renderPass.stageReadsFromAlt.contains(6), renderPass.generateMipmap[6]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_7, renderTargets.get(7), renderPass.stageReadsFromAlt.contains(7), renderPass.generateMipmap[7]);

			float scaledWidth = baseWidth * renderPass.viewportScale;
			float scaledHeight = baseHeight * renderPass.viewportScale;
			RenderSystem.viewport(0, 0, (int) scaledWidth, (int) scaledHeight);

			renderPass.program.use();
			FullScreenQuadRenderer.INSTANCE.renderQuad();
		}

		FullScreenQuadRenderer.end();

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		// Also bind the "main" framebuffer if it isn't already bound.
		main.beginWrite(true);
		GlStateManager.useProgram(0);

		// NB: Unbinding all of these textures is necessary for proper shaderpack reloading.
		resetRenderTarget(SamplerUniforms.COLOR_TEX_0, renderTargets.get(0));
		resetRenderTarget(SamplerUniforms.COLOR_TEX_1, renderTargets.get(1));
		resetRenderTarget(SamplerUniforms.COLOR_TEX_2, renderTargets.get(2));
		resetRenderTarget(SamplerUniforms.COLOR_TEX_3, renderTargets.get(3));
		resetRenderTarget(SamplerUniforms.COLOR_TEX_4, renderTargets.get(4));
		resetRenderTarget(SamplerUniforms.COLOR_TEX_5, renderTargets.get(5));
		resetRenderTarget(SamplerUniforms.COLOR_TEX_6, renderTargets.get(6));
		resetRenderTarget(SamplerUniforms.COLOR_TEX_7, renderTargets.get(7));

		unbindTexture(SamplerUniforms.DEPTH_TEX_0);
		unbindTexture(SamplerUniforms.DEPTH_TEX_1);
		unbindTexture(SamplerUniforms.DEPTH_TEX_2);

		unbindTexture(SamplerUniforms.SHADOW_TEX_0);
		unbindTexture(SamplerUniforms.SHADOW_TEX_1);

		unbindTexture(SamplerUniforms.SHADOW_COLOR_0);
		unbindTexture(SamplerUniforms.SHADOW_COLOR_1);

		unbindTexture(SamplerUniforms.NOISE_TEX);

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
	}

	private static void bindRenderTarget(int textureUnit, RenderTarget target, boolean readFromAlt, boolean generateMipmap) {
		bindTexture(textureUnit, readFromAlt ? target.getAltTexture() : target.getMainTexture());

		if (generateMipmap) {
			// TODO: Only generate the mipmap if a valid mipmap hasn't been generated or if we've written to the buffer
			// (since the last mipmap was generated)
			//
			// NB: We leave mipmapping enabled even if the buffer is written to again, this appears to match the
			// behavior of ShadersMod/OptiFine, however I'm not sure if it's desired behavior. It's possible that a
			// program could use mipmapped sampling with a stale mipmap, which probably isn't great. However, the
			// sampling mode is always reset between frames, so this only persists after the first program to use
			// mipmapping on this buffer.
			//
			// Also note that this only applies to one of the two buffers in a render target buffer pair - making it
			// unlikely that this issue occurs in practice with most shader packs.
			GL30C.glGenerateMipmap(GL20C.GL_TEXTURE_2D);
			GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR_MIPMAP_LINEAR);
		}
	}

	private static void resetRenderTarget(int textureUnit, RenderTarget target) {
		// Resets the sampling mode of the given render target and then unbinds it to prevent accidental sampling of it
		// elsewhere.
		unbindTexture(textureUnit);
	}

	private static void bindTexture(int textureUnit, int texture) {
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(texture);
	}

	private static void unbindTexture(int textureUnit) {
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(0);
	}

	// TODO: Don't just copy this from DeferredWorldRenderingPipeline
	private Pair<Program, ProgramDirectives> createProgram(ProgramSource source) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null), source.getGeometrySource().orElse(null),
				source.getFragmentSource().orElse(null));
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		if (SamplerUniforms.hasShadowSamplers(builder)) {
			usesShadows = true;
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), updateNotifier);
		SamplerUniforms.addCompositeSamplerUniforms(builder);
		SamplerUniforms.addDepthSamplerUniforms(builder);

		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "centerDepthSmooth", this.centerDepthSampler::getCenterDepthSmoothSample);

		return new Pair<>(builder.build(), source.getDirectives());
	}

	public void destroy() {
		for (Pass renderPass : passes) {
			renderPass.destroy();
		}
	}

	public boolean usesShadows() {
		return usesShadows;
	}
}
