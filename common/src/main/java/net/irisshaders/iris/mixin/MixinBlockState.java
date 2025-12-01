package net.irisshaders.iris.mixin;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SimpleModelWrapper.class)
public class MixinBlockState {
	//public BlockState getBlockAppearance() {
	//	return Blocks.AIR.defaultBlockState();
	//}
}
