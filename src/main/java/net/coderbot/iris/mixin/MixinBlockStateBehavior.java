package net.coderbot.iris.mixin;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBehavior {
	@Shadow
	public abstract Block getBlock();

	@Shadow
	protected abstract BlockState asState();

	/**
	 * @author IMS
	 * @reason ambientOcclusionLevel support
	 */
	@Environment(EnvType.CLIENT)
	@Deprecated
	@Overwrite
	public float getShadeBrightness(BlockGetter blockGetter, BlockPos blockPos) {
		float originalValue = this.getBlock().getShadeBrightness(this.asState(), blockGetter, blockPos);
		float aoLightValue = BlockRenderingSettings.INSTANCE.getAmbientOcclusionLevel();
		return 1.0F - aoLightValue * (1.0F - originalValue);
	}
}
