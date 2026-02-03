package net.irisshaders.iris.gl.buffer;

import com.mojang.blaze3d.opengl.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShaderStorageBufferHolder {
	private static final List<ShaderStorageBuffer> ACTIVE_BUFFERS = new ArrayList<>();
	private int cachedWidth;
	private int cachedHeight;
	private ShaderStorageBuffer[] buffers;
	private boolean destroyed;


	public ShaderStorageBufferHolder(Int2ObjectArrayMap<BuiltShaderStorageInfo> overrides, int width, int height) {
		destroyed = false;
		cachedWidth = width;
		cachedHeight = height;
		buffers = new ShaderStorageBuffer[Collections.max(overrides.keySet()) + 1];
		overrides.forEach((index, bufferInfo) -> {
			if (bufferInfo.size() > IrisRenderSystem.getVRAM()) {
				throw new OutOfVideoMemoryError("We only have " + toMib(IrisRenderSystem.getVRAM()) + "MiB of RAM to work with, but the pack is requesting " + bufferInfo.size() + "! Can't continue.");
			}

			if (index > SamplerLimits.get().getMaxShaderStorageUnits()) {
				throw new IllegalStateException("We don't have enough SSBO units??? (index: " + index + ", max: " + SamplerLimits.get().getMaxShaderStorageUnits());
			}

			buffers[index] = new ShaderStorageBuffer(index, bufferInfo);
			ACTIVE_BUFFERS.add(buffers[index]);
			int buffer = buffers[index].getId();

			if (bufferInfo.relative()) {
				buffers[index].resizeIfRelative(width, height);
			} else {
				buffers[index].createStatic();
			}
		});
		GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
	}

	private static long toMib(long x) {
		return x / 1024L / 1024L;
	}

	public static void forceDeleteBuffers() {
		if (!ACTIVE_BUFFERS.isEmpty()) {
			Iris.logger.warn("Found " + ACTIVE_BUFFERS.size() + " stored buffers with a total size of " + ACTIVE_BUFFERS.stream().map(ShaderStorageBuffer::getSize).reduce(0L, Long::sum) + ", forcing them to be deleted.");
			ACTIVE_BUFFERS.forEach(ShaderStorageBuffer::destroy);
			ACTIVE_BUFFERS.clear();
		}
	}

	public void hasResizedScreen(int width, int height) {
		if (width != cachedWidth || height != cachedHeight) {
			cachedWidth = width;
			cachedHeight = height;
			for (ShaderStorageBuffer buffer : buffers) {
				if (buffer != null) {
					buffer.resizeIfRelative(width, height);
				}
			}
		}
	}

	public void setupBuffers() {
		if (destroyed) {
			throw new IllegalStateException("Tried to use destroyed buffer objects");
		}

		for (ShaderStorageBuffer buffer : buffers) {
			if (buffer != null) {
				buffer.bind();
			}
		}
	}

	public int getBufferIndex(int index) {
		if (buffers.length < index || buffers[index] == null)
			throw new RuntimeException("Tried to query a buffer for indirect dispatch that doesn't exist!");

		return buffers[index].getId();
	}

	public void destroyBuffers() {
		for (ShaderStorageBuffer buffer : buffers) {
			if (buffer != null) {
				ACTIVE_BUFFERS.remove(buffer);
				buffer.destroy();
			}
		}
		buffers = null;
		destroyed = true;
	}

	private static class OutOfVideoMemoryError extends RuntimeException {
		public OutOfVideoMemoryError(String s) {
			super(s);
		}
	}
}
