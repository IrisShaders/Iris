package net.irisshaders.iris.layer;

import java.util.Objects;
import net.minecraft.client.renderer.RenderStateShard;

public class UseProgramRenderStateShard extends RenderStateShard {
	private final GbufferProgram program;

	public UseProgramRenderStateShard(GbufferProgram program) {
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

		UseProgramRenderStateShard other = (UseProgramRenderStateShard) object;

		return Objects.equals(this.program, other.program);
	}
}
