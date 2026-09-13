package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.Uniform;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.irisshaders.iris.compat.SkipList;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.irisshaders.iris.helpers.MatrixUtils;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL46C;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExtendedShader extends GlProgram implements IrisProgram {
	private static final Matrix4f identity;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static ExtendedShader lastApplied;

	static {
		identity = new Matrix4f();
		identity.identity();
	}

	private final boolean intensitySwizzle;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final boolean hasOverrides;
	private final int modelViewInverse;
	private final int projectionInverse;
	private final Matrix3f normalMatrix = new Matrix3f();
	private final CustomUniforms customUniforms;
	private final IrisRenderingPipeline parent;
	private final ProgramUniforms uniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;
	private final GlFramebuffer writingToBeforeTranslucent;
	private final GlFramebuffer writingToAfterTranslucent;
	private final BlendModeOverride blendModeOverride;
	private final float alphaTest;
	private final boolean usesTessellation;
	private final Matrix4f tempMatrix4f = new Matrix4f();
	private final Matrix3f tempMatrix3f = new Matrix3f();
	private final float[] tempFloats = new float[16];
	private final float[] tempFloats2 = new float[9];
	private final int normalMat;
	private boolean hasUV;
	private int textureToUnswizzle;
	private boolean isSetup;
	private final IrisRenderingPipeline pipeline;

	public ExtendedShader(int programId, String string, VertexFormat vertexFormat, boolean usesTessellation,
                          GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
                          BlendModeOverride blendModeOverride, AlphaTest alphaTest,
                          Consumer<DynamicLocationalUniformHolder> uniformCreator, BiConsumer<SamplerHolder, ImageHolder> samplerCreator, boolean isIntensity,
                          IrisRenderingPipeline parent, @Nullable List<BufferBlendOverride> bufferBlendOverrides, CustomUniforms customUniforms, Patch patch) throws IOException {
		super(programId, string);

		this.pipeline = parent;

		GLDebug.nameObject(GL43C.GL_PROGRAM, programId, string);

		((ShaderInstanceInterface) this).setShouldSkip(SkipList.NONE);

		boolean has1 = false, has2 = false, has0 = false;
		if (vertexFormat.contains("UV0") || vertexFormat.contains("a_TexCoord")) {
			this.hasUV = true;
			has0 = true;
		}

		if (vertexFormat.contains("UV1")) {
			has1 = true;
		}

		if (vertexFormat.contains("UV2") || vertexFormat.contains("a_LightAndData")) {
			has2 = true;
		}
        List<BindGroupLayout.UniformDescription> layouts = new ArrayList<>();

		if (patch == Patch.VANILLA) {
            BindGroupLayout samplr = null;

            if (has0) {
                if (has1) {
                    if (has2) {
                        samplr = BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2;
                    } else {
                        samplr = BindGroupLayouts.SAMPLER0_SAMPLER1;
                    }
                } else if (has2) {
                    samplr = BindGroupLayouts.SAMPLER0_SAMPLER2;
                } else {
                    samplr = BindGroupLayouts.SAMPLER0;
                }
            }

            if (samplr != null) layouts.addAll(samplr.uniforms());
            layouts.addAll(BindGroupLayouts.DYNAMIC_TRANSFORMS.uniforms());
            layouts.addAll(BindGroupLayouts.CLOUD_INFO.uniforms());
            layouts.addAll(BindGroupLayouts.PROJECTION.uniforms());
            layouts.addAll(BindGroupLayouts.GLOBALS.uniforms());
            layouts.addAll(BindGroupLayouts.FOG.uniforms());
        } else {
            layouts.addAll(ShaderChunkRenderer.BIND_GROUP.uniforms());
        }

		super.setupBindGroupLayouts(layouts);


		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		ProgramSamplers.Builder samplerBuilder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		uniformCreator.accept(uniformBuilder);
		this.normalMat = GlStateManager._glGetUniformLocation(programId, "iris_NormalMat");
		ProgramImages.Builder builder = ProgramImages.builder(programId);
		samplerCreator.accept(samplerBuilder, builder);
		customUniforms.mapholderToPass(uniformBuilder, this);
		this.usesTessellation = usesTessellation;

		uniforms = uniformBuilder.buildUniforms();
		this.customUniforms = customUniforms;
		samplers = samplerBuilder.build();
		images = builder.build();
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;
		this.blendModeOverride = blendModeOverride;
		this.bufferBlendOverrides = bufferBlendOverrides;
		this.hasOverrides = bufferBlendOverrides != null && !bufferBlendOverrides.isEmpty();
		this.alphaTest = alphaTest.reference();
		this.parent = parent;

		this.modelViewInverse = GlStateManager._glGetUniformLocation(programId, "iris_ModelViewMatInverse");
		this.projectionInverse = GlStateManager._glGetUniformLocation(programId, "iris_ProjMatInverse");

		this.intensitySwizzle = isIntensity;
	}

	public boolean isIntensitySwizzle() {
		return intensitySwizzle;
	}

	@Override
	public void iris$clearState() {
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();

		if (this.blendModeOverride != null || hasOverrides) {
			BlendModeOverride.restore();
		}

		isSetup = false;
	}

	private float[] tempF = new float[9];

	@Override
	public void iris$setupState(List<BindGroupLayout.UniformDescription> samplers) {
		isSetup = true;
		DepthColorStorage.unlockDepthColor();

		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest);
		GlStateManager._glUseProgram(getProgramId());

		if (modelViewInverse > -1) {
			IrisRenderSystem.uniformMatrix4fv(modelViewInverse, false, RenderSystem.getModelViewMatrixCopy().invert(tempMatrix4f).get(tempFloats));
		}


		if (normalMat > -1) {
			tempF = RenderSystem.getModelViewMatrixCopy().invert(tempMatrix4f).transpose3x3(normalMatrix).get(tempF);

			IrisRenderSystem.uniformMatrix3fv(normalMat, false, tempF);
		}

		if (projectionInverse > -1) {
			// TODO: This is wrong. (1.21.6)
			IrisRenderSystem.uniformMatrix4fv(projectionInverse, false, (ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? MatrixUtils.undoRevZ(ShadowRenderer.PROJECTION) : CapturedRenderingState.INSTANCE.getGbufferProjection()).invert(tempMatrix4f).get(tempFloats));
		}

		ImmediateState.usingTessellation = usesTessellation;

		if (!hasUV) {
			IrisRenderSystem.bindTextureToUnit(GL46C.GL_TEXTURE_2D, 0, pipeline.getWhitePixel().getTexture().iris$getGlId());
		}

		this.samplers.update();
		uniforms.update();

		customUniforms.push(this);

		images.update();

		//GL46C.glUniform1i(GlStateManager._glGetUniformLocation(getProgramId(), "iris_overlay"), 1);
		BlendModeOverride.restore();

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

	public boolean hasActiveImages() {
		return images.getActiveImages() > 0;
	}

	@Override
	public int iris$getBlockIndex(int program, CharSequence uniformBlockName) {
        if (((String) uniformBlockName).contains("u_")) {
            return GL46C.glGetUniformBlockIndex(program, uniformBlockName);
        }
		return GL46C.glGetUniformBlockIndex(program, "iris_" + uniformBlockName);
	}

	public boolean usesTessellation() {
		return usesTessellation;
	}

	@Override
	public boolean iris$isSetUp() {
		return isSetup;
	}
}
