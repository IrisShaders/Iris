package net.coderbot.iris.gl.uniform;

import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public interface UniformHolder {
	UniformHolder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform);

	OptionalInt location(String name);

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new FloatUniform(id, value)));

		return this;
	}

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new FloatUniform(id, () -> (float) value.getAsInt())));

		return this;
	}

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new FloatUniform(id, () -> (float) value.getAsDouble())));

		return this;
	}

	default UniformHolder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new IntUniform(id, value)));

		return this;
	}

	default UniformHolder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new BooleanUniform(id, value)));

		return this;
	}

	default UniformHolder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new Vector3Uniform(id, value)));

		return this;
	}

	default UniformHolder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, Vector3Uniform.truncated(id, value)));

		return this;
	}

	default UniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec3d> value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, Vector3Uniform.converted(id, value)));

		return this;
	}

	default UniformHolder uniform4f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new Vector4Uniform(id, value)));

		return this;
	}

	default UniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value) {
		location(name).ifPresent(id -> addUniform(updateFrequency, new MatrixUniform(id, value)));

		return this;
	}
}
