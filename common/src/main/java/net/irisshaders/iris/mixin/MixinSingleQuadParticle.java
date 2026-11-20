package net.irisshaders.iris.mixin;

import net.irisshaders.iris.mixinterface.ParticleExtension;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.particle.SingleQuadParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SingleQuadParticle.class)
public class MixinSingleQuadParticle implements ParticleExtension {
	@Unique
	private int iris$particleId;

	@Inject(method = "extractRotatedQuad(Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState;Lorg/joml/Quaternionf;FFFF)V", at = @At("HEAD"))
	private void iris$captureParticleId(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedParticle(iris$particleId);
	}

	@Override
	public int iris$getParticleId() {
		return this.iris$particleId;
	}

	@Override
	public void iris$setParticleId(int id) {
		this.iris$particleId = id;
	}
}
