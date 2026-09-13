package net.irisshaders.iris.shaderpack;

import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.shaderpack.properties.IndirectPointer;

public record FilledIndirectPointer(ShaderStorageBufferHolder holder, int index, long offset) {
	public FilledIndirectPointer {
		if (holder == null || offset < 0 || offset % 4 != 0) {
			throw new IllegalArgumentException("Invalid indirect dispatch pointer");
		}
	}

	public VulkanGpuBuffer buffer() {
		var buffer = holder.getBuffer(index).buffer();

		if (offset > buffer.size() - 12) {
			throw new IllegalArgumentException("Indirect dispatch exceeds bufferObject." + index);
		}

		return buffer;
	}

	public static FilledIndirectPointer basedOff(ShaderStorageBufferHolder holder, IndirectPointer pointer) {
		if (pointer == null) {
			return null;
		}

		var result = new FilledIndirectPointer(holder, pointer.buffer(), pointer.offset());
		result.buffer();

		return result;
	}
}
