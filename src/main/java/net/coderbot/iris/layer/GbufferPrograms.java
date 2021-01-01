package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;

import net.minecraft.client.render.RenderLayer;

public class GbufferPrograms {
	public static void useProgram(GbufferProgram program) {
		if (program == GbufferProgram.TERRAIN) {
			Iris.getPipeline().beginTerrainLayer(RenderLayer.getSolid());
		} else if (program == GbufferProgram.TRANSLUCENT_TERRAIN) {
			Iris.getPipeline().beginTerrainLayer(RenderLayer.getTranslucent());
		} else {
			// TODO
			throw new UnsupportedOperationException("TODO");
		}
	}

	public static void end() {
		Iris.getPipeline().end();
	}
}
