package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlTexelBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat3v;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformInt;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformUnsignedInt;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderTextureSlot;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
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
import net.minecraft.client.TextureFilteringMethod;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL46C;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class SodiumShader implements ChunkShaderInterface {
	private static final int SUB_TEXEL_PRECISION_BITS = 5;

	private final ProgramImages images;
	private final ProgramSamplers samplers;
	private final ProgramUniforms uniforms;
	private final CustomUniforms customUniforms;
	private final BlendModeOverride blendModeOverride;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final float alphaTest;
	private final boolean containsTessellation;
	private final boolean anisotropySupported;
	private final GlUniformInt sectionDataUniform;
	private boolean isShadowPass;
	private final GlUniformBlock uniformGlobals;
	private final GlUniformFloat3v regionUniform;
	private final GlUniformInt timeUniform;
	private final GlUniformUnsignedInt idUniform;

	public SodiumShader(IrisRenderingPipeline pipeline, SodiumPrograms.Pass pass, ShaderBindingContext context,
						int handle, BlendModeOverride blendModeOverride,
						List<BufferBlendOverride> bufferBlendOverrides,
						CustomUniforms customUniforms, Supplier<ImmutableSet<Integer>> flipState, float alphaTest,
						boolean containsTessellation) {
		this.anisotropySupported = pipeline.hasFeature(FeatureFlags.TEXTURE_FILTERING);

		this.regionUniform = context.bindUniformOptional("u_RegionOffset", GlUniformFloat3v::new);
		this.timeUniform = context.bindUniformOptional("u_CurrentTime", GlUniformInt::new);
		this.idUniform = context.bindUniformOptional("u_RegionID", GlUniformUnsignedInt::new);

		this.sectionDataUniform = context.bindUniformOptional("u_SectionTimeInfo", GlUniformInt::new);

		this.uniformGlobals = context.bindUniformBlock("u_Globals", 0);

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
	public void setupState(TerrainRenderPass pass, FogParameters fogParameters, GpuSampler gpuSampler, GpuBufferSlice uniformData, GlTexelBuffer glTexelBuffer) {
		int maxAnisotropy = Minecraft.getInstance().options.textureFiltering().get() == TextureFilteringMethod.ANISOTROPIC
			? Minecraft.getInstance().options.maxAnisotropyValue()
			: 1;
		bindTextures(pass.getAtlas(), (GlSampler) IrisSamplers.getTerrainCache(maxAnisotropy), glTexelBuffer); // oh no

		if (sectionDataUniform != null) {
			GlStateManager._activeTexture(GL32C.GL_TEXTURE0 + ChunkShaderTextureSlot.SECTION.ordinal());
			GL46C.glBindTexture(GL46C.GL_TEXTURE_BUFFER, glTexelBuffer.handle());
			sectionDataUniform.setInt(ChunkShaderTextureSlot.SECTION.ordinal());
		}

		this.uniformGlobals.bindBufferRange(uniformData);
		DepthColorStorage.unlockDepthColor();

		applyBlendModes();
		if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline irp) {
			irp.onSetAlbedoTex(pass.getAtlas());
		}
		updateUniforms();
		images.update();

		if (containsTessellation) {
			ImmediateState.usingTessellation = true;
		}

		if (ShadowRenderer.ACTIVE) {
			GlStateManager._disableCull();
			GlStateManager._viewport(0, 0, ShadowRenderer.RESOLUTION, ShadowRenderer.RESOLUTION);
		}
	}

	private void bindTextures(GpuTextureView atlas, GlSampler sampler, GlTexelBuffer glTexelBuffer) {
		IrisRenderSystem.bindTextureToUnit(GL20C.GL_TEXTURE_2D, 0, atlas.texture().iris$getGlId());
		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);
		GlStateManager._texParameter(3553, 33084, atlas.baseMipLevel());
		GlStateManager._texParameter(3553, 33085, atlas.baseMipLevel() + atlas.mipLevels() - 1);
		GL33C.glBindSampler(0, sampler.getId());

		GpuTextureView lightmap = Minecraft.getInstance().gameRenderer.lightmap();
		GL33C.glBindSampler(2, ((GlSampler) RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false)).getId());

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

	private static float getCameraTranslation(int chunkBlockPos, int cameraBlockPos, float cameraPos) {
		return (chunkBlockPos - cameraBlockPos) - cameraPos;
	}

	@Override
	public void setRegionData(CameraTransform camera, RenderRegion region) {
		float x = getCameraTranslation(region.getOriginX(), camera.intX, camera.fracX);
		float y = getCameraTranslation(region.getOriginY(), camera.intY, camera.fracY);
		float z = getCameraTranslation(region.getOriginZ(), camera.intZ, camera.fracZ);

		if (this.regionUniform != null) this.regionUniform.set(x, y, z);
		if (this.timeUniform != null) this.timeUniform.set(Math.toIntExact(System.currentTimeMillis() - region.getCreationTime()));
		if (this.idUniform != null) this.idUniform.set(region.getId());
	}
}
