package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ExtendedShader extends ShaderInstance implements SamplerHolder, ImageHolder, ShaderInstanceInterface {
	private final boolean intensitySwizzle;
	private final ProgramImages.Builder imageBuilder;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final boolean hasOverrides;
	private final CustomUniforms customUniforms;
	NewWorldRenderingPipeline parent;
	ProgramUniforms uniforms;
	GlFramebuffer writingToBeforeTranslucent;
	GlFramebuffer writingToAfterTranslucent;
	GlFramebuffer baseline;
	BlendModeOverride blendModeOverride;
	HashMap<String, IntSupplier> dynamicSamplers;
	float alphaTest;
	private ProgramImages currentImages;
	private Program geometry;
	private final ShaderAttributeInputs inputs;

	public ExtendedShader(ResourceProvider resourceFactory, String string, VertexFormat vertexFormat,
						  GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
						  GlFramebuffer baseline, BlendModeOverride blendModeOverride, AlphaTest alphaTest,
						  Consumer<DynamicLocationalUniformHolder> uniformCreator, boolean isIntensity,
						  NewWorldRenderingPipeline parent, ShaderAttributeInputs inputs, @Nullable List<BufferBlendOverride> bufferBlendOverrides, CustomUniforms customUniforms) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getId();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		uniformCreator.accept(uniformBuilder);
		customUniforms.mapholderToPass(uniformBuilder, this);

		uniforms = uniformBuilder.buildUniforms();
		this.customUniforms = customUniforms;
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;
		this.baseline = baseline;
		this.blendModeOverride = blendModeOverride;
		this.bufferBlendOverrides = bufferBlendOverrides;
		this.hasOverrides = bufferBlendOverrides != null && !bufferBlendOverrides.isEmpty();
		this.dynamicSamplers = new HashMap<>();
		this.alphaTest = alphaTest.getReference();
		this.parent = parent;
		this.imageBuilder = ProgramImages.builder(programId);
		this.currentImages = null;
		this.inputs = inputs;

		this.intensitySwizzle = isIntensity;
	}

	public boolean isIntensitySwizzle() {
		return intensitySwizzle;
	}

	@Override
	public void clear() {
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		super.clear();

		if (this.blendModeOverride != null || hasOverrides) {
			BlendModeOverride.restore();
		}

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	@Override
	public void apply() {
		dynamicSamplers.forEach((name, supplier) -> this.addIrisSampler(name, supplier.getAsInt()));

		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest);

		if (!inputs.hasTex()) {
			setSampler("Sampler0", parent.getWhitePixel().getId());
		}

		if (!inputs.hasLight()) {
			setSampler("Sampler2", parent.getWhitePixel().getId());
		}

		super.apply();
		uniforms.update();

		customUniforms.push(this);

		if (currentImages == null) {
			// rebuild if needed
			currentImages = imageBuilder.build();
		}

		currentImages.update();

		if (this.blendModeOverride != null) {
			this.blendModeOverride.apply();
		}

		if (hasOverrides) {
			bufferBlendOverrides.forEach(BufferBlendOverride::apply);
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
	public boolean addDynamicSampler(IntSupplier sampler, ValueUpdateNotifier notifier, String... names) {
		// TODO: This isn't right, but it should work due to how 1.17+ handles samplers?
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

	@Override
	public void attachToProgram() {
		super.attachToProgram();
		if (this.geometry != null) {
			this.geometry.attachToShader(this);
		}
	}

	@Override
	public void iris$createGeometryShader(ResourceProvider factory, String name) throws IOException {
		Resource geometry = factory.getResource(new ResourceLocation("minecraft", name + "_geometry.gsh"));
		if (geometry != null) {
			this.geometry = Program.compileShader(IrisProgramTypes.GEOMETRY, name, geometry.getInputStream(), geometry.getSourceName(), new GlslPreprocessor() {
				@Nullable
				@Override
				public String applyImport(boolean bl, String string) {
					return null;
				}
			});
		}
	}

	public Program getGeometry() {
		return this.geometry;
	}
}
