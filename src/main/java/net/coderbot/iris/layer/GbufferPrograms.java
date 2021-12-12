package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;

public class GbufferPrograms {
	private static boolean entities;
	private static boolean blockEntities;

	/**
	 * Uses additional information to choose a more specific (and appropriate) GbufferProgram.
	 */
	private static GbufferProgram refine(GbufferProgram program) {
		if (program == GbufferProgram.ENTITIES || program == GbufferProgram.TERRAIN || program == GbufferProgram.TRANSLUCENT_TERRAIN) {
			if (HandRenderer.INSTANCE.isActive()) {
				return HandRenderer.INSTANCE.isRenderingSolid() ? GbufferProgram.HAND : GbufferProgram.HAND_TRANSLUCENT;
			} else if (entities) {
				return GbufferProgram.ENTITIES;
			} else if (blockEntities) {
				return GbufferProgram.BLOCK_ENTITIES;
			}
		}

		return program;
	}

	public static void beginEntities() {
		if (entities || blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call beginEntities when entities = "
					+ entities + ", blockEntities = " + blockEntities);
		}

		entities = true;
	}

	public static boolean isRenderingEntities() {
		return entities;
	}

	public static void endEntities() {
		if (!entities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call endEntities when entities = false");
		}

		entities = false;
	}

	public static void beginBlockEntities() {
		if (entities || blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call beginBlockEntities when entities = "
					+ entities + ", blockEntities = " + blockEntities);
		}

		blockEntities = true;
	}

	public static boolean isRenderingBlockEntities() {
		return blockEntities;
	}

	public static void endBlockEntities() {
		if (!blockEntities) {
			throw new IllegalStateException("GbufferPrograms in weird state, tried to call endBlockEntities when blockEntities = false");
		}

		blockEntities = false;
	}

	public static void push(GbufferProgram program) {
		program = refine(program);

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.pushProgram(program);
		}
	}

	public static void pop(GbufferProgram program) {
		program = refine(program);

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.popProgram(program);
		}
	}
}
