package net.irisshaders.iris.pipeline.programs;

import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;

public class ShaderOverrides {
	public static ShaderKey getSkyShader(WorldRenderingPipeline pipeline) {
		if (isSky(pipeline)) {
			return ShaderKey.SKY_BASIC;
		} else {
			return ShaderKey.BASIC;
		}
	}

	public static ShaderKey getSkyTexShader(WorldRenderingPipeline pipeline) {
		if (isSky(pipeline)) {
			return ShaderKey.SKY_TEXTURED;
		} else {
			return ShaderKey.TEXTURED;
		}
	}

	public static ShaderKey getSkyTexColorShader(WorldRenderingPipeline pipeline) {
		if (isSky(pipeline)) {
			return ShaderKey.SKY_TEXTURED_COLOR;
		} else {
			return ShaderKey.TEXTURED_COLOR;
		}
	}

	public static ShaderKey getSkyColorShader(WorldRenderingPipeline pipeline) {
		if (isSky(pipeline)) {
			return ShaderKey.SKY_BASIC_COLOR;
		} else {
			return ShaderKey.BASIC_COLOR;
		}
	}

	public static boolean isBlockEntities(WorldRenderingPipeline pipeline) {
		return pipeline != null && pipeline.getPhase() == WorldRenderingPhase.BLOCK_ENTITIES;
	}

	public static boolean isEntities(WorldRenderingPipeline pipeline) {
		return pipeline != null && pipeline.getPhase() == WorldRenderingPhase.ENTITIES;
	}

	public static boolean isSky(WorldRenderingPipeline pipeline) {
		if (pipeline != null) {
			return switch (pipeline.getPhase()) {
				case CUSTOM_SKY, SKY, SUNSET, SUN, STARS, VOID, MOON -> true;
				default -> false;
			};
		} else {
			return false;
		}
	}

	// ignored: getRendertypeEndGatewayShader (we replace the end portal rendering for shaders)
	// ignored: getRendertypeEndPortalShader (we replace the end portal rendering for shaders)

	public static boolean isPhase(WorldRenderingPipeline pipeline, WorldRenderingPhase phase) {
		if (pipeline != null) {
			return pipeline.getPhase() == phase;
		} else {
			return false;
		}
	}
}
