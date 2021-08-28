package net.coderbot.iris.mixin;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AbstractBlock.class)
public class MixinAbstractBlock {
	/**
	 * @author IMS
	 * @reason ambientOcclusionLevel support
	 */
	@Environment(EnvType.CLIENT)
	@Deprecated
	@Overwrite
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		float aoLightValue = 1.0F - BlockRenderingSettings.INSTANCE.getAmbientOcclusionLevel();
		return state.isFullCube(world, pos) ? aoLightValue * 0.2F : aoLightValue;
	}
}
