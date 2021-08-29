package net.coderbot.iris.mixin;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {
	@Shadow
	public abstract boolean isFullCube(BlockView world, BlockPos pos);

	/**
	 * @author IMS
	 * @reason ambientOcclusionLevel support
	 */
	@Environment(EnvType.CLIENT)
	@Deprecated
	@Overwrite
	public float getAmbientOcclusionLightLevel(BlockView world, BlockPos pos) {
		float originalValue = this.isFullCube(world, pos) ? 0.2F : 1.0F;
		float aoLightValue = BlockRenderingSettings.INSTANCE.getAmbientOcclusionLevel();
		if (aoLightValue == 1.0F) {
			return originalValue;
		}
		return 1.0F - aoLightValue * (1.0F - originalValue);
	}
}
