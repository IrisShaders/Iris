package net.coderbot.iris.compat.sodium.impl.block_context;

import net.minecraft.world.level.block.state.BlockState;

public interface ChunkBuildBuffersExt {
	void iris$setLocalPos(int localPosX, int localPosY, int localPosZ);

	void iris$setMaterialId(BlockState state, short renderType);

	void iris$resetBlockContext();
}
