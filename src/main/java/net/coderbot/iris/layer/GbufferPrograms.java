package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.ValueUpdateNotifier;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.RenderStages;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.RenderType;

public class GbufferPrograms {
	private static boolean entities;
	private static boolean blockEntities;

	public static Runnable renderStageListener = null;

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

	public static void setRenderStage(RenderStages stage) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.setStage(stage));
	}

	public static int getRenderStage() {
		if (Iris.getPipelineManager().getPipeline().isPresent()) {
			return Iris.getPipelineManager().getPipelineNullable().getStage().ordinal();
		}

		return RenderStages.MC_RENDER_STAGE_NONE.ordinal();
	}

	public static ValueUpdateNotifier getRenderStageNotifier() {
		return listener -> renderStageListener = listener;
	}

	public static RenderStages refineStage(RenderType renderType) {
		if (renderType == RenderType.solid()) {
			return RenderStages.MC_RENDER_STAGE_TERRAIN_SOLID;
		} else if (renderType == RenderType.cutout()) {
			return RenderStages.MC_RENDER_STAGE_TERRAIN_CUTOUT;
		} else if (renderType == RenderType.cutoutMipped()) {
			return RenderStages.MC_RENDER_STAGE_TERRAIN_CUTOUT_MIPPED;
		} else if (renderType == RenderType.translucent()) {
			return RenderStages.MC_RENDER_STAGE_TERRAIN_TRANSLUCENT;
		} else if (renderType == RenderType.tripwire()) {
			return RenderStages.MC_RENDER_STAGE_TRIPWIRE;
		} else {
			throw new IllegalStateException("Tried to refine an unknown render stage: " + renderType.toString());
		}
	}
}
