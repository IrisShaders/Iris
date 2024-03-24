package net.irisshaders.iris.mixin.fantastic;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.particle.FireworkParticles$SparkParticle")
public class MixinFireworkSparkParticle extends SimpleAnimatedParticle {
	private MixinFireworkSparkParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteProvider, float upwardsAcceleration) {
		super(level, x, y, z, spriteProvider, upwardsAcceleration);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
}
