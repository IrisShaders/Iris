package net.coderbot.iris.mixin;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public class MixinItemBlockRenderTypes {
	@Inject(method = "getChunkRenderType", at = @At("HEAD"), cancellable = true)
	private static void iris$setCustomRenderType(BlockState arg, CallbackInfoReturnable<RenderType> cir) {
		if (BlockRenderingSettings.INSTANCE.getBlockTypeIds() != null) {
			RenderType type = BlockRenderingSettings.INSTANCE.getBlockTypeIds().get(arg.getBlock());
			if (type != null) {
				cir.setReturnValue(type);
			}
		}
	}
}
