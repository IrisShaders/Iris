package net.coderbot.iris.gl.uniform;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.coderbot.iris.vendored.joml.Vector4i;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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

	default DynamicLocationalUniformHolder uniform2f(String name, Supplier<Vector2f> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.VEC2).ifPresent(id -> addDynamicUniform(new Vector2Uniform(id, value, notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform2i(String name, Supplier<Vector2i> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.VEC2I).ifPresent(id -> addDynamicUniform(new Vector2IntegerJomlUniform(id, value, notifier), notifier));

		return this;
	}

	default DynamicUniformHolder uniform3f(String name, Supplier<Vector3f> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.VEC3).ifPresent(id -> addDynamicUniform(new Vector3Uniform(id, value, notifier), notifier));

		return this;
	}

	default DynamicUniformHolder uniform4f(String name, Supplier<Vector4f> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.VEC4).ifPresent(id -> addDynamicUniform(new Vector4Uniform(id, value, notifier), notifier));

		return this;
	}

	default DynamicUniformHolder uniform4fArray(String name, Supplier<float[]> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.VEC4).ifPresent(id -> addDynamicUniform(new Vector4ArrayUniform(id, value, notifier), notifier));

		return this;
	}

	default DynamicUniformHolder uniform4i(String name, Supplier<Vector4i> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.VEC4I).ifPresent(id -> addDynamicUniform(new Vector4IntegerJomlUniform(id, value, notifier), notifier));

		return this;
	}

	default DynamicUniformHolder uniformMatrix(String name, Supplier<Matrix4f> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.MAT4).ifPresent(id -> addDynamicUniform(new MatrixUniform(id, value, notifier), notifier));

		return this;
	}

}
