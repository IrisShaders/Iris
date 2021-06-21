package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.render.Shader;

public interface CoreWorldRenderingPipeline extends WorldRenderingPipeline {
	Shader getSkyBasic();
	Shader getSkyBasicColor();
	Shader getSkyTextured();
	Shader getSkyTexturedColor();
	Shader getTerrain();
	Shader getTerrainCutout();
	Shader getTerrainCutoutMipped();
	Shader getEntitiesSolid();
	Shader getEntitiesCutout();
	Shader getEntitiesEyes();
	Shader getLeash();
	Shader getLightning();
	Shader getParticles();
	Shader getWeather();
	Shader getCrumbling();
	Shader getText();
	Shader getBlock();
	Shader getShadowTerrainCutout();
	Shader getShadowEntitiesCutout();
	Shader getTranslucent();
	WorldRenderingPhase getPhase();
	FrameUpdateNotifier getUpdateNotifier();
	void destroy();
}
