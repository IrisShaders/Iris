package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class ExtendedShader extends Shader {
	ProgramUniforms uniforms;
	GlFramebuffer writingTo;
	GlFramebuffer baseline;

	public ExtendedShader(ResourceFactory resourceFactory, String string, VertexFormat vertexFormat, GlFramebuffer writingTo, GlFramebuffer baseline, Consumer<LocationalUniformHolder> uniformCreator) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getProgramRef();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		uniformCreator.accept(uniformBuilder);

		uniforms = uniformBuilder.buildUniforms();
		this.writingTo = writingTo;
		this.baseline = baseline;
	}

	@Override
	public void method_34585() {
		baseline.bind();
	}

	@Override
	public void method_34586() {
		super.method_34586();

		uniforms.update();
		writingTo.bind();
	}
}
