package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.Shader;

public interface CoreWorldRenderingPipeline extends WorldRenderingPipeline {
	Shader getSkyBasic();
	Shader getSkyBasicColor();
	Shader getSkyTextured();
	Shader getTerrain();
	Shader getTerrainCutout();
	Shader getTerrainCutoutMipped();
	Shader getShadowTerrainCutout();
	Shader getTranslucent();
	WorldRenderingPhase getPhase();
	void destroy();
}
