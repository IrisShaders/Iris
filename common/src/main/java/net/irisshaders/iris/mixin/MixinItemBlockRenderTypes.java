package net.irisshaders.iris.mixin;

import net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.BlockRenderType;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public class MixinItemBlockRenderTypes {
	@Unique
	private static final ChunkSectionLayer[] LAYER_SET_VANILLA;

	static {
		LAYER_SET_VANILLA = new ChunkSectionLayer[BlockRenderType.values().length];
		for (int i = 0; i < BlockRenderType.values().length; i++) {
			LAYER_SET_VANILLA[i] = BlockMaterialMapping.convertBlockToRenderType(BlockRenderType.values()[i]);
		}
	}

	@Inject(method = "getChunkRenderType", at = @At("HEAD"), cancellable = true)
	private static void iris$setCustomRenderType(BlockState arg, CallbackInfoReturnable<ChunkSectionLayer> cir) {
		BlockRenderType type = WorldRenderingSettings.INSTANCE.getBlockTypeIds().get(arg.getBlock());
		if (type != null) {
			cir.setReturnValue(LAYER_SET_VANILLA[type.ordinal()]);
		}
	}
}
