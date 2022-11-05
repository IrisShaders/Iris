package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.SodiumClientMod;
import net.caffeinemc.sodium.render.SodiumWorldRenderer;
import net.caffeinemc.sodium.render.chunk.TerrainRenderManager;
import net.caffeinemc.sodium.render.chunk.draw.ChunkCameraContext;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.MdbvChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.MdiChunkRenderer;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexFormats;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkRenderer;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.MdbvChunkRendererIris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.MdiChunkRendererIris;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TerrainRenderManager.class)
public class MixinRenderSectionManager {
	@Mutable
	@Shadow
	@Final
	private ChunkRenderer chunkRenderer;

	@Unique
	private IrisChunkProgramOverrides irisChunkProgramOverrides;

	@Unique
	private ChunkRenderPassManager manager;

	@Unique
	private int versionCounterForSodiumShaderReload = -1;

	@Unique
	private RenderDevice device;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createShadow(RenderDevice device, SodiumWorldRenderer worldRenderer, ChunkRenderPassManager renderPassManager, ClientLevel world, ChunkCameraContext camera, int chunkViewDistance, CallbackInfo ci) {
		this.device = device;

		this.irisChunkProgramOverrides = new IrisChunkProgramOverrides();

		this.chunkRenderer = irisChunkRendererCreation(device, camera, createVertexType(), renderPassManager);

		this.manager = renderPassManager;
	}

	@Inject(method = "renderLayer", at = @At("HEAD"), remap = false)
	private void renderLayerHead(ChunkRenderMatrices matrices, ChunkRenderPass renderPass, CallbackInfo ci) {
		if (versionCounterForSodiumShaderReload != Iris.getPipelineManager().getVersionCounterForSodiumShaderReload()) {
			versionCounterForSodiumShaderReload = Iris.getPipelineManager().getVersionCounterForSodiumShaderReload();
			irisChunkProgramOverrides.deleteShaders(device);

			if (chunkRenderer instanceof IrisChunkRenderer irisChunkRenderer) {
				irisChunkRenderer.deletePipeline();
				irisChunkRenderer.createPipelines(irisChunkProgramOverrides);
			}
		}
	}

	@Inject(method = "destroy", at = @At("TAIL"), remap = false)
	private void destroyShadow(CallbackInfo ci) {
		irisChunkProgramOverrides.deleteShaders(device);
	}

	/**
	 * @author IMS
	 */
	@Overwrite(remap = false)
	private static ChunkRenderer createChunkRenderer(RenderDevice device, ChunkCameraContext context, ChunkRenderPassManager renderPassManager, TerrainVertexType vertexType) {
		return null;
	}

	private ChunkRenderer irisChunkRendererCreation(RenderDevice device, ChunkCameraContext camera, TerrainVertexType vertexType, ChunkRenderPassManager renderPassManager) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			try {
				switch (SodiumClientMod.options().advanced.chunkRendererBackend) {
					case DEFAULT:
						return device.properties().preferences.directRendering ? new MdbvChunkRendererIris(irisChunkProgramOverrides, device, camera, renderPassManager, vertexType) : new MdiChunkRendererIris(irisChunkProgramOverrides, device, camera, renderPassManager, vertexType);
					case BASEVERTEX:
						return new MdbvChunkRendererIris(irisChunkProgramOverrides, device, camera, renderPassManager, vertexType);
					case INDIRECT:
						return new MdiChunkRendererIris(irisChunkProgramOverrides, device, camera, renderPassManager, vertexType);
					default:
						throw new IncompatibleClassChangeError();
				}
			} catch (RuntimeException e) {
				Iris.logger.fatal("Failed to load Sodium shader, falling back to vanilla rendering. See log for more details!", e);
				switch (SodiumClientMod.options().advanced.chunkRendererBackend) {
					case DEFAULT:
						return device.properties().preferences.directRendering ? new MdbvChunkRenderer(device, camera, renderPassManager, vertexType) : new MdiChunkRenderer(device, camera, renderPassManager, vertexType);
					case BASEVERTEX:
						return new MdbvChunkRenderer(device, camera, renderPassManager, vertexType);
					case INDIRECT:
						return new MdiChunkRenderer(device, camera, renderPassManager, vertexType);
					default:
						throw new IncompatibleClassChangeError();
				}
			}
		} else {
			switch (SodiumClientMod.options().advanced.chunkRendererBackend) {
				case DEFAULT:
					return device.properties().preferences.directRendering ? new MdbvChunkRenderer(device, camera, renderPassManager, vertexType) : new MdiChunkRenderer(device, camera, renderPassManager, vertexType);
				case BASEVERTEX:
					return new MdbvChunkRenderer(device, camera, renderPassManager, vertexType);
				case INDIRECT:
					return new MdiChunkRenderer(device, camera, renderPassManager, vertexType);
				default:
					throw new IncompatibleClassChangeError();
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
