package net.irisshaders.iris.gl.uniform;

import java.util.function.BooleanSupplier;

public class BooleanUniform extends IntUniform {
	BooleanUniform(int location, BooleanSupplier value) {
		super(location, () -> value.getAsBoolean() ? 1 : 0);
	}
}
