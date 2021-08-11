package net.coderbot.iris.block_rendering;

import net.coderbot.iris.shaderpack.IdMap;

public class BlockRenderingSettings {
	public static final BlockRenderingSettings INSTANCE = new BlockRenderingSettings();

	private boolean reloadRequired;
	private IdMap idMap;
	private boolean disableDirectionalShading;
	private boolean useSeparateAo;

	public BlockRenderingSettings() {
		reloadRequired = false;
		idMap = null;
		disableDirectionalShading = false;
		useSeparateAo = false;
	}

	public boolean isReloadRequired() {
		return reloadRequired;
	}

	public void clearReloadRequired() {
		reloadRequired = false;
	}

	public IdMap getIdMap() {
		return idMap;
	}

	public void setIdMap(IdMap idMap) {
		if (this.idMap != null && this.idMap.equals(idMap)) {
			return;
		}

		this.reloadRequired = true;
		this.idMap = idMap;
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
