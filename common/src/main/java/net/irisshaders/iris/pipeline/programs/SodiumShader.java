package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat2v;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat3v;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.mixin.texture.TextureAtlasAccessor;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL46C;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class SodiumShader implements ChunkShaderInterface {
	private static final int SUB_TEXEL_PRECISION_BITS = 5;

	private final GlUniformMatrix4f uniformModelViewMatrix;
	private final GlUniformMatrix4f uniformModelViewMatrixInv;
	private final GlUniformMatrix4f uniformProjectionMatrix;
	private final GlUniformMatrix4f uniformProjectionMatrixInv;
	private final GlUniformMatrix3f uniformNormalMatrix;
	private final GlUniformFloat3v uniformRegionOffset;
	private final GlUniformFloat2v uniformTexCoordShrink;
	private final ProgramImages images;
	private final ProgramSamplers samplers;
	private final ProgramUniforms uniforms;
	private final CustomUniforms customUniforms;
	private final BlendModeOverride blendModeOverride;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final float alphaTest;
	private final boolean containsTessellation;
	private boolean isShadowPass;

	public SodiumShader(IrisRenderingPipeline pipeline, SodiumPrograms.Pass pass, ShaderBindingContext context,
						int handle, BlendModeOverride blendModeOverride,
						List<BufferBlendOverride> bufferBlendOverrides,
						CustomUniforms customUniforms, Supplier<ImmutableSet<Integer>> flipState, float alphaTest,
						boolean containsTessellation) {
		this.uniformModelViewMatrix = context.bindUniformOptional("iris_ModelViewMatrix", GlUniformMatrix4f::new);
		this.uniformModelViewMatrixInv = context.bindUniformOptional("iris_ModelViewMatrixInverse", GlUniformMatrix4f::new);
		this.uniformNormalMatrix = context.bindUniformOptional("iris_NormalMatrix", GlUniformMatrix3f::new);
		this.uniformProjectionMatrix = context.bindUniformOptional("iris_ProjectionMatrix", GlUniformMatrix4f::new);
		this.uniformProjectionMatrixInv = context.bindUniformOptional("iris_ProjectionMatrixInv", GlUniformMatrix4f::new);
		this.uniformRegionOffset = context.bindUniformOptional("u_RegionOffset", GlUniformFloat3v::new);
		this.uniformTexCoordShrink = context.bindUniformOptional("u_TexCoordShrink", GlUniformFloat2v::new);

		this.alphaTest = alphaTest;
		this.containsTessellation = containsTessellation;

		isShadowPass = pass == SodiumPrograms.Pass.SHADOW || pass == SodiumPrograms.Pass.SHADOW_CUTOUT;

		this.uniforms = buildUniforms(pass, handle, customUniforms);
		this.customUniforms = customUniforms;
		this.samplers = buildSamplers(pipeline, pass, handle, isShadowPass, flipState);
		this.images = buildImages(pipeline, pass, handle, isShadowPass, flipState);

		this.blendModeOverride = blendModeOverride;
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
	public void setupState(TerrainRenderPass pass, FogParameters fogParameters) {
		DepthColorStorage.unlockDepthColor();

		applyBlendModes();
		RenderSystem.setShaderTexture(0, pass.getAtlas());
		updateUniforms();
		images.update();


		if (isShadowPass) {
			GlStateManager._disableCull();
		}

		var textureAtlas = Minecraft.getInstance()
			.getTextureManager()
			.getTexture(TextureAtlas.LOCATION_BLOCKS);
		textureAtlas.setFilter(false, textureAtlas.getTexture().getMipLevels() > 1);

		// There is a limited amount of sub-texel precision when using hardware texture sampling. The mapped texture
		// area must be "shrunk" by at least one sub-texel to avoid bleed between textures in the atlas. And since we
		// offset texture coordinates in the vertex format by one texel, we also need to undo that here.
		double subTexelPrecision = (1 << GLRenderDevice.INSTANCE.getSubTexelPrecisionBits());
		double subTexelOffset = 1.0f / CompactChunkVertex.TEXTURE_MAX_VALUE;

		if (this.uniformTexCoordShrink != null) {
			this.uniformTexCoordShrink.set(
				(float) (subTexelOffset - (((1.0D / ((TextureAtlasAccessor) textureAtlas).callGetWidth()) / subTexelPrecision))),
				(float) (subTexelOffset - (((1.0D / ((TextureAtlasAccessor) textureAtlas).callGetHeight()) / subTexelPrecision)))
			);
		}
		bindTextures(pass.getAtlas());

		if (containsTessellation) {
			ImmediateState.usingTessellation = true;
		}
	}

	private void bindTextures(GpuTextureView atlas) {
		((GlTexture) atlas.texture()).flushModeChanges(GL46C.GL_TEXTURE_2D);
		IrisRenderSystem.bindTextureToUnit(GL20C.GL_TEXTURE_2D, 0, atlas.texture().iris$getGlId());
		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);
		GlStateManager._texParameter(3553, 33084, atlas.baseMipLevel());
		GlStateManager._texParameter(3553, 33085, atlas.baseMipLevel() + atlas.mipLevels() - 1);
		((GlTexture) atlas.texture()).flushModeChanges(GL20C.GL_TEXTURE_2D);

		GpuTextureView lightmap = Minecraft.getInstance().gameRenderer.lightTexture().getTextureView();
		((GlTexture) lightmap.texture()).flushModeChanges(GL46C.GL_TEXTURE_2D);
		IrisRenderSystem.bindTextureToUnit(GL20C.GL_TEXTURE_2D, 2, lightmap.texture().iris$getGlId());
		GlStateManager._activeTexture(GL20C.GL_TEXTURE0 + IrisSamplers.LIGHTMAP_TEXTURE_UNIT);
	}

	private void applyBlendModes() {
		if (blendModeOverride != null) {
			blendModeOverride.apply();
		}
		bufferBlendOverrides.forEach(BufferBlendOverride::apply);
	}

	private void updateUniforms() {
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
		ImmediateState.usingTessellation = false;
	}
}
