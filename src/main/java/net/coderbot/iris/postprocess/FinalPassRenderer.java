package net.coderbot.iris.postprocess;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.rendertarget.FramebufferBlitter;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.SamplerUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.Map;
import java.util.Objects;

public class FinalPassRenderer {
	private final RenderTargets renderTargets;

	@Nullable
	private final Pass finalPass;
	private final ImmutableList<SwapPass> swapPasses;
	private final GlFramebuffer baseline;
	private final AbstractTexture noiseTexture;
	private final FrameUpdateNotifier updateNotifier;
	private final CenterDepthSampler centerDepthSampler;

	private boolean usesShadows = false;

	public FinalPassRenderer(ProgramSet pack, RenderTargets renderTargets, AbstractTexture noiseTexture,
							 FrameUpdateNotifier updateNotifier, ImmutableSet<Integer> flippedBuffers,
							 CenterDepthSampler centerDepthSampler) {
		this.updateNotifier = updateNotifier;
		this.centerDepthSampler = centerDepthSampler;

		final PackRenderTargetDirectives renderTargetDirectives = pack.getPackDirectives().getRenderTargetDirectives();
		final Map<Integer, PackRenderTargetDirectives.RenderTargetSettings> renderTargetSettings =
				renderTargetDirectives.getRenderTargetSettings();

		Pair<Program, ProgramDirectives> finalProgramEntry =
				pack.getCompositeFinal().map(this::createProgram).orElse(null);

		if (finalProgramEntry != null) {
			Pass pass = new Pass();
			ProgramDirectives directives = finalProgramEntry.getRight();

			pass.program = finalProgramEntry.getLeft();
			pass.stageReadsFromAlt = flippedBuffers;
			pass.generateMipmap = new boolean[RenderTargets.MAX_RENDER_TARGETS];

			for (int i = 0; i < pass.generateMipmap.length; i++) {
				pass.generateMipmap[i] = directives.getMipmappedBuffers().contains(i);
			}

			finalPass = pass;
		} else {
			finalPass = null;
		}

		IntList buffersToBeCleared = pack.getPackDirectives().getRenderTargetDirectives().getBuffersToBeCleared();

		this.renderTargets = renderTargets;

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

		this.noiseTexture = noiseTexture;
	}

	private static final class Pass {
		Program program;
		ImmutableSet<Integer> stageReadsFromAlt;
		boolean[] generateMipmap;

		private void destroy() {
			this.program.destroy();
		}
	}

	private static final class SwapPass {
		GlFramebuffer from;
		int targetTexture;
	}

	public void renderFinalPass(ShadowMapRenderer shadowMapRenderer) {
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

		main.beginWrite(true);

		if (this.finalPass != null) {
			bindRenderTarget(SamplerUniforms.COLOR_TEX_0, renderTargets.get(0), finalPass.stageReadsFromAlt.contains(0), finalPass.generateMipmap[0]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_1, renderTargets.get(1), finalPass.stageReadsFromAlt.contains(1), finalPass.generateMipmap[1]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_2, renderTargets.get(2), finalPass.stageReadsFromAlt.contains(2), finalPass.generateMipmap[2]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_3, renderTargets.get(3), finalPass.stageReadsFromAlt.contains(3), finalPass.generateMipmap[3]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_4, renderTargets.get(4), finalPass.stageReadsFromAlt.contains(4), finalPass.generateMipmap[4]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_5, renderTargets.get(5), finalPass.stageReadsFromAlt.contains(5), finalPass.generateMipmap[5]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_6, renderTargets.get(6), finalPass.stageReadsFromAlt.contains(6), finalPass.generateMipmap[6]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_7, renderTargets.get(7), finalPass.stageReadsFromAlt.contains(7), finalPass.generateMipmap[7]);

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

		for (SwapPass swapPass : swapPasses) {
			swapPass.from.bindAsReadBuffer();

			RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
			RenderSystem.bindTexture(swapPass.targetTexture);
			GL20C.glCopyTexSubImage2D(GL20C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, baseWidth, baseHeight);
		}

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
		bindTexture(textureUnit, target.getMainTexture());
		GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);

		bindTexture(textureUnit, target.getAltTexture());
		GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);

		RenderSystem.bindTexture(0);
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
		if (finalPass != null) {
			finalPass.destroy();
		}
	}

	public boolean usesShadows() {
		return usesShadows;
	}
}
