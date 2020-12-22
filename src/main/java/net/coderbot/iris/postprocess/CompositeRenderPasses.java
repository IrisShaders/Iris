package net.coderbot.iris.postprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.postprocess.target.CompositeRenderTarget;
import net.coderbot.iris.postprocess.target.CompositeRenderTargets;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL21C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public class CompositeRenderPasses {
	private final Program baseline;
	private final CompositeRenderTargets renderTargets;

	private final GlFramebuffer writesToMain;
	private final ImmutableList<Pass> passes;

	private final FullScreenQuadRenderer quadRenderer;
	private final SmoothedFloat centerDepthSmooth;

	public CompositeRenderPasses(ShaderPack pack) {
		baseline = createBaselineProgram(pack);

		final List<Program> programs = new ArrayList<>();

		for (ShaderPack.ProgramSource source: pack.getComposite()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			programs.add(createProgram(source));
		}

		pack.getCompositeFinal().ifPresent(
				compositeFinal -> {
					programs.add(createProgram(compositeFinal));
				}
		);

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();

		this.renderTargets = new CompositeRenderTargets(main.textureWidth, main.textureHeight);

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();

		boolean stageReadsFromAlt = false;

		this.writesToMain = createStageFramebuffer(renderTargets, false);

		for (Program program: programs) {
			Pass pass = new Pass();

			pass.program = program;

			boolean stageWritesToAlt = !stageReadsFromAlt;
			GlFramebuffer framebuffer = createStageFramebuffer(renderTargets, stageWritesToAlt);

			pass.stageReadsFromAlt = stageReadsFromAlt;
			pass.framebuffer = framebuffer;

			if (program == programs.get(programs.size() - 1)) {
				pass.isLastPass = true;
			}

			passes.add(pass);
			// TODO: Depth?

			// Flip the buffers
			stageReadsFromAlt = !stageReadsFromAlt;
		}

		this.passes = passes.build();
		this.quadRenderer = new FullScreenQuadRenderer();

		centerDepthSmooth = new SmoothedFloat(1.0f, () -> {
			float[] depthValue = new float[1];
			// Read a single pixel from the depth buffer
			// TODO: glReadPixels forces a full pipeline stall / flush, and probably isn't too great for performance
			GL11C.glReadPixels(
					main.textureWidth / 2, main.textureHeight / 2, 1, 1,
					GL11C.GL_DEPTH_COMPONENT, GL11C.GL_FLOAT, depthValue
			);

			return depthValue[0];
		});
	}

	private static final class Pass {
		Program program;
		GlFramebuffer framebuffer;
		boolean stageReadsFromAlt;
		boolean isLastPass;
	}

	private static GlFramebuffer createStageFramebuffer(CompositeRenderTargets renderTargets, boolean stageWritesToAlt) {
		GlFramebuffer framebuffer = new GlFramebuffer();
		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();

		System.out.println("creating framebuffer: stageWritesToAlt = " + stageWritesToAlt);

		for (int i = 0; i < CompositeRenderTargets.MAX_RENDER_TARGETS; i++) {
			CompositeRenderTarget target = renderTargets.get(i);
			int textureId = stageWritesToAlt ? target.getAltTexture() : target.getMainTexture();

			System.out.println("  attachment " + i + " -> texture " + textureId);

			framebuffer.addColorAttachment(i, textureId);
		}

		framebuffer.addDepthAttachment(renderTargets.getDepthTexture().getTextureId());

		if (!framebuffer.isComplete()) {
			throw new IllegalStateException("Unexpected error while creating framebuffer");
		}

		return framebuffer;
	}

	public void renderAll() {
		// Make sure we're using texture unit 0
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		renderTargets.resizeIfNeeded(main.textureWidth, main.textureHeight);

		// We're actually reading from the framebuffer, but it needs to be bound to the GL_FRAMEBUFFER target
		MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
		float centerDepth = centerDepthSmooth.getAsFloat();

		int mainAttachment = MinecraftClient.getInstance().getFramebuffer().getColorAttachment();

		this.writesToMain.bind();

		RenderSystem.bindTexture(main.getColorAttachment());
		baseline.use();
		quadRenderer.render();

		for (Pass renderPass: passes) {
			if (!renderPass.isLastPass) {
				renderPass.framebuffer.bind();
			} else {
				MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
			}

			RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_DEPTH);
			RenderSystem.bindTexture(main.getDepthAttachment());

			RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_COLOR);
			CompositeRenderTarget target = renderTargets.get(0);

			RenderSystem.bindTexture(renderPass.stageReadsFromAlt ? target.getAltTexture() : target.getMainTexture());

			renderPass.program.use();
			GL21C.glUniform1f(GL21C.glGetUniformLocation(renderPass.program.getProgramId(), "centerDepthSmooth"), centerDepth);
			quadRenderer.render();
		}

		GlStateManager.useProgram(0);

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_DEPTH);
		RenderSystem.bindTexture(0);
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_COLOR);
		RenderSystem.bindTexture(0);
	}

	// TODO: Don't just copy this from ShaderPipeline
	private static Program createProgram(ShaderPack.ProgramSource source) {
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
		PostProcessUniforms.addPostProcessUniforms(builder);

		return builder.build();
	}

	private static Program createBaselineProgram(ShaderPack parent) {
		ShaderPack.ProgramSource source = new ShaderPack.ProgramSource("<iris builtin baseline composite>", BASELINE_COMPOSITE_VSH, BASELINE_COMPOSITE_FSH, parent);

		return createProgram(source);
	}

	private static final String BASELINE_COMPOSITE_VSH =
			"#version 120\n" +
			"\n" +
			"varying vec2 texcoord;\n" +
			"\n" +
			"void main() {\n" +
			"\tgl_Position = ftransform();\n" +
			"\ttexcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;\n" +
			"}";

	private static final String BASELINE_COMPOSITE_FSH =
			"#version 120\n" +
			"\n" +
			"uniform sampler2D gcolor;\n" +
			"\n" +
			"varying vec2 texcoord;\n" +
			"\n" +
			"void main() {\n" +
			"\tvec3 color = texture2D(gcolor, texcoord).rgb;\n" +
			"\n" +
			"/* DRAWBUFFERS:0 */\n" +
			"\tgl_FragData[0] = vec4(color, 1.0); // gcolor\n" +
			"}";
}
