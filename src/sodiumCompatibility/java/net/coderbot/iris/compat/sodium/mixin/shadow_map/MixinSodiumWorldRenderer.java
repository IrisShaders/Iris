package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shadow_map.SwappableRenderSectionManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

/**
 * Ensures that the state of the chunk render visibility graph gets properly swapped when in the shadow map pass,
 * because we must maintain one visibility graph for the shadow camera and one visibility graph for the player camera.
 *
 * Also ensures that the visibility graph is always rebuilt in the shadow pass, since the shadow camera is generally
 * always moving.
 */
@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {
    @Shadow(remap = false)
    private RenderSectionManager renderSectionManager;

    @Unique
    private boolean wasRenderingShadows = false;

	@Shadow(remap = false)
	private double lastCameraX, lastCameraY, lastCameraZ, lastCameraPitch, lastCameraYaw;

	@Unique
	private double iris$swapLastCameraX, iris$swapLastCameraY, iris$swapLastCameraZ,
		iris$swapLastCameraPitch, iris$swapLastCameraYaw;

	@Unique
	private void swapCachedCameraPositions() {
		double tmp;

		tmp = lastCameraX;
		lastCameraX = iris$swapLastCameraX;
		iris$swapLastCameraX = tmp;

		tmp = lastCameraY;
		lastCameraY = iris$swapLastCameraY;
		iris$swapLastCameraY = tmp;

		tmp = lastCameraZ;
		lastCameraZ = iris$swapLastCameraZ;
		iris$swapLastCameraZ = tmp;

		tmp = lastCameraPitch;
		lastCameraPitch = iris$swapLastCameraPitch;
		iris$swapLastCameraPitch = tmp;

		tmp = lastCameraYaw;
		lastCameraYaw = iris$swapLastCameraYaw;
		iris$swapLastCameraYaw = tmp;
	}

	@Inject(method = "renderBlockEntities", at = @At("HEAD"))
	private void resetEntityList(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci) {
		beList = 0;
	}

	@Inject(method = "renderBlockEntities", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/SodiumWorldRenderer;renderBlockEntity(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
	private void addToList(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci) {
		beList++;
	}

	@Inject(method = "renderGlobalBlockEntities", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/SodiumWorldRenderer;renderBlockEntity(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
	private void addToList2(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci) {
		beList++;
	}

	private static int beList = 0;

	static {
		ShadowRenderingState.setBlockEntityRenderFunction((shadowRenderer, bufferSource, modelView, camera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum) -> {
			((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).invokeRenderBlockEntities(modelView, Minecraft.getInstance().renderBuffers(), Long2ObjectMaps.emptyMap(), tickDelta, bufferSource, cameraX, cameraY, cameraZ, Minecraft.getInstance().getBlockEntityRenderDispatcher());
			((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).invokeRenderGlobalBlockEntities(modelView, Minecraft.getInstance().renderBuffers(), Long2ObjectMaps.emptyMap(), tickDelta, bufferSource, cameraX, cameraY, cameraZ, Minecraft.getInstance().getBlockEntityRenderDispatcher());
            return beList;
        });
	}

    @Unique
    private void iris$ensureStateSwapped() {
        if (!wasRenderingShadows && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (this.renderSectionManager instanceof SwappableRenderSectionManager) {
				((SwappableRenderSectionManager) this.renderSectionManager).iris$swapVisibilityState();
				swapCachedCameraPositions();
			}

            wasRenderingShadows = true;
        } else if (wasRenderingShadows && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (this.renderSectionManager instanceof SwappableRenderSectionManager) {
				((SwappableRenderSectionManager) this.renderSectionManager).iris$swapVisibilityState();
				swapCachedCameraPositions();
			}

			wasRenderingShadows = false;
		}
    }

    @Inject(method = "scheduleTerrainUpdate()V", remap = false,
            at = @At(value = "INVOKE",
                    target = "me/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager.markGraphDirty ()V",
                    remap = false))
    private void iris$ensureStateSwappedBeforeMarkDirty(CallbackInfo ci) {
        iris$ensureStateSwapped();
    }

    // note: inject after the reload() check, but before the markDirty() call. This injection point was chosen just
    //       because it's relatively solid and is in between those two calls.
    @Inject(method = "setupTerrain", remap = false,
            at = @At(value = "FIELD",
                     target = "me/jellysquid/mods/sodium/client/render/SodiumWorldRenderer.lastCameraX : D",
                     ordinal = 0,
                     remap = false))
    private void iris$ensureStateSwappedInUpdateChunks(Camera camera, Viewport viewport, int frame, boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
        iris$ensureStateSwapped();
    }

    @Redirect(method = "setupTerrain", remap = false,
            at = @At(value = "FIELD",
                    target = "me/jellysquid/mods/sodium/client/render/SodiumWorldRenderer.lastCameraX : D",
                    ordinal = 0,
                    remap = false))
    private double iris$forceChunkGraphRebuildInShadowPass(SodiumWorldRenderer worldRenderer) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            // Returning NaN forces the comparison with the current camera to return false, making SodiumWorldRenderer
            // think that the chunk graph always needs to be rebuilt. This is generally true in the shadow map pass,
            // unless time is frozen.
            //
            // TODO: Detect when the sun/moon isn't moving
            return Double.NaN;
        } else {
            return lastCameraX;
        }
    }

    @Inject(method = "drawChunkLayer",  remap = false, at = @At("HEAD"))
    private void iris$beforeDrawChunkLayer(RenderType renderType, PoseStack poseStack, double x, double y,
										   double z, CallbackInfo ci) {
        iris$ensureStateSwapped();
    }
}
