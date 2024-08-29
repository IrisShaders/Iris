package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.lwjgl.opengl.GL46C;

import java.util.Arrays;
import java.util.function.Supplier;

public class FloatArrayUniform extends Uniform {
	private final Supplier<float[]> value;
	private float[] cachedValue;

	FloatArrayUniform(int location, int count, Supplier<float[]> value) {
		this(location, value, count, null);
	}

	FloatArrayUniform(int location, Supplier<float[]> value, int count, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = new float[count];
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
		float[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			System.arraycopy(newValue, 0, cachedValue, 0, newValue.length);
			GL46C.glUniform1fv(location, newValue);
		}
	}
}
