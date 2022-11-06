package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

public class VanillaVector3Uniform extends Uniform {
	private final Vector3f cachedValue;
	private final Supplier<com.mojang.math.Vector3f> value;

	VanillaVector3Uniform(int location, Supplier<com.mojang.math.Vector3f> value) {
		super(location);

		this.cachedValue = new Vector3f();
		this.value = value;
	}

	@Override
	public int getStandardOffsetBytes() {
		return 16;
	}

	@Override
	public void putInBuffer(long memoryOffset) {
		com.mojang.math.Vector3f value = this.value.get();
		MemoryUtil.memPutFloat(memoryOffset, value.x());
		MemoryUtil.memPutFloat(memoryOffset + 4, value.y());
		MemoryUtil.memPutFloat(memoryOffset + 8, value.z());
		// Pad an extra 4 bytes to conform to std140
	}

	@Override
	public void update() {
		com.mojang.math.Vector3f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z());
			IrisRenderSystem.uniform3f(location, cachedValue.x(), cachedValue.y(), cachedValue.z());
		}
	}

	@Override
	protected int getAlignment() {
		return 16;
	}

	@Override
	public String getTypeName() {
		return "vec3";
	}
}
