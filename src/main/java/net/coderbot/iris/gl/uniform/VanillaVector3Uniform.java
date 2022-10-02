package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector3f;

import java.util.function.Supplier;

public class VanillaVector3Uniform extends Uniform {
	private final Vector3f cachedValue;
	private final Supplier<com.mojang.math.Vector3f> value;

	VanillaVector3Uniform(int location, Supplier<com.mojang.math.Vector3f> value) {
		super(location);

		this.cachedValue = new Vector3f();
		this.value = value;
	}

	@Override
	public void update() {
		com.mojang.math.Vector3f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z());
			IrisRenderSystem.uniform3f(location, cachedValue.x(), cachedValue.y(), cachedValue.z());
		}
	}
}
