package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.VisibleChunkCollectorAsync;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.RemovableMultiForest;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.irisshaders.iris.compat.sodium.mixinterface.ShadowRenderRegion;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Camera;
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
public abstract class MixinRenderSectionManagerShadow {
	@Shadow(remap = false)
	private @NotNull SortedRenderLists renderLists;

	@Shadow(remap = false)
	private boolean needsRenderListUpdate;

	@Shadow
	@Final
	private RenderRegionManager regions;
	@Shadow
	private int frame;

	@Unique
	private @NotNull SortedRenderLists shadowRenderLists = SortedRenderLists.empty();

	@Unique
	private boolean shadowNeedsRenderListUpdate = true;

	@Unique
	private boolean renderListStateIsShadow = false;

	@WrapOperation(method = "notifyChangedCamera", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;cameraChanged:Z"))
	private void notifyChangedCamera(RenderSectionManager instance, boolean value, Operation<Void> original) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			original.call(instance, value);
			return;
		}

		this.shadowNeedsRenderListUpdate = true;
	}

	@Inject(method = "updateRenderLists", at = @At(target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/occlusion/AsyncCameraTimingControl;getShouldRenderSync(Lnet/minecraft/client/Camera;)Z", value = "INVOKE"))
	private void updateRenderLists(Camera camera, Viewport viewport, boolean spectator, boolean updateImmediately, CallbackInfo ci) {
		this.shadowNeedsRenderListUpdate |= this.needsRenderListUpdate;
	}

	@Shadow(remap = false)
	public abstract int getVisibleChunkCount();

	@Shadow(remap = false)
	public abstract int getTotalSections();

	@Shadow
	@Final
	private RemovableMultiForest renderableSectionTree;

	@Shadow
	protected abstract float getSearchDistance();

	@Inject(method = "updateRenderLists", at = @At("HEAD"), cancellable = true)
	private void updateShadowRenderLists(Camera camera, Viewport viewport, boolean spectator, boolean updateImmediately, CallbackInfo ci) {
		if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (this.renderListStateIsShadow) {
				for (var region : this.regions.getLoadedRegions()) {
					((ShadowRenderRegion) region).swapToRegularRenderList();
				}
				this.renderListStateIsShadow = false;
			}
			return;
		}

		if (this.shadowNeedsRenderListUpdate) {
			if (!this.renderListStateIsShadow) {
				for (var region : this.regions.getLoadedRegions()) {
					((ShadowRenderRegion) region).swapToShadowRenderList();
				}
				this.renderListStateIsShadow = true;
			}

			var visibleCollector = new VisibleChunkCollectorAsync(this.regions, -this.frame);
			this.renderableSectionTree.prepareForTraversal();
			this.renderableSectionTree.traverse(visibleCollector, viewport, this.getSearchDistance());
			this.shadowRenderLists = visibleCollector.createRenderLists(viewport);
		}

		this.shadowNeedsRenderListUpdate = false;
		ci.cancel();
	}

	@Inject(method = "updateSectionInfo", at = @At("HEAD"))
	private void updateSectionInfo(RenderSection render, BuiltSectionInfo info, CallbackInfoReturnable<Boolean> cir) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Inject(method = "onSectionRemoved", at = @At("HEAD"))
	private void onSectionRemoved(int x, int y, int z, CallbackInfo ci) {
		this.shadowNeedsRenderListUpdate = true;
	}

	@Redirect(method = {
		"getRenderLists",
		"getVisibleChunkCount",
		"renderLayer"
	}, at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/SortedRenderLists;"), remap = false)
	private SortedRenderLists useShadowRenderList(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? this.shadowRenderLists : this.renderLists;
	}

	@Inject(method = "getChunksDebugString", at = @At("HEAD"), cancellable = true)
	private void getShadowChunksDebugString(CallbackInfoReturnable<String> cir) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			var renderLists = this.renderLists;
			this.renderLists = this.shadowRenderLists;
			cir.setReturnValue(String.format("C: %d/%d", this.getVisibleChunkCount(), this.getTotalSections()));
			this.renderLists = renderLists;
			cir.cancel();
		}
	}
}
