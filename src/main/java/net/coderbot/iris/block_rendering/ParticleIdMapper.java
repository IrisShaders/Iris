package net.coderbot.iris.block_rendering;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.coderbot.iris.fantastic.ParticleIdHolder;
import net.coderbot.iris.mixin.TerrainParticleAccessor;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;

public class ParticleIdMapper {
	public static ParticleIdMapper instance = new ParticleIdMapper();

	private Object2IntFunction<NamespacedId> idMap = new Object2IntOpenHashMap<>();

	public int currentParticle = 0;
	public int currentBlockParticle = 0;

	public void setIdMap(Object2IntFunction<NamespacedId> idMap) {
		this.idMap = idMap;
	}

	public void setCurrentParticle(Particle particle) {
		this.currentParticle = idMap.applyAsInt(((ParticleIdHolder) particle).getParticleId());
		if (particle instanceof TerrainParticle && BlockRenderingSettings.INSTANCE.getBlockStateIds() != null) {
			this.currentBlockParticle = BlockRenderingSettings.INSTANCE.getBlockStateIds().applyAsInt(((TerrainParticleAccessor) particle).getBlockState());
		}
	}

	public void resetParticle() {
		this.currentParticle = 0;
		this.currentBlockParticle = 0;
	}
}
