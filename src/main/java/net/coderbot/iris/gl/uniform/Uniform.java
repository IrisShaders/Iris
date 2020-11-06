package net.coderbot.iris.gl.uniform;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import org.lwjgl.opengl.GL21;

public abstract class Uniform {
	protected final int location;

	Uniform(int location) {
		this.location = location;
	}

	abstract void update();

	public static Uniform of(int program, String name, IntSupplier value) {
		return new IntUniform(GL21.glGetUniformLocation(program, name), value);
	}

	public static Uniform of(int program, String name, BooleanSupplier value) {
		return new BooleanUniform(GL21.glGetUniformLocation(program, name), value);
	}
}
