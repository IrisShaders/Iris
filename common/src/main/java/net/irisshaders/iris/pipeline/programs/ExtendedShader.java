package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
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
import net.irisshaders.iris.gl.program.IrisProgramTypes;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.irisshaders.iris.mixinterface.ShaderInstanceInterface;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.KHRDebug;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExtendedShader extends ShaderInstance implements ShaderInstanceInterface {
	private static final Matrix4f IDENTITY = new Matrix4f().identity();
	private static final Uniform FAKE_UNIFORM = new Uniform("", 1, 2, null);
	private static ExtendedShader lastApplied;

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
	private Program geometry, tessControl, tessEval;

	public ExtendedShader(ResourceProvider resourceFactory, String name, VertexFormat vertexFormat,
						  boolean usesTessellation, GlFramebuffer writingToBeforeTranslucent,
						  GlFramebuffer writingToAfterTranslucent, BlendModeOverride blendModeOverride,
						  AlphaTest alphaTest, Consumer<DynamicLocationalUniformHolder> uniformCreator,
						  BiConsumer<SamplerHolder, ImageHolder> samplerCreator, boolean isIntensity,
						  IrisRenderingPipeline parent, @Nullable List<BufferBlendOverride> bufferBlendOverrides,
						  CustomUniforms customUniforms) throws IOException {
		super(resourceFactory, name, vertexFormat);

		setupDebugNames(name);

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(name, this.getId());
		ProgramSamplers.Builder samplerBuilder = ProgramSamplers.builder(this.getId(), IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		ProgramImages.Builder imageBuilder = ProgramImages.builder(this.getId());

		uniformCreator.accept(uniformBuilder);
		samplerCreator.accept(samplerBuilder, imageBuilder);
		customUniforms.mapholderToPass(uniformBuilder, this);

		this.uniforms = uniformBuilder.buildUniforms();
		this.samplers = samplerBuilder.build();
		this.images = imageBuilder.build();

		this.usesTessellation = usesTessellation;
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;
		this.blendModeOverride = blendModeOverride;
		this.bufferBlendOverrides = bufferBlendOverrides;
		this.hasOverrides = bufferBlendOverrides != null && !bufferBlendOverrides.isEmpty();
		this.alphaTest = alphaTest.reference();
		this.parent = parent;
		this.customUniforms = customUniforms;
		this.intensitySwizzle = isIntensity;

		this.modelViewInverse = this.getUniform("ModelViewMatInverse");
		this.projectionInverse = this.getUniform("ProjMatInverse");
		this.normalMatrix = this.getUniform("NormalMat");
	}

	private void setupDebugNames(String name) {
		GLDebug.nameObject(KHRDebug.GL_SHADER, this.getVertexProgram().getId(), name + "_vertex.vsh");
		GLDebug.nameObject(KHRDebug.GL_SHADER, this.getFragmentProgram().getId(), name + "_fragment.fsh");
		GLDebug.nameObject(KHRDebug.GL_PROGRAM, this.getId(), name);
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

	@Override
	public void apply() {
		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest);

		ProgramManager.glUseProgram(this.getId());

		setupTextures();
		updateMatrices();
		updateUniforms();
		applyBlendModes();
		bindFramebuffer();
	}

	private void setupTextures() {
		if (intensitySwizzle) {
			IrisRenderSystem.texParameteriv(RenderSystem.getShaderTexture(0), TextureType.TEXTURE_2D.getGlType(),
				ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA, new int[]{GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED});
		}

		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.ALBEDO_TEXTURE_UNIT, RenderSystem.getShaderTexture(0));
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.OVERLAY_TEXTURE_UNIT, RenderSystem.getShaderTexture(1));
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));

		ImmediateState.usingTessellation = usesTessellation;
	}

	private void updateMatrices() {
		if (PROJECTION_MATRIX != null && projectionInverse != null) {
			projectionInverse.set(tempMatrix4f.set(PROJECTION_MATRIX.getFloatBuffer()).invert().get(tempFloats));
		} else if (projectionInverse != null) {
			projectionInverse.set(IDENTITY);
		}

		if (MODEL_VIEW_MATRIX != null) {
			if (modelViewInverse != null) {
				modelViewInverse.set(tempMatrix4f.set(MODEL_VIEW_MATRIX.getFloatBuffer()).invert().get(tempFloats));
			}

			if (normalMatrix != null) {
				normalMatrix.set(tempMatrix3f.set(tempMatrix4f.set(MODEL_VIEW_MATRIX.getFloatBuffer())).invert().transpose().get(tempFloats2));
			}
		}
	}

	private void updateUniforms() {
		uploadIfNotNull(projectionInverse);
		uploadIfNotNull(modelViewInverse);
		uploadIfNotNull(normalMatrix);

		super.uniforms.forEach(this::uploadIfNotNull);

		samplers.update();
		uniforms.update();
		customUniforms.push(this);
		images.update();
	}

	private void applyBlendModes() {
		if (this.blendModeOverride != null) {
			this.blendModeOverride.apply();
		}

		if (hasOverrides) {
			bufferBlendOverrides.forEach(BufferBlendOverride::apply);
		}
	}

	private void bindFramebuffer() {
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
			return FAKE_UNIFORM;
		}
		return uniform;
	}

	private void uploadIfNotNull(Uniform uniform) {
		if (uniform != null) {
			uniform.upload();
		}
	}

	@Override
	public void attachToProgram() {
		super.attachToProgram();
		attachExtraShaders();
	}

	private void attachExtraShaders() {
		if (this.geometry != null) {
			this.geometry.attachToShader(this);
		}
		if (this.tessControl != null) {
			this.tessControl.attachToShader(this);
		}
		if (this.tessEval != null) {
			this.tessEval.attachToShader(this);
		}
	}

	@Override
	public void iris$createExtraShaders(ResourceProvider factory, String name) {
		createGeometryShader(factory, name);
		createTessControlShader(factory, name);
		createTessEvalShader(factory, name);
	}

	private void createGeometryShader(ResourceProvider factory, String name) {
		createShader(factory, name, "_geometry.gsh", IrisProgramTypes.GEOMETRY,
			program -> this.geometry = program);
	}

	private void createTessControlShader(ResourceProvider factory, String name) {
		createShader(factory, name, "_tessControl.tcs", IrisProgramTypes.TESS_CONTROL,
			program -> this.tessControl = program);
	}

	private void createTessEvalShader(ResourceProvider factory, String name) {
		createShader(factory, name, "_tessEval.tes", IrisProgramTypes.TESS_EVAL,
			program -> this.tessEval = program);
	}

	private void createShader(ResourceProvider factory, String name, String suffix,
							  Program.Type type, Consumer<Program> programSetter) {
		factory.getResource(ResourceLocation.fromNamespaceAndPath("minecraft", name + suffix)).ifPresent(resource -> {
			try {
				Program program = Program.compileShader(type, name, resource.open(), resource.sourcePackId(),
					new GlslPreprocessor() {
						@Nullable
						@Override
						public String applyImport(boolean bl, String string) {
							return null;
						}
					});
				GLDebug.nameObject(KHRDebug.GL_SHADER, program.getId(), name + suffix);
				programSetter.accept(program);
			} catch (IOException e) {
				Iris.logger.error("Failed to create shader program", e);
			}
		});
	}

	public Program getGeometry() {
		return this.geometry;
	}

	public Program getTessControl() {
		return this.tessControl;
	}

	public Program getTessEval() {
		return this.tessEval;
	}

	public boolean hasActiveImages() {
		return images.getActiveImages() > 0;
	}
}
