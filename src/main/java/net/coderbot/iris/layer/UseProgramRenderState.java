package net.coderbot.iris.layer;

import java.util.Objects;
import net.minecraft.client.renderer.RenderStateShard;

public class UseProgramRenderState extends RenderStateShard {
	private GbufferProgram program;

	public UseProgramRenderState(GbufferProgram program) {
		super("iris:use_program", () -> GbufferPrograms.push(program), () -> GbufferPrograms.pop(program));

		this.program = program;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}

		if (object.getClass() != this.getClass()) {
			return false;
		}

		UseProgramRenderState other = (UseProgramRenderState) object;

		return Objects.equals(this.program, other.program);
	}
}
