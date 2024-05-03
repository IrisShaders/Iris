package net.irisshaders.iris.compat.sodium.mixin.shadow_map;

import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkUpdateType;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@Shadow
	private @NotNull SortedRenderLists renderLists;
	@Shadow
	private @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> taskLists;
	@Unique
	private @NotNull SortedRenderLists shadowRenderLists = SortedRenderLists.empty();
	@Unique
	private @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> shadowTaskLists = new EnumMap<>(ChunkUpdateType.class);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void create(ClientLevel level, int renderDistance, CommandList commandList, CallbackInfo ci) {
		for(int var6 = 0; var6 < ChunkUpdateType.values().length; ++var6) {
			ChunkUpdateType type = ChunkUpdateType.values()[var6];
			shadowTaskLists.put(type, new ArrayDeque<>());
		}
	}

	@Redirect(method = "createTerrainRenderList", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"))
	private void useShadowRenderList(RenderSectionManager instance, SortedRenderLists value) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			shadowRenderLists = value;
		} else {
			renderLists = value;
		}
	}
	@Redirect(method = "createTerrainRenderList", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;taskLists:Ljava/util/Map;"))
	private void useShadowTaskrList(RenderSectionManager instance, @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> value) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			shadowTaskLists = value;
		} else {
			taskLists = value;
		}
	}

	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;createTerrainRenderList(Lnet/minecraft/client/Camera;Lnet/caffeinemc/mods/sodium/client/render/viewport/Viewport;IZ)V", shift = At.Shift.AFTER), cancellable = true)
	private void cancelIfShadow(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) ci.cancel();
	}

	@Redirect(method = {
		"getRenderLists",
		"getVisibleChunkCount",
		"renderLayer"
	}, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"), remap = false)
	private SortedRenderLists useShadowRenderList2(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? shadowRenderLists : renderLists;
	}

	@Inject(method = "updateChunks", at = @At("HEAD"), cancellable = true)
	private void doNotUpdateDuringShadow(boolean updateImmediately, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) ci.cancel();
	}

	@Inject(method = "uploadChunks", at = @At("HEAD"), cancellable = true)
	private void doNotUploadDuringShadow(CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) ci.cancel();
	}

	@Redirect(method = {
		"resetRenderLists",
		"submitSectionTasks(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/executor/ChunkJobCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/executor/ChunkJobCollector;Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/executor/ChunkJobCollector;)V"
	}, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;taskLists:Ljava/util/Map;"), remap = false)
	private @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> useShadowTaskList3(RenderSectionManager instance) {
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
