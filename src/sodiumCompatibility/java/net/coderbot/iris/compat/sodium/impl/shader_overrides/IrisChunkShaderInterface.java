package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.gfx.api.shader.BufferBlock;
import net.caffeinemc.gfx.api.shader.ShaderBindingContext;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.texunits.TextureUnit;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class IrisChunkShaderInterface {
	public final BufferBlock uniformCameraMatrices;
	public final BufferBlock uniformFogParameters;
	public final BufferBlock uniformInstanceData;

	private BlendModeOverride blendModeOverride;
	private ProgramUniforms irisProgramUniforms;
	private ProgramSamplers irisProgramSamplers;
	private ProgramImages irisProgramImages;

	public IrisChunkShaderInterface(ShaderBindingContext context) {
		this.uniformCameraMatrices = context.bindUniformBlock("ubo_CameraMatrices", 0);
		this.uniformFogParameters = context.bindUniformBlock("ubo_FogParameters", 1);
		this.uniformInstanceData = context.bindUniformBlock("ubo_InstanceData", 2);
	}

	public void setInfo(boolean isShadowPass, SodiumTerrainPipeline pipeline, int handle, BlendModeOverride blendModeOverride) {
		this.blendModeOverride = blendModeOverride;

		this.irisProgramUniforms = pipeline.initUniforms(handle);
		this.irisProgramSamplers
			= isShadowPass? pipeline.initShadowSamplers(handle) : pipeline.initTerrainSamplers(handle);
		this.irisProgramImages = isShadowPass ? pipeline.initShadowImages(handle) : pipeline.initTerrainImages(handle);
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

		irisProgramUniforms.update();
		irisProgramSamplers.update();
		irisProgramImages.update();
	}

	public void restore() {
		if (blendModeOverride != null) {
			BlendModeOverride.restore();
		}
	}
}
