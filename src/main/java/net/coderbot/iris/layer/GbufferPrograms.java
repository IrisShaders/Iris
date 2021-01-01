package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;

public class GbufferPrograms {
	public static void useProgram(GbufferProgram program) {
		if (program == GbufferProgram.TERRAIN) {
			Iris.getPipeline().beginTerrain();
		} else if (program == GbufferProgram.TRANSLUCENT_TERRAIN) {
			Iris.getPipeline().beginTranslucentTerrain();
		} else if (program == GbufferProgram.BASIC) {
			Iris.getPipeline().beginBasic();
		} else {
			// TODO
			throw new UnsupportedOperationException("TODO");
		}
	}

	public static void end() {
		Iris.getPipeline().end();
	}
}
