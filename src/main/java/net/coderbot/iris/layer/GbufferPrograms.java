package net.coderbot.iris.layer;

import net.coderbot.iris.Iris;

public class GbufferPrograms {
	public static void push(GbufferProgram program) {
		Iris.getPipeline().pushProgram(program);
	}

	public static void pop() {
		Iris.getPipeline().popProgram();
	}
}
