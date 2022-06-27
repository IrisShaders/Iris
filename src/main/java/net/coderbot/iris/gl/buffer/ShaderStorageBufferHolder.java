package net.coderbot.iris.gl.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL43C;

import java.util.Collections;

public class ShaderStorageBufferHolder {
	private int[] buffers;

	public ShaderStorageBufferHolder(Int2IntArrayMap overrides) {
		buffers = new int[Collections.max(overrides.keySet()) + 1];
		IrisRenderSystem.genBuffers(buffers);
		overrides.forEach((index, size) -> {
			GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, buffers[index]);
			GL43C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, size, GL43C.GL_DYNAMIC_DRAW);
			GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, buffers[index]);
		});
		GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
	}

	public void setupBuffers() {
		for (int i = 0; i < buffers.length; i++) {
			GL43C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, i, buffers[i]);
		}
	}
}
