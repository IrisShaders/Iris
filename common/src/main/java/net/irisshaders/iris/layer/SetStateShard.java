package net.irisshaders.iris.layer;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.RenderStateShard;

public class SetStateShard extends RenderStateShard {
	public static final RenderStateShard SUN = new SetStateShard("iris_sun", WorldRenderingPhase.SUN);
	public static final RenderStateShard SUNSET = new SetStateShard("iris_sunset", WorldRenderingPhase.SUNSET);
	public static final RenderStateShard MOON = new SetStateShard("iris_moon", WorldRenderingPhase.MOON);

	public SetStateShard(String string, WorldRenderingPhase name) {
		super(string, () -> {
			WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
			if (pipeline != null) {
				pipeline.setPhase(name);
			}
		}, () -> {
			Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setPhase(WorldRenderingPhase.NONE)); // TODO: store previous phase
		});
	}
}
