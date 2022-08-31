package net.coderbot.iris.shadows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import net.coderbot.iris.pipeline.patcher.CompositeDepthTransformer;
import net.coderbot.iris.postprocess.BufferFlipper;
import net.coderbot.iris.postprocess.CenterDepthSampler;
import net.coderbot.iris.postprocess.FullScreenQuadRenderer;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisImages;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ShadowCompositeRenderer {
	private final ShadowRenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final IntSupplier noiseTexture;
	private final FrameUpdateNotifier updateNotifier;
	private final Object2ObjectMap<String, IntSupplier> customTextureIds;
	private final ImmutableSet<Integer> flippedAtLeastOnceFinal;
	private final GlFramebuffer baseline;
	private ImmutableList<SwapPass> swapPasses;

	public ShadowCompositeRenderer(PackDirectives packDirectives, ProgramSource[] sources, ShadowRenderTargets renderTargets,
								   IntSupplier noiseTexture, FrameUpdateNotifier updateNotifier,
								   Object2ObjectMap<String, IntSupplier> customTextureIds, ImmutableMap<Integer, Boolean> explicitPreFlips) {
		this.noiseTexture = noiseTexture;
		this.updateNotifier = updateNotifier;
		this.renderTargets = renderTargets;
		this.customTextureIds = customTextureIds;

		final PackRenderTargetDirectives renderTargetDirectives = packDirectives.getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings =
				renderTargetDirectives.getRenderTargetSettings();

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();
		final ImmutableSet.Builder<Integer> flippedAtLeastOnce = new ImmutableSet.Builder<>();

		explicitPreFlips.forEach((buffer, shouldFlip) -> {
			if (shouldFlip) {
				renderTargets.flip(buffer);
				// NB: Flipping deferred_pre or composite_pre does NOT cause the "flippedAtLeastOnce" flag to trigger
			}
		});

		for (ProgramSource source : sources) {
			if (source == null || !source.isValid()) {
				continue;
			}

			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			ImmutableSet<Integer> flipped = renderTargets.snapshot();
			ImmutableSet<Integer> flippedAtLeastOnceSnapshot = flippedAtLeastOnce.build();

			pass.program = createProgram(source, flipped, flippedAtLeastOnceSnapshot, renderTargets);
			int[] drawBuffers = directives.getDrawBuffers();

			GlFramebuffer framebuffer = renderTargets.createColorFramebuffer(flipped, drawBuffers);

			pass.stageReadsFromAlt = flipped;
			pass.framebuffer = framebuffer;
			pass.viewportScale = directives.getViewportScale();
			pass.mipmappedBuffers = directives.getMipmappedBuffers();
			pass.flippedAtLeastOnce = flippedAtLeastOnceSnapshot;

			passes.add(pass);

			ImmutableMap<Integer, Boolean> explicitFlips = directives.getExplicitFlips();

			// Flip the buffers that this shader wrote to
			for (int buffer : drawBuffers) {
				// compare with boxed Boolean objects to avoid NPEs
				if (explicitFlips.get(buffer) == Boolean.FALSE) {
					continue;
				}

				renderTargets.flip(buffer);
				flippedAtLeastOnce.add(buffer);
			}

			explicitFlips.forEach((buffer, shouldFlip) -> {
				if (shouldFlip) {
					renderTargets.flip(buffer);
					flippedAtLeastOnce.add(buffer);
				}
			});
		}

		this.passes = passes.build();
		this.flippedAtLeastOnceFinal = flippedAtLeastOnce.build();

		this.baseline = renderTargets.createShadowFramebuffer(flippedAtLeastOnceFinal, new int[] {0});

		ImmutableList.Builder<SwapPass> swapPasses = ImmutableList.builder();

		renderTargets.snapshot().forEach((i) -> {
			int target = i;

			if (renderTargets.getBuffersToBeCleared().contains(target)) {
				return;
			}

			SwapPass swap = new SwapPass();
			swap.from = renderTargets.createFramebufferWritingToAlt(new int[] {target});
			// NB: This is handled in RenderTargets now.
			//swap.from.readBuffer(target);
			swap.targetTexture = renderTargets.get(target).getMainTexture();

			swapPasses.add(swap);
		});

		this.swapPasses = swapPasses.build();

		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
	}

	public ImmutableSet<Integer> getFlippedAtLeastOnceFinal() {
		return this.flippedAtLeastOnceFinal;
	}

	private static final class Pass {
		Program program;
		GlFramebuffer framebuffer;
		ImmutableSet<Integer> flippedAtLeastOnce;
		ImmutableSet<Integer> stageReadsFromAlt;
		ImmutableSet<Integer> mipmappedBuffers;
		float viewportScale;

		private void destroy() {
			this.program.destroy();
		}
	}

	private static final class SwapPass {
		GlFramebuffer from;
		int targetTexture;
	}

	public void renderAll() {
		RenderSystem.disableBlend();
		RenderSystem.disableAlphaTest();

		FullScreenQuadRenderer.INSTANCE.begin();

		for (Pass renderPass : passes) {
			if (!renderPass.mipmappedBuffers.isEmpty()) {
				RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

				for (int index : renderPass.mipmappedBuffers) {
					setupMipmapping(renderTargets.get(index), renderPass.stageReadsFromAlt.contains(index));
				}
			}

			float scaledWidth = renderTargets.getResolution() * renderPass.viewportScale;
			float scaledHeight = renderTargets.getResolution() * renderPass.viewportScale;
			RenderSystem.viewport(0, 0, (int) scaledWidth, (int) scaledHeight);

			renderPass.framebuffer.bind();
			renderPass.program.use();

			FullScreenQuadRenderer.INSTANCE.renderQuad();
		}

		FullScreenQuadRenderer.end();

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		ProgramUniforms.clearActiveUniforms();
		GlStateManager._glUseProgram(0);

		for (SwapPass swapPass : swapPasses) {
			// NB: We need to use bind(), not bindAsReadBuffer()... Previously we used bindAsReadBuffer() here which
			//     broke TAA on many packs and on many drivers.
			//
			// Note that glCopyTexSubImage2D reads from the current GL_READ_BUFFER (given by glReadBuffer()) for the
			// current framebuffer bound to GL_FRAMEBUFFER, but that is distinct from the current GL_READ_FRAMEBUFFER,
			// which is what bindAsReadBuffer() binds.
			//
			// Also note that RenderTargets already calls readBuffer(0) for us.
			swapPass.from.bind();

			RenderSystem.bindTexture(swapPass.targetTexture);
			GlStateManager._glCopyTexSubImage2D(GL20C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, renderTargets.getResolution(), renderTargets.getResolution());
		}

		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			// Reset mipmapping states at the end of the frame.
			resetRenderTarget(renderTargets.get(i));
		}

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
	}

	private static void setupMipmapping(net.coderbot.iris.rendertarget.RenderTarget target, boolean readFromAlt) {
		RenderSystem.bindTexture(readFromAlt ? target.getAltTexture() : target.getMainTexture());

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
		IrisRenderSystem.generateMipmaps(GL20C.GL_TEXTURE_2D);
		RenderSystem.texParameter(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR_MIPMAP_LINEAR);
	}

	private static void resetRenderTarget(RenderTarget target) {
		// Resets the sampling mode of the given render target and then unbinds it to prevent accidental sampling of it
		// elsewhere.
		RenderSystem.bindTexture(target.getMainTexture());
		RenderSystem.texParameter(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);

		RenderSystem.bindTexture(target.getAltTexture());
		RenderSystem.texParameter(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);

		RenderSystem.bindTexture(0);
	}

	// TODO: Don't just copy this from DeferredWorldRenderingPipeline
	private Program createProgram(ProgramSource source, ImmutableSet<Integer> flipped, ImmutableSet<Integer> flippedAtLeastOnceSnapshot,
														   ShadowRenderTargets targets) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		Objects.requireNonNull(flipped);
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), CompositeDepthTransformer.patch(source.getVertexSource().orElse(null)), CompositeDepthTransformer.patch(source.getGeometrySource().orElse(null)),
				CompositeDepthTransformer.patch(source.getFragmentSource().orElse(null)), IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		ProgramSamplers.CustomTextureSamplerInterceptor customTextureSamplerInterceptor = ProgramSamplers.customTextureSamplerInterceptor(builder, customTextureIds, flippedAtLeastOnceSnapshot);

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), updateNotifier);

		IrisSamplers.addNoiseSampler(customTextureSamplerInterceptor, noiseTexture);

		IrisSamplers.addShadowSamplers(customTextureSamplerInterceptor, targets, flipped);
		IrisImages.addShadowColorImages(builder, targets, flipped);

		return builder.build();
	}

	public void destroy() {
		for (Pass renderPass : passes) {
			renderPass.destroy();
		}
	}
}
