package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.sodium.interop.vanilla.math.frustum.Frustum;
import net.caffeinemc.sodium.render.SodiumWorldRenderer;
import net.caffeinemc.sodium.render.chunk.RenderSection;
import net.caffeinemc.sodium.render.chunk.TerrainRenderManager;
import net.caffeinemc.sodium.render.chunk.compile.ChunkBuilder;
import net.caffeinemc.sodium.render.chunk.draw.ChunkCameraContext;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.caffeinemc.sodium.render.chunk.region.RenderRegionManager;
import net.caffeinemc.sodium.render.chunk.state.ChunkRenderData;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shadow_map.SwappableRenderSectionManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Modifies {@link TerrainRenderManager
 * } to support maintaining a separate visibility list for the shadow camera, as well
 * as disabling chunk rebuilds when computing visibility for the shadow camera.
 */
@Mixin(TerrainRenderManager.class)
public abstract class MixinTerrainRenderManager implements SwappableRenderSectionManager {

	@Shadow(remap = false)
	private boolean needsUpdate;

	@Shadow
	@Final
	@Mutable
	private List<RenderSection> visibleMeshedSections;
	@Shadow
	@Final
	@Mutable

	private List<RenderSection> visibleTickingSections;
	@Mutable
	@Shadow
	@Final
	private List<RenderSection> visibleBlockEntitySections;

	@Shadow
	public abstract Iterable<BlockEntity> getVisibleBlockEntities();

	@Mutable
	@Shadow
	@Final
	private RenderRegionManager regionManager;

	@Shadow(remap = false)
	protected static TerrainVertexType createVertexType() {
		return null;
	}

	@Shadow
	@Final
	private ChunkBuilder builder;

	@Shadow
	protected abstract void onChunkDataChanged(RenderSection section, ChunkRenderData prev, ChunkRenderData next);

	@Shadow
	private int frameIndex;
	@Unique
    private List<RenderSection> visibleSectionsSwap;

    @Unique
    private List<RenderSection> tickableChunksSwap;

    @Unique
    private List<RenderSection> visibleBlockEntitiesSwap;


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
        this.needsUpdateSwap = true;
    }

    @Override
    public void iris$swapVisibilityState() {
		List<RenderSection> visibleSectionsTmp = visibleMeshedSections;
		visibleMeshedSections = visibleSectionsSwap;
        visibleSectionsSwap = visibleSectionsTmp;

        List<RenderSection> tickableChunksTmp = visibleTickingSections;
        visibleTickingSections = tickableChunksSwap;
        tickableChunksSwap = tickableChunksTmp;

		List<RenderSection> visibleBlockEntitiesTmp = visibleBlockEntitySections;
        visibleBlockEntitySections = visibleBlockEntitiesSwap;
        visibleBlockEntitiesSwap = visibleBlockEntitiesTmp;

        boolean needsUpdateTmp = needsUpdate;
        needsUpdate = needsUpdateSwap;
        needsUpdateSwap = needsUpdateTmp;
    }

    @Inject(method = "update", at = @At("RETURN"), remap = false)
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

	/**
	 * @author
	 * @reason
	 */
	@Overwrite(remap = false)
	public boolean isGraphDirty() {
		return true;
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

	// TODO: check needsUpdate and needsUpdateSwap patches?
}
