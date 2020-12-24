package net.coderbot.iris.gl.uniform;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL21;

import net.minecraft.client.util.math.Vector4f;

public class Vector4Uniform extends Uniform {
	private final Vector4f cachedValue;
	private final Supplier<Vector4f> value;

	Vector4Uniform(int location, Supplier<Vector4f> value) {
		super(location);

		this.cachedValue = new Vector4f();
		this.value = value;
	}

	@Override
	public void update() {
		Vector4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.getX(), newValue.getY(), newValue.getZ(), newValue.getW());
			GL21.glUniform4f(location, cachedValue.getX(), cachedValue.getY(), cachedValue.getZ(), cachedValue.getW());
		}
	}
}
