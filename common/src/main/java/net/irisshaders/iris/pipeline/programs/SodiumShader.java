package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat3v;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformInt;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL20C;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public class SodiumShader implements ChunkShaderInterface {
	private final GlUniformMatrix4f uniformModelViewMatrix;
	private final GlUniformMatrix4f uniformModelViewMatrixInv;
	private final GlUniformMatrix4f uniformProjectionMatrix;
	private final GlUniformMatrix4f uniformProjectionMatrixInv;
	private final GlUniformMatrix3f uniformNormalMatrix;
	private final GlUniformFloat3v uniformRegionOffset;
	private final ProgramImages images;
	private final ProgramSamplers samplers;
	private final ProgramUniforms uniforms;
	private final CustomUniforms customUniforms;
	private final BlendModeOverride blendModeOverride;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final float alphaTest;
	private final boolean containsTessellation;
	private final @Nullable GlUniformInt uniformCascade;

	public SodiumShader(IrisRenderingPipeline pipeline, SodiumPrograms.Pass pass, ShaderBindingContext context,
						int handle, Optional<BlendModeOverride> blendModeOverride,
						List<BufferBlendOverride> bufferBlendOverrides,
						CustomUniforms customUniforms, Supplier<ImmutableSet<Integer>> flipState, float alphaTest,
						boolean containsTessellation) {
		this.uniformModelViewMatrix = context.bindUniformOptional("iris_ModelViewMatrix", GlUniformMatrix4f::new);
		this.uniformModelViewMatrixInv = context.bindUniformOptional("iris_ModelViewMatrixInverse", GlUniformMatrix4f::new);
		this.uniformNormalMatrix = context.bindUniformOptional("iris_NormalMatrix", GlUniformMatrix3f::new);
		this.uniformProjectionMatrix = context.bindUniformOptional("iris_ProjectionMatrix", GlUniformMatrix4f::new);
		this.uniformProjectionMatrixInv = context.bindUniformOptional("iris_ProjectionMatrixInv", GlUniformMatrix4f::new);
		this.uniformRegionOffset = context.bindUniformOptional("u_RegionOffset", GlUniformFloat3v::new);
		this.uniformCascade = context.bindUniformOptional("u_CascadeNum", GlUniformInt::new);

		this.alphaTest = alphaTest;
		this.containsTessellation = containsTessellation;

		boolean isShadowPass = pass == SodiumPrograms.Pass.SHADOW || pass == SodiumPrograms.Pass.SHADOW_CUTOUT;

		this.uniforms = buildUniforms(pass, handle, customUniforms);
		this.customUniforms = customUniforms;
		this.samplers = buildSamplers(pipeline, pass, handle, isShadowPass, flipState);
		this.images = buildImages(pipeline, pass, handle, isShadowPass, flipState);

		this.blendModeOverride = blendModeOverride.orElse(null);
		this.bufferBlendOverrides = bufferBlendOverrides;
	}

	private ProgramUniforms buildUniforms(SodiumPrograms.Pass pass, int handle, CustomUniforms customUniforms) {
		ProgramUniforms.Builder builder = ProgramUniforms.builder(pass.name().toLowerCase(Locale.ROOT), handle);
		CommonUniforms.addDynamicUniforms(builder, FogMode.PER_VERTEX);
		customUniforms.assignTo(builder);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(builder);
		customUniforms.mapholderToPass(builder, this);
		return builder.buildUniforms();
	}

	private ProgramSamplers buildSamplers(IrisRenderingPipeline pipeline, SodiumPrograms.Pass pass, int handle,
										  boolean isShadowPass, Supplier<ImmutableSet<Integer>> flipState) {
		ProgramSamplers.Builder builder = ProgramSamplers.builder(handle, IrisSamplers.SODIUM_RESERVED_TEXTURE_UNITS);
		pipeline.addGbufferOrShadowSamplers(builder, ProgramImages.builder(handle),
			flipState, isShadowPass, true, true, false);
		return builder.build();
	}

	private ProgramImages buildImages(IrisRenderingPipeline pipeline, SodiumPrograms.Pass pass, int handle,
									  boolean isShadowPass, Supplier<ImmutableSet<Integer>> flipState) {
		ProgramImages.Builder builder = ProgramImages.builder(handle);
		pipeline.addGbufferOrShadowSamplers(ProgramSamplers.builder(handle, IrisSamplers.SODIUM_RESERVED_TEXTURE_UNITS),
			builder, flipState, isShadowPass, true, true, false);
		return builder.build();
	}

	@Override
	public void setRegionOffset(float x, float y, float z) {
		if (uniformRegionOffset != null) {
			uniformRegionOffset.set(x, y, z);
		}
	}

	@Override
	public void setModelViewMatrix(Matrix4fc matrix) {
		if (uniformModelViewMatrix != null) {
			uniformModelViewMatrix.set(matrix);
		}

		Matrix4f invertedMatrix = matrix.invert(new Matrix4f());

		if (uniformModelViewMatrixInv != null) {
			uniformModelViewMatrixInv.set(invertedMatrix);
		}

		if (uniformNormalMatrix != null) {
			Matrix3f normalMatrix = invertedMatrix.transpose3x3(new Matrix3f());
			uniformNormalMatrix.set(normalMatrix);
		}
	}

	@Override
	public void setProjectionMatrix(Matrix4fc matrix) {
		if (uniformProjectionMatrix != null) {
			uniformProjectionMatrix.set(matrix);
		}

		if (uniformProjectionMatrixInv != null) {
			Matrix4f invertedMatrix = matrix.invert(new Matrix4f());

			uniformProjectionMatrixInv.set(invertedMatrix);
		}
	}

	@Override
	public void setupState() {
		applyBlendModes();
		updateUniforms();
		images.update();
		bindTextures();

		if (containsTessellation) {
			ImmediateState.usingTessellation = true;
		}
	}

	private void bindTextures() {
		IrisRenderSystem.bindTextureToUnit(GL20C.GL_TEXTURE_2D, 0, RenderSystem.getShaderTexture(0));
		IrisRenderSystem.bindTextureToUnit(GL20C.GL_TEXTURE_2D, 2, RenderSystem.getShaderTexture(2));
		GlStateManager._activeTexture(GL20C.GL_TEXTURE0 + IrisSamplers.LIGHTMAP_TEXTURE_UNIT);
	}

	private void applyBlendModes() {
		if (blendModeOverride != null) {
			blendModeOverride.apply();
		}
		bufferBlendOverrides.forEach(BufferBlendOverride::apply);
	}

	private void updateUniforms() {
		if (uniformCascade != null) {
			// TODO
			uniformCascade.set(ShadowRenderer.CASCADE);
		}
		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest);
		samplers.update();
		uniforms.update();
		customUniforms.push(this);
	}

	@Override
	public void resetState() {
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		BlendModeOverride.restore();
		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
		ImmediateState.usingTessellation = false;
	}
}
