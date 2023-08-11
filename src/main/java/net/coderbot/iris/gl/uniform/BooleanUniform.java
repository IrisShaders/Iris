package net.coderbot.iris.gl.uniform;

import java.util.function.BooleanSupplier;

public class BooleanUniform extends IntUniform {
	BooleanUniform(String name, int location, BooleanSupplier value) {
		super(name, location, () -> value.getAsBoolean() ? 1 : 0);
	}

}
