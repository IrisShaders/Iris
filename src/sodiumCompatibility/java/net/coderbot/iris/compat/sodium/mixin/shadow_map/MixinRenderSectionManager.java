package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.interop.vanilla.math.frustum.Frustum;
import net.caffeinemc.sodium.render.SodiumWorldRenderer;
import net.caffeinemc.sodium.render.chunk.RenderSection;
import net.caffeinemc.sodium.render.chunk.RenderSectionManager;
import net.caffeinemc.sodium.render.chunk.draw.ChunkCameraContext;
import net.caffeinemc.sodium.render.chunk.draw.RenderListBuilder;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.coderbot.iris.pipeline.ShadowRenderer;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Modifies {@link RenderSectionManager} to support maintaining a separate visibility list for the shadow camera, as well
 * as disabling chunk rebuilds when computing visibility for the shadow camera.
 */
@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager implements SwappableRenderSectionManager {

	@Shadow(remap = false)
	private boolean needsUpdate;


	@Shadow
	private Map<ChunkRenderPass, RenderListBuilder.RenderList> renderLists;
	@Shadow
	@Final
	@Mutable
	private ReferenceArrayList<RenderSection> visibleMeshedSections;
	@Shadow
	@Final
	@Mutable

	private ReferenceArrayList<RenderSection> visibleTickingSections;
	@Mutable
	@Shadow
	@Final
	private ReferenceArrayList<RenderSection> visibleBlockEntitySections;

	@Shadow
	public abstract Iterable<BlockEntity> getVisibleBlockEntities();

	@Unique
    private ReferenceArrayList<RenderSection> visibleSectionsSwap;

    @Unique
    private ReferenceArrayList<RenderSection> tickableChunksSwap;

    @Unique
    private ReferenceArrayList<RenderSection> visibleBlockEntitiesSwap;

	@Unique
	private Map<ChunkRenderPass, RenderListBuilder.RenderList> renderListsSwap;

    @Unique
	private boolean needsUpdateSwap;

    @Unique
    private static final ObjectArrayFIFOQueue<?> EMPTY_QUEUE = new ObjectArrayFIFOQueue<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void iris$onInit(RenderDevice device, SodiumWorldRenderer worldRenderer,
							 ChunkRenderPassManager renderPassManager, ClientLevel world, int renderDistance, CallbackInfo ci) {
        this.visibleSectionsSwap = new ReferenceArrayList<>();
        this.tickableChunksSwap = new ReferenceArrayList<>();
        this.visibleBlockEntitiesSwap = new ReferenceArrayList<>();
		this.renderListsSwap = new Reference2ReferenceArrayMap<>();
        this.needsUpdateSwap = true;
    }

    @Override
    public void iris$swapVisibilityState() {
        ReferenceArrayList<RenderSection> visibleSectionsTmp = visibleMeshedSections;
		visibleMeshedSections = visibleSectionsSwap;
        visibleSectionsSwap = visibleSectionsTmp;

        ReferenceArrayList<RenderSection> tickableChunksTmp = visibleTickingSections;
        visibleTickingSections = tickableChunksSwap;
        tickableChunksSwap = tickableChunksTmp;

        ReferenceArrayList<RenderSection> visibleBlockEntitiesTmp = visibleBlockEntitySections;
        visibleBlockEntitySections = visibleBlockEntitiesSwap;
        visibleBlockEntitiesSwap = visibleBlockEntitiesTmp;

		Map<ChunkRenderPass, RenderListBuilder.RenderList> renderListsTmp = renderLists;
		renderLists = renderListsSwap;
		renderListsSwap = renderListsTmp;

        boolean needsUpdateTmp = needsUpdate;
        needsUpdate = needsUpdateSwap;
        needsUpdateSwap = needsUpdateTmp;
    }

    @Inject(method = "update", at = @At("RETURN"))
	private void iris$captureVisibleBlockEntities(ChunkCameraContext camera, Frustum frustum, boolean spectator, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ShadowRenderer.visibleBlockEntities = StreamSupport
				.stream(this.getVisibleBlockEntities().spliterator(), false)
				.collect(Collectors.toList());;
		}
	}

	@Inject(method = "schedulePendingUpdates", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$noRebuildEnqueueingInShadowPass(RenderSection section, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
		}
	}

//	@Redirect(method = "resetLists", remap = false,
//			at = @At(value = "INVOKE", target = "java/util/Collection.iterator ()Ljava/util/Iterator;"))
	private Iterator<?> iris$noQueueClearingInShadowPass(Collection<?> collection) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return Collections.emptyIterator();
		} else {
			return collection.iterator();
		}
	}

//	@Redirect(method = "calculateVisibilityFlags",
//			at = @At(value = "FIELD",
//					target = "net/caffeinemc/sodium/render/chunk/RenderSectionManager.isBlockFaceCullingEnabled : Z"))
//	private boolean iris$disableBlockFaceCullingInShadowPass(RenderSectionManager manager) {
//		return isBlockFaceCullingEnabled && !ShadowRenderingState.areShadowsCurrentlyBeingRendered();
//	}

	// TODO: check needsUpdate and needsUpdateSwap patches?
}
