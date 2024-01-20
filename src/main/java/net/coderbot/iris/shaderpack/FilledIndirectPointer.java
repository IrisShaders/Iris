package net.coderbot.iris.shaderpack;

import net.coderbot.iris.gl.buffer.ShaderStorageBufferHolder;

public record FilledIndirectPointer(int buffer, long offset) {
	public static FilledIndirectPointer basedOff(ShaderStorageBufferHolder holder, IndirectPointer pointer) {
		if (pointer == null || holder == null) return null;

		return new FilledIndirectPointer(holder.getBufferIndex(pointer.buffer()), pointer.offset());
	}
}
