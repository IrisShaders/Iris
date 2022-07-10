package net.coderbot.iris.gl.buffer;

public class EmptyBufferHolder implements ShaderStorageBufferHolder {
	@Override
	public int getBuffer(int location) {
		return 0;
	}

	@Override
	public void destroyBuffers() {
	}

	@Override
	public void onNewFrame() {

	}
}
