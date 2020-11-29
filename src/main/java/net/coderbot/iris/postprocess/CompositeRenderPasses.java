package net.coderbot.iris.postprocess;

import java.io.IOException;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;

public class CompositeRenderPasses {
	private final ImmutableList<Program> stages;
	private final CompositeRenderer renderer;

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
	}

	public void renderAll() {
		for (Program stage : stages) {
			stage.use();
			renderer.render();
		}

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
