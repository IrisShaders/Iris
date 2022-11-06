package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.shadows.Matrix4fAccess;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class MatrixUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private Matrix4f cachedValue;
	private final Supplier<Matrix4f> value;

	MatrixUniform(int location, Supplier<Matrix4f> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	MatrixUniform(int location, Supplier<Matrix4f> value, ValueUpdateNotifier notifier) {
		super(location, notifier);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public int getStandardOffsetBytes() {
		return 64;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		Matrix4fAccess access = (Matrix4fAccess) (Object) this.value.get();
		float[] value = access.copyIntoArray();
		MemoryUtil.memPutFloat(memoryOffset, value[0]);
		MemoryUtil.memPutFloat(memoryOffset + 4, value[1]);
		MemoryUtil.memPutFloat(memoryOffset + 8, value[2]);
		MemoryUtil.memPutFloat(memoryOffset + 12, value[3]);
		MemoryUtil.memPutFloat(memoryOffset + 16, value[4]);
		MemoryUtil.memPutFloat(memoryOffset + 20, value[5]);
		MemoryUtil.memPutFloat(memoryOffset + 24, value[6]);
		MemoryUtil.memPutFloat(memoryOffset + 28, value[7]);
		MemoryUtil.memPutFloat(memoryOffset + 32, value[8]);
		MemoryUtil.memPutFloat(memoryOffset + 36, value[9]);
		MemoryUtil.memPutFloat(memoryOffset + 40, value[10]);
		MemoryUtil.memPutFloat(memoryOffset + 44, value[11]);
		MemoryUtil.memPutFloat(memoryOffset + 48, value[12]);
		MemoryUtil.memPutFloat(memoryOffset + 52, value[13]);
		MemoryUtil.memPutFloat(memoryOffset + 56, value[14]);
		MemoryUtil.memPutFloat(memoryOffset + 60, value[15]);
	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	public void updateValue() {
		Matrix4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue.copy();

			cachedValue.store(buffer);
			buffer.rewind();

			RenderSystem.glUniformMatrix4(location, false, buffer);
		}
	}

	@Override
	protected int getAlignment() {
		return 16;
	}

	@Override
	public String getTypeName() {
		return "mat4";
	}
}
