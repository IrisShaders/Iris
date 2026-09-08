package net.irisshaders.iris.compat.sodium.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.gpu.device.batch.MultiDrawBatch;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.mixinterface.ShadowRenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RenderRegion.class)
public class MixinRenderRegion implements ShadowRenderRegion {
	@Final
	@Shadow
	@Mutable
	private ChunkRenderList renderList;

	@Mutable
	@Shadow
	@Final
	private Map<TerrainRenderPass, MultiDrawBatch> cachedBatches;

	@Unique
	ChunkRenderList regularRenderList;

	@Unique
	ChunkRenderList shadowRenderList;

	@Unique
	Map<TerrainRenderPass, MultiDrawBatch> regularCachedBatches;

	@Unique
	Map<TerrainRenderPass, MultiDrawBatch> shadowCachedBatches;

	@Unique
	private boolean iris$shadowRenderListActive;

	@Unique
	@Override
	public void swapToShadowRenderList() {
		if (this.iris$shadowRenderListActive) {
			return;
		}

		this.regularRenderList = this.renderList;
		this.renderList = this.shadowRenderList;
		this.regularCachedBatches = this.cachedBatches;
		this.cachedBatches = this.shadowCachedBatches;
		this.shadowCachedBatches = null;
		this.iris$shadowRenderListActive = true;
		this.ensureRenderList();
	}

	@Unique
	@Override
	public void swapToRegularRenderList() {
		if (!this.iris$shadowRenderListActive) {
			return;
		}

		this.shadowRenderList = this.renderList;
		this.renderList = this.regularRenderList;
		this.shadowCachedBatches = this.cachedBatches;
		this.cachedBatches = this.regularCachedBatches;
		this.regularCachedBatches = null;
		this.iris$shadowRenderListActive = false;
		this.ensureRenderList();
	}

	@Unique
	private void ensureRenderList() {
		if (this.renderList == null) {
			this.renderList = new ChunkRenderList((RenderRegion) (Object) this);
		}

		if (this.cachedBatches == null) {
			this.cachedBatches = new Reference2ReferenceOpenHashMap<>();
		}
	}

	@Inject(method = "delete", at = @At("HEAD"))
	private void iris$delete(CallbackInfo ci) {
		if (this.regularCachedBatches != null) {
			for(MultiDrawBatch batch : this.regularCachedBatches.values()) {
				batch.delete();
			}
		}

		if (this.shadowCachedBatches != null) {
			for(MultiDrawBatch batch : this.shadowCachedBatches.values()) {
				batch.delete();
			}
		}
	}

	@Override
	public void iris$forceClearAllBatches() {
		if (this.regularCachedBatches != null) {
			for(MultiDrawBatch batch : this.regularCachedBatches.values()) {
				batch.clear();
			}
		}

		if (this.shadowCachedBatches != null) {
			for(MultiDrawBatch batch : this.shadowCachedBatches.values()) {
				batch.clear();
			}
		}

		if (this.cachedBatches != null) {
			for(MultiDrawBatch batch : this.cachedBatches.values()) {
				batch.clear();
			}
		}
	}

	@Override
	public void iris$forceClearBatchFor(TerrainRenderPass pass) {
		if (this.regularCachedBatches != null) {
			MultiDrawBatch batch = this.regularCachedBatches.get(pass);
			if (batch != null) {
				batch.clear();
			}
		}
		if (this.shadowCachedBatches != null) {
			MultiDrawBatch batch = this.shadowCachedBatches.get(pass);
			if (batch != null) {
				batch.clear();
			}
		}
		if (this.cachedBatches != null) {
			MultiDrawBatch batch = this.cachedBatches.get(pass);
			if (batch != null) {
				batch.clear();
			}
		}
	}

	@Redirect(method = { "onGeometryBufferChange", "onGeometrySegmentChange", "update" }, at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;clearAllCachedBatches()V"))
	private void iris$clearAllBatchesAfterGeometryChange(RenderRegion instance) {
		((ShadowRenderRegion) instance).iris$forceClearAllBatches();
	}

	@Redirect(method = { "onIndexBufferChange", "onIndexSegmentChange" }, at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;clearCachedBatchFor(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V"))
	private void iris$clearBatchAfterIndexChange(RenderRegion instance, TerrainRenderPass pass) {
		((ShadowRenderRegion) instance).iris$forceClearBatchFor(pass);
	}
}
