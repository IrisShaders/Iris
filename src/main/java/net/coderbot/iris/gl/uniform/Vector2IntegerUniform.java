package net.coderbot.iris.gl.uniform;

import org.lwjgl.opengl.GL20;

import java.util.function.Supplier;
import net.minecraft.world.phys.Vec2;

public class Vector2IntegerUniform extends Uniform {
	private Vec2 cachedValue;
	private final Supplier<Vec2> value;

	Vector2IntegerUniform(int location, Supplier<Vec2> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public void update() {
		Vec2 newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			GL20.glUniform2i(this.location, (int) newValue.x, (int) newValue.y);
		}
	}
}
