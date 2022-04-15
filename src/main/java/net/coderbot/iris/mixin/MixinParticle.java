package net.coderbot.iris.mixin;

import net.coderbot.iris.fantastic.ParticleIdHolder;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Particle.class)
public class MixinParticle implements ParticleIdHolder {
	@Unique
	private NamespacedId particleId;


	@Override
	public NamespacedId getParticleId() {
		return particleId;
	}

	@Override
	public void setParticleId(ParticleType type) {
		ResourceLocation location = Registry.PARTICLE_TYPE.getKey(type);
		this.particleId = new NamespacedId(location.getNamespace(), location.getPath());
	}
}
