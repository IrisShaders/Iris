package net.coderbot.iris.pipeline.programs;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.CoreWorldRenderingPipeline;
import net.coderbot.iris.pipeline.ShaderKey;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
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
