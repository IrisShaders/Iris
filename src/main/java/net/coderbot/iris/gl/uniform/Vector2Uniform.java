package net.coderbot.iris.gl.uniform;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL20;

import net.minecraft.util.math.Vec2f;

public class Vector2Uniform extends Uniform {
	private Vec2f cachedValue;
	private final Supplier<Vec2f> value;
	private final boolean floatingPoint;

	Vector2Uniform(int location, Supplier<Vec2f> value, boolean floatingPoint) {
		super(location);

		this.cachedValue = null;
		this.value = value;
		this.floatingPoint = floatingPoint;

	}

	@Override
	public void update() {
		Vec2f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue;
			if (floatingPoint) {
				GL20.glUniform2f(this.location, newValue.x, newValue.y);
			} else {
				GL20.glUniform2i(this.location, (int) newValue.x, (int) newValue.y);
			}
		}
	}
}
