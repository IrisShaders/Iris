package net.irisshaders.iris.gl.buffer;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.irisshaders.iris.Iris;

public final class ShaderStorageBuffer implements AutoCloseable {
	public static final int USAGE_STORAGE = 1 << 20;
	private final int index;
	private final BuiltShaderStorageInfo info;
	private GpuBuffer buffer;
	private boolean closed;

	public ShaderStorageBuffer(int index, BuiltShaderStorageInfo info, int width, int height) {
		this.index = index;
		this.info = info;
		this.resize(width, height);
	}

	public int getIndex() {
		return index;
	}

	public long getSize() {
		return this.buffer().size();
	}

	public GpuBufferSlice slice() {
		return this.buffer().slice();
	}

	public GpuBuffer buffer() {
		if (this.closed) {
			throw new IllegalStateException("storage buffer " + this.index + " is closed");
		}

		return this.buffer;
	}

	private void resize(int width, int height) {
		long size = this.info.size();

		if (this.info.relative()) {
			if (width < 1 || height < 1 || !Float.isFinite(this.info.scaleX()) || !Float.isFinite(
				this.info.scaleY()) || this.info.scaleX() <= 0 || this.info.scaleY() <= 0) {
				throw new IllegalArgumentException("invalid relative SSBO dimensions: " + this.index);
			}

			long scaledWidth = Math.max(1, (long) (width * this.info.scaleX()));
			long scaledHeight = Math.max(1, (long) (height * this.info.scaleY()));
			size = Math.multiplyExact(size, Math.multiplyExact(scaledWidth, scaledHeight));
		}

		var replacement = Iris.getRenderBackend().createStorageBuffer("Iris SSBO " + this.index, size, this.info.content());

		if (this.buffer != null) {
			this.buffer.close();
		}

		this.buffer = replacement;
	}

	public void resizeIfRelative(int width, int height) {
		this.buffer();

		if (this.info.relative()) {
			this.resize(width, height);
		}
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;

		if (this.buffer != null) {
			this.buffer.close();
		}
	}

}
