package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

public class MatrixFromFloatArrayUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private float[] cachedValue;
	private final Supplier<float[]> value;

	MatrixFromFloatArrayUniform(int location, Supplier<float[]> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public int getStandardOffsetBytes() {
		return 64;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		float[] value = this.value.get();
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
		float[] newValue = value.get();

		if (!Arrays.equals(newValue, cachedValue)) {
			cachedValue = Arrays.copyOf(newValue, 16);

			buffer.put(cachedValue);
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
