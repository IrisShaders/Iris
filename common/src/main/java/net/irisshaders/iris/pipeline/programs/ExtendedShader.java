package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.CompiledShader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgramConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.opengl.KHRDebug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExtendedShader extends CompiledShaderProgram {
	private static final Matrix4f identity;
	private static ExtendedShader lastApplied;

	static {
		identity = new Matrix4f();
		identity.identity();
	}

	private final boolean intensitySwizzle;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final boolean hasOverrides;
	private final Uniform modelViewInverse;
	private final Uniform projectionInverse;
	private final Uniform normalMatrix;
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

	public ExtendedShader(int programId, ResourceProvider resourceFactory, String string, VertexFormat vertexFormat, boolean usesTessellation,
						  GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
						  BlendModeOverride blendModeOverride, AlphaTest alphaTest,
						  Consumer<DynamicLocationalUniformHolder> uniformCreator, BiConsumer<SamplerHolder, ImageHolder> samplerCreator, boolean isIntensity,
						  IrisRenderingPipeline parent, @Nullable List<BufferBlendOverride> bufferBlendOverrides, CustomUniforms customUniforms) throws IOException {
		super(programId);

		List<ShaderProgramConfig.Uniform> uniformList = new ArrayList<>();
		List<ShaderProgramConfig.Sampler> samplerList = new ArrayList<>();
		uniformList.add(new ShaderProgramConfig.Uniform("iris_ModelViewMat", "matrix4x4", 16, List.of(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_NormalMat", "matrix3x3", 9, List.of(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_ProjMat", "matrix4x4", 16, List.of(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_TextureMat", "matrix4x4", 16, List.of(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_ColorModulator", "float", 4, List.of(1.0f, 1.0f, 1.0f, 1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_FogColor", "float", 4, List.of(1.0f, 1.0f, 1.0f, 1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_ModelOffset", "float", 3, List.of(0.0f, 0.0f, 0.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_FogStart", "float", 1, List.of(0.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_FogEnd", "float", 1, List.of(1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_GlintAlpha", "float", 1, List.of(0.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_ModelViewMatInverse", "matrix4x4", 16, List.of(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)));
		uniformList.add(new ShaderProgramConfig.Uniform("iris_ProjMatInverse", "matrix4x4", 16, List.of(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)));

		samplerList.add(new ShaderProgramConfig.Sampler("Sampler0"));
		setupUniforms(uniformList, samplerList);


		GLDebug.nameObject(KHRDebug.GL_PROGRAM, programId, string);

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		ProgramSamplers.Builder samplerBuilder = ProgramSamplers.builder(programId, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		uniformCreator.accept(uniformBuilder);
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

		if (this.blendModeOverride != null || hasOverrides) {
			BlendModeOverride.restore();
		}

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	@Override
	public void apply() {
		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest);

		GlStateManager._glUseProgram(getProgramId());

		int i = GlStateManager._getActiveTexture();

		GlStateManager._activeTexture(i);

		if (intensitySwizzle) {
			IrisRenderSystem.texParameteriv(RenderSystem.getShaderTexture(0), TextureType.TEXTURE_2D.getGlType(), ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
				new int[]{GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED});
		}

		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.ALBEDO_TEXTURE_UNIT, RenderSystem.getShaderTexture(0));
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.OVERLAY_TEXTURE_UNIT, RenderSystem.getShaderTexture(1));
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));

		ImmediateState.usingTessellation = usesTessellation;

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
		}

		uploadIfNotNull(projectionInverse);
		uploadIfNotNull(modelViewInverse);
		uploadIfNotNull(normalMatrix);

		samplers.update();
		uniforms.update();

		List<Uniform> uniformList = super.uniforms;
		for (Uniform uniform : uniformList) {
			uploadIfNotNull(uniform);
		}

		customUniforms.push(this);

		images.update();

		GL46C.glUniform1i(GlStateManager._glGetUniformLocation(getProgramId(), "iris_overlay"), 1);

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
	public Uniform getUniform(@NotNull String name) {
		// Prefix all uniforms with Iris to help avoid conflicts with existing names within the shader.
		Uniform uniform = super.getUniform("iris_" + name);

		if (uniform == null && (name.equalsIgnoreCase("OverlayUV") || name.equalsIgnoreCase("LightUV"))) {
			return null;
		} else {
			return uniform;
		}
	}

	@Override
	public void setupUniforms(List<ShaderProgramConfig.Uniform> list, List<ShaderProgramConfig.Sampler> list2) {
		RenderSystem.assertOnRenderThread();
		Iterator<?> var3 = list.iterator();

		while(var3.hasNext()) {
			ShaderProgramConfig.Uniform uniform = (ShaderProgramConfig.Uniform)var3.next();
			String string = uniform.name();
			int i = Uniform.glGetUniformLocation(this.getProgramId(), string);
			if (i != -1) {
				Uniform uniform2 = this.parseUniformNode(uniform);
				uniform2.setLocation(i);
				super.uniforms.add(uniform2);
				super.uniformsByName.put(string, uniform2);
			}
		}

		var3 = list2.iterator();

		while(var3.hasNext()) {
			ShaderProgramConfig.Sampler sampler = (ShaderProgramConfig.Sampler)var3.next();
			int j = Uniform.glGetUniformLocation(this.getProgramId(), sampler.name());
			if (j != -1) {
				super.samplers.add(sampler);
				super.samplerLocations.add(j);
			}
		}

		this.MODEL_VIEW_MATRIX = super.getUniform("iris_ModelViewMat");
		this.PROJECTION_MATRIX = super.getUniform("iris_ProjMat");
		this.TEXTURE_MATRIX = super.getUniform("iris_TextureMat");
		this.SCREEN_SIZE = super.getUniform("iris_ScreenSize");
		this.COLOR_MODULATOR = super.getUniform("iris_ColorModulator");
		this.LIGHT0_DIRECTION = super.getUniform("iris_Light0_Direction");
		this.LIGHT1_DIRECTION = super.getUniform("iris_Light1_Direction");
		this.GLINT_ALPHA = super.getUniform("iris_GlintAlpha");
		this.FOG_START = super.getUniform("iris_FogStart");
		this.FOG_END = super.getUniform("iris_FogEnd");
		this.FOG_COLOR = super.getUniform("iris_FogColor");
		this.FOG_SHAPE = super.getUniform("iris_FogShape");
		this.LINE_WIDTH = super.getUniform("iris_LineWidth");
		this.GAME_TIME = super.getUniform("iris_GameTime");
		this.MODEL_OFFSET = super.getUniform("iris_ModelOffset");
	}


	private void uploadIfNotNull(Uniform uniform) {
		if (uniform != null) {
			uniform.upload();
		}
	}

	public boolean hasActiveImages() {
		return images.getActiveImages() > 0;
	}
}
