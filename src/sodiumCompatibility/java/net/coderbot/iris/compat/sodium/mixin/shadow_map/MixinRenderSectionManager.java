package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderListBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.coderbot.iris.compat.sodium.impl.shadow_map.RenderSectionExt;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shadow_map.SwappableRenderSectionManager;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Modifies {@link RenderSectionManager} to support maintaining a separate visibility list for the shadow camera, as well
 * as disabling chunk rebuilds when computing visibility for the shadow camera.
 */
@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager implements SwappableRenderSectionManager {

	@Shadow(remap = false)
	private boolean needsUpdate;

	@Mutable
	@Shadow
	@Final
	private ArrayDeque<RenderSection> iterationQueue;
	@Unique
	private Map<ChunkUpdateType, PriorityQueue<RenderSection>> rebuildQueuesSwap = new HashMap<>();

    @Unique
	private boolean needsUpdateSwap;

    @Unique
    private static final ObjectArrayFIFOQueue<?> EMPTY_QUEUE = new ObjectArrayFIFOQueue<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void iris$onInit(ClientLevel world, int renderDistance, CommandList commandList, CallbackInfo ci) {
        this.needsUpdateSwap = true;

		for (ChunkUpdateType type : ChunkUpdateType.values()) {
			this.rebuildQueuesSwap.put(type, new ObjectArrayFIFOQueue<>());
		}
    }

	@Redirect(method = "initSearch", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;useBlockFaceCulling:Z"))
	private boolean iris$disableBlockFaceCullingInShadowPass(SodiumGameOptions.PerformanceSettings instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return false;
		} else {
			return instance.useBlockFaceCulling;
		}
	}

    @Override
    public void iris$swapVisibilityState() {

        boolean needsUpdateTmp = needsUpdate;
        needsUpdate = needsUpdateSwap;
        needsUpdateSwap = needsUpdateTmp;
    }

    @Inject(method = "updateRenderLists", at = @At("RETURN"))
	private void iris$captureVisibleBlockEntities(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			//ShadowRenderer.visibleBlockEntities = visibleBlockEntities;
		}
	}

	//@Inject(method = "schedulePendingUpdates", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$noRebuildEnqueueingInShadowPass(RenderSection section, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
		}
	}

	//@Inject(method = "bfsEnqueue", at = @At(value = "HEAD"), cancellable = true)
	private void changeVisibleFrame(RenderSection render, int incomingDirections, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
			this.iterationQueue.add(render);

		}
	}

	//@Redirect(method = "bfsEnqueue", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;setLastVisibleFrame(I)V"))
	private void changeVisibleFrame2(RenderSection instance, int frame) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			((RenderSectionExt) instance).setPreviousFrameShadow(frame);
			return;
		}
		instance.setLastVisibleFrame(frame);
	}


	//@Redirect(method = "bfsEnqueue", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;setIncomingDirections(I)V"))
	private void changeVisibleFrame3(RenderSection instance, int frame) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return;
		}
		instance.setIncomingDirections(frame);
	}

	@Redirect(method = "resetLists", remap = false,
			at = @At(value = "INVOKE", target = "java/util/Collection.iterator ()Ljava/util/Iterator;"))
	private Iterator<?> iris$noQueueClearingInShadowPass(Collection<?> collection) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return Collections.emptyIterator();
		} else {
			return collection.iterator();
		}
	}

	// TODO: check needsUpdate and needsUpdateSwap patches?
}
