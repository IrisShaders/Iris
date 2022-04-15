package net.coderbot.iris.mixin;

import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TerrainParticle.class)
public interface TerrainParticleAccessor {
	@Accessor
	BlockState getBlockState();
}
