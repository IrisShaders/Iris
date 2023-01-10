package net.coderbot.iris.pipeline.newshader;

import com.ibm.icu.impl.ICUNotifier;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendMode;
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
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import net.coderbot.iris.vendored.joml.FrustumRayBuilder;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ExtendedShader extends ShaderInstance implements ShaderInstanceInterface {
	private final boolean intensitySwizzle;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final boolean hasOverrides;
	private final Uniform modelViewInverse;
	private final Uniform projectionInverse;
	private final Uniform normalMatrix;
	private final CustomUniforms customUniforms;
	NewWorldRenderingPipeline parent;
	ProgramUniforms uniforms;
	ProgramSamplers samplers;
	ProgramImages images;
	GlFramebuffer writingToBeforeTranslucent;
	GlFramebuffer writingToAfterTranslucent;
	GlFramebuffer baseline;
	BlendModeOverride blendModeOverride;
	float alphaTest;
	private Program geometry;
	private final ShaderAttributeInputs inputs;

	private static ExtendedShader lastApplied;
	private final Vector3f chunkOffset = new Vector3f();

	public ExtendedShader(ResourceProvider resourceFactory, String string, VertexFormat vertexFormat,
						  GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
						  GlFramebuffer baseline, BlendModeOverride blendModeOverride, AlphaTest alphaTest,
						  Consumer<DynamicLocationalUniformHolder> uniformCreator, BiConsumer<SamplerHolder, ImageHolder> samplerCreator, boolean isIntensity,
						  NewWorldRenderingPipeline parent, ShaderAttributeInputs inputs, @Nullable List<BufferBlendOverride> bufferBlendOverrides, CustomUniforms customUniforms) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getId();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		ProgramSamplers.Builder samplerBuilder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		uniformCreator.accept(uniformBuilder);
		ProgramImages.Builder builder = ProgramImages.builder(programId);
		samplerCreator.accept(samplerBuilder, builder);
		customUniforms.mapholderToPass(uniformBuilder, this);

		uniforms = uniformBuilder.buildUniforms();
		this.customUniforms = customUniforms;
		samplers = samplerBuilder.build();
		images = builder.build();
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;
		this.baseline = baseline;
		this.blendModeOverride = blendModeOverride;
		this.bufferBlendOverrides = bufferBlendOverrides;
		this.hasOverrides = bufferBlendOverrides != null && !bufferBlendOverrides.isEmpty();
		this.alphaTest = alphaTest.getReference();
		this.parent = parent;
		this.inputs = inputs;

		this.modelViewInverse = this.getUniform("ModelViewMatInverse");
		this.projectionInverse = this.getUniform("ProjMatInverse");
		this.normalMatrix = this.getUniform("NormalMat");

		this.intensitySwizzle = isIntensity;
	}

	public boolean isIntensitySwizzle() {
		return intensitySwizzle;
	}

	@Override
	public void clear() {
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		lastApplied = null;

		if (this.blendModeOverride != null || hasOverrides) {
			BlendModeOverride.restore();
		}

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	Matrix4f tempMatrix4f = new Matrix4f();
	Matrix3f tempMatrix3f = new Matrix3f();
	private static final com.mojang.math.Matrix4f identity;

	static {
		identity = new com.mojang.math.Matrix4f();
		identity.setIdentity();
	}

	float[] tempFloats = new float[16];
	float[] tempFloats2 = new float[9];

	@Override
	public void apply() {
		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest);

		if (lastApplied != this) {
			lastApplied = this;
			ProgramManager.glUseProgram(this.getId());
		}

		if (intensitySwizzle) {
			IrisRenderSystem.texParameteriv(RenderSystem.getShaderTexture(0), TextureType.TEXTURE_2D.getGlType(), ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
				new int[] { GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED });
		}

		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.ALBEDO_TEXTURE_UNIT, RenderSystem.getShaderTexture(0));
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.OVERLAY_TEXTURE_UNIT, RenderSystem.getShaderTexture(1));
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));

		if (PROJECTION_MATRIX != null) {
			if (projectionInverse != null) {
				projectionInverse.set(tempMatrix4f.set(PROJECTION_MATRIX.getFloatBuffer()).invert().get(tempFloats));
			}
		} else {
			if (projectionInverse != null) {
				projectionInverse.set(identity);
			}
		}

		if (MODEL_VIEW_MATRIX != null) {
			if (modelViewInverse != null) {
				modelViewInverse.set(tempMatrix4f.set(MODEL_VIEW_MATRIX.getFloatBuffer()).invert().get(tempFloats));
			}

			if (normalMatrix != null) {
				normalMatrix.set(tempMatrix3f.set(tempMatrix4f.set(MODEL_VIEW_MATRIX.getFloatBuffer())).invert().transpose().get(tempFloats2));
			}
		} else {
			if (modelViewInverse != null) {
				modelViewInverse.set(identity);
			}

			if (normalMatrix != null) {
				normalMatrix.set(identity);
			}
		}

		uploadIfNotNull(projectionInverse);
		uploadIfNotNull(modelViewInverse);
		uploadIfNotNull(normalMatrix);

		List<Uniform> uniformList = super.uniforms;
		for (Uniform uniform : uniformList) {
			uploadIfNotNull(uniform);
		}

		samplers.update();
		uniforms.update();

		customUniforms.push(this);

		images.update();


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

	@Nullable
	@Override
	public Uniform getUniform(String name) {
		// Prefix all uniforms with Iris to help avoid conflicts with existing names within the shader.
		return super.getUniform("iris_" + name);
	}

	private void uploadIfNotNull(Uniform uniform) {
		if (uniform != null) {
			uniform.upload();
		}
	}

	@Override
	public void close() {
		super.close();
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

	public boolean hasActiveImages() {
		return images.getActiveImages() > 0;
	}
}
