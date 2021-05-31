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

	// TODO: Yarn WTF: That's the unbind method, not the bind method!
	@Override
	public void bind() {
		super.bind();

		baseline.bind();
	}

	// TODO: Yarn WTF: That's the bind method...
	@Override
	public void upload() {
		super.upload();

		uniforms.update();
		writingTo.bind();
	}
}
