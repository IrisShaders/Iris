package net.irisshaders.iris.pipeline;

import net.irisshaders.iris.Iris;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

public class ShaderAccess {
	public static ShaderInstance getParticleTranslucentShader() {
		if (Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldOverrideShaders).orElse(false)) {
			ShaderInstance override = Iris.getPipelineManager().getPipelineNullable().getShaderMap().getShader(ShaderKey.PARTICLES_TRANS);

			if (override != null) {
				return override;
			}
		}

		return GameRenderer.getParticleShader();
	}
}
