package net.irisshaders.iris.compat.general;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.level.block.state.BlockState;

public class IrisModSupport {
	public static final IrisModSupport INSTANCE = new IrisModSupport();

	public BlockState getModelPartState(BlockStateModelPart model) {
		return ((IrisModelPart) model).getBlockAppearance();
	}
}
