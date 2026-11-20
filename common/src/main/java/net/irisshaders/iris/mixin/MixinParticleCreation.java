package net.irisshaders.iris.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.irisshaders.iris.mixinterface.ParticleExtension;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class MixinParticleCreation {
	@Unique
	private static final Object2ObjectMap<ParticleType<?>, NamespacedId> PARTICLE_IDS = new Object2ObjectOpenHashMap<>();

	@Inject(method = "makeParticle", at = @At("RETURN"))
	private void iris$setParticleId(ParticleOptions options, double x, double y, double z, double xa, double ya, double za,
		CallbackInfoReturnable<Particle> cir) {
		Object2IntFunction<NamespacedId> particleIds = WorldRenderingSettings.INSTANCE.getParticleIds();
		Object2IntMap<BlockState> blockStateIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();

		if (particleIds == null || !(cir.getReturnValue() instanceof ParticleExtension particle)) {
			return;
		}

		int particleId = particleIds.applyAsInt(PARTICLE_IDS.computeIfAbsent(options.getType(), (ParticleType<?> type) -> {
			Identifier location = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
			return new NamespacedId(location.getNamespace(), location.getPath());
		}));
		int blockId = -1;

		if (options instanceof BlockParticleOption block && blockStateIds != null) {
			blockId = blockStateIds.getInt(block.getState());
		}

		particle.iris$setParticleId(ExtendedDataHelper.packParticleIds(particleId, blockId));
	}
}
