package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.TaskQueueType;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.TreeSectionCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.mixinterface.ShadowRenderRegion;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManagerShadow {
	@Shadow(remap = false)
	private @NotNull SortedRenderLists renderLists;
	@Shadow(remap = false)
	private @NotNull Map<TaskQueueType, ArrayDeque<RenderSection>> taskLists;
	@Shadow
	private int lastUpdatedFrame;

	@Shadow
	protected abstract boolean isOutOfGraph(SectionPos pos);

	@Shadow
	@Final
	private RenderRegionManager regions;
	@Unique
	private @NotNull SortedRenderLists shadowRenderLists = SortedRenderLists.empty();
	@Unique
	private @NotNull Map<TaskQueueType, ArrayDeque<RenderSection>> shadowTaskLists = new EnumMap<>(TaskQueueType.class);
	private int lastUpdatedFrameShadow;

	@Unique
	private boolean shadowNeedsRenderListUpdate = true;

	@Unique
	private boolean renderListStateIsShadow = false;

	@Inject(method = "needsUpdate", at = @At(value = "HEAD"))
	private void notifyChangedCamera(CallbackInfoReturnable<Boolean> cir) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void create(ClientLevel level, int renderDistance, SortBehavior sortBehavior, CommandList commandList, CallbackInfo ci) {
		for (int var6 = 0; var6 < TaskQueueType.values().length; ++var6) {
			TaskQueueType type = TaskQueueType.values()[var6];
			shadowTaskLists.put(type, new ArrayDeque<>());
		}
	}

	@Redirect(remap = false, method = "finalizeRenderLists", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"))
	private void useShadowRenderList(RenderSectionManager instance, SortedRenderLists value) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			shadowRenderLists = value;
		} else {
			renderLists = value;
		}
	}

	@WrapMethod(method = "createTerrainRenderList")
	private boolean updateShadowRenderLists(Camera camera, Viewport viewport, FogParameters fogParameters, int frame, boolean spectator, Operation<Boolean> original) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (this.renderListStateIsShadow) {
				for (var region : this.regions.getLoadedRegions()) {
					((net.irisshaders.iris.mixinterface.ShadowRenderRegion) region).swapToRegularRenderList();
				}
				this.renderListStateIsShadow = false;
			}
		} else {
			if (this.shadowNeedsRenderListUpdate) {
				if (!this.renderListStateIsShadow) {
					for (var region : this.regions.getLoadedRegions()) {
						((ShadowRenderRegion) region).swapToShadowRenderList();
					}
					this.renderListStateIsShadow = true;
				}
			}
		}

		return original.call(camera, viewport, fogParameters, frame, spectator);
	}

	@Inject(method = "updateSectionInfo", at = @At("HEAD"))
	private void updateSectionInfo(RenderSection render, BuiltSectionInfo info, CallbackInfoReturnable<Boolean> cir) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Inject(method = "onSectionRemoved", at = @At("HEAD"))
	private void onSectionRemoved(int x, int y, int z, CallbackInfo ci) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Redirect(remap = false, method = "createTerrainRenderList", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;taskLists:Ljava/util/Map;"))
	private void useShadowTaskrList(RenderSectionManager instance, @NotNull Map<TaskQueueType, ArrayDeque<RenderSection>> value) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			shadowTaskLists = value;
		} else {
			taskLists = value;
		}
	}

	/**
	 * Adding a note for myself: This is how the occlusion culling skip for the shadow map is done. Remember this.
	 */
	@Redirect(method = "createTerrainRenderList", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;isOutOfGraph(Lnet/minecraft/core/SectionPos;)Z"))
	private boolean iris$setOutOfGraph(RenderSectionManager instance, SectionPos pos) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() || this.isOutOfGraph(pos);
	}

	@Redirect(method = {
		"getRenderLists",
		"getVisibleChunkCount",
		"renderLayer"
	}, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"), remap = false)
	private SortedRenderLists useShadowRenderList2(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? shadowRenderLists : renderLists;
	}

	@Inject(method = "updateChunks", at = @At("HEAD"), cancellable = true, remap = false)
	private void doNotUpdateDuringShadow(boolean updateImmediately, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) ci.cancel();
	}

	@Inject(method = "uploadChunks", at = @At("HEAD"), cancellable = true, remap = false)
	private void doNotUploadDuringShadow(CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) ci.cancel();
	}

	@Redirect(method = {
		"resetRenderLists",
		"submitSectionTasks(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/executor/ChunkJobCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/estimation/UploadResourceBudget;Lnet/caffeinemc/mods/sodium/client/render/chunk/TaskQueueType;)V"
	}, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;taskLists:Ljava/util/Map;"), remap = false)
	private @NotNull Map<TaskQueueType, ArrayDeque<RenderSection>> useShadowTaskList3(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? shadowTaskLists : taskLists;
	}

	@Redirect(method = {
		"resetRenderLists"
	}, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"), remap = false)
	private void useShadowRenderList3(RenderSectionManager instance, SortedRenderLists value) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) shadowRenderLists = value;
		else renderLists = value;
	}
}
