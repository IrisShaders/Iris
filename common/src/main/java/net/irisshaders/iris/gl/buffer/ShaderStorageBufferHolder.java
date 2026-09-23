package net.irisshaders.iris.gl.buffer;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ShaderStorageBufferHolder implements AutoCloseable {
	private static final ArrayList<ShaderStorageBufferHolder> ACTIVE = new ArrayList<>();
	private final Map<Integer, ShaderStorageBuffer> buffers = new LinkedHashMap<>();
	private int cachedWidth;
	private int cachedHeight;
	private boolean closed;

	public ShaderStorageBufferHolder(Int2ObjectArrayMap<BuiltShaderStorageInfo> overrides, int width, int height) {
		cachedWidth = width;
		cachedHeight = height;

		try {
			overrides.forEach((index, info) -> {
				if (index < 0) {
					throw new IllegalArgumentException("negative SSBO binding: " + index);
				}

				this.buffers.put(index, new ShaderStorageBuffer(index, info, width, height));
			});
			ACTIVE.add(this);
		} catch (RuntimeException | Error failure) {
			this.close();

			throw failure;
		}
	}

	public static void forceDeleteBuffers() {
		for (var holder : new ArrayList<>(ACTIVE)) {
			holder.close();
		}
	}

	public ShaderStorageBuffer getBuffer(int index) {
		if (this.closed) {
			throw new IllegalStateException("storage buffers are closed");
		}

		var buffer = this.buffers.get(index);

		if (buffer == null) {
			throw new IllegalArgumentException("no bufferObject." + index + " declared by the shader pack");
		}

		return buffer;
	}

	public void hasResizedScreen(int width, int height) {
		if (this.closed) {
			throw new IllegalStateException("storage buffers are closed");
		}

		if (width == this.cachedWidth && height == this.cachedHeight) {
			return;
		}

		for (var buffer : this.buffers.values()) {
			buffer.resizeIfRelative(width, height);
		}

		this.cachedWidth = width;
		this.cachedHeight = height;
	}

	public void destroyBuffers() {
		this.close();
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.buffers.values().forEach(ShaderStorageBuffer::close);
		this.buffers.clear();
		ACTIVE.remove(this);
	}
}
