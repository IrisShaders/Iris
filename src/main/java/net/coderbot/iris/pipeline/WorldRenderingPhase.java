package net.coderbot.iris.pipeline;

import net.minecraft.client.renderer.RenderType;

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

	public static WorldRenderingPhase fromTerrainRenderType(RenderType renderType) {
		if (renderType == RenderType.solid()) {
			return WorldRenderingPhase.TERRAIN_SOLID;
		} else if (renderType == RenderType.cutout()) {
			return WorldRenderingPhase.TERRAIN_CUTOUT;
		} else if (renderType == RenderType.cutoutMipped()) {
			return WorldRenderingPhase.TERRAIN_CUTOUT_MIPPED;
		} else if (renderType == RenderType.translucent()) {
			return WorldRenderingPhase.TERRAIN_TRANSLUCENT;
		} else if (renderType == RenderType.tripwire()) {
			return WorldRenderingPhase.TRIPWIRE;
		} else {
			throw new IllegalStateException("Illegal render type!");
		}
	}
}
