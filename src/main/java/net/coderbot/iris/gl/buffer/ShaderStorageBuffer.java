package net.coderbot.iris.gl.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL43C;

public class ShaderStorageBuffer extends GlResource {
	protected final int index;
	protected final long size;

	public ShaderStorageBuffer(int buffer, int index, long size) {
		super(buffer);
		this.index = index;
		this.size = size;
	}

	public final int getIndex() {
		return index;
	}

	public final long getSize() {
		return size;
	}

	@Override
	protected void destroyInternal() {
		IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, 0);
		GlStateManager._glDeleteBuffers(getGlId());
	}

	public void bind() {
		IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, getGlId());
	}
}
