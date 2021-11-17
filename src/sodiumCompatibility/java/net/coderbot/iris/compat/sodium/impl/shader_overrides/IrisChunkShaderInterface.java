package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.texunits.TextureUnit;
import org.jetbrains.annotations.Nullable;

public class IrisChunkShaderInterface {
	@Nullable
	private final GlUniformFloat uniformModelScale;
	@Nullable
	private final GlUniformFloat uniformModelOffset;
	@Nullable
	private final GlUniformFloat uniformTextureScale;
	@Nullable
	private final GlUniformMatrix4f uniformModelViewMatrix;
	@Nullable
	private final GlUniformMatrix4f uniformProjectionMatrix;
	@Nullable
	private final GlUniformMatrix4f uniformModelViewProjectionMatrix;
	@Nullable
	private final GlUniformMatrix4f uniformNormalMatrix;
	@Nullable
	private final GlUniformBlock uniformBlockDrawParameters;

	private final IrisShaderFogComponent fogShaderComponent;
	private final ProgramUniforms irisProgramUniforms;
	private final ProgramSamplers irisProgramSamplers;

	public IrisChunkShaderInterface(int handle, ShaderBindingContextExt contextExt, SodiumTerrainPipeline pipeline,
									boolean isShadowPass) {
		this.uniformModelViewMatrix = contextExt.bindUniformIfPresent("u_ModelViewMatrix", GlUniformMatrix4f::new);
		this.uniformProjectionMatrix = contextExt.bindUniformIfPresent("u_ProjectionMatrix", GlUniformMatrix4f::new);
		this.uniformModelViewProjectionMatrix = contextExt.bindUniformIfPresent("u_ModelViewProjectionMatrix", GlUniformMatrix4f::new);
		this.uniformNormalMatrix = contextExt.bindUniformIfPresent("u_NormalMatrix", GlUniformMatrix4f::new);
		this.uniformModelScale = contextExt.bindUniformIfPresent("u_ModelScale", GlUniformFloat::new);
		this.uniformModelOffset = contextExt.bindUniformIfPresent("u_ModelOffset", GlUniformFloat::new);
		this.uniformTextureScale = contextExt.bindUniformIfPresent("u_TextureScale", GlUniformFloat::new);
		this.uniformBlockDrawParameters = contextExt.bindUniformBlockIfPresent("ubo_DrawParameters", 0);

		this.fogShaderComponent = new IrisShaderFogComponent(contextExt);

		this.irisProgramUniforms = pipeline.initUniforms(handle);
		this.irisProgramSamplers
				= isShadowPass? pipeline.initShadowSamplers(handle) : pipeline.initTerrainSamplers(handle);
	}

	public void setup(ChunkVertexType vertexType) {
		// See IrisSamplers#addLevelSamplers
		RenderSystem.activeTexture(TextureUnit.TERRAIN.getUnitId());
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(0));
		RenderSystem.activeTexture(TextureUnit.LIGHTMAP.getUnitId());
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));

		if (this.uniformModelScale != null) {
			this.uniformModelScale.setFloat(vertexType.getModelScale());
		}

		if (this.uniformModelOffset != null) {
			this.uniformModelOffset.setFloat(vertexType.getModelOffset());
		}

		if (this.uniformTextureScale != null) {
			this.uniformTextureScale.setFloat(vertexType.getTextureScale());
		}

		fogShaderComponent.setup();
		irisProgramUniforms.update();
		irisProgramSamplers.update();
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

		if (this.uniformModelViewProjectionMatrix != null) {
			Matrix4f modelViewProjection = RenderSystem.getProjectionMatrix().copy();
			modelViewProjection.multiply(modelView);
			this.uniformModelViewProjectionMatrix.set(modelViewProjection);
		}

		if (this.uniformNormalMatrix != null) {
			Matrix4f normalMatrix = modelView.copy();
			normalMatrix.invert();
			normalMatrix.transpose();
			this.uniformNormalMatrix.set(normalMatrix);
		}
	}

	public void setDrawUniforms(GlMutableBuffer buffer) {
		if (this.uniformBlockDrawParameters != null) {
			this.uniformBlockDrawParameters.bindBuffer(buffer);
		}
	}
}
