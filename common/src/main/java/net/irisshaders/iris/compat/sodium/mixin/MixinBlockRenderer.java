package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.sodium.terrain.ChunkVertexExtension;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderer.class)
public class MixinBlockRenderer implements VertexEncoderInterface {
	@Unique
	private boolean hasOverride;

	@Unique
	private int blockId;

	@Unique
	private byte isFluid;

	@Unique
	private byte lightEmission;

	@Unique
	private int localX, localY, localZ;

	@Inject(method = "renderModel", at = @At("HEAD"))
	private void iris$renderModelHead(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.getBlockTypeIds().containsKey(state.getBlock())) {
			hasOverride = true;
		}
	}

	@Inject(method = "renderModel", at = @At("TAIL"))
	private void iris$renderModelTail(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
		hasOverride = false;
	}

	@WrapOperation(method = "bufferQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;attemptPassDowngrade(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;"))
	private TerrainRenderPass iris$skipPassDowngrade(BlockRenderer instance, TextureAtlasSprite textureAtlasSprite, TerrainRenderPass sprite, Operation<TerrainRenderPass> original) {
		if (hasOverride) return null;

		return original.call(instance, textureAtlasSprite, sprite);
	}

	@Override
	public void beginBlock(int blockId, byte isFluid, byte lightEmission, int x, int y, int z) {
		this.blockId = blockId;
		this.isFluid = isFluid;
		this.lightEmission = lightEmission;
		this.localX = x;
		this.localY = y;
		this.localZ = z;
	}

	@Inject(method = "bufferQuad", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;x:F"))
	private void iris$writeVertex(MutableQuadViewImpl quad, float[] brightnesses, Material material, CallbackInfo ci, @Local ChunkVertexEncoder.Vertex vertex) {
		((ChunkVertexExtension) vertex).iris$setData(lightEmission, isFluid, blockId, localX, localY, localZ);
	}
}
