package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;

public class GbufferPrograms {
	private static boolean entities;
	private static boolean blockEntities;
	private static Runnable phaseChangeListener;

	public static void beginEntities() {
		if (entities || blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call beginEntities when entities = "
					+ entities + ", blockEntities = " + blockEntities);
		}

		setPhase(WorldRenderingPhase.ENTITIES);
		entities = true;
	}

	public static void endEntities() {
		if (!entities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call endEntities when entities = false");
		}

		setPhase(WorldRenderingPhase.NONE);
		entities = false;
	}

	public static void beginBlockEntities() {

		if (entities || blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call beginBlockEntities when entities = "
					+ entities + ", blockEntities = " + blockEntities);
		}

		setPhase(WorldRenderingPhase.BLOCK_ENTITIES);
		blockEntities = true;
	}

	public static void endBlockEntities() {
		if (!blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call endBlockEntities when blockEntities = false");
		}

		setPhase(WorldRenderingPhase.NONE);
		blockEntities = false;
	}

	public static WorldRenderingPhase getCurrentPhase() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			return pipeline.getPhase();
		} else {
			return WorldRenderingPhase.NONE;
		}
	}

	private static void setPhase(WorldRenderingPhase phase) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.setPhase(phase);
		}
	}

	public static void runPhaseChangeNotifier() {
		if (phaseChangeListener != null) {
			phaseChangeListener.run();
		}
	}

	static {
		StateUpdateNotifiers.phaseChangeNotifier = listener -> phaseChangeListener = listener;
	}

	public static void init() {
		// Empty initializer to run static
	}
}
