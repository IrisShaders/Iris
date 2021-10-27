package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.renderer.ShaderInstance;

public interface CoreWorldRenderingPipeline extends WorldRenderingPipeline {
	ShaderInstance getBasic();
	ShaderInstance getBasicColor();
	// TODO: Shader getShadowBasic();
	// TODO: Shader getShadowBasicColor();
	ShaderInstance getTextured();
	ShaderInstance getTexturedColor();
	// TODO: Shader getShadowTextured();
	// TODO: Shader getShadowTexturedColor();

	ShaderInstance getSkyBasic();
	ShaderInstance getSkyBasicColor();
	ShaderInstance getSkyTextured();
	ShaderInstance getSkyTexturedColor();
	ShaderInstance getClouds();
	ShaderInstance getTerrain();
	ShaderInstance getTerrainCutout();
	ShaderInstance getTerrainCutoutMipped();
	ShaderInstance getEntitiesSolid();
	ShaderInstance getEntitiesCutout();
	ShaderInstance getEntitiesEyes();
	ShaderInstance getLeash();
	ShaderInstance getLightning();
	ShaderInstance getParticles();
	ShaderInstance getWeather();
	ShaderInstance getCrumbling();
	ShaderInstance getText();
	ShaderInstance getTextIntensity();
	ShaderInstance getBlock();
	ShaderInstance getBeacon();
	ShaderInstance getShadowTerrainCutout();
	ShaderInstance getShadowEntitiesCutout();
	ShaderInstance getShadowBeaconBeam();
	ShaderInstance getTranslucent();
	ShaderInstance getGlint();
	ShaderInstance getLines();
	ShaderInstance getShadowLines();
	WorldRenderingPhase getPhase();
	FrameUpdateNotifier getUpdateNotifier();
	void destroy();
}
