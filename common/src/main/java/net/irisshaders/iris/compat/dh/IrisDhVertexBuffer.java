package net.irisshaders.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiVertexBufferWrapper;
import net.minecraft.client.Minecraft;

import java.nio.ByteBuffer;

public class IrisDhVertexBuffer implements IDhApiVertexBufferWrapper {
	private final String name;
	private GpuBuffer vertices;
	private GpuBuffer indices;
	private int vertexCount;

	public IrisDhVertexBuffer(String name) {
		this.name = name;
	}

	@Override
	public void uploadVertexBuffer(ByteBuffer data, int vertexCount) {
		this.vertices = this.upload(this.vertices, data, GpuBuffer.USAGE_VERTEX);
		this.vertexCount = vertexCount;
	}

	@Override
	public void uploadIndexBuffer(ByteBuffer data, int vertexCount) {
		this.indices = this.upload(this.indices, data, GpuBuffer.USAGE_INDEX);
	}

	private GpuBuffer upload(GpuBuffer buffer, ByteBuffer data, int usage) {
		if (data.hasRemaining()) {
			if (buffer != null && buffer.size() == data.remaining()) {
				RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
				return buffer;
			}
		}
		if (buffer != null) {
			buffer.close();
		}
		return data.hasRemaining() ? RenderSystem.getDevice().createBuffer(() -> this.name, usage | GpuBuffer.USAGE_COPY_DST, data) : null;
	}

	@Override
	public int getVertexCount() {
		return this.vertexCount;
	}

	public int getIndexCount() {
		return this.vertexCount / 4 * 6;
	}

	public GpuBuffer getVertexBuffer() {
		return this.vertices;
	}

	public GpuBuffer getIndexBuffer() {
		return this.indices != null ? this.indices : RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS)
			.getBuffer(this.getIndexCount());
	}

	public IndexType getIndexType() {
		return this.indices != null ? IndexType.INT : RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).type();
	}

	@Override
	public void close() {
		Minecraft.getInstance().execute(() -> {
			if (this.vertices != null) {
				this.vertices.close();
				this.vertices = null;
			}
			if (this.indices != null) {
				this.indices.close();
				this.indices = null;
			}
			this.vertexCount = 0;
		});
	}
}
