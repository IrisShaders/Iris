package net.coderbot.iris.gl.uniform;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public interface DynamicUniformHolder extends UniformHolder {
	DynamicUniformHolder uniform1f(String name, FloatSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1f(String name, IntSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1f(String name, DoubleSupplier value, ValueUpdateNotifier notifier);
	DynamicUniformHolder uniform1i(String name, IntSupplier value, ValueUpdateNotifier notifier);
}
