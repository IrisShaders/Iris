package net.coderbot.iris.gl.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.OutOfVideoMemoryError;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL43C;

import java.util.Collections;
import java.util.List;

public class ActiveBufferHolder implements ShaderStorageBufferHolder {
	private ShaderStorageBuffer[] buffers;
	private boolean destroyed;

	public ActiveBufferHolder(List<BufferObjectInformation> overrides) {
		destroyed = false;
		buffers = new ShaderStorageBuffer[Collections.max(overrides).getIndex() + 1];
		overrides.forEach((mapping) -> {
			int buffer = GlStateManager._glGenBuffers();
			GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, buffer);
			long vram = IrisRenderSystem.getVRAM();
			if (mapping.getSize() > vram) {
				throw new OutOfVideoMemoryError("Trying to allocate more than VRAM allows! mapping: " + mapping.getSize() + " vram " + vram);
			}
			IrisRenderSystem.bufferData(GL43C.GL_SHADER_STORAGE_BUFFER, mapping.getSize(), GL43C.GL_DYNAMIC_DRAW);
			IrisRenderSystem.clearBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, GL43C.GL_R8UI, GL43C.GL_RED_INTEGER, GL43C.GL_UNSIGNED_BYTE, null);
			Iris.logger.warn("Creating SSBO " + mapping.getIndex() + " with size " + mapping + " at buffer location " + buffer);
			buffers[mapping.getIndex()] = new ShaderStorageBuffer(buffer, mapping);
		});
		GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
	}

	@Override
	public int getBuffer(int location) {
		if (buffers.length < location) {
			return -1;
		}

		if (buffers[location] == null) {
			return -1;
		}

		return buffers[location].getBuffer();
	}

	public void onNewFrame() {
		for (ShaderStorageBuffer buffer : buffers) {
			if (buffer != null) {
				buffer.onNewFrame();
			}
		}
	}

	@Override
	public void destroyBuffers() {
		for (ShaderStorageBuffer buffer : buffers) {
			if (buffer != null) {
				buffer.destroy();
			}
		}
		buffers = null;
		destroyed = true;
	}
}
