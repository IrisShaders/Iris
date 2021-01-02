package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;

public class GbufferPrograms {
	public static void useProgram(GbufferProgram program) {
		Iris.getPipeline().useProgram(program);
	}

	public static void end() {
		Iris.getPipeline().end();
	}
}
