package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.IrisParticleRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$resolveTranslucency(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState blockState, CallbackInfo callback) {
		RenderType type = ItemBlockRenderTypes.getChunkRenderType(blockState);

		if (type == RenderType.solid() || type == RenderType.cutout() || type == RenderType.cutoutMipped()) {
			isOpaque = true;
		}
	}

	@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
	private void iris$overrideParticleRenderType(CallbackInfoReturnable<ParticleRenderType> cir) {
		if (isOpaque) {
			cir.setReturnValue(IrisParticleRenderTypes.OPAQUE_TERRAIN);
		}
	}
}
