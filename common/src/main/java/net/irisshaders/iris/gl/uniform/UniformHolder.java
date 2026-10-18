package net.irisshaders.iris.gl.uniform;

import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface UniformHolder {
	UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value);

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		return this.uniform1f(updateFrequency, name, (FloatSupplier) value::getAsInt);
	}

	default UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
		return this.uniform1f(updateFrequency, name, (FloatSupplier) () -> (float) value.getAsDouble());
	}

	UniformHolder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value);

	UniformHolder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value);

	UniformHolder uniform2f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector2f> value);

	UniformHolder uniform2i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector2i> value);

	UniformHolder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value);

	UniformHolder uniform3i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3i> value);

	default UniformHolder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		Vector3f converted = new Vector3f();
		return this.uniform3f(updateFrequency, name, () -> {
			Vector4f vector = value.get();
			return converted.set(vector.x, vector.y, vector.z);
		});
	}

	default UniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3d> value) {
		Vector3f converted = new Vector3f();
		return this.uniform3f(updateFrequency, name, () -> {
			Vector3d vector = value.get();
			return converted.set((float) vector.x, (float) vector.y, (float) vector.z);
		});
	}

	UniformHolder uniform4f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value);

	default UniformHolder uniform4fArray(UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value) {
		Vector4f converted = new Vector4f();
		return this.uniform4f(updateFrequency, name, () -> converted.set(value.get()));
	}

	UniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4fc> value);

	UniformHolder uniformMatrix3(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix3fc> value);

	default UniformHolder uniformMatrixFromArray(UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value) {
		Matrix4f converted = new Matrix4f();
		return this.uniformMatrix(updateFrequency, name, () -> converted.set(value.get()));
	}

	UniformHolder externallyManagedUniform(String name, UniformType type);
}
