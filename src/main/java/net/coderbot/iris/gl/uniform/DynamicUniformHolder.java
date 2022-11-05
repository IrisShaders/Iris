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

public interface DynamicUniformHolder extends UniformHolder {
	DynamicUniformHolder uniform1f(String name, FloatSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1f(String name, IntSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1f(String name, DoubleSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1i(String name, IntSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform2f(String name, Supplier<Vector2f> value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform2i(String name, Supplier<Vector2i> value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform3f(String name, Supplier<Vector3f> value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform4f(String name, Supplier<Vector4f> value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform4fArray(String name, Supplier<float[]> value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform4i(String name, Supplier<Vector4i> value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniformMatrix(String name, Supplier<Matrix4f> value, ValueUpdateNotifier notifier);

}
