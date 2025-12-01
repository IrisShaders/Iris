package net.irisshaders.iris.mixin.fantastic;

import net.irisshaders.iris.fantastic.IrisParticleRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TerrainParticle.class)
public class MixinTerrainParticle {
	@Unique
	private boolean isOpaque;

	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At("RETURN"))
	private void iris$resolveTranslucency(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
		ChunkSectionLayer type = ItemBlockRenderTypes.getChunkRenderType(blockState);

		if (type == ChunkSectionLayer.SOLID || type == ChunkSectionLayer.CUTOUT || type == ChunkSectionLayer.CUTOUT_MIPPED) {
			isOpaque = true;
		}
	}

	@Inject(method = "getLayer", at = @At("HEAD"), cancellable = true)
	private void iris$overrideParticleSheet(CallbackInfoReturnable<SingleQuadParticle.Layer> cir) {
		if (isOpaque) {
			cir.setReturnValue(IrisParticleRenderTypes.TERRAIN_OPAQUE);
		}
	}
}
