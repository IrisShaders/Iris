package net.coderbot.iris.gl.uniform;

import net.minecraft.util.math.Vec2f;
import org.lwjgl.opengl.GL20;

import java.util.function.Supplier;

public class Vector2IntegerUniform extends Uniform {
	private Vec2f cachedValue;
	private final Supplier<Vec2f> value;

	Vector2IntegerUniform(int location, Supplier<Vec2f> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public void update() {
		Vec2f newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			GL20.glUniform2i(this.location, (int) newValue.x, (int) newValue.y);
		}
	}
}
