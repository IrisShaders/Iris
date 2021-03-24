package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.IrisParticleTextureSheets;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockDustParticle.class)
public class MixinBlockDustParticle {
	@Unique
	private boolean isOpaque;

	@Inject(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;)V", at = @At("RETURN"))
	private void iris$resolveTranslucency(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState blockState, CallbackInfo callback) {
		RenderLayer layer = RenderLayers.getBlockLayer(blockState);

		if (layer == RenderLayer.getSolid() || layer == RenderLayer.getCutout() || layer == RenderLayer.getCutoutMipped()) {
			isOpaque = true;
		}
	}

	@Inject(method = "getType()Lnet/minecraft/client/particle/ParticleTextureSheet;", at = @At("HEAD"), cancellable = true)
	private void iris$overrideParticleSheet(CallbackInfoReturnable<ParticleTextureSheet> cir) {
		if (isOpaque) {
			cir.setReturnValue(IrisParticleTextureSheets.OPAQUE_TERRAIN_SHEET);
		}
	}
}
