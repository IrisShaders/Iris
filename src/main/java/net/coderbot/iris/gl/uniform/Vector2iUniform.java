package net.coderbot.iris.gl.uniform;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL20;

public class Vector2iUniform extends Uniform {

	private Vec2i cachedValue;
	private final Supplier<Vec2i> value;

	Vector2iUniform(int location, Supplier<Vec2i> value) {
		super(location);

		this.value = value;
		this.cachedValue = null;
	}

	@Override
	public void update() {
		Vec2i newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue;
			GL20.glUniform2i(this.location, newValue.x, newValue.y);
		}
	}

	@SuppressWarnings("FieldMayBeFinal")
	public static class Vec2i {
		//use as a default/0 value
		public static final Vec2i EMPTY = new Vec2i(0, 0);

		private int x;
		private int y;

		public Vec2i(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Vec2i)) return false;
			Vec2i other = (Vec2i) obj;

			return other.y == this.y && other.x == this.x;
		}
	}
}
