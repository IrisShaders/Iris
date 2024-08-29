package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

public class Vector2ArrayUniform extends Uniform {
	private final Supplier<Vector2f[]> value;
	private Vector2f[] cachedValue;

	Vector2ArrayUniform(int location, int count, Supplier<Vector2f[]> value) {
		super(location);

		this.cachedValue = new Vector2f[count];

		for (int i = 0; i < count; i++) {
			this.cachedValue[i] = new Vector2f();
		}
		this.value = value;
	}

	Vector2ArrayUniform(int location, Supplier<Vector2f[]> value, int count, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = new Vector2f[count];

		for (int i = 0; i < count; i++) {
			this.cachedValue[i] = new Vector2f();
		}
		this.value = value;

	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	private void updateValue() {
		Vector2f[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer buffer = stack.mallocFloat(newValue.length * 2);

				int index = 0;
				for (int i = 0; i < cachedValue.length; i++) {
					cachedValue[i].set(newValue[i]);
					newValue[i].get(index, buffer);
					index += 2;
				}

				GL46C.glUniform2fv(this.location, buffer);
			}
		}
	}
}
