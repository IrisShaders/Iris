package net.coderbot.iris.gl.uniform;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public interface DynamicLocationalUniformHolder extends LocationalUniformHolder, DynamicUniformHolder {
	DynamicLocationalUniformHolder addDynamicUniform(Uniform uniform, ValueUpdateNotifier notifier);

	default DynamicLocationalUniformHolder uniform1f(String name, FloatSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.FLOAT).ifPresent(id -> addDynamicUniform(new FloatUniform(id, value, notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform1f(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.FLOAT).ifPresent(id -> addDynamicUniform(new FloatUniform(id, () -> (float) value.getAsInt(), notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform1f(String name, DoubleSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.FLOAT).ifPresent(id -> addDynamicUniform(new FloatUniform(id, () -> (float) value.getAsDouble(), notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform1i(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.INT).ifPresent(id -> addDynamicUniform(new IntUniform(id, value, notifier), notifier));

		return this;
	}
}
