package net.irisshaders.iris.ubo;

import org.lwjgl.opengl.GL46C;

public class UniformBufferObject {
	private final long size;
	private int buffer;
	private long address;

	static int frameCount = 3;

	public UniformBufferObject(long size) {
		buffer = GL46C.glCreateBuffers();
		this.size = size;

		GL46C.glNamedBufferStorage(buffer, size * frameCount, GL46C.GL_MAP_WRITE_BIT | GL46C.GL_MAP_PERSISTENT_BIT);

		address = GL46C.nglMapNamedBufferRange(buffer, 0, size * frameCount, GL46C.GL_MAP_WRITE_BIT | GL46C.GL_MAP_PERSISTENT_BIT | GL46C.GL_MAP_FLUSH_EXPLICIT_BIT);
	}

	public long getRange(int frame) {
		return address + (size * frame);
	}

	public void flushRange(int frame) {
		GL46C.glFlushMappedNamedBufferRange(buffer, frame * size, size);
	}

	public void bindRange(int frame) {
		GL46C.glBindBufferRange(GL46C.GL_UNIFORM_BUFFER, 5, buffer, size * frame, size);
	}
}
