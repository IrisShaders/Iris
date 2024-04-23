package net.irisshaders.iris.gl.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL43C;

// Do not extend GlResource, this is immutable.
public class ShaderStorageBuffer {
	protected final int index;
	protected final ShaderStorageInfo info;
	protected int id;

	public ShaderStorageBuffer(int index, ShaderStorageInfo info) {
		this.id = IrisRenderSystem.createBuffers();
		GLDebug.nameObject(GL43C.GL_BUFFER, id, "SSBO " + index);
		this.index = index;
		this.info = info;
	}

	public final int getIndex() {
		return index;
	}

	public final long getSize() {
		return info.size();
	}

	protected void destroy() {
		IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, 0);
		// DO NOT use the GlStateManager version here! On Linux, it will attempt to clear the data using BufferData and cause GL errors.
		IrisRenderSystem.deleteBuffers(id);
	}

	public void bind() {
		IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, id);
	}

	public void resizeIfRelative(int width, int height) {
		if (!info.relative()) return;

		IrisRenderSystem.deleteBuffers(id);
		int newId = GlStateManager._glGenBuffers();
		GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, newId);

		// Calculation time
		int newWidth = (int) (width * info.scaleX());
		int newHeight = (int) (height * info.scaleY());
		int finalSize = (newHeight * newWidth) * info.size();
		IrisRenderSystem.bufferStorage(GL43C.GL_SHADER_STORAGE_BUFFER, finalSize, 0);
		IrisRenderSystem.clearBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, GL43C.GL_R8, 0, finalSize, GL43C.GL_RED, GL43C.GL_BYTE, new int[]{0});
		IrisRenderSystem.bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, index, newId);
		id = newId;
	}

	public int getId() {
		return id;
	}
}
