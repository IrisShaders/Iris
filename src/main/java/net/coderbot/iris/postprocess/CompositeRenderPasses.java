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
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public class CompositeRenderPasses {
	private final ImmutableList<Program> stages;
	private final CompositeRenderer renderer;
	private final Framebuffer swap;

	public CompositeRenderPasses(ShaderPack pack) {
		ShaderPack.ProgramSource[] compositeSource = pack.getComposite();

		final ImmutableList.Builder<Program> stages = ImmutableList.builder();

		for (int i = 0; i < compositeSource.length; i++) {
			ShaderPack.ProgramSource source = compositeSource[i];

			if (source == null || !source.isValid()) {
				continue;
			}

			stages.add(createProgram(source));
		}

		pack.getCompositeFinal().ifPresent(
				compositeFinal -> stages.add(createProgram(compositeFinal))
		);

		this.stages = stages.build();
		this.renderer = new CompositeRenderer();

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		this.swap = new Framebuffer(main.textureWidth, main.textureHeight, false, true);
	}

	public void renderAll() {
		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();

		if (main.textureWidth != swap.textureWidth || main.textureHeight != swap.textureHeight) {
			swap.resize(main.textureWidth, main.textureHeight, true);
		}

		RenderSystem.activeTexture(GL15.GL_TEXTURE0);
		main.beginRead();

		float[] depthValue = new float[1];
		// Read a single pixel from the depth buffer
		GL11C.glReadPixels(
				main.textureWidth / 2, main.textureHeight / 2, 1, 1,
				GL11C.GL_DEPTH_COMPONENT, GL11C.GL_FLOAT, depthValue
		);

		RenderSystem.activeTexture(GL15.GL_TEXTURE1);
		RenderSystem.bindTexture(main.getDepthAttachment());
		RenderSystem.activeTexture(GL15.GL_TEXTURE0);

		swap.beginWrite(false);
		stages.get(0).use();
		GL21C.glUniform1f(GL21C.glGetUniformLocation(stages.get(0).getProgramId(), "centerDepthSmooth"), depthValue[0]);
		renderer.render();

		main.beginWrite(false);
		swap.beginRead();
		stages.get(1).use();
		renderer.render();

		swap.endRead();

		/*for (Program stage : stages) {
			stage.use();
			renderer.render();
		}*/

		GlStateManager.useProgram(0);
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

		return builder.build();
	}
}
