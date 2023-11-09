package net.coderbot.iris.gl.uniform;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector3i;

import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface LocationalUniformHolder extends UniformHolder {
	LocationalUniformHolder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform);

	OptionalInt location(String name, UniformType type);

	@Override
	default LocationalUniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value) {
		location(name, UniformType.FLOAT).ifPresent(id -> addUniform(updateFrequency, new FloatUniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		location(name, UniformType.FLOAT).ifPresent(id -> addUniform(updateFrequency, new FloatUniform(id, () -> (float) value.getAsInt())));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
		location(name, UniformType.FLOAT).ifPresent(id -> addUniform(updateFrequency, new FloatUniform(id, () -> (float) value.getAsDouble())));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		location(name, UniformType.INT).ifPresent(id -> addUniform(updateFrequency, new IntUniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
		location(name, UniformType.INT).ifPresent(id -> addUniform(updateFrequency, new BooleanUniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform2f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector2f> value) {
		location(name, UniformType.VEC2).ifPresent(id -> addUniform(updateFrequency, new Vector2Uniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform2i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector2i> value) {
		location(name, UniformType.VEC2I).ifPresent(id -> addUniform(updateFrequency, new Vector2IntegerJomlUniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value) {
		location(name, UniformType.VEC3).ifPresent(id -> addUniform(updateFrequency, new Vector3Uniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform3i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3i> value) {
		location(name, UniformType.VEC3I).ifPresent(id -> addUniform(updateFrequency, new Vector3IntegerUniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		location(name, UniformType.VEC3).ifPresent(id -> addUniform(updateFrequency, Vector3Uniform.truncated(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3d> value) {
		location(name, UniformType.VEC3).ifPresent(id -> addUniform(updateFrequency, Vector3Uniform.converted(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform4f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		location(name, UniformType.VEC4).ifPresent(id -> addUniform(updateFrequency, new Vector4Uniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniform4fArray(UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value) {
		location(name, UniformType.VEC4).ifPresent(id -> addUniform(updateFrequency, new Vector4ArrayUniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value) {
		location(name, UniformType.MAT4).ifPresent(id -> addUniform(updateFrequency, new MatrixUniform(id, value)));

		return this;
	}

	@Override
	default LocationalUniformHolder uniformMatrixFromArray(UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value) {
		location(name, UniformType.MAT4).ifPresent(id -> addUniform(updateFrequency, new MatrixFromFloatArrayUniform(id, value)));

		return this;
	}
}
