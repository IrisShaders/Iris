package net.coderbot.iris.gl.uniform;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface UniformHolder {
	UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value);

	UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value);

	UniformHolder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value);

	UniformHolder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value);

	UniformHolder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value);

	UniformHolder uniform2f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec2> value);

	UniformHolder uniform2i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec2> value);

	UniformHolder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value);

	UniformHolder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value);

	UniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec3> value);

	UniformHolder uniform4f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value);

	UniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value);

	UniformHolder uniformJomlMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<net.coderbot.iris.vendored.joml.Matrix4f> value);

	UniformHolder uniformMatrixFromArray(UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value);

	UniformHolder externallyManagedUniform(String name, UniformType type);
}
