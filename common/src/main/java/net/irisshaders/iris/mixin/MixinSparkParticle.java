package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.mixinterface.ParticleExtension;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.particle.FireworkParticles$SparkParticle")
public class MixinSparkParticle {
	// Trail sparks skip ParticleEngine#makeParticle, so they take the ID of the spark that spawned them.
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V"))
	private void iris$copyParticleId(ParticleEngine engine, Particle trail, Operation<Void> original) {
		((ParticleExtension) trail).iris$setParticleId(((ParticleExtension) (Object) this).iris$getParticleId());
		original.call(engine, trail);
	}
}
