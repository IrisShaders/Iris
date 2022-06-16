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
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32C;

public class IrisChunkShaderInterface extends ChunkShaderInterface {
	private BlendModeOverride blendModeOverride;
	private ProgramUniforms irisProgramUniforms;
	private ProgramSamplers irisProgramSamplers;
	private ProgramImages irisProgramImages;
	private IrisTerrainPass pass;
	private int handle;
	private float alpha;

	public IrisChunkShaderInterface(ShaderBindingContext context) {
		super(context);
	}

	public void setInfo(boolean isShadowPass, SodiumTerrainPipeline pipeline, int handle, IrisTerrainPass pass, BlendModeOverride blendModeOverride, float alpha) {
		this.blendModeOverride = blendModeOverride;
		this.pass = pass;
		this.handle = handle;
		this.alpha = alpha;
		this.irisProgramUniforms = pipeline.initUniforms(handle);
		this.irisProgramSamplers
			= isShadowPass? pipeline.initShadowSamplers(handle) : pipeline.initTerrainSamplers(handle);
		this.irisProgramImages = isShadowPass ? pipeline.initShadowImages(handle) : pipeline.initTerrainImages(handle);
	}

	public void setup() {
		if (Iris.getPipelineManager().getPipelineNullable() != null) {
			GlStateManager._glUseProgram(handle);
			GlFramebuffer framebuffer = getFramebuffer(pass);
			if (framebuffer != null) {
				framebuffer.bind();
			}

			if (blendModeOverride != null) {
				blendModeOverride.apply();
			}

			CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alpha);

			irisProgramUniforms.update();
			irisProgramSamplers.update();
			irisProgramImages.update();
		}
	}

	public void restore() {
		if (blendModeOverride != null) {
			BlendModeOverride.restore();
		}
		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	public GlFramebuffer getFramebuffer(IrisTerrainPass pass) {
		SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();

		if (pipeline != null) {
			GlFramebuffer framebuffer;

			if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT || ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
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
