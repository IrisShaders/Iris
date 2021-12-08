package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat3v;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.texunits.TextureUnit;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class IrisChunkShaderInterface {
	@Nullable
	private final GlUniformMatrix4f uniformModelViewMatrix;
	@Nullable
	private final GlUniformMatrix4f uniformProjectionMatrix;
	@Nullable
	private final GlUniformFloat3v uniformRegionOffset;
	@Nullable
	private final GlUniformMatrix4f uniformNormalMatrix;
	@Nullable
	private final GlUniformBlock uniformBlockDrawParameters;

	private final BlendModeOverride blendModeOverride;
	private final IrisShaderFogComponent fogShaderComponent;
	private final ProgramUniforms irisProgramUniforms;
	private final ProgramSamplers irisProgramSamplers;

	public IrisChunkShaderInterface(int handle, ShaderBindingContextExt contextExt, SodiumTerrainPipeline pipeline,
									boolean isShadowPass, BlendModeOverride blendModeOverride) {
		this.uniformModelViewMatrix = contextExt.bindUniformIfPresent("u_ModelViewMatrix", GlUniformMatrix4f::new);
		this.uniformProjectionMatrix = contextExt.bindUniformIfPresent("u_ProjectionMatrix", GlUniformMatrix4f::new);
		this.uniformRegionOffset = contextExt.bindUniformIfPresent("u_RegionOffset", GlUniformFloat3v::new);
		this.uniformNormalMatrix = contextExt.bindUniformIfPresent("u_NormalMatrix", GlUniformMatrix4f::new);
		this.uniformBlockDrawParameters = contextExt.bindUniformBlockIfPresent("ubo_DrawParameters", 0);

		this.blendModeOverride = blendModeOverride;
		this.fogShaderComponent = new IrisShaderFogComponent(contextExt);

		this.irisProgramUniforms = pipeline.initUniforms(handle);
		this.irisProgramSamplers
				= isShadowPass? pipeline.initShadowSamplers(handle) : pipeline.initTerrainSamplers(handle);
	}

	public void setup() {
		// See IrisSamplers#addLevelSamplers
		RenderSystem.activeTexture(TextureUnit.TERRAIN.getUnitId());
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(0));
		RenderSystem.activeTexture(TextureUnit.LIGHTMAP.getUnitId());
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));

		if (blendModeOverride != null) {
			blendModeOverride.apply();
		}

		fogShaderComponent.setup();
		irisProgramUniforms.update();
		irisProgramSamplers.update();
	}

	public void restore() {
		if (blendModeOverride != null) {
			BlendModeOverride.restore();
		}
	}

	public void setProjectionMatrix(Matrix4f matrix) {
		if (this.uniformProjectionMatrix != null) {
			this.uniformProjectionMatrix.set(matrix);
		}
	}

	public void setModelViewMatrix(Matrix4f modelView) {
		if (this.uniformModelViewMatrix != null) {
			this.uniformModelViewMatrix.set(modelView);
		}

		if (this.uniformNormalMatrix != null) {
			Matrix4f normalMatrix = new Matrix4f(modelView);
			normalMatrix.invert();
			normalMatrix.transpose();
			this.uniformNormalMatrix.set(normalMatrix);
		}
	}

	public void setRegionOffset(float x, float y, float z) {
		this.uniformRegionOffset.set(x, y, z);
	}

	public void setDrawUniforms(GlMutableBuffer buffer) {
		if (this.uniformBlockDrawParameters != null) {
			this.uniformBlockDrawParameters.bindBuffer(buffer);
		}
	}
}
