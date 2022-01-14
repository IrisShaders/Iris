package net.coderbot.iris.compat.sodium.impl.block_id;

import net.minecraft.world.level.block.state.BlockState;

public interface TerrainBuildBuffersExt {
    void iris$setMaterialId(BlockState state, short renderType);
    void iris$resetMaterialId();
}
