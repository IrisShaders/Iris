package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat3v;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderTextureSlot;
import me.jellysquid.mods.sodium.client.util.TextureUtil;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;

import java.util.EnumMap;
import java.util.List;

public class IrisChunkShaderInterface {
	@Nullable
	private final GlUniformMatrix4f uniformModelViewMatrix;
	@Nullable
	private final GlUniformMatrix4f uniformModelViewMatrixInverse;
	@Nullable
	private final GlUniformMatrix4f uniformProjectionMatrix;
	@Nullable
	private final GlUniformMatrix4f uniformProjectionMatrixInverse;
	@Nullable
	private final GlUniformFloat3v uniformRegionOffset;
	@Nullable
	private final GlUniformMatrix4f uniformNormalMatrix;

	private final BlendModeOverride blendModeOverride;
	private final IrisShaderFogComponent fogShaderComponent;
	private final float alpha;
	private final ProgramUniforms irisProgramUniforms;
	private final ProgramSamplers irisProgramSamplers;
	private final ProgramImages irisProgramImages;
	private final List<BufferBlendOverride> bufferBlendOverrides;
	private final boolean hasOverrides;
	private CustomUniforms customUniforms;

	public IrisChunkShaderInterface(int handle, ShaderBindingContextExt contextExt, SodiumTerrainPipeline pipeline,
									boolean isShadowPass, BlendModeOverride blendModeOverride, List<BufferBlendOverride> bufferOverrides, float alpha, CustomUniforms customUniforms) {
		this.uniformModelViewMatrix = contextExt.bindUniformIfPresent("iris_ModelViewMatrix", GlUniformMatrix4f::new);
		this.uniformModelViewMatrixInverse = contextExt.bindUniformIfPresent("iris_ModelViewMatrixInverse", GlUniformMatrix4f::new);
		this.uniformProjectionMatrix = contextExt.bindUniformIfPresent("iris_ProjectionMatrix", GlUniformMatrix4f::new);
		this.uniformProjectionMatrixInverse = contextExt.bindUniformIfPresent("iris_ProjectionMatrixInverse", GlUniformMatrix4f::new);
		this.uniformRegionOffset = contextExt.bindUniformIfPresent("u_RegionOffset", GlUniformFloat3v::new);
		this.uniformNormalMatrix = contextExt.bindUniformIfPresent("iris_NormalMatrix", GlUniformMatrix4f::new);
		this.customUniforms = customUniforms;

		this.alpha = alpha;

		this.blendModeOverride = blendModeOverride;
		this.bufferBlendOverrides = bufferOverrides;
		this.hasOverrides = bufferBlendOverrides != null && !bufferBlendOverrides.isEmpty();
		this.fogShaderComponent = new IrisShaderFogComponent(contextExt);

		ProgramUniforms.Builder builder = pipeline.initUniforms(handle);
		customUniforms.mapholderToPass(builder, this);
		this.irisProgramUniforms = builder.buildUniforms();
		this.irisProgramSamplers
				= isShadowPass? pipeline.initShadowSamplers(handle) : pipeline.initTerrainSamplers(handle);
		this.irisProgramImages = isShadowPass ? pipeline.initShadowImages(handle) : pipeline.initTerrainImages(handle);
	}

	public void startDrawing() {
		IrisRenderSystem.bindTextureToUnit(GL30C.GL_TEXTURE_2D, 0, RenderSystem.getShaderTexture(0));
		RenderSystem.activeTexture(GL30C.GL_TEXTURE2);
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));


		if (blendModeOverride != null) {
			blendModeOverride.apply();
		}

		if (hasOverrides) {
			bufferBlendOverrides.forEach(BufferBlendOverride::apply);
		}

		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alpha);

		fogShaderComponent.setup();
		irisProgramUniforms.update();
		irisProgramSamplers.update();
		irisProgramImages.update();

		customUniforms.push(this);
	}

	public void restore() {
		if (blendModeOverride != null || hasOverrides) {
			BlendModeOverride.restore();
		}
	}

	public void setProjectionMatrix(Matrix4f matrix) {
		if (this.uniformProjectionMatrix != null) {
			this.uniformProjectionMatrix.set(matrix);
		}

		if (this.uniformProjectionMatrixInverse != null) {
			Matrix4f inverted = new Matrix4f(matrix);
			inverted.invert();
			this.uniformProjectionMatrixInverse.set(inverted);
		}
	}

	@Deprecated // the shader interface should not modify pipeline state
	public void endDrawing() {
	}

	private void bindTexture(ChunkShaderTextureSlot slot, int textureId) {
		IrisRenderSystem.bindTextureToUnit(GL33C.GL_TEXTURE_2D, slot.ordinal(), textureId);
	}

	private static int getTextureId(ChunkShaderTextureSlot slot) {
		return switch (slot) {
			case BLOCK -> TextureUtil.getBlockTextureId();
			case LIGHT -> TextureUtil.getLightTextureId();
		};
	}

	public void setModelViewMatrix(Matrix4f modelView) {
		if (this.uniformModelViewMatrix != null) {
			this.uniformModelViewMatrix.set(modelView);
		}

		if (this.uniformModelViewMatrixInverse != null) {
			Matrix4f invertedMatrix = new Matrix4f(modelView);
			invertedMatrix.invert();
			this.uniformModelViewMatrixInverse.set(invertedMatrix);
			if (this.uniformNormalMatrix != null) {
				invertedMatrix.transpose();
				this.uniformNormalMatrix.set(invertedMatrix);
			}
		} else if (this.uniformNormalMatrix != null) {
			Matrix4f normalMatrix = new Matrix4f(modelView);
			normalMatrix.invert();
			normalMatrix.transpose();
			this.uniformNormalMatrix.set(normalMatrix);
		}
	}

	public void setRegionOffset(float x, float y, float z) {
		if (this.uniformRegionOffset != null) {
			this.uniformRegionOffset.set(x, y, z);
		}
	}
}
