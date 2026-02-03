package net.irisshaders.iris.layer;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;

public class SetStateShard implements RenderingWrapper {
	public static final RenderingWrapper SUN = new SetStateShard("iris_sun", WorldRenderingPhase.SUN);
	public static final RenderingWrapper SUNSET = new SetStateShard("iris_sunset", WorldRenderingPhase.SUNSET);
	public static final RenderingWrapper MOON = new SetStateShard("iris_moon", WorldRenderingPhase.MOON);
	private final WorldRenderingPhase name;

	public SetStateShard(String string, WorldRenderingPhase name) {
		this.name = name;
	}

	@Override
	public void setup() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
		if (pipeline != null) {
			pipeline.setPhase(name);
		}
	}

	@Override
	public void clear() {
		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setPhase(WorldRenderingPhase.NONE)); // TODO: store previous phase
	}
}
