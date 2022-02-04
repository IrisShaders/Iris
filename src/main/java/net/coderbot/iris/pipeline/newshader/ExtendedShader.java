package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ExtendedShader extends ShaderInstance implements SamplerHolder, ImageHolder {
	private final boolean intensitySwizzle;
	private final ProgramImages.Builder imageBuilder;
	NewWorldRenderingPipeline parent;
	ProgramUniforms uniforms;
	GlFramebuffer writingToBeforeTranslucent;
	GlFramebuffer writingToAfterTranslucent;
	GlFramebuffer baseline;
	BlendModeOverride blendModeOverride;
	HashMap<String, IntSupplier> dynamicSamplers;
	private ProgramImages currentImages;

	public ExtendedShader(ResourceProvider resourceFactory, String string, VertexFormat vertexFormat, GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent, GlFramebuffer baseline, BlendModeOverride blendModeOverride, Consumer<DynamicUniformHolder> uniformCreator, NewWorldRenderingPipeline parent) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getId();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		uniformCreator.accept(uniformBuilder);

		uniforms = uniformBuilder.buildUniforms();
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;
		this.baseline = baseline;
		this.blendModeOverride = blendModeOverride;
		this.dynamicSamplers = new HashMap<>();
		this.parent = parent;
		this.imageBuilder = ProgramImages.builder(programId);
		this.currentImages = null;

		// TODO(coderbot): consider a way of doing this that doesn't rely on checking the shader name.
		this.intensitySwizzle = getName().contains("intensity");
	}

	public boolean isIntensitySwizzle() {
		return intensitySwizzle;
	}

	@Override
	public void clear() {
		ProgramUniforms.clearActiveUniforms();
		super.clear();

		if (this.blendModeOverride != null) {
			BlendModeOverride.restore();
		}

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	@Override
	public void apply() {
		dynamicSamplers.forEach((name, supplier) -> this.addIrisSampler(name, supplier.getAsInt()));

		super.apply();
		uniforms.update();

		if (currentImages == null) {
			// rebuild if needed
			currentImages = imageBuilder.build();
		}

		currentImages.update();

		if (this.blendModeOverride != null) {
			this.blendModeOverride.apply();
		}

		if (parent.isBeforeTranslucent) {
			writingToBeforeTranslucent.bind();
		} else {
			writingToAfterTranslucent.bind();
		}
	}

	public void addIrisSampler(String name, int id) {
		super.setSampler(name, id);
	}

	public void addIrisSampler(String name, IntSupplier supplier) {
		dynamicSamplers.put(name, supplier);
	}

	@Override
	public void setSampler(String name, Object sampler) {
		// Translate vanilla sampler names to Iris / ShadersMod sampler names
		if (name.equals("Sampler0")) {
			name = "gtexture";

			// "tex" and "texture" are also valid sampler names.
			super.setSampler("texture", sampler);
			super.setSampler("tex", sampler);
		} else if (name.equals("Sampler1")) {
			name = "iris_overlay";
		} else if (name.equals("Sampler2")) {
			name = "lightmap";
		} else if (name.startsWith("Sampler")) {
			// We only care about the texture, lightmap, and overlay for now from vanilla.
			// All other samplers will be coming from Iris.
			return;
		} else {
			Iris.logger.warn("Iris: didn't recognize the sampler name " + name + " in addSampler, please use addIrisSampler for custom Iris-specific samplers instead.");
			return;
		}

		super.setSampler(name, sampler);
	}

	@Nullable
	@Override
	public Uniform getUniform(String name) {
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
		return GlStateManager._glGetUniformLocation(this.getId(), name) != -1;
	}

	@Override
	public boolean addDefaultSampler(IntSupplier sampler, String... names) {
		throw new UnsupportedOperationException("addDefaultSampler is not yet implemented");
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

	@Override
	public boolean hasImage(String name) {
		return imageBuilder.hasImage(name);
	}

	@Override
	public void addTextureImage(IntSupplier textureID, InternalTextureFormat internalFormat, String name) {
		imageBuilder.addTextureImage(textureID, internalFormat, name);

		// mark for rebuild if needed
		this.currentImages = null;
	}
}
