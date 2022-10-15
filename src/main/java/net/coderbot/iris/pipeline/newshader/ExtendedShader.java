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
import com.mojang.math.Matrix4f;
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
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.vendored.joml.FrustumRayBuilder;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
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
	private Runnable chunkOffsetListener;
	private final Vector3f chunkOffset = new Vector3f();
	private Matrix4f projectionOverride;
	private Matrix4f modelViewOverride;

	public ExtendedShader(ResourceProvider resourceFactory, String string, VertexFormat vertexFormat,
						  GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
						  GlFramebuffer baseline, BlendModeOverride blendModeOverride, AlphaTest alphaTest,
						  Consumer<DynamicUniformHolder> uniformCreator, BiConsumer<SamplerHolder, ImageHolder> samplerCreator, boolean isIntensity,
						  NewWorldRenderingPipeline parent, ShaderAttributeInputs inputs, @Nullable List<BufferBlendOverride> bufferBlendOverrides) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getId();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		ProgramSamplers.Builder samplerBuilder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		uniformCreator.accept(uniformBuilder);
		addDynamicUniforms(uniformBuilder);
		ProgramImages.Builder builder = ProgramImages.builder(programId);
		samplerCreator.accept(samplerBuilder, builder);

		uniforms = uniformBuilder.buildUniforms();
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

		this.PROJECTION_MATRIX = new RedirectingUniform("ProjMat2", 0, 0, this, this::setProjectionOverride);
		this.MODEL_VIEW_MATRIX = new RedirectingUniform("ModelViewMat2", 0, 0, this, this::setModelViewOverride);

		this.intensitySwizzle = isIntensity;
	}

	private void addDynamicUniforms(DynamicUniformHolder uniformBuilder) {
		uniformBuilder.uniform3f("iris_ChunkOffset", this::getChunkOffset, getChunkOffsetNotifier());
		uniformBuilder.uniformMatrix("iris_ProjMat", () -> {
			if (projectionOverride != null) {
				return projectionOverride;
			} else {
				return RenderSystem.getProjectionMatrix();
			}
		}, listener -> {});
		uniformBuilder.uniformMatrix("iris_ModelViewMat", () -> {
			if (modelViewOverride != null) {
				return modelViewOverride;
			} else {
				return RenderSystem.getModelViewMatrix();
			}
		}, listener -> {});
	}

	public boolean isIntensitySwizzle() {
		return intensitySwizzle;
	}

	@Override
	public void clear() {
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		lastApplied = null;
		projectionOverride = null;
		modelViewOverride = null;

		if (this.blendModeOverride != null || hasOverrides) {
			BlendModeOverride.restore();
		}

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	@Override
	public void apply() {
		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest);

		if (lastApplied != this) {
			lastApplied = this;
			ProgramManager.glUseProgram(this.getId());
		}

		IrisRenderSystem.bindTextureToUnit(IrisSamplers.ALBEDO_TEXTURE_UNIT, RenderSystem.getShaderTexture(0));
		IrisRenderSystem.bindTextureToUnit(IrisSamplers.OVERLAY_TEXTURE_UNIT, RenderSystem.getShaderTexture(1));
		IrisRenderSystem.bindTextureToUnit(IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));

		samplers.update();
		uniforms.update();
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

	@Override
	public void attachToProgram() {
		super.attachToProgram();
		if (this.geometry != null) {
			this.geometry.attachToShader(this);
		}
	}

	@Override
	public void iris$createGeometryShader(ResourceProvider factory, String name) throws IOException {
		 factory.getResource(new ResourceLocation("minecraft", name + "_geometry.gsh")).ifPresent(geometry -> {
			 try {
				 this.geometry = Program.compileShader(IrisProgramTypes.GEOMETRY, name, geometry.open(), geometry.sourcePackId(), new GlslPreprocessor() {
					 @Nullable
					 @Override
					 public String applyImport(boolean bl, String string) {
						 return null;
					 }
				 });
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
		 });
	}

	public Program getGeometry() {
		return this.geometry;
	}

	public boolean hasActiveImages() {
		return images.getActiveImages() > 0;
	}

    public void setChunkOffset(float x, float y, float z) {
		chunkOffset.set(x, y, z);
		if (this.chunkOffsetListener != null) {
			chunkOffsetListener.run();
		}
    }

	private ValueUpdateNotifier getChunkOffsetNotifier() {
		return listener -> this.chunkOffsetListener = listener;
	}

	private Vector3f getChunkOffset() {
		return chunkOffset;
	}

    public void setProjectionOverride(Matrix4f arg3) {
		this.projectionOverride = arg3;
    }

	public void setModelViewOverride(Matrix4f arg3) {
		this.modelViewOverride = arg3;
    }
}
