package net.irisshaders.iris.gl.buffer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;

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
		this(VulkanCommandBridge.device(RenderSystem.getDevice()), overrides, width, height);
	}

	public ShaderStorageBufferHolder(VulkanDevice device, Int2ObjectArrayMap<BuiltShaderStorageInfo> overrides, int width, int height) {
		cachedWidth = width;
		cachedHeight = height;

		try {
			overrides.forEach((index, info) -> {
				if (index < 0) {
					throw new IllegalArgumentException("Negative SSBO binding: " + index);
				}

				buffers.put(index, new ShaderStorageBuffer(device, index, info, width, height));
			});
			ACTIVE.add(this);
		} catch (RuntimeException | Error failure) {
			close();

			throw failure;
		}
	}

	public ShaderStorageBuffer getBuffer(int index) {
		if (closed) {
			throw new IllegalStateException("Storage buffers are closed");
		}

		var buffer = buffers.get(index);

		if (buffer == null) {
			throw new IllegalArgumentException("No bufferObject." + index + " declared by the shader pack");
		}

		return buffer;
	}

	public void hasResizedScreen(int width, int height) {
		if (closed) {
			throw new IllegalStateException("Storage buffers are closed");
		}

		if (width == cachedWidth && height == cachedHeight) {
			return;
		}

		for (var buffer : buffers.values()) {
			buffer.resizeIfRelative(width, height);
		}

		cachedWidth = width;
		cachedHeight = height;
	}

	public static void forceDeleteBuffers() {
		for (var holder : new ArrayList<>(ACTIVE)) {
			holder.close();
		}
	}

	public void destroyBuffers() {
		close();
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}

		closed = true;
		buffers.values().forEach(ShaderStorageBuffer::close);
		buffers.clear();
		ACTIVE.remove(this);
	}
}
