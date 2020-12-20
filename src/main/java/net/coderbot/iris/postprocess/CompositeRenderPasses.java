package net.coderbot.iris.postprocess;

import java.io.IOException;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public class CompositeRenderPasses {
	private final Program baseline;
	private final ImmutableList<Program> stages;
	private final FullScreenQuadRenderer quadRenderer;
	private final Framebuffer swap;
	private final SmoothedFloat centerDepthSmooth;

	public CompositeRenderPasses(ShaderPack pack) {
		baseline = createBaselineProgram(pack);

		final ImmutableList.Builder<Program> stages = ImmutableList.builder();
		int numStages = 0;

		for (ShaderPack.ProgramSource source: pack.getComposite()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			stages.add(createProgram(source));
			numStages += 1;
		}

		pack.getCompositeFinal().ifPresent(
				compositeFinal -> stages.add(createProgram(compositeFinal))
		);

		if (pack.getCompositeFinal().isPresent()) {
			numStages += 1;
		}

		if ((numStages % 2) != 0) {
			// Add a "padding" stage so that the final rendering result is always in the main framebuffer as it should be
			Iris.logger.info("Added a padding composite stage");
			stages.add(baseline);
		}

		this.stages = stages.build();
		this.quadRenderer = new FullScreenQuadRenderer();

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		this.swap = new Framebuffer(main.textureWidth, main.textureHeight, false, true);

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

	public void renderAll() {
		Framebuffer mainFb = MinecraftClient.getInstance().getFramebuffer();

		if (mainFb.textureWidth != this.swap.textureWidth || mainFb.textureHeight != this.swap.textureHeight) {
			this.swap.resize(mainFb.textureWidth, mainFb.textureHeight, true);
		}

		GlFramebuffer renderingTo = new GlFramebuffer();
		renderingTo.addColorAttachment(0, mainFb.getColorAttachment());

		GlFramebuffer readingFrom = new GlFramebuffer();
		readingFrom.addColorAttachment(0, this.swap.getColorAttachment());

		final GlFramebuffer mainMcFramebuffer = renderingTo;

		// We're actually reading from the framebuffer, but it needs to be bound to the GL_FRAMEBUFFER target
		mainFb.beginWrite(false);
		float centerDepth = centerDepthSmooth.getAsFloat();

		for (Program stage : stages) {
			// Swap the main / swap framebuffers
			GlFramebuffer temp = readingFrom;
			readingFrom = renderingTo;
			renderingTo = temp;

			renderingTo.bind();

			RenderSystem.activeTexture(GL15.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_DEPTH);
			RenderSystem.bindTexture(mainFb.getDepthAttachment());
			RenderSystem.activeTexture(GL15.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_COLOR);
			RenderSystem.bindTexture(readingFrom.getColorAttachment(0));

			stage.use();
			GL21C.glUniform1f(GL21C.glGetUniformLocation(stage.getProgramId(), "centerDepthSmooth"), centerDepth);
			quadRenderer.render();
		}

		GlStateManager.useProgram(0);

		if (renderingTo != mainMcFramebuffer) {
			// TODO
			throw new UnsupportedOperationException("TODO: Need to transfer the content of the swap framebuffer to the main Minecraft framebuffer");
		}

		RenderSystem.activeTexture(GL15.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_DEPTH);
		RenderSystem.bindTexture(0);
		RenderSystem.activeTexture(GL15.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_COLOR);
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

	private static String BASELINE_COMPOSITE_VSH =
			"#version 120\n" +
			"\n" +
			"varying vec2 texcoord;\n" +
			"\n" +
			"void main() {\n" +
			"\tgl_Position = ftransform();\n" +
			"\ttexcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;\n" +
			"}";

	private static String BASELINE_COMPOSITE_FSH =
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
