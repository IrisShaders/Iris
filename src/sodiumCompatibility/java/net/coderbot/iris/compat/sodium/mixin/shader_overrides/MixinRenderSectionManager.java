package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.SodiumClientMod;
import net.caffeinemc.sodium.render.SodiumWorldRenderer;
import net.caffeinemc.sodium.render.chunk.TerrainRenderManager;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.MdiChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.MdiCountChunkRenderer;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexFormats;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkRenderer;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkRendererMDI;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkRendererMDIC;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;

@Mixin(TerrainRenderManager.class)
public class MixinRenderSectionManager {
	@Shadow
	@Final
	private RenderDevice device;
	@Mutable
	@Shadow
	@Final
	private ChunkRenderer chunkRenderer;
	@Unique
	private ChunkRenderer chunkRendererShadow;

	@Unique
	private IrisChunkProgramOverrides irisChunkProgramOverrides;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createShadow(RenderDevice device, SodiumWorldRenderer worldRenderer, ChunkRenderPassManager renderPassManager, ClientLevel world, int renderDistance, CallbackInfo ci) {
		this.irisChunkProgramOverrides = new IrisChunkProgramOverrides();

		this.chunkRenderer = irisChunkRendererCreation(false, device, createVertexType(), renderPassManager);


		if (Iris.getPipelineManager().getPipeline().isPresent() && Iris.getPipelineManager().getPipelineNullable().getSodiumTerrainPipeline() != null && Iris.getPipelineManager().getPipelineNullable().getSodiumTerrainPipeline().hasShadowPass()) {
			this.chunkRendererShadow = irisChunkRendererCreation(true, device, createVertexType(), renderPassManager);
		} else {
			this.chunkRendererShadow = null;
		}
	}

	@Redirect(method = "renderLayer", at = @At(value = "FIELD", target = "Lnet/caffeinemc/sodium/render/chunk/TerrainRenderManager;chunkRenderer:Lnet/caffeinemc/sodium/render/chunk/draw/ChunkRenderer;"))
	private ChunkRenderer redirectShadowRenderers(TerrainRenderManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? chunkRendererShadow : chunkRenderer;
	}

	@Redirect(method = "update", at = @At(value = "FIELD", target = "Lnet/caffeinemc/sodium/render/chunk/TerrainRenderManager;chunkRenderer:Lnet/caffeinemc/sodium/render/chunk/draw/ChunkRenderer;"))
	private ChunkRenderer redirectShadowRenderers2(TerrainRenderManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? chunkRendererShadow : chunkRenderer;
	}

	@Inject(method = "renderLayer", at = @At("HEAD"))
	private void renderLayerHead(ChunkRenderMatrices matrices, ChunkRenderPass renderPass, CallbackInfo ci) {
		if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
			if (chunkRenderer instanceof IrisChunkRenderer irisChunkRenderer) {
				irisChunkRenderer.deletePipeline(irisChunkProgramOverrides);
			}
			if (chunkRendererShadow instanceof IrisChunkRenderer irisChunkRenderer) {
				irisChunkRenderer.deletePipeline(irisChunkProgramOverrides);
			}
			Iris.getPipelineManager().clearSodiumShaderReloadNeeded();
		}
	}
	@Inject(method = "destroy", at = @At("TAIL"))
	private void destroyShadow(CallbackInfo ci) {
		if (chunkRendererShadow != null) {
			chunkRendererShadow.delete();
		}
		irisChunkProgramOverrides.deleteShaders(device);
	}

	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	private static ChunkRenderer createChunkRenderer(RenderDevice device, ChunkRenderPassManager renderPassManager, TerrainVertexType vertexType) {
		return null;
	}

	private ChunkRenderer irisChunkRendererCreation(boolean isShadowPass, RenderDevice device, TerrainVertexType vertexType, ChunkRenderPassManager renderPassManager) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			if (device.properties().driverWorkarounds.forceIndirectCount) {
				return new IrisChunkRendererMDIC(irisChunkProgramOverrides, isShadowPass, device, renderPassManager, vertexType);
			} else {
				return new IrisChunkRendererMDI(irisChunkProgramOverrides, isShadowPass, device, renderPassManager, vertexType);
			}
		} else {
			if (device.properties().driverWorkarounds.forceIndirectCount) {
				return new MdiCountChunkRenderer(device, renderPassManager, vertexType);
			} else {
				return new MdiChunkRenderer(device, renderPassManager, vertexType);
			}
		}
	}

	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	private static TerrainVertexType createVertexType() {
		return IrisApi.getInstance().isShaderPackInUse() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : (SodiumClientMod.options().performance.useCompactVertexFormat ? TerrainVertexFormats.COMPACT : TerrainVertexFormats.STANDARD);
	}
}
