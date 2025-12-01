package net.irisshaders.iris.mixin.forge;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.platform.LayerSet;
import net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.BlockRenderType;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.BlockModelPartExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(BlockModelPartExtension.class)
public interface MixinItemBlockRenderTypes {
	@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
	private void iris$setCustomRenderType(BlockState arg, CallbackInfoReturnable<ChunkSectionLayer> cir) {
		BlockRenderType type = WorldRenderingSettings.INSTANCE.getBlockTypeIds().get(arg.getBlock());
		if (type != null) {
			cir.setReturnValue(LayerSet.LAYER_SET_VANILLA[type.ordinal()]);
		}
	}
}
