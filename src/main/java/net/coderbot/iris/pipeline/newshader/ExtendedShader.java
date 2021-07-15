package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ExtendedShader extends Shader implements SamplerHolder {
	NewWorldRenderingPipeline parent;
	ProgramUniforms uniforms;
	GlFramebuffer writingToBeforeTranslucent;
	GlFramebuffer writingToAfterTranslucent;
	GlFramebuffer baseline;
	HashMap<String, IntSupplier> dynamicSamplers;

	public ExtendedShader(ResourceFactory resourceFactory, String string, VertexFormat vertexFormat, GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent, GlFramebuffer baseline, Consumer<LocationalUniformHolder> uniformCreator, NewWorldRenderingPipeline parent) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getProgramRef();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		uniformCreator.accept(uniformBuilder);

		uniforms = uniformBuilder.buildUniforms();
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;
		this.baseline = baseline;
		this.dynamicSamplers = new HashMap<>();
		this.parent = parent;
	}

	@Override
	public void unbind() {
		super.unbind();

		MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
	}

	@Override
	public void bind() {
		dynamicSamplers.forEach((name, supplier) -> this.addIrisSampler(name, supplier.getAsInt()));

		super.bind();
		uniforms.update();

		if (parent.isBeforeTranslucent) {
			writingToBeforeTranslucent.bind();
		} else {
			writingToAfterTranslucent.bind();
		}
	}

	public void addIrisSampler(String name, int id) {
		super.addSampler(name, id);
	}

	public void addIrisSampler(String name, IntSupplier supplier) {
		dynamicSamplers.put(name, supplier);
	}

	@Override
	public void addSampler(String name, Object sampler) {
		// Translate vanilla sampler names to Iris / ShadersMod sampler names
		if (name.equals("Sampler0")) {
			name = "texture";

			// "tex" is also a valid sampler name.
			super.addSampler("tex", sampler);
		} else if (name.equals("Sampler2")) {
			name = "lightmap";
		} else if (name.startsWith("Sampler")) {
			// We only care about the texture and the lightmap for now from vanilla.
			// All other samplers will be coming from Iris.
			return;
		} else {
			Iris.logger.warn("Iris: didn't recognize the sampler name " + name + " in addSampler, please use addIrisSampler for custom Iris-specific samplers instead.");
			return;
		}

		// TODO: Expose Sampler1 (the mob overlay flash)

		super.addSampler(name, sampler);
	}

	@Nullable
	@Override
	public GlUniform getUniform(String name) {
		// Prefix all uniforms with Iris to help avoid conflicts with existing names within the shader.
		return super.getUniform("iris_" + name);
	}

	// TODO: This is kind of a mess. The interface might need some cleanup here.
	@Override
	public void addExternalSampler(int textureUnit, String... names) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public boolean hasSampler(String name) {
		return GL20C.glGetUniformLocation(this.getProgramRef(), name) != -1;
	}

	@Override
	public boolean addDefaultSampler(IntSupplier sampler, Runnable postBind, String... names) {
		throw new UnsupportedOperationException("addDefaultSampler is not yet implemented");
	}

	@Override
	public boolean addDynamicSampler(IntSupplier sampler, Runnable postBind, String... names) {
		throw new UnsupportedOperationException("postBind isn't supported.");
	}

	@Override
	public boolean addDynamicSampler(IntSupplier sampler, String... names) {
		boolean used = false;

		for (String name : names) {
			if (hasSampler(name)) {
				used = true;
			}

			addIrisSampler(name, sampler);
		}

		return used;
	}
}
