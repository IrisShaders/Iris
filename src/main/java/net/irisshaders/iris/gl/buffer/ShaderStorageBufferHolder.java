package net.irisshaders.iris.gl.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import org.lwjgl.opengl.GL43C;

import java.util.Collections;

public class ShaderStorageBufferHolder {
	private int cachedWidth;
	private int cachedHeight;
	private ShaderStorageBuffer[] buffers;
	private boolean destroyed;

	public ShaderStorageBufferHolder(Int2ObjectArrayMap<ShaderStorageInfo> overrides, int width, int height) {
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
			int buffer = buffers[index].getId();

			if (bufferInfo.relative()) {
				buffers[index].resizeIfRelative(width, height);
			} else {
				GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, buffer);
				IrisRenderSystem.bufferStorage(GL43C.GL_SHADER_STORAGE_BUFFER, bufferInfo.size(), 0);
				IrisRenderSystem.clearBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, GL43C.GL_R8, 0, bufferInfo.size(), GL43C.GL_RED, GL43C.GL_BYTE, new int[]{0});
				IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, buffer);
			}
		});
		GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
	}

	private static long toMib(long x) {
		return x / 1024L / 1024L;
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
