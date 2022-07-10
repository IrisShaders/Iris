package net.coderbot.iris.gl.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL43C;

public class ShaderStorageBuffer extends GlResource {
	protected final int index;
	protected final long size;
	protected final boolean clear;

	public ShaderStorageBuffer(int buffer, BufferObjectInformation information) {
		super(buffer);
		this.index = information.getIndex();
		this.size = information.getSize();
		this.clear = information.shouldClear();
	}

	public final int getIndex() {
		return index;
	}

	public final long getSize() {
		return size;
	}

	public int getBuffer() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, 0);
		IrisRenderSystem.bindBufferBase(GL43C.GL_UNIFORM_BUFFER, index, 0);
		GlStateManager._glDeleteBuffers(getGlId());
	}

	public void bind(int target) {
		IrisRenderSystem.bindBufferBase(target, index, getGlId());
	}

	public void onNewFrame() {
		if (clear) {
			GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, getGlId());
			IrisRenderSystem.clearBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, GL43C.GL_R8UI, GL43C.GL_RED_INTEGER, GL43C.GL_UNSIGNED_BYTE, null);
			GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
		}
	}
}
