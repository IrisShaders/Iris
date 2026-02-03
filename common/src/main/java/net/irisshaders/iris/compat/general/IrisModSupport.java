package net.irisshaders.iris.compat.general;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.irisshaders.iris.Iris;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

public class IrisModSupport {
	public static final IrisModSupport INSTANCE = new IrisModSupport();

	public BlockState getModelPartState(BlockModelPart model) {
		return ((IrisModelPart) model).getBlockAppearance();
	}
}
