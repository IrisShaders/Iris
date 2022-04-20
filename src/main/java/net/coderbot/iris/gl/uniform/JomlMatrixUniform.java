package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.vendored.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class JomlMatrixUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private Matrix4f cachedValue;
	private final Supplier<Matrix4f> value;

	JomlMatrixUniform(int location, Supplier<Matrix4f> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public void update() {
		Matrix4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = new Matrix4f(newValue);

			cachedValue.get(buffer);
			buffer.rewind();

			RenderSystem.glUniformMatrix4(location, false, buffer);
		}
	}
}
