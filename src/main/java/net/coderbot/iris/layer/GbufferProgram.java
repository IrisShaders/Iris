package net.coderbot.iris.layer;

import net.coderbot.iris.pipeline.RenderStages;

public enum GbufferProgram {
	NONE(RenderStages.MC_RENDER_STAGE_NONE),
	BASIC(RenderStages.MC_RENDER_STAGE_OUTLINE), // We can't assume anything here
	TEXTURED(RenderStages.MC_RENDER_STAGE_PARTICLES), //TODO: Is this correct?
	TEXTURED_LIT(null), // We can't assume anything here, since this can be both the world border and lit particles
	SKY_BASIC(null), // We'll manually set the stage in MixinLevelRenderer for the sky
	SKY_TEXTURED(null), // We'll manually set the stage in MixinLevelRenderer for the sun/moon
	CLOUDS(RenderStages.MC_RENDER_STAGE_CLOUDS),
	TERRAIN(null), // We'll manually set the stage in MixinLevelRenderer for terrain
	TRANSLUCENT_TERRAIN(RenderStages.MC_RENDER_STAGE_TERRAIN_TRANSLUCENT),
	DAMAGED_BLOCKS(RenderStages.MC_RENDER_STAGE_DESTROY),
	BLOCK_ENTITIES(RenderStages.MC_RENDER_STAGE_BLOCK_ENTITIES),
	BEACON_BEAM(RenderStages.MC_RENDER_STAGE_NONE), // Beacon beams do not have a set render stage
	ENTITIES(RenderStages.MC_RENDER_STAGE_ENTITIES),
	ENTITIES_GLOWING(null), // We don't use this program
	ARMOR_GLINT(RenderStages.MC_RENDER_STAGE_NONE),
	EYES(RenderStages.MC_RENDER_STAGE_ENTITIES), //TODO: Is this correct?
	HAND(RenderStages.MC_RENDER_STAGE_HAND_SOLID),
	HAND_TRANSLUCENT(RenderStages.MC_RENDER_STAGE_HAND_TRANSLUCENT),
	WEATHER(RenderStages.MC_RENDER_STAGE_RAIN_SNOW);

	RenderStages stage;

	GbufferProgram(RenderStages stage) {
		this.stage = stage;
	}

	public RenderStages getStage() {
		return stage;
	}
}
