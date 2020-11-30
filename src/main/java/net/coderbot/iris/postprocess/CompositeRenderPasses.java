package net.coderbot.iris.postprocess;

import java.io.IOException;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
	private final ImmutableList<Program> stages;
	private final FullScreenQuadRenderer quadRenderer;
	private final Framebuffer swap;
	private final SmoothedFloat centerDepthSmooth;

	public CompositeRenderPasses(ShaderPack pack) {
		final ImmutableList.Builder<Program> stages = ImmutableList.builder();

		for (ShaderPack.ProgramSource source: pack.getComposite()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			stages.add(createProgram(source));
		}

		pack.getCompositeFinal().ifPresent(
				compositeFinal -> stages.add(createProgram(compositeFinal))
		);

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
		Framebuffer renderingTo = MinecraftClient.getInstance().getFramebuffer();
		Framebuffer readingFrom = this.swap;

		if (renderingTo.textureWidth != readingFrom.textureWidth || renderingTo.textureHeight != readingFrom.textureHeight) {
			readingFrom.resize(renderingTo.textureWidth, renderingTo.textureHeight, true);
		}

		// We're actually reading from the framebuffer, but it needs to be bound to the GL_FRAMEBUFFER target
		renderingTo.beginWrite(false);
		float centerDepth = centerDepthSmooth.getAsFloat();

		for (Program stage : stages) {
			// Swap the main / swap framebuffers
			Framebuffer temp = readingFrom;
			readingFrom = renderingTo;
			renderingTo = temp;

			renderingTo.beginWrite(false);

			RenderSystem.activeTexture(GL15.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_DEPTH);
			RenderSystem.bindTexture(readingFrom.getDepthAttachment());
			RenderSystem.activeTexture(GL15.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_COLOR);
			RenderSystem.bindTexture(readingFrom.getColorAttachment());

			stage.use();
			GL21C.glUniform1f(GL21C.glGetUniformLocation(stage.getProgramId(), "centerDepthSmooth"), centerDepth);
			quadRenderer.render();
		}

		GlStateManager.useProgram(0);

		if (renderingTo != MinecraftClient.getInstance().getFramebuffer()) {
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
}
