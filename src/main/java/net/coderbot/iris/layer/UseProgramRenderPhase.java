package net.coderbot.iris.layer;

import net.minecraft.client.render.RenderPhase;

public class UseProgramRenderPhase extends RenderPhase {
	public UseProgramRenderPhase(GbufferProgram program) {
		super("iris:use_program", () -> GbufferPrograms.useProgram(program), GbufferPrograms::end);
	}
}
