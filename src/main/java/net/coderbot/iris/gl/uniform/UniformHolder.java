package net.coderbot.iris.gl.uniform;

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
	int location(String name);

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value) {
		return addUniform(updateFrequency, new FloatUniform(location(name), value));
	}

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		return addUniform(updateFrequency, new FloatUniform(location(name), () -> (float) value.getAsInt()));
	}

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
		return addUniform(updateFrequency, new FloatUniform(location(name), () -> (float) value.getAsDouble()));
	}

	default UniformHolder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		return addUniform(updateFrequency, new IntUniform(location(name), value));
	}

	default UniformHolder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
		return addUniform(updateFrequency, new BooleanUniform(location(name), value));
	}

	default UniformHolder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value) {
		return addUniform(updateFrequency, new Vector3Uniform(location(name), value));
	}

	default UniformHolder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		return addUniform(updateFrequency, Vector3Uniform.truncated(location(name), value));
	}

	default UniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec3d> value) {
		return addUniform(updateFrequency, Vector3Uniform.converted(location(name), value));
	}

	default UniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value) {
		return addUniform(updateFrequency, new MatrixUniform(location(name), value));
	}
}
