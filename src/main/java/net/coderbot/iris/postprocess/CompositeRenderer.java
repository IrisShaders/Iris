package net.coderbot.iris.postprocess;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.rendertarget.*;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.texture.AbstractTexture;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public class CompositeRenderer {
	private final RenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final AbstractTexture noiseTexture;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;

	public CompositeRenderer(PackDirectives packDirectives, ProgramSource[] sources, RenderTargets renderTargets,
							 AbstractTexture noiseTexture, FrameUpdateNotifier updateNotifier,
							 CenterDepthSampler centerDepthSampler, BufferFlipper bufferFlipper,
							 Supplier<ShadowMapRenderer> shadowMapRendererSupplier) {
		this.noiseTexture = noiseTexture;
		this.updateNotifier = updateNotifier;
		this.centerDepthSampler = centerDepthSampler;
		this.renderTargets = renderTargets;

		final PackRenderTargetDirectives renderTargetDirectives = packDirectives.getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings =
				renderTargetDirectives.getRenderTargetSettings();

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();

		for (ProgramSource source : sources) {
			if (source == null || !source.isValid()) {
				continue;
			}

			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			ImmutableSet<Integer> flipped = bufferFlipper.snapshot();

			pass.program = createProgram(source, flipped, shadowMapRendererSupplier);
			int[] drawBuffers = directives.getDrawBuffers();

			boolean[] stageWritesToAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

			for (int i = 0; i < stageWritesToAlt.length; i++) {
				stageWritesToAlt[i] = !bufferFlipper.isFlipped(i);
			}

			GlFramebuffer framebuffer = renderTargets.createColorFramebuffer(stageWritesToAlt, drawBuffers);

			pass.stageReadsFromAlt = flipped;
			pass.framebuffer = framebuffer;
			pass.viewportScale = directives.getViewportScale();
			pass.generateMipmap = new boolean[RenderTargets.MAX_RENDER_TARGETS];

			for (int i = 0; i < pass.generateMipmap.length; i++) {
				pass.generateMipmap[i] = directives.getMipmappedBuffers().contains(i);
			}

			passes.add(pass);

			// Flip the buffers that this shader wrote to
			for (int buffer : drawBuffers) {
				if (buffer >= RenderTargets.MAX_RENDER_TARGETS) {
					// Don't flip extended buffers
					// TODO: Support extended buffers
					continue;
				}

				bufferFlipper.flip(buffer);
			}
		}

		this.passes = passes.build();

		GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
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

	public void renderAll() {
		// TODO: Is this valid in the deferred pass?
		centerDepthSampler.endWorldRendering();

		RenderSystem.disableBlend();
		RenderSystem.disableAlphaTest();

		final Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		final int baseWidth = main.textureWidth;
		final int baseHeight = main.textureHeight;

		FullScreenQuadRenderer.INSTANCE.begin();

		for (Pass renderPass : passes) {
			renderPass.framebuffer.bind();

			bindRenderTarget(0, renderTargets.get(1), renderPass.stageReadsFromAlt.contains(1), renderPass.generateMipmap[1]);
			bindRenderTarget(0, renderTargets.get(2), renderPass.stageReadsFromAlt.contains(2), renderPass.generateMipmap[2]);
			bindRenderTarget(0, renderTargets.get(3), renderPass.stageReadsFromAlt.contains(3), renderPass.generateMipmap[3]);
			bindRenderTarget(0, renderTargets.get(4), renderPass.stageReadsFromAlt.contains(4), renderPass.generateMipmap[4]);
			bindRenderTarget(0, renderTargets.get(5), renderPass.stageReadsFromAlt.contains(5), renderPass.generateMipmap[5]);
			bindRenderTarget(0, renderTargets.get(6), renderPass.stageReadsFromAlt.contains(6), renderPass.generateMipmap[6]);
			bindRenderTarget(0, renderTargets.get(7), renderPass.stageReadsFromAlt.contains(7), renderPass.generateMipmap[7]);

			float scaledWidth = baseWidth * renderPass.viewportScale;
			float scaledHeight = baseHeight * renderPass.viewportScale;
			RenderSystem.viewport(0, 0, (int) scaledWidth, (int) scaledHeight);

			renderPass.program.use();

			// TODO: Better default handling.
			bindRenderTarget(0, renderTargets.get(0), renderPass.stageReadsFromAlt.contains(0), renderPass.generateMipmap[0]);

			FullScreenQuadRenderer.INSTANCE.renderQuad();
		}

		FullScreenQuadRenderer.end();

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		// Also bind the "main" framebuffer if it isn't already bound.
		main.beginWrite(true);
		GlStateManager.useProgram(0);

		// NB: Unbinding all of these textures is necessary for proper shaderpack reloading.
		for (int i = 0; i < SamplerLimits.get().getMaxTextureUnits(); i++) {
			// Unbind all textures that we may have used.
			// NB: This is necessary for shader pack reloading to work propely
			unbindTexture(i);
		}

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

	private static void bindTexture(int textureUnit, int texture) {
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(texture);
	}

	private static void unbindTexture(int textureUnit) {
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(0);
	}

	// TODO: Don't just copy this from DeferredWorldRenderingPipeline
	private Program createProgram(ProgramSource source, ImmutableSet<Integer> flipped,
														   Supplier<ShadowMapRenderer> shadowMapRendererSupplier) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		Objects.requireNonNull(flipped);
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null), source.getGeometrySource().orElse(null),
				source.getFragmentSource().orElse(null), IrisSamplers.RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), updateNotifier);
		IrisSamplers.addNoiseSampler(builder, noiseTexture);
		IrisSamplers.addCompositeSamplers(builder, renderTargets);
		IrisSamplers.addRenderTargetSamplers(builder, () -> flipped, renderTargets, true);

		if (IrisSamplers.hasShadowSamplers(builder)) {
			IrisSamplers.addShadowSamplers(builder, shadowMapRendererSupplier.get());
		}

		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "centerDepthSmooth", this.centerDepthSampler::getCenterDepthSmoothSample);

		return builder.build();
	}

	public void destroy() {
		for (Pass renderPass : passes) {
			renderPass.destroy();
		}
	}
}
