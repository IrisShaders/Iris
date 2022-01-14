package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.interop.vanilla.math.frustum.Frustum;
import me.jellysquid.mods.sodium.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.render.chunk.RenderSectionManager;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shadow_map.SwappableRenderSectionManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Shadow(remap = false)
    private double lastCameraX;

    @Unique
    private boolean wasRenderingShadows = false;

    @Unique
    public void iris$restoreStateIfShadowsWereBeingRendered() {
        if (wasRenderingShadows && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            if (this.renderSectionManager instanceof SwappableRenderSectionManager) {
                ((SwappableRenderSectionManager) this.renderSectionManager).iris$swapVisibilityState();
            }

            wasRenderingShadows = false;
        }
    }

    @Unique
    private void iris$ensureStateSwapped() {
        if (!wasRenderingShadows && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            if (this.renderSectionManager instanceof SwappableRenderSectionManager) {
                ((SwappableRenderSectionManager) this.renderSectionManager).iris$swapVisibilityState();
            }

            wasRenderingShadows = true;
        }
    }

    @Inject(method = "scheduleTerrainUpdate()V", remap = false,
            at = @At(value = "INVOKE",
                    target = "me/jellysquid/mods/sodium/render/chunk/RenderSectionManager.markGraphDirty ()V",
                    remap = false))
    private void iris$ensureStateSwappedBeforeMarkDirty(CallbackInfo ci) {
        iris$ensureStateSwapped();
    }

    // note: inject after the reload() check, but before the markDirty() call. This injection point was chosen just
    //       because it's relatively solid and is in between those two calls.
    @Inject(method = "updateChunks", remap = false,
            at = @At(value = "FIELD",
                     target = "me/jellysquid/mods/sodium/render/SodiumWorldRenderer.lastCameraX : D",
                     ordinal = 0,
                     remap = false))
    private void iris$ensureStateSwappedInUpdateChunks(Camera camera, Frustum frustum, int frame, boolean spectator, CallbackInfo ci) {
        iris$ensureStateSwapped();
    }

    @Redirect(method = "updateChunks", remap = false,
            at = @At(value = "FIELD",
                    target = "me/jellysquid/mods/sodium/render/SodiumWorldRenderer.lastCameraX : D",
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
        iris$restoreStateIfShadowsWereBeingRendered();
    }
}
