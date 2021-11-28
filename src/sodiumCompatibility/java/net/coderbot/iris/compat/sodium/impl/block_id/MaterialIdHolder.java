package net.coderbot.iris.compat.sodium.impl.block_id;

import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class MaterialIdHolder {
    private final Map<BlockState, Integer> idMap;
    public short id;
    public short renderType;

    public MaterialIdHolder() {
        this.idMap = Object2IntMaps.emptyMap();
        this.id = -1;
        this.renderType = -1;
    }

    public MaterialIdHolder(Map<BlockState, Integer> idMap) {
        this.idMap = idMap;
        this.id = -1;
        this.renderType = -1;
    }

    public void set(BlockState state, short renderType) {
        this.id = (short) this.idMap.getOrDefault(state, -1).intValue();
        this.renderType = renderType;
    }

    public void reset() {
        this.id = -1;
        this.renderType = -1;
    }
}
