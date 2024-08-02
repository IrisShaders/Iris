package net.irisshaders.iris.compat.sodium.mixin.block_id;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.services.PlatformModelAccess;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.BlockRenderType;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.Map;

@Mixin(BlockRenderer.class)
public class MixinBlockRenderer {
	@Unique
	private static Iterable[] SINGLETONS = new Iterable[BlockRenderType.values().length];

	static {
		BlockRenderType[] values = BlockRenderType.values();
		for (int i = 0; i < values.length; i++) {
			BlockRenderType type = values[i];
			SINGLETONS[type.ordinal()] = Collections.singleton(BlockMaterialMapping.convertBlockToRenderType(type));
		}
	}


	@Redirect(remap = false, method = "renderModel", at = @At(remap = true, value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformModelAccess;getModelRenderTypes(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;Lnet/caffeinemc/mods/sodium/client/services/SodiumModelData;)Ljava/lang/Iterable;"))
	private Iterable<RenderType> iris$redirectRenderTypes(PlatformModelAccess instance, BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, RandomSource randomSource, SodiumModelData sodiumModelData) {
		Map<Block, BlockRenderType> idMap = WorldRenderingSettings.INSTANCE.getBlockTypeIds();
		if (idMap != null) {
			BlockRenderType type = idMap.get(blockState.getBlock());
			if (type != null) {
				return SINGLETONS[type.ordinal()];
			}
		}

		return instance.getModelRenderTypes(blockAndTintGetter, bakedModel, blockState, blockPos, randomSource, sodiumModelData);
	}
}
