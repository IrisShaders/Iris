package net.coderbot.iris.mixin.fantastic;

import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.particle.FireworksSparkParticle$Explosion")
public class MixinFireworkSparkParticle extends AnimatedParticle {
	private MixinFireworkSparkParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, float upwardsAcceleration) {
		super(world, x, y, z, spriteProvider, upwardsAcceleration);
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}
}
