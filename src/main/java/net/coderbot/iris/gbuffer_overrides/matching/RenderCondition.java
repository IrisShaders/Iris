package net.coderbot.iris.gbuffer_overrides.matching;

public enum RenderCondition {
	DEFAULT,
	SKY,
	TERRAIN_OPAQUE,
	TERRAIN_TRANSLUCENT,
	CLOUDS,
	DESTROY,
	BLOCK_ENTITIES,
	BEACON_BEAM,
	ENTITIES,
	ENTITIES_TRANSLUCENT,
	GLINT,
	ENTITY_EYES,
	HAND_OPAQUE,
	HAND_TRANSLUCENT,
	RAIN_SNOW,
	WORLD_BORDER,
	// NB: Must be last due to implementation details of DeferredWorldRenderingPipeline
	SHADOW
}
