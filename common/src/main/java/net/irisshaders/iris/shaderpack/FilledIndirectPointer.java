package net.irisshaders.iris.shaderpack;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.shaderpack.properties.IndirectPointer;

public record FilledIndirectPointer(ShaderStorageBufferHolder holder, int index, long offset) {
	public FilledIndirectPointer {
		if (holder == null || offset < 0 || offset % 4 != 0) {
			throw new IllegalArgumentException("invalid indirect dispatch pointer");
		}
	}

	public static FilledIndirectPointer basedOff(ShaderStorageBufferHolder holder, IndirectPointer pointer) {
		if (pointer == null) {
			return null;
		}

		var result = new FilledIndirectPointer(holder, pointer.buffer(), pointer.offset());
		result.buffer();

		return result;
	}

	public GpuBuffer buffer() {
		var buffer = this.holder.getBuffer(this.index).buffer();

		if (this.offset > buffer.size() - 12) {
			throw new IllegalArgumentException("indirect dispatch exceeds bufferObject." + this.index);
		}

		return buffer;
	}
}
