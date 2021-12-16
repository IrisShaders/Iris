package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface DynamicUniformHolder extends UniformHolder {
	DynamicUniformHolder uniform1f(String name, FloatSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1f(String name, IntSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1f(String name, DoubleSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1i(String name, IntSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform2i(String name, Supplier<Vector2i> value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform4f(String name, Supplier<Vector4f> value, ValueUpdateNotifier notifier);
}
