package net.coderbot.iris.gl.uniform;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector3i;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface UniformHolder {
	UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value);

	UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value);

	UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value);

	UniformHolder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value);

	UniformHolder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value);

	UniformHolder uniform2f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector2f> value);

	UniformHolder uniform2i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector2i> value);

	UniformHolder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value);
	UniformHolder uniform3i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3i> value);

	UniformHolder uniformVanilla3f(UniformUpdateFrequency updateFrequency, String name, Supplier<com.mojang.math.Vector3f> value);

	UniformHolder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value);

	UniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3d> value);

	UniformHolder uniform4f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value);
	UniformHolder uniform4fArray(UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value);

	UniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value);

	UniformHolder uniformJomlMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<net.coderbot.iris.vendored.joml.Matrix4f> value);

	UniformHolder uniformMatrixFromArray(UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value);

	UniformHolder externallyManagedUniform(String name, UniformType type);
}
