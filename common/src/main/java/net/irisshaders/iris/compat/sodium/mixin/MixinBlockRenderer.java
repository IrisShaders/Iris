package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderer.class)
public class MixinBlockRenderer {
	@Unique
	private boolean hasOverride;

	@Inject(method = "renderModel", at = @At("HEAD"))
	private void iris$renderModelHead(BakedModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.getBlockTypeIds().containsKey(state.getBlock())) {
			hasOverride = true;
		}
	}

	@Inject(method = "renderModel", at = @At("TAIL"))
	private void iris$renderModelTail(BakedModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
		hasOverride = false;
	}

	@WrapOperation(method = "bufferQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;attemptPassDowngrade(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;"))
	private TerrainRenderPass iris$skipPassDowngrade(BlockRenderer instance, MutableQuadViewImpl mutableQuadView, TextureAtlasSprite quad, TerrainRenderPass sprite, Operation<TerrainRenderPass> original) {
		if (hasOverride) return null;

		return original.call(instance, mutableQuadView, quad, sprite);
	}
}
