package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.SodiumClientMod;
import net.caffeinemc.sodium.render.SodiumWorldRenderer;
import net.caffeinemc.sodium.render.chunk.RenderSectionManager;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.caffeinemc.sodium.render.chunk.passes.DefaultRenderPasses;
import net.caffeinemc.sodium.render.sequence.SequenceIndexBuffer;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexFormats;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
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

	@Unique
	private Map<ChunkRenderPass, ChunkRenderer> chunkRenderersShadow;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createShadow(RenderDevice device, SodiumWorldRenderer worldRenderer, ChunkRenderPassManager renderPassManager, ClientLevel world, int renderDistance, CallbackInfo ci) {
		this.chunkRenderersShadow = new Reference2ReferenceOpenHashMap<>();

		for (var renderPass : DefaultRenderPasses.ALL) {
			this.chunkRenderersShadow.put(renderPass, irisChunkRendererCreation(true, device, this.indexBuffer, createVertexType(), renderPass));
		}
	}

	@Redirect(method = "renderLayer", at = @At(value = "FIELD", target = "Lnet/caffeinemc/sodium/render/chunk/RenderSectionManager;chunkRenderers:Ljava/util/Map;"))
	private Map<ChunkRenderPass, ChunkRenderer> redirectShadowRenderers(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? chunkRenderersShadow : chunkRenderers;
	}

	@Inject(method = "destroy", at = @At("TAIL"))
	private void destroyShadow(CallbackInfo ci) {
		for (ChunkRenderer renderer : this.chunkRenderersShadow.values()) {
			renderer.delete();
		}

		this.chunkRenderersShadow.clear();
	}

	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	private static ChunkRenderer createChunkRenderer(RenderDevice device, SequenceIndexBuffer indexBuffer, TerrainVertexType vertexType, ChunkRenderPass pass) {
		return irisChunkRendererCreation(false, device, indexBuffer, vertexType, pass);
	}

	private static ChunkRenderer irisChunkRendererCreation(boolean isShadowPass, RenderDevice device, SequenceIndexBuffer indexBuffer, TerrainVertexType vertexType, ChunkRenderPass pass) {
		return IrisApi.getInstance().isShaderPackInUse() ? new IrisChunkRenderer(isShadowPass, device, indexBuffer, IrisModelVertexFormats.MODEL_VERTEX_XHFP, pass) : new DefaultChunkRenderer(device, indexBuffer, vertexType, pass);
	}

	/**
	 * @author
	 */
	@Overwrite(remap = false)
	private static TerrainVertexType createVertexType() {
		return IrisApi.getInstance().isShaderPackInUse() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : (SodiumClientMod.options().performance.useCompactVertexFormat ? TerrainVertexFormats.COMPACT : TerrainVertexFormats.STANDARD);
	}
}
