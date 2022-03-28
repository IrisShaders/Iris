package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.gfx.api.device.commands.RenderCommandList;
import net.caffeinemc.gfx.api.pipeline.Pipeline;
import net.caffeinemc.gfx.api.pipeline.PipelineState;
import net.caffeinemc.gfx.api.texture.Sampler;
import net.caffeinemc.sodium.render.chunk.draw.ChunkCameraContext;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderList;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.ShaderChunkRenderer;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.region.RenderRegion;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.texunits.TextureUnit;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinRegionChunkRenderer extends ShaderChunkRenderer implements ShaderChunkRendererExt {
	public MixinRegionChunkRenderer(RenderDevice device, TerrainVertexType vertexType) {
		super(device, vertexType);
	}

	@Inject(method = "lambda$render$0", at = @At("HEAD"))
	private void setup(ChunkRenderPass pass, ChunkRenderMatrices matrices, ChunkRenderList list, ChunkCameraContext camera, RenderCommandList commandList, ChunkShaderInterface chunkShaderInterface, PipelineState state, CallbackInfo ci) {
		if (chunkShaderInterface instanceof IrisChunkShaderInterface programInterface2) {
			programInterface2.setup();
		}
	}

	@Redirect(method = "bindTextures", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/gfx/api/pipeline/PipelineState;bindTexture(IILnet/caffeinemc/gfx/api/texture/Sampler;)V"))
	private void redirectLightMap(PipelineState instance, int i, int j, Sampler sampler) {
		if (i == 1 && IrisApi.getInstance().isShaderPackInUse()) {
			i = TextureUnit.LIGHTMAP.getSamplerId();
		}
		instance.bindTexture(i, j, sampler);
	}

	@Inject(method = "lambda$render$0", at = @At("TAIL"))
	private void end(ChunkRenderPass pass, ChunkRenderMatrices matrices, ChunkRenderList list, ChunkCameraContext camera, RenderCommandList commandList, ChunkShaderInterface chunkShaderInterface, PipelineState state, CallbackInfo ci) {
		if (chunkShaderInterface instanceof IrisChunkShaderInterface programInterface2) {
			programInterface2.restore();
		}
	}
}
