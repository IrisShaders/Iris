package net.coderbot.iris.gl.buffer;

import net.coderbot.iris.Iris;
import org.lwjgl.opengl.GL46C;

public class UniformBuffer {
	private final long size;
	private int buffer;
	private long address;
	private int lastFrameId;
	private int frameId;

	public UniformBuffer(long size) {
		this.size = size;
		this.buffer = GL46C.glCreateBuffers();
		GL46C.glNamedBufferStorage(buffer, size * 3L, GL46C.GL_MAP_WRITE_BIT | GL46C.GL_MAP_PERSISTENT_BIT | GL46C.GL_CLIENT_STORAGE_BIT);
		this.address = GL46C.nglMapNamedBufferRange(buffer, 0, size * 3L, GL46C.GL_MAP_PERSISTENT_BIT | GL46C.GL_MAP_INVALIDATE_BUFFER_BIT | GL46C.GL_MAP_WRITE_BIT | GL46C.GL_MAP_FLUSH_EXPLICIT_BIT);
	}

	public long getWriteAddressForFrame() {
		return address + (size * frameId);
	}

	public long getReadAddressForFrame() {
		return address + (size * lastFrameId);
	}

	public void updateFrame() {
		GL46C.glFlush();
		GL46C.glFinish();
		GL46C.glFlushMappedNamedBufferRange(buffer, size * frameId, size);
		GL46C.glBindBufferRange(GL46C.GL_UNIFORM_BUFFER, 2, buffer, (size * frameId), size);

		lastFrameId = frameId;
		frameId = (frameId+1)%3;

	}

	public void destroy() {
		GL46C.glUnmapNamedBuffer(buffer);
		GL46C.glDeleteBuffers(buffer);
	}
}
