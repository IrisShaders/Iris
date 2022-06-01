package net.coderbot.iris.compat.sodium.impl.block_id;

import net.minecraft.world.level.block.state.BlockState;

public interface ChunkBuildBuffersExt {
	void iris$setMaterialId(BlockState state, short renderType);
	void iris$resetMaterialId();
}
