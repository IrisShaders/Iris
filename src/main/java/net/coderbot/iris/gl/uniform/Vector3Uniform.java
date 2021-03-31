package net.coderbot.iris.gl.uniform;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL21;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

public class Vector3Uniform extends Uniform {
	private final Vec3f cachedValue;
	private final Supplier<Vec3f> value;

	Vector3Uniform(int location, Supplier<Vec3f> value) {
		super(location);

		this.cachedValue = new Vec3f();
		this.value = value;
	}

	static Vector3Uniform converted(int location, Supplier<Vec3d> value) {
		Vec3f held = new Vec3f();

		return new Vector3Uniform(location, () -> {
			Vec3d updated = value.get();

			held.set((float) updated.x, (float) updated.y, (float) updated.z);

			return held;
		});
	}

	static Vector3Uniform truncated(int location, Supplier<Vector4f> value) {
		Vec3f held = new Vec3f();

		return new Vector3Uniform(location, () -> {
			Vector4f updated = value.get();

			held.set(updated.getX(), updated.getY(), updated.getZ());

			return held;
		});
	}

	@Override
	public void update() {
		Vec3f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.getX(), newValue.getY(), newValue.getZ());
			GL21.glUniform3f(location, cachedValue.getX(), cachedValue.getY(), cachedValue.getZ());
		}
	}
}
