package net.irisshaders.iris.postprocess;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.rendertarget.FramebufferBlitter;
import net.irisshaders.iris.rendertarget.RenderTarget;
import net.irisshaders.iris.rendertarget.RenderTargets;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shaderpack.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.ProgramDirectives;
import net.irisshaders.iris.shaderpack.ProgramSet;
import net.irisshaders.iris.shaderpack.ProgramSource;
import net.irisshaders.iris.shadows.ShadowMapRenderer;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class FinalPassRenderer {
	private final RenderTargets renderTargets;

	@Nullable
	private final Pass finalPass;
	private final ImmutableList<SwapPass> swapPasses;
	private final GlFramebuffer baseline;
	private final AbstractTexture noiseTexture;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;

	public FinalPassRenderer(ProgramSet pack, RenderTargets renderTargets, AbstractTexture noiseTexture,
							 FrameUpdateNotifier updateNotifier, ImmutableSet<Integer> flippedBuffers,
							 CenterDepthSampler centerDepthSampler,
							 Supplier<ShadowMapRenderer> shadowMapRendererSupplier) {
		this.updateNotifier = updateNotifier;
		this.centerDepthSampler = centerDepthSampler;

		final PackRenderTargetDirectives renderTargetDirectives = pack.getPackDirectives().getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings =
				renderTargetDirectives.getRenderTargetSettings();

		this.noiseTexture = noiseTexture;
		this.renderTargets = renderTargets;
		this.finalPass = pack.getCompositeFinal().map(source -> {
			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			pass.program = createProgram(source, flippedBuffers, shadowMapRendererSupplier);
			pass.stageReadsFromAlt = flippedBuffers;
			pass.mipmappedBuffers = directives.getMipmappedBuffers();

			return pass;
		}).orElse(null);

		IntList buffersToBeCleared = pack.getPackDirectives().getRenderTargetDirectives().getBuffersToBeCleared();

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		// TODO: We don't actually fully swap the content, we merely copy it from alt to main
		// This works for the most part, but it's not perfect. A better approach would be creating secondary
		// framebuffers for every other frame, but that would be a lot more complex...
		ImmutableList.Builder<SwapPass> swapPasses = ImmutableList.builder();

		flippedBuffers.forEach((i) -> {
			int target = i;

			if (buffersToBeCleared.contains(target)) {
				return;
			}

			SwapPass swap = new SwapPass();
			swap.from = renderTargets.createFramebufferWritingToAlt(new int[] {target});
			swap.from.readBuffer(target);
			swap.targetTexture = renderTargets.get(target).getMainTexture();

			swapPasses.add(swap);
		});

		this.swapPasses = swapPasses.build();

		GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
	}

	private static final class Pass {
		Program program;
		ImmutableSet<Integer> stageReadsFromAlt;
		ImmutableSet<Integer> mipmappedBuffers;

		private void destroy() {
			this.program.destroy();
		}
	}

	private static final class SwapPass {
		GlFramebuffer from;
		int targetTexture;
	}

	public void renderFinalPass() {
		RenderSystem.disableBlend();
		RenderSystem.disableAlphaTest();

		final com.mojang.blaze3d.pipeline.RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		final int baseWidth = main.width;
		final int baseHeight = main.height;

		FullScreenQuadRenderer.INSTANCE.begin();

		main.bindWrite(true);

		if (this.finalPass != null) {
			if (!finalPass.mipmappedBuffers.isEmpty()) {
				RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

				for (int index : finalPass.mipmappedBuffers) {
					setupMipmapping(renderTargets.get(index), finalPass.stageReadsFromAlt.contains(index));
				}
			}

			finalPass.program.use();
			FullScreenQuadRenderer.INSTANCE.renderQuad();
		}

		FullScreenQuadRenderer.end();

		if (finalPass == null) {
			// If there are no passes, we somehow need to transfer the content of the Iris render targets into the main
			// Minecraft framebuffer.
			//
			// Thus, the following call transfers the content of colortex0 and the depth buffer into the main Minecraft
			// framebuffer.
			//
			// TODO: What if colortex0 has a weird size or format?
			FramebufferBlitter.copyFramebufferContent(this.baseline, main);
		} else {
			// We still need to copy the depth buffer content as finalized in the gbuffer pass to the main framebuffer.
			//
			// This is needed for things like on-screen overlays to work properly.
			FramebufferBlitter.copyDepthBufferContent(this.baseline, main);
		}

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		for (int i = 0; i < RenderTargets.MAX_RENDER_TARGETS; i++) {
			// Reset mipmapping states at the end of the frame.
			resetRenderTarget(renderTargets.get(i));
		}

		for (SwapPass swapPass : swapPasses) {
			swapPass.from.bindAsReadBuffer();

			RenderSystem.bindTexture(swapPass.targetTexture);
			GL20C.glCopyTexSubImage2D(GL20C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, baseWidth, baseHeight);
		}

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		// Also bind the "main" framebuffer if it isn't already bound.
		main.bindWrite(true);
		GlStateManager._glUseProgram(0);

		for (int i = 0; i < SamplerLimits.get().getMaxTextureUnits(); i++) {
			// Unbind all textures that we may have used.
			// NB: This is necessary for shader pack reloading to work propely
			RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + i);
			RenderSystem.bindTexture(0);
		}

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
	}

	private static void setupMipmapping(RenderTarget target, boolean readFromAlt) {
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
		GL30C.glGenerateMipmap(GL20C.GL_TEXTURE_2D);
		GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR_MIPMAP_LINEAR);
	}

	private static void resetRenderTarget(RenderTarget target) {
		// Resets the sampling mode of the given render target and then unbinds it to prevent accidental sampling of it
		// elsewhere.
		RenderSystem.bindTexture(target.getMainTexture());
		GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);

		RenderSystem.bindTexture(target.getAltTexture());
		GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);

		RenderSystem.bindTexture(0);
	}

	// TODO: Don't just copy this from DeferredWorldRenderingPipeline
	private Program createProgram(ProgramSource source, ImmutableSet<Integer> flipped,
								  Supplier<ShadowMapRenderer> shadowMapRendererSupplier) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null), source.getGeometrySource().orElse(null),
				source.getFragmentSource().orElse(null), IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), updateNotifier);
		IrisSamplers.addRenderTargetSamplers(builder, () -> flipped, renderTargets, true);
		IrisSamplers.addNoiseSampler(builder, noiseTexture);
		IrisSamplers.addCompositeSamplers(builder, renderTargets);

		if (IrisSamplers.hasShadowSamplers(builder)) {
			IrisSamplers.addShadowSamplers(builder, shadowMapRendererSupplier.get());
		}

		// TODO: Don't duplicate this with CompositeRenderer
		// TODO: Parse the value of const float centerDepthSmoothHalflife from the shaderpack's fragment shader configuration
		builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "centerDepthSmooth", this.centerDepthSampler::getCenterDepthSmoothSample);

		return builder.build();
	}

	public void destroy() {
		if (finalPass != null) {
			finalPass.destroy();
		}
	}
}
