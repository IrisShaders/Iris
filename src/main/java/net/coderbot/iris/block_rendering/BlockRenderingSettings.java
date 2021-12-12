package net.coderbot.iris.block_rendering;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockRenderingSettings {
	public static final BlockRenderingSettings INSTANCE = new BlockRenderingSettings();

	private boolean reloadRequired;
	private Object2IntMap<BlockState> blockStateIds;
	private Object2IntFunction<NamespacedId> entityIds;
	private float ambientOcclusionLevel;
	private boolean disableDirectionalShading;
	private boolean useSeparateAo;

	public BlockRenderingSettings() {
		reloadRequired = false;
		blockStateIds = null;
		ambientOcclusionLevel = 1.0F;
		disableDirectionalShading = false;
		useSeparateAo = false;
	}

	public boolean isReloadRequired() {
		return reloadRequired;
	}

	public void clearReloadRequired() {
		reloadRequired = false;
	}

	@Nullable
	public Object2IntMap<BlockState> getBlockStateIds() {
		return blockStateIds;
	}

	// TODO (coderbot): This doesn't belong here. But I couldn't think of a nicer place to put it.
	@Nullable
	public Object2IntFunction<NamespacedId> getEntityIds() {
		return entityIds;
	}

	public void setBlockStateIds(Object2IntMap<BlockState> blockStateIds) {
		if (this.blockStateIds != null && this.blockStateIds.equals(blockStateIds)) {
			return;
		}

		this.reloadRequired = true;
		this.blockStateIds = blockStateIds;
	}

	public void setEntityIds(Object2IntFunction<NamespacedId> entityIds) {
		// note: no reload needed, entities are rebuilt every frame.
		this.entityIds = entityIds;
	}

	public float getAmbientOcclusionLevel() {
		return ambientOcclusionLevel;
	}

	public void setAmbientOcclusionLevel(float ambientOcclusionLevel) {
		if (ambientOcclusionLevel == this.ambientOcclusionLevel) {
			return;
		}

		this.reloadRequired = true;
		this.ambientOcclusionLevel = ambientOcclusionLevel;
	}

	public boolean shouldDisableDirectionalShading() {
		return disableDirectionalShading;
	}

	public void setDisableDirectionalShading(boolean disableDirectionalShading) {
		if (disableDirectionalShading == this.disableDirectionalShading) {
			return;
		}

		this.reloadRequired = true;
		this.disableDirectionalShading = disableDirectionalShading;
	}

	public boolean shouldUseSeparateAo() {
		return useSeparateAo;
	}

	public void setUseSeparateAo(boolean useSeparateAo) {
		if (useSeparateAo == this.useSeparateAo) {
			return;
		}

		this.reloadRequired = true;
		this.useSeparateAo = useSeparateAo;
	}
}
