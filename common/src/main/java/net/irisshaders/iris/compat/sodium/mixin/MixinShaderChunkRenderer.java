package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShaderChunkRenderer.class, remap = false)
public abstract class MixinShaderChunkRenderer {
	@Shadow
	protected abstract GlProgram<ChunkShaderInterface> compileProgram(ChunkShaderOptions options);

	@Inject(method = "begin", at = @At("HEAD"))
	private void iris$resetState(TerrainRenderPass pass, FogParameters parameters, CallbackInfo ci) {
		BlendModeOverride.restore();
	}

	@Redirect(method = "begin", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glBindFramebuffer(II)V"))
	private void bindFramebufferLater(int p_412624_, int p_412635_) {}

	@Redirect(method = "begin", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/ShaderChunkRenderer;compileProgram(Lnet/caffeinemc/mods/sodium/client/render/chunk/shader/ChunkShaderOptions;)Lnet/caffeinemc/mods/sodium/client/gl/shader/GlProgram;"))
	private GlProgram<ChunkShaderInterface> redirectIrisProgram(ShaderChunkRenderer instance, ChunkShaderOptions options, TerrainRenderPass pass, @Local RenderTarget target) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		GlProgram<ChunkShaderInterface> program = null;

		if (pipeline instanceof IrisRenderingPipeline irisRenderingPipeline) {
			irisRenderingPipeline.getSodiumPrograms().getFramebuffer(pass).bind();
			program = irisRenderingPipeline.getSodiumPrograms().getProgram(pass);
		}

		if (program == null) {
			GlStateManager._glBindFramebuffer(36160, ((GlTexture)target.getColorTexture()).getFbo(((GlDevice) RenderSystem.getDevice()).directStateAccess(), target.getDepthTexture()));
			return this.compileProgram(options);
		}

		return program;
	}

	@Redirect(method = "begin", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"))
	private void redirectViewport(int i, int j, int k, int l) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			GlStateManager._viewport(i, j, k, l);
		}
	}
}
