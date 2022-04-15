package net.coderbot.iris.fantastic;

import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.core.particles.ParticleType;

public interface ParticleIdHolder {
	NamespacedId getParticleId();
	void setParticleId(ParticleType type);
}
