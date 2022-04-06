package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.RenderType;

public class GbufferPrograms {
	private static boolean entities;
	private static boolean blockEntities;
	private static Runnable phaseChangeListener;

	/**
	 * Uses additional information to choose a more specific (and appropriate) GbufferProgram.
	 */
	private static GbufferProgram refine(GbufferProgram program, boolean push) {
		if (program == GbufferProgram.ENTITIES || program == GbufferProgram.TERRAIN || program == GbufferProgram.TRANSLUCENT_TERRAIN) {
			if (HandRenderer.INSTANCE.isActive()) {
				return HandRenderer.INSTANCE.isRenderingSolid() ? GbufferProgram.HAND : GbufferProgram.HAND_TRANSLUCENT;
			} else if (entities) {
				return GbufferProgram.ENTITIES;
			} else if (blockEntities) {
				return GbufferProgram.BLOCK_ENTITIES;
			}
		}

		if (program == GbufferProgram.DAMAGED_BLOCKS) {
			setPhase(push ? WorldRenderingPhase.DESTROY : WorldRenderingPhase.NONE);
		} else if (program == GbufferProgram.LINES) {
			setPhase(push ? WorldRenderingPhase.OUTLINE : WorldRenderingPhase.NONE);
		}

		return program;
	}

	public static WorldRenderingPhase refineTerrainPhase(RenderType renderType) {
		if (renderType == RenderType.solid()) {
			return WorldRenderingPhase.TERRAIN_SOLID;
		} else if (renderType == RenderType.cutout()) {
			return WorldRenderingPhase.TERRAIN_CUTOUT;
		} else if (renderType == RenderType.cutoutMipped()) {
			return WorldRenderingPhase.TERRAIN_CUTOUT_MIPPED;
		} else if (renderType == RenderType.translucent()) {
			return WorldRenderingPhase.TERRAIN_TRANSLUCENT;
		} else if (renderType == RenderType.tripwire()) {
			return WorldRenderingPhase.TRIPWIRE;
		} else {
			throw new IllegalStateException("Illegal render type!");
		}
	}

	public static void beginEntities() {
		if (entities || blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call beginEntities when entities = "
					+ entities + ", blockEntities = " + blockEntities);
		}

		setPhase(WorldRenderingPhase.ENTITIES);
		entities = true;
	}

	public static boolean isRenderingEntities() {
		return entities;
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

	public static boolean isRenderingBlockEntities() {
		return blockEntities;
	}

	public static void endBlockEntities() {
		if (!blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call endBlockEntities when blockEntities = false");
		}

		setPhase(WorldRenderingPhase.NONE);
		blockEntities = false;
	}

	public static void push(GbufferProgram program) {
		program = refine(program, true);

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.pushProgram(program);
		}
	}

	public static void pop(GbufferProgram program) {
		program = refine(program, false);

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.popProgram(program);
		}
	}

	public static WorldRenderingPhase getCurrentPhase() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			return pipeline.getPhase();
		} else {
			return WorldRenderingPhase.NONE;
		}
	}

	public static void setPhase(WorldRenderingPhase phase) {
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
