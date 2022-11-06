package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.vendored.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

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
	public int getStandardOffsetBytes() {
		return 64;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		Matrix4f value = this.value.get();
		MemoryUtil.memPutFloat(memoryOffset, value.m00());
		MemoryUtil.memPutFloat(memoryOffset + 4, value.m01());
		MemoryUtil.memPutFloat(memoryOffset + 8, value.m02());
		MemoryUtil.memPutFloat(memoryOffset + 12, value.m03());
		MemoryUtil.memPutFloat(memoryOffset + 16, value.m10());
		MemoryUtil.memPutFloat(memoryOffset + 20, value.m11());
		MemoryUtil.memPutFloat(memoryOffset + 24, value.m12());
		MemoryUtil.memPutFloat(memoryOffset + 28, value.m13());
		MemoryUtil.memPutFloat(memoryOffset + 32, value.m20());
		MemoryUtil.memPutFloat(memoryOffset + 36, value.m21());
		MemoryUtil.memPutFloat(memoryOffset + 40, value.m22());
		MemoryUtil.memPutFloat(memoryOffset + 44, value.m23());
		MemoryUtil.memPutFloat(memoryOffset + 48, value.m30());
		MemoryUtil.memPutFloat(memoryOffset + 52, value.m31());
		MemoryUtil.memPutFloat(memoryOffset + 56, value.m32());
		MemoryUtil.memPutFloat(memoryOffset + 60, value.m33());
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

	@Override
	protected int getAlignment() {
		return 16;
	}

	@Override
	public String getTypeName() {
		return "mat4";
	}
}
