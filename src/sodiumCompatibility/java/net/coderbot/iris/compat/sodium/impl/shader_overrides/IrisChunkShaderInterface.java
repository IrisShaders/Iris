package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.gfx.api.shader.BufferBlock;
import net.caffeinemc.gfx.api.shader.ShaderBindingContext;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.texunits.TextureUnit;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class IrisChunkShaderInterface extends ChunkShaderInterface {
	private BlendModeOverride blendModeOverride;
	private ProgramUniforms irisProgramUniforms;
	private ProgramSamplers irisProgramSamplers;
	private ProgramImages irisProgramImages;
	private IrisTerrainPass pass;
	private int handle;

	public IrisChunkShaderInterface(ShaderBindingContext context) {
		super(context);
	}

	public void setInfo(boolean isShadowPass, SodiumTerrainPipeline pipeline, int handle, IrisTerrainPass pass, BlendModeOverride blendModeOverride) {
		this.blendModeOverride = blendModeOverride;

		this.pass = pass;
		this.handle = handle;
		this.irisProgramUniforms = pipeline.initUniforms(handle);
		this.irisProgramSamplers
			= isShadowPass? pipeline.initShadowSamplers(handle) : pipeline.initTerrainSamplers(handle);
		this.irisProgramImages = isShadowPass ? pipeline.initShadowImages(handle) : pipeline.initTerrainImages(handle);
	}

	public void setup() {
		GlStateManager._glUseProgram(handle);
		getFramebuffer(pass).bind();

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
		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	public GlFramebuffer getFramebuffer(IrisTerrainPass pass) {
		SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();
		boolean isShadowPass = ShadowRenderingState.areShadowsCurrentlyBeingRendered();

		if (pipeline != null) {
			GlFramebuffer framebuffer;

			if (isShadowPass) {
				framebuffer = pipeline.getShadowFramebuffer();
			} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
				framebuffer = pipeline.getTranslucentFramebuffer();
			} else {
				framebuffer = pipeline.getTerrainFramebuffer();
			}

			return framebuffer;
		}

		return null;
	}

	private SodiumTerrainPipeline getSodiumTerrainPipeline() {
		WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();

		if (worldRenderingPipeline != null) {
			return worldRenderingPipeline.getSodiumTerrainPipeline();
		} else {
			return null;
		}
	}
}
