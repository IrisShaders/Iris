package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.DeferredTaskList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.SectionTree;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.CameraMovement;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.mixinterface.ShadowRenderListAccess;
import net.irisshaders.iris.mixinterface.ShadowRenderRegion;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MappableRingBuffer;
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

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManagerShadow implements ShadowRenderListAccess {
	@Shadow(remap = false)
	private @NotNull SortedRenderLists renderLists;

	@Shadow(remap = false)
	private DeferredTaskList taskLists;

	@Shadow(remap = false)
	private boolean needsRenderListUpdate;

	@Shadow(remap = false)
	private boolean needsGraphUpdate;

	@Shadow(remap = false)
	private boolean cameraChanged;

	@Shadow
	@Final
	private RenderRegionManager regions;

    @Unique
    private SectionTree regularTree;


    @Unique
    private SectionTree shadowTree;

    @Shadow(remap = false)
	private void renderOutOfGraph(Viewport viewport, FogParameters fogParameters) {
		throw new AssertionError();
	}

    @Shadow private SectionTree renderTree;
    @Unique
	private @NotNull SortedRenderLists shadowRenderLists = SortedRenderLists.empty();

	@Unique
	private DeferredTaskList shadowTaskLists;

	@Unique
	private boolean shadowNeedsRenderListUpdate = true;

	@Unique
	private boolean renderListStateIsShadow;

	@Unique
	private int regularUboUpdated;

	@Unique
	private int shadowUboUpdated;

	@Unique
	private MappableRingBuffer regularUbo;

	@Unique
	private MappableRingBuffer shadowUbo;

	@Unique
	private SortedRenderLists regularRenderLists;

	@Unique
	private DeferredTaskList regularTaskLists;

	@Unique
	private boolean regularNeedsRenderListUpdate;

	@Unique
	private boolean regularNeedsGraphUpdate;

	@Unique
	private boolean regularCameraChanged;

	@Unique
	private boolean shadowScopeActive;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initIris(ClientLevel level, int renderDistance, SortBehavior sortBehavior, CallbackInfo ci) {
        this.shadowUbo = new MappableRingBuffer(() -> "Iris terrain uniform buffer (Shadow)", 130, 256);
    }

	@Unique
	private void iris$swapToShadowRenderLists() {
		if (this.renderListStateIsShadow) {
			return;
		}

		for (var region : this.regions.getLoadedRegions()) {
			((ShadowRenderRegion) region).swapToShadowRenderList();
		}

		this.renderListStateIsShadow = true;
	}

	@Unique
	private void iris$swapToRegularRenderLists() {
		if (!this.renderListStateIsShadow) {
			return;
		}

		for (var region : this.regions.getLoadedRegions()) {
			((ShadowRenderRegion) region).swapToRegularRenderList();
		}

		this.renderListStateIsShadow = false;
	}

	@Override
	public void iris$beginShadowRenderListScope() {
		if (!this.shadowScopeActive) {
			this.regularRenderLists = this.renderLists;
			this.regularTaskLists = this.taskLists;
			this.regularNeedsRenderListUpdate = this.needsRenderListUpdate;
			this.regularNeedsGraphUpdate = this.needsGraphUpdate;
			this.regularCameraChanged = this.cameraChanged;
			this.shadowScopeActive = true;
            this.regularTree = this.renderTree;
            this.renderTree = this.shadowTree;
        }

		this.iris$swapToShadowRenderLists();
		this.renderLists = this.shadowRenderLists;
		this.taskLists = this.shadowTaskLists;
	}

	@Override
	public void iris$endShadowRenderListScope() {
		this.shadowRenderLists = this.renderLists;
		this.shadowTaskLists = this.taskLists;
		this.iris$swapToRegularRenderLists();

		if (this.shadowScopeActive) {
			this.renderLists = this.regularRenderLists;
            this.renderTree = this.regularTree;
			this.taskLists = this.regularTaskLists;
			this.needsRenderListUpdate = this.regularNeedsRenderListUpdate;
			this.needsGraphUpdate = this.regularNeedsGraphUpdate;
			this.cameraChanged = this.regularCameraChanged;
			this.shadowScopeActive = false;
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void create(ClientLevel level, int renderDistance, SortBehavior sortBehavior, CallbackInfo ci) {
		this.shadowTaskLists = null;
	}

	@Inject(method = "markGraphDirty", at = @At("HEAD"), remap = false)
	private void markShadowGraphDirty(CallbackInfo ci) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Inject(method = "notifyChangedCamera", at = @At("HEAD"), remap = false)
	private void markShadowCameraDirty(CallbackInfo ci) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Inject(method = "updateSectionInfo", at = @At("HEAD"), remap = false)
	private void updateSectionInfo(RenderSection render, BuiltSectionInfo info, CallbackInfoReturnable<Integer> cir) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Inject(method = "onSectionRemoved", at = @At("HEAD"), remap = false)
	private void onSectionRemoved(int x, int y, int z, CallbackInfo ci) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Redirect(method = "onSectionAdded", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegionManager;createForChunk(III)Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;"), remap = false)
	private RenderRegion createRegionForCurrentRenderListState(RenderRegionManager regions, int x, int y, int z) {
		RenderRegion region = regions.createForChunk(x, y, z);

		if (this.renderListStateIsShadow) {
			((ShadowRenderRegion) region).swapToShadowRenderList();
		}

		return region;
	}

	@Inject(method = "prepareRenderTrees", at = @At("HEAD"), cancellable = true, remap = false)
	private void skipAsyncCullDuringShadow(Viewport viewport, FogParameters fogParameters, boolean useOcclusionCulling, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
		}
	}

	@Inject(method = "cleanupAndFlip", at = @At("HEAD"), cancellable = true, remap = false)
	private void skipCleanupAndFlipDuringShadow(CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
		}
	}

	@Inject(method = "processChunkBuilds", at = @At("HEAD"), cancellable = true, remap = false)
	private void skipChunkBuildProcessingDuringShadow(Viewport viewport, UniformBufferManager uniforms, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
		}
	}

	@Inject(method = "processGFNIMovement", at = @At("HEAD"), cancellable = true, remap = false)
	private void skipTranslucentSortingDuringShadow(CameraMovement movement, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
		}
	}

	@WrapMethod(method = "finalizeRenderLists", remap = false)
	private void finalizeShadowRenderLists(Camera camera, Viewport viewport, FogParameters fogParameters, boolean updateChunksImmediately, Operation<Void> original) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			original.call(camera, viewport, fogParameters, updateChunksImmediately);
			return;
		}

		this.iris$swapToShadowRenderLists();

		if (this.shadowNeedsRenderListUpdate) {
			this.renderOutOfGraph(viewport, fogParameters);
			this.shadowRenderLists = this.renderLists;
			this.shadowTaskLists = this.taskLists;
			this.shadowNeedsRenderListUpdate = false;
		}

		this.needsRenderListUpdate = false;
		this.needsGraphUpdate = false;
		this.cameraChanged = false;
	}

	@Redirect(method = {
		"getRenderLists",
		"getVisibleChunkCount"
	}, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"), remap = false)
	private SortedRenderLists useShadowRenderLists(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? this.shadowRenderLists : this.renderLists;
	}

    @Inject(method = "destroy", at = @At("HEAD"))
    private void iris$destroy(CallbackInfo ci) {
        if (this.shadowUbo != null) this.shadowUbo.close();
    }
}
