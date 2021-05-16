package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;

public class GbufferPrograms {
	public static void push(GbufferProgram program) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline != null) {
			pipeline.pushProgram(program);
		}
	}

	public static void pop(GbufferProgram program) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline != null) {
			pipeline.popProgram(program);
		}
	}
}
