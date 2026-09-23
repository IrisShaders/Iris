package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;

public abstract class ForwardingRenderPass implements RenderPassBackend {
	protected final RenderPassBackend delegate;

	protected ForwardingRenderPass(RenderPassBackend delegate) {
		this.delegate = delegate;
	}

	protected void beforeDraw() {
	}

	@Override
	public void pushDebugGroup(Supplier<String> label) {
		this.delegate.pushDebugGroup(label);
	}

	@Override
	public void popDebugGroup() {
		this.delegate.popDebugGroup();
	}

	@Override
	public void setPipeline(BackendRenderPipeline pipeline) {
		this.delegate.setPipeline(pipeline);
	}

	@Override
	public void setUniform(int index, Object value) {
		this.delegate.setUniform(index, value);
	}

	@Override
	public void pushConstants(ByteBuffer data) {
		this.delegate.pushConstants(data);
	}

	@Override
	public void enableScissor(int x, int y, int w, int h) {
		this.delegate.enableScissor(x, y, w, h);
	}

	@Override
	public void disableScissor() {
		this.delegate.disableScissor();
	}

	@Override
	public void setVertexBuffer(int index, GpuBufferSlice slice) {
		this.delegate.setVertexBuffer(index, slice);
	}

	@Override
	public void setIndexBuffer(GpuBuffer buffer, IndexType type) {
		this.delegate.setIndexBuffer(buffer, type);
	}

	@Override
	public void writeTimestamp(GpuQueryPool pool, int query) {
		this.delegate.writeTimestamp(pool, query);
	}

	@Override
	public void draw(int count, int instances, int first, int firstInstance) {
		this.beforeDraw();
		this.delegate.draw(count, instances, first, firstInstance);
	}

	@Override
	public void drawIndexed(int count, int instances, int first, int offset, int firstInstance) {
		this.beforeDraw();
		this.delegate.drawIndexed(count, instances, first, offset, firstInstance);
	}

	@Override
	public void drawIndirect(GpuBufferSlice commands, int count) {
		this.beforeDraw();
		this.delegate.drawIndirect(commands, count);
	}

	@Override
	public void drawIndexedIndirect(GpuBufferSlice commands, int count) {
		this.beforeDraw();
		this.delegate.drawIndexedIndirect(commands, count);
	}

	@Override
	public void multiDraw(IntBuffer commands, int instances, int firstInstance, int count) {
		this.beforeDraw();
		this.delegate.multiDraw(commands, instances, firstInstance, count);
	}

	@Override
	public void multiDrawIndexed(IntBuffer commands, int instances, int firstInstance, int count) {
		this.beforeDraw();
		this.delegate.multiDrawIndexed(commands, instances, firstInstance, count);
	}

	@Override
	public void multiDraw(IntBuffer first, IntBuffer counts, int count) {
		this.beforeDraw();
		this.delegate.multiDraw(first, counts, count);
	}

	@Override
	public void multiDrawIndexed(PointerBuffer offsets, IntBuffer counts, IntBuffer bases, int count) {
		this.beforeDraw();
		this.delegate.multiDrawIndexed(offsets, counts, bases, count);
	}
}
