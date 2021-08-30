package net.coderbot.iris.mixin;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {
	@Shadow
	public abstract Block getBlock();

	@Shadow
	protected abstract BlockState asBlockState();

	/**
	 * @author IMS
	 * @reason ambientOcclusionLevel support
	 */
	@Environment(EnvType.CLIENT)
	@Deprecated
	@Overwrite
	public float getAmbientOcclusionLightLevel(BlockView world, BlockPos pos) {
		float originalValue = this.getBlock().getAmbientOcclusionLightLevel(this.asBlockState(), world, pos);
		float aoLightValue = BlockRenderingSettings.INSTANCE.getAmbientOcclusionLevel();
		return 1.0F - aoLightValue * (1.0F - originalValue);
	}
}
