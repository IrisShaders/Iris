package net.irisshaders.iris.pipeline;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;

public enum WorldRenderingPhase {
	NONE,
	SKY,
	SUNSET,
	CUSTOM_SKY,
	SUN,
	MOON,
	STARS,
	VOID,
	TERRAIN_SOLID,
	TERRAIN_CUTOUT_MIPPED,
	TERRAIN_CUTOUT,
	ENTITIES,
	BLOCK_ENTITIES,
	DESTROY,
	OUTLINE,
	DEBUG,
	HAND_SOLID,
	TERRAIN_TRANSLUCENT,
	TRIPWIRE,
	PARTICLES,
	CLOUDS,
	RAIN_SNOW,
	WORLD_BORDER,
	HAND_TRANSLUCENT;

	public static WorldRenderingPhase fromTerrainRenderType(ChunkSectionLayerGroup renderType) {
		if (renderType == ChunkSectionLayerGroup.OPAQUE) {
			return WorldRenderingPhase.TERRAIN_SOLID;
		} else if (renderType == ChunkSectionLayerGroup.TRANSLUCENT) {
			return WorldRenderingPhase.TERRAIN_TRANSLUCENT;
		} else if (renderType == ChunkSectionLayerGroup.TRIPWIRE) {
			return WorldRenderingPhase.TRIPWIRE;
		} else {
			throw new IllegalStateException("Illegal render type!");
		}
	}
}
