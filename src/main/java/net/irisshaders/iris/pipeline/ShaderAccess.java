package net.irisshaders.iris.pipeline;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.newshader.CoreWorldRenderingPipeline;
import net.irisshaders.iris.pipeline.newshader.ShaderKey;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

public class ShaderAccess {
	public static ShaderInstance getParticleTranslucentShader() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof CoreWorldRenderingPipeline) {
			ShaderInstance override = ((CoreWorldRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.PARTICLES_TRANS);

			if (override != null) {
				return override;
			}
		}

		return GameRenderer.getParticleShader();
	}
}
