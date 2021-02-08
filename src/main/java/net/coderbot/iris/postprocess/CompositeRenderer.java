package net.coderbot.iris.postprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.rendertarget.FramebufferBlitter;
import net.coderbot.iris.rendertarget.NoiseTexture;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.lwjgl.opengl.GL15C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Pair;

public class CompositeRenderer {
	private final RenderTargets renderTargets;

	private final ImmutableList<Pass> passes;
	private final GlFramebuffer baseline;
	private final NoiseTexture noisetex;

	final CenterDepthSampler centerDepthSampler;

	public CompositeRenderer(ShaderPack pack, RenderTargets renderTargets) {
		centerDepthSampler = new CenterDepthSampler(renderTargets);

		final List<Pair<Program, ProgramDirectives>> programs = new ArrayList<>();

		for (ShaderPack.ProgramSource source : pack.getComposite()) {
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

		this.passes = passes.build();
		this.renderTargets = renderTargets;

		this.baseline = renderTargets.createFramebufferWritingToMain(new int[] {0});

		// TODO: Use noiseTextureResolution here instead.
		int noiseTextureResolution = 128;
		this.noisetex = new NoiseTexture(noiseTextureResolution, noiseTextureResolution);
	}

	private static final class Pass {
		Program program;
		GlFramebuffer framebuffer;
		boolean[] stageReadsFromAlt;
		boolean isLastPass;
		float viewportScale;

		private void destroy() {
			this.program.destroy();
			this.framebuffer.destroy();
		}
	}

	public void renderAll() {
		centerDepthSampler.endWorldRendering();

		// Make sure we're using texture unit 0
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		renderTargets.resizeIfNeeded(main.textureWidth, main.textureHeight);

		int depthAttachment = renderTargets.getDepthTexture().getTextureId();
		int depthAttachmentNoTranslucents = renderTargets.getDepthTextureNoTranslucents().getTextureId();

		for (Pass renderPass : passes) {
			if (!renderPass.isLastPass) {
				renderPass.framebuffer.bind();
			} else {
				main.beginWrite(false);
			}

			// TODO: Consider copying the depth texture content into a separate texture that won't be modified? Probably
			// isn't an issue though.
			bindTexture(PostProcessUniforms.DEPTH_TEX_0, depthAttachment);
			// TODO: No translucent objects
			bindTexture(PostProcessUniforms.DEPTH_TEX_1, depthAttachmentNoTranslucents);
			// Note: Since we haven't rendered the hand yet, this won't contain any handheld items.
			// Once we start rendering the hand before composite content, this will need to be addressed.
			bindTexture(PostProcessUniforms.DEPTH_TEX_2, depthAttachmentNoTranslucents);

			bindRenderTarget(PostProcessUniforms.COLOR_TEX_0, renderTargets.get(0), renderPass.stageReadsFromAlt[0]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_1, renderTargets.get(1), renderPass.stageReadsFromAlt[1]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_2, renderTargets.get(2), renderPass.stageReadsFromAlt[2]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_3, renderTargets.get(3), renderPass.stageReadsFromAlt[3]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_4, renderTargets.get(4), renderPass.stageReadsFromAlt[4]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_5, renderTargets.get(5), renderPass.stageReadsFromAlt[5]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_6, renderTargets.get(6), renderPass.stageReadsFromAlt[6]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_7, renderTargets.get(7), renderPass.stageReadsFromAlt[7]);

			bindTexture(PostProcessUniforms.NOISE_TEX, noisetex.getTextureId());

			float scaledWidth = main.textureWidth * renderPass.viewportScale;
			float scaledHeight = main.textureHeight * renderPass.viewportScale;
			RenderSystem.viewport(0, 0, (int) scaledWidth, (int) scaledHeight);

			renderPass.program.use();
			FullScreenQuadRenderer.INSTANCE.render();
		}

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
		}

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		// Also bind the "main" framebuffer if it isn't already bound.
		main.beginWrite(true);
		GlStateManager.useProgram(0);

		// TODO: We unbind these textures but it would probably make sense to unbind the other ones too.
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_DEPTH);
		RenderSystem.bindTexture(0);
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_COLOR);
		RenderSystem.bindTexture(0);
	}

	private static void bindRenderTarget(int textureUnit, RenderTarget target, boolean readFromAlt) {
		bindTexture(textureUnit, readFromAlt ? target.getAltTexture() : target.getMainTexture());
	}

	private static void bindTexture(int textureUnit, int texture) {
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(texture);
	}

	// TODO: Don't just copy this from ShaderPipeline
	private Pair<Program, ProgramDirectives> createProgram(ShaderPack.ProgramSource source) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null),
				source.getFragmentSource().orElse(null));
		} catch (IOException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getIdMap());
		PostProcessUniforms.addPostProcessUniforms(builder, this);

		return new Pair<>(builder.build(), source.getDirectives());
	}

	public void destroy() {
		baseline.destroy();
		centerDepthSampler.destroy();
		noisetex.destroy();

		for (Pass renderPass : passes) {
			renderPass.destroy();
		}
	}
}
