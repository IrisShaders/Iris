package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.ImageBinding;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.GlUniform1iCall;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ExtendedShader extends ShaderInstance implements SamplerHolder, ImageHolder {
	NewWorldRenderingPipeline parent;
	ProgramUniforms uniforms;
	GlFramebuffer writingToBeforeTranslucent;
	GlFramebuffer writingToAfterTranslucent;
	GlFramebuffer baseline;
	BlendModeOverride blendModeOverride;
	HashMap<String, IntSupplier> dynamicSamplers;
	private final boolean intensitySwizzle;
	private final List<ImageBinding> images;
	private final List<GlUniform1iCall> calls;
	private int nextImageUnit;
	private final int maxImageUnits;

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
		this.images = new ArrayList<>();
		this.calls = new ArrayList<>();
		this.maxImageUnits = IrisRenderSystem.getMaxImageUnits();

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

		if (calls != null) {
			for (GlUniform1iCall call : calls) {
				IrisRenderSystem.uniform1i(call.getLocation(), call.getValue());
			}

			calls.clear();
		}

		for (ImageBinding imageBinding : images) {
			imageBinding.update();
		}

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
		return GlStateManager._glGetUniformLocation(this.getId(), name) != -1;
	}

	@Override
	public void addTextureImage(IntSupplier textureID, InternalTextureFormat internalFormat, String name) {
		int location = GlStateManager._glGetUniformLocation(this.getId(), name);

		if (location == -1) {
			return;
		}

		if (nextImageUnit >= maxImageUnits) {
			if (maxImageUnits == 0) {
				throw new IllegalStateException("Image units are not supported on this platform, but a shader" +
						" program attempted to reference " + name + ".");
			} else {
				throw new IllegalStateException("No more available texture units while activating image " + name + "." +
						" Only " + maxImageUnits + " image units are available.");
			}
		}

		images.add(new ImageBinding(nextImageUnit, internalFormat.getGlFormat(), textureID));
		calls.add(new GlUniform1iCall(location, nextImageUnit));

		nextImageUnit += 1;
	}
}
