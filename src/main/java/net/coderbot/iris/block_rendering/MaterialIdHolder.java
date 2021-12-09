package net.coderbot.iris.block_rendering;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.world.level.block.state.BlockState;

public class MaterialIdHolder {
    private final Object2IntMap<BlockState> blockStateIds;
    public short id;
    public short renderType;

    public MaterialIdHolder() {
        this.blockStateIds = Object2IntMaps.emptyMap();
        this.id = -1;
        this.renderType = -1;
    }

    public MaterialIdHolder(Object2IntMap<BlockState> idMap) {
        this.blockStateIds = idMap;
        this.id = -1;
        this.renderType = -1;
    }

    public void set(BlockState state, short renderType) {
        this.id = (short) this.blockStateIds.getOrDefault(state, -1);
        this.renderType = renderType;
    }

    public void reset() {
        this.id = -1;
        this.renderType = -1;
    }
}
