package net.coderbot.iris.postprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.rendertarget.*;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.EmptyShadowMapRenderer;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.SamplerUniforms;
import net.minecraft.client.texture.AbstractTexture;
import org.lwjgl.opengl.GL15C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Pair;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class CompositeRenderer {
	private final RenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final ImmutableList<SwapPass> swapPasses;
	private final GlFramebuffer baseline;
	private final EmptyShadowMapRenderer shadowMapRenderer;
	private final AbstractTexture noiseTexture;

	final CenterDepthSampler centerDepthSampler;

	public CompositeRenderer(ProgramSet pack, RenderTargets renderTargets, EmptyShadowMapRenderer shadowMapRenderer, AbstractTexture noiseTexture) {
		centerDepthSampler = new CenterDepthSampler(renderTargets);

		final List<Pair<Program, ProgramDirectives>> programs = new ArrayList<>();

		for (ProgramSource source : pack.getComposite()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			programs.add(createProgram(source));
		}

		pack.getCompositeFinal().map(this::createProgram).ifPresent(programs::add);

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();

		// Initially filled with false values
		boolean[] stageReadsFromAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

		for (Pair<Program, ProgramDirectives> programEntry : programs) {
			Pass pass = new Pass();
			ProgramDirectives directives = programEntry.getRight();

			pass.program = programEntry.getLeft();
			int[] drawBuffers = directives.getDrawBuffers();

			boolean[] stageWritesToAlt = Arrays.copyOf(stageReadsFromAlt, RenderTargets.MAX_RENDER_TARGETS);

			for (int i = 0; i < stageWritesToAlt.length; i++) {
				stageWritesToAlt[i] = !stageWritesToAlt[i];
			}

			GlFramebuffer framebuffer = renderTargets.createColorFramebuffer(stageWritesToAlt, drawBuffers);

			pass.stageReadsFromAlt = Arrays.copyOf(stageReadsFromAlt, stageReadsFromAlt.length);
			pass.framebuffer = framebuffer;
			pass.viewportScale = directives.getViewportScale();

			if (programEntry == programs.get(programs.size() - 1)) {
				pass.isLastPass = true;
			}

			passes.add(pass);

			// Flip the buffers that this shader wrote to
			for (int buffer : drawBuffers) {
				stageReadsFromAlt[buffer] = !stageReadsFromAlt[buffer];
			}
		}

		IntList buffersToBeCleared = pack.getPackDirectives().getBuffersToBeCleared();
		boolean[] willBeCleared = new boolean[RenderTargets.MAX_RENDER_TARGETS];

		buffersToBeCleared.forEach((int buffer) -> {
			willBeCleared[buffer] = true;
		});

		this.passes = passes.build();
		this.renderTargets = renderTargets;

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		// TODO: We don't actually fully swap the content, we merely copy it from alt to main
		// This works for the most part, but it's not perfect. A better approach would be creating secondary
		// framebuffers for every other frame, but that would be a lot more complex...
		ImmutableList.Builder<SwapPass> swapPasses = ImmutableList.builder();

		for (int i = 0; i < stageReadsFromAlt.length; i++) {
			if (stageReadsFromAlt[i] && !willBeCleared[i]) {
				SwapPass swap = new SwapPass();
				swap.from = renderTargets.createFramebufferWritingToAlt(new int[] {i});
				swap.from.readBuffer(i);
				swap.targetTexture = renderTargets.get(i).getMainTexture();

				swapPasses.add(swap);
			}
		}

		this.swapPasses = swapPasses.build();

		GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);

		this.shadowMapRenderer = shadowMapRenderer;
		this.noiseTexture = noiseTexture;
	}

	private static final class Pass {
		Program program;
		GlFramebuffer framebuffer;
		boolean[] stageReadsFromAlt;
		boolean isLastPass;
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
		bindTexture(SamplerUniforms.SHADOW_TEX_1, shadowMapRenderer.getDepthTextureId());
		bindTexture(SamplerUniforms.NOISE_TEX, noiseTexture.getGlId());

		FullScreenQuadRenderer.INSTANCE.begin();

		for (Pass renderPass : passes) {
			if (!renderPass.isLastPass) {
				renderPass.framebuffer.bind();
			} else {
				main.beginWrite(false);
			}

			bindRenderTarget(SamplerUniforms.COLOR_TEX_0, renderTargets.get(0), renderPass.stageReadsFromAlt[0]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_1, renderTargets.get(1), renderPass.stageReadsFromAlt[1]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_2, renderTargets.get(2), renderPass.stageReadsFromAlt[2]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_3, renderTargets.get(3), renderPass.stageReadsFromAlt[3]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_4, renderTargets.get(4), renderPass.stageReadsFromAlt[4]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_5, renderTargets.get(5), renderPass.stageReadsFromAlt[5]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_6, renderTargets.get(6), renderPass.stageReadsFromAlt[6]);
			bindRenderTarget(SamplerUniforms.COLOR_TEX_7, renderTargets.get(7), renderPass.stageReadsFromAlt[7]);

			float scaledWidth = baseWidth * renderPass.viewportScale;
			float scaledHeight = baseHeight * renderPass.viewportScale;
			RenderSystem.viewport(0, 0, (int) scaledWidth, (int) scaledHeight);

			renderPass.program.use();
			FullScreenQuadRenderer.INSTANCE.renderQuad();
		}

		FullScreenQuadRenderer.end();

		if (passes.size() == 0) {
			// If there are no passes, we somehow need to transfer the content of the Iris render targets into the main
			// Minecraft framebuffer.
			//
			// Thus, the following call transfers the content of colortex0 and the depth buffer into the main Minecraft
			// framebuffer.
			FramebufferBlitter.copyFramebufferContent(this.baseline, main);
		} else {
			// We still need to copy the depth buffer content as finalized in the gbuffer pass to the main framebuffer.
			//
			// This is needed for things like on-screen overlays to work properly.
			FramebufferBlitter.copyDepthBufferContent(this.baseline, main);

			for (SwapPass swapPass : swapPasses) {
				swapPass.from.bindAsReadBuffer();

				RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
				RenderSystem.bindTexture(swapPass.targetTexture);
				GL20C.glCopyTexSubImage2D(GL20C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, baseWidth, baseHeight);
			}
		}

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		// Also bind the "main" framebuffer if it isn't already bound.
		main.beginWrite(true);
		GlStateManager.useProgram(0);

		// TODO: We unbind these textures but it would probably make sense to unbind the other ones too.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + SamplerUniforms.DEFAULT_DEPTH);
		RenderSystem.bindTexture(0);
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + SamplerUniforms.DEFAULT_COLOR);
		RenderSystem.bindTexture(0);
	}

	private static void bindRenderTarget(int textureUnit, RenderTarget target, boolean readFromAlt) {
		bindTexture(textureUnit, readFromAlt ? target.getAltTexture() : target.getMainTexture());
	}

	private static void bindTexture(int textureUnit, int texture) {
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(texture);
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

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives());
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
}
