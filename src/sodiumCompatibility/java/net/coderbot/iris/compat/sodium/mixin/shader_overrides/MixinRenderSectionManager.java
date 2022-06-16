package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.SodiumClientMod;
import net.caffeinemc.sodium.render.SodiumWorldRenderer;
import net.caffeinemc.sodium.render.buffer.StreamingBuffer;
import net.caffeinemc.sodium.render.chunk.RenderSectionManager;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.RenderListBuilder;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.caffeinemc.sodium.render.chunk.passes.DefaultRenderPasses;
import net.caffeinemc.sodium.render.sequence.SequenceIndexBuffer;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexFormats;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkRenderer;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@Shadow
	@Final
	private SequenceIndexBuffer indexBuffer;

	@Shadow
	@Final
	private Map<ChunkRenderPass, ChunkRenderer> chunkRenderers;

	@Shadow
	@Final
	private RenderDevice device;
	@Shadow
	@Final
	private RenderListBuilder renderListBuilder;
	@Unique
	private Map<ChunkRenderPass, ChunkRenderer> chunkRenderersShadow;

	@Unique
	private IrisChunkProgramOverrides irisChunkProgramOverrides;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createShadow(RenderDevice device, SodiumWorldRenderer worldRenderer, ChunkRenderPassManager renderPassManager, ClientLevel world, int renderDistance, CallbackInfo ci) {
		this.chunkRenderersShadow = new Reference2ReferenceOpenHashMap<>();

		this.irisChunkProgramOverrides = new IrisChunkProgramOverrides();

		for (var renderPass : DefaultRenderPasses.ALL) {
			this.chunkRenderers.put(renderPass, irisChunkRendererCreation(false, device, this.renderListBuilder.getInstanceBuffer(), this.renderListBuilder.getCommandBuffer(), this.indexBuffer, createVertexType(), renderPass));
		}

		for (var renderPass : DefaultRenderPasses.ALL) {
			this.chunkRenderersShadow.put(renderPass, irisChunkRendererCreation(true, device, this.renderListBuilder.getInstanceBuffer(), this.renderListBuilder.getCommandBuffer(), this.indexBuffer, createVertexType(), renderPass));
		}
	}

	@Redirect(method = "renderLayer", at = @At(value = "FIELD", target = "Lnet/caffeinemc/sodium/render/chunk/RenderSectionManager;chunkRenderers:Ljava/util/Map;"))
	private Map<ChunkRenderPass, ChunkRenderer> redirectShadowRenderers(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? chunkRenderersShadow : chunkRenderers;
	}

	@Inject(method = "renderLayer", at = @At("HEAD"))
	private void renderLayerHead(ChunkRenderMatrices matrices, ChunkRenderPass renderPass, CallbackInfo ci) {
		if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
			irisChunkProgramOverrides.deleteShaders(device);
			for (ChunkRenderer renderer : chunkRenderers.values()) {
				if (renderer instanceof IrisChunkRenderer irisChunkRenderer) {
					irisChunkRenderer.deletePipeline(irisChunkProgramOverrides);
				}
			}
			for (ChunkRenderer renderer : chunkRenderersShadow.values()) {
				if (renderer instanceof IrisChunkRenderer irisChunkRenderer) {
					irisChunkRenderer.deletePipeline(irisChunkProgramOverrides);
				}
			}
			Iris.getPipelineManager().clearSodiumShaderReloadNeeded();
		}
	}
	@Inject(method = "destroy", at = @At("TAIL"))
	private void destroyShadow(CallbackInfo ci) {
		for (ChunkRenderer renderer : this.chunkRenderersShadow.values()) {
			renderer.delete();
		}

		this.chunkRenderersShadow.clear();

		irisChunkProgramOverrides.deleteShaders(device);
	}

	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	private static ChunkRenderer createChunkRenderer(RenderDevice device, StreamingBuffer instanceBuffer, StreamingBuffer commandBuffer, SequenceIndexBuffer indexBuffer, TerrainVertexType vertexType, ChunkRenderPass pass) {
		return null;
	}

	private ChunkRenderer irisChunkRendererCreation(boolean isShadowPass, RenderDevice device, StreamingBuffer instanceBuffer, StreamingBuffer commandBuffer, SequenceIndexBuffer indexBuffer, TerrainVertexType vertexType, ChunkRenderPass pass) {
		return IrisApi.getInstance().isShaderPackInUse() ? new IrisChunkRenderer(irisChunkProgramOverrides, isShadowPass, device, instanceBuffer, commandBuffer, indexBuffer, IrisModelVertexFormats.MODEL_VERTEX_XHFP, pass) : new DefaultChunkRenderer(device, instanceBuffer, commandBuffer, indexBuffer, vertexType, pass);
	}

	/**
	 * @author
	 */
	@Overwrite(remap = false)
	private static TerrainVertexType createVertexType() {
		return IrisApi.getInstance().isShaderPackInUse() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : (SodiumClientMod.options().performance.useCompactVertexFormat ? TerrainVertexFormats.COMPACT : TerrainVertexFormats.STANDARD);
	}
}
