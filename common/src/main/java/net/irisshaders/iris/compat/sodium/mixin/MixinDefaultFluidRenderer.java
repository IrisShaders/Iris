package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.sodium.terrain.ChunkVertexExtension;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultFluidRenderer.class)
public class MixinDefaultFluidRenderer implements VertexEncoderInterface {
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/" +
		"pipeline/DefaultFluidRenderer;updateQuad(Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;" +
		"Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/core/BlockPos;Lnet/caffeinemc/" +
		"mods/sodium/client/model/light/LightPipeline;Lnet/minecraft/core/Direction;Lnet/caffeinemc/mods/" +
		"sodium/client/model/quad/properties/ModelQuadFacing" +
		";FLnet/caffeinemc/mods/sodium/client/model/color/ColorProvider;Lnet/minecraft/world/level/material/FluidState;)V", ordinal = 2))
	private float setBrightness(float br) {
		return WorldRenderingSettings.INSTANCE.shouldDisableDirectionalShading() ? 1.0f : br;
	}

	@Unique
	private int blockId;

	@Unique
	private byte isFluid;

	@Unique
	private byte lightEmission;

	@Unique
	private int localX, localY, localZ;

	@Override
	public void beginBlock(int blockId, byte isFluid, byte lightEmission, int x, int y, int z) {
		this.blockId = blockId;
		this.isFluid = isFluid;
		this.lightEmission = lightEmission;
		this.localX = x;
		this.localY = y;
		this.localZ = z;
	}

	@Inject(method = "writeQuad", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;x:F"))
	private void iris$writeVertex(ChunkModelBuilder builder, TranslucentGeometryCollector collector, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip, CallbackInfo ci, @Local ChunkVertexEncoder.Vertex vertex) {
		((ChunkVertexExtension) vertex).iris$setData(lightEmission, isFluid, blockId, localX, localY, localZ);
	}
}
