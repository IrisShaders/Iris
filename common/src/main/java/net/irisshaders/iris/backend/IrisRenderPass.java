package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.IndexType;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public interface IrisRenderPass extends AutoCloseable {

	void bindPipeline(GraphicsPipeline pipeline);

	void setViewport(int x, int y, int width, int height);

	void enableScissor(int x, int y, int width, int height);

	void disableScissor();

	void setVertexBuffer(int slot, GpuBufferSlice buffer);

	void setIndexBuffer(GpuBuffer buffer, IndexType type);

	void pushConstants(ByteBuffer data);

	void draw(int count, int instances, int first, int firstInstance);

	void drawIndexed(int count, int instances, int first, int offset, int firstInstance);

	void drawIndirect(GpuBufferSlice commands, int count);

	void drawIndexedIndirect(GpuBufferSlice commands, int count);

	void multiDraw(IntBuffer commands, int instances, int firstInstance, int count);

	void multiDrawIndexed(IntBuffer commands, int instances, int firstInstance, int count);

	void multiDraw(IntBuffer first, IntBuffer counts, int count);

	void multiDrawIndexed(PointerBuffer offsets, IntBuffer counts, IntBuffer bases, int count);

	@Override
	void close();
}
