package net.coderbot.iris.gl.uniform;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL21;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Vec3d;

public class Vector3Uniform extends Uniform {
	private final Vector3f cachedValue;
	private final Supplier<Vector3f> value;

	Vector3Uniform(int location, Supplier<Vector3f> value) {
		super(location);

		this.cachedValue = new Vector3f();
		this.value = value;
	}

	static Vector3Uniform converted(int location, Supplier<Vec3d> value) {
		Vector3f held = new Vector3f();

		return new Vector3Uniform(location, () -> {
			Vec3d updated = value.get();

			held.set((float) updated.x, (float) updated.y, (float) updated.z);

			return held;
		});
	}

	static Vector3Uniform truncated(int location, Supplier<Vector4f> value) {
		Vector3f held = new Vector3f();

		return new Vector3Uniform(location, () -> {
			Vector4f updated = value.get();

			held.set(updated.getX(), updated.getY(), updated.getZ());

			return held;
		});
	}

	@Override
	public void update() {
		Vector3f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.getX(), newValue.getY(), newValue.getZ());
			GL21.glUniform3f(location, cachedValue.getX(), cachedValue.getY(), cachedValue.getZ());
		}
	}
}
