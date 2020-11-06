package net.coderbot.iris.gl.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public class ProgramUniforms {
	private final int program;
	private final List<Uniform> perTick;
	private final List<Uniform> perFrame;

	public ProgramUniforms(int program) {
		this.program = program;

		perTick = new ArrayList<>();
		perFrame = new ArrayList<>();
	}

	private void addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
		switch (updateFrequency) {
			case PER_TICK:
				perTick.add(uniform);
				break;
			case PER_FRAME:
				perFrame.add(uniform);
				break;
		}
	}

	public void uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		addUniform(updateFrequency, Uniform.of(program, name, value));
	}

	public void uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
		addUniform(updateFrequency, Uniform.of(program, name, value));
	}

	// TODO

	public void tick() {
		for (Uniform uniform : perTick) {
			uniform.update();
		}
	}
}
